package index.indexdb;

import com.koloboke.collect.hash.HashConfig;
import com.koloboke.collect.map.LongIntMap;
import com.koloboke.collect.map.hash.HashLongIntMapFactory;
import com.koloboke.collect.map.hash.HashLongIntMaps;
import report.Report;
import report.UpdateReceiver;
import report.UpdateSender;
import sequence.encoding.Encoding;
import sequence.encoding.IEncoding;
import taxonomy.SimpleTaxonomy;
import utils.KolobokeUtils;

import java.io.*;
import java.util.*;

/**
 * Created by joachim on 19.06.19.
 */
public class SplitLongIndex implements IndexLoader, IndexStore, UpdateSender {
    private HashMap<Character, LongIntMap> indexMap = new HashMap<>();
    private IEncoding encoder;
    private SimpleTaxonomy taxonomy;
    private String folder;
    private int k;


    private List<UpdateReceiver> receiverList;
    private Report report;
    private Timer timer;
    private int processedFiles = 0;
    private long fileSize = 0;
    private long currentPos = 0;

    private HashLongIntMapFactory mapFactory;

    public SplitLongIndex(SimpleTaxonomy taxonomy, String folder, IEncoding encoder, int k) {
        this.taxonomy = taxonomy;
        this.folder = folder;
        this.k = k;
        this.encoder = encoder;
        this.mapFactory = HashLongIntMaps.getDefaultFactory();
        initialize();
    }

    public SplitLongIndex(){
        System.out.println("Old values:");

        HashLongIntMapFactory factory = HashLongIntMaps.getDefaultFactory();
        HashConfig config = factory.getHashConfig();
        System.out.println("min: " + config.getMinLoad() + " max: " + config.getMaxLoad() + " growth: " + config.getGrowthFactor());
        this.mapFactory = factory.withHashConfig(config.withMaxLoad(0.95).withGrowthFactor(1.3).withMinLoad(0.5).withTargetLoad(0.9));
        System.out.println("min: " + mapFactory.getHashConfig().getMinLoad() + " max: " + mapFactory.getHashConfig().getMaxLoad() + " growth: " + mapFactory.getHashConfig().getGrowthFactor());
    }

    private void initialize() {
        for (Character key : encoder.getAlphabet()) {
            System.out.println("initialize " + key);
            indexMap.put(key, HashLongIntMaps.newMutableMap());
        }
    }

    @Override
    public void load(String folder) {
        this.folder = folder;
        loadMeta();
        long keySetSize = 0;

        fileSize = getTotalFileSize(folder);
        File idx;
        for (Character character : encoder.getAlphabet()) {
            try {
                idx = new File(folder+"/"+character+".idx");
                loadIndexFile(character, idx, computeSizeForMap2(idx.length()));
                keySetSize += indexMap.get(character).sizeAsLong();
                System.out.println("size for " + character + ": " + indexMap.get(character).sizeAsLong());
                processedFiles++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("size of keyset: " + keySetSize);
        // Stop sending reports to receivers. Loading completed
        endUpdate();
    }

    public long getTotalFileSize(String folder) {
        long size = 0;
        File idx;
        for (Character character : encoder.getAlphabet()) {
            idx = new File(folder+"/"+character+".idx");
            size += idx.length();
        }
        return size;
    }

    @Override
    public Set<Long> getKeySet() {
        return null;
    }

    @Override
    public List<Integer> getTaxIds(List<Long> kmers) {
        List<Integer> ids = new ArrayList<>();
        for (Long kmer : kmers) {
            ids.add(getTaxId(kmer));
        }
        return ids;
    }


    private int computeSizeForMap(long filebytes) {
        return (int) ((filebytes / 12) * 1.53f);
    }
    private int computeSizeForMap2(long filebytes) { return (int) ((filebytes / 12) * 1.00f); }

    private void loadIndexFile(char c, File file, int expectedSize) throws IOException {
        System.out.println("load " + c + ".idx (with expected size " + expectedSize + ")");

        if (expectedSize < 1)
            indexMap.put(c, HashLongIntMaps.newMutableMap());
        else {
            indexMap.put(c, HashLongIntMaps.newMutableMap(expectedSize));
        }


        LongIntMap index = indexMap.get(c);

        BufferedReader reader = new BufferedReader(new FileReader(file));

        String line;
        String[] split;

        long kmer;
        int taxid;

        while ((line = reader.readLine()) != null) {
            currentPos += line.getBytes().length;

            split = line.split("\t");
            kmer = Long.parseLong(split[0]);
            taxid = Integer.parseInt(split[1]);

            index.put(kmer, taxid);
        }
    }

    @Override
    public void put(char[] sequence, int index, int taxid) {
        LongIntMap map = indexMap.get(encoder.reduce(sequence[index]));

        //String kmer = new String(sequence, index, k);
        //System.out.println(KmerEncoder.reduce(sequence[index]) + " -> " + kmer + " reduced: " + KmerEncoder.reduce(kmer) + " -> long " + KmerEncoder.kmerToLong(sequence, index, this.k));

        synchronized (map) {
            long key = encoder.kmerToLong(sequence, index, this.k);
            put(map, key, taxid);
        }
    }

    private void writeMeta(String folder) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/meta.txt"))) {
            writer.write(encoder.getEncodingString());
            writer.newLine();
            writer.write(Integer.toString(k));
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMeta() {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.folder + "/meta.txt"))) {
            encoder = new Encoding(reader.readLine());
            this.k = Integer.parseInt(reader.readLine());

            System.out.println(this.k);
            System.out.println(encoder.getEncodingString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void shutdown() {
        if (indexMap != null) {
            for (Character character : indexMap.keySet()) {
                System.out.println("Write " + character);
                KolobokeUtils.writeLongIntMap(indexMap.get(character), character.toString(), folder);
            }
            writeMeta(folder);
        }
    }


    private void put(LongIntMap map, long key, int taxid) {
        if (map.containsKey(key)) {
            if (map.get(key) == taxid) return;
            else map.put(key, taxonomy.getLCA(taxid, map.get(key)));
        } else
            map.put(key, taxid);
    }


    @Override
    public void put(String s, Integer taxid) {
        LongIntMap map = indexMap.get(encoder.reduce(s.charAt(0)));

        synchronized (map) {
            long key = encoder.kmerToLong(s);
            put(map, key, taxid);
        }
    }

    @Override
    public int getTaxId(String accession) {
        return getTaxId(accession.toCharArray(),0);
    }

    @Override
    public int getTaxId(long kmer) {
        return indexMap.get(encoder.getFirstChar(kmer, k)).get(kmer);
    }

    @Override
    public IEncoding getEncoder() {
        return encoder;
    }

    @Override
    public int getTaxId(char[] sequence, int index) {
        long key = encoder.kmerToLong(sequence, index, this.k);

        if (encoder.reduce(sequence[index])== 'X') {
            System.out.println("X at " + index);
            System.out.println(new String(sequence));
            System.exit(0);
        }

        return indexMap.get(encoder.reduce(sequence[index])).get(key);
    }

    /**
     * Extract keyset of Index
     * @return
     */
    public Set<Long> getKeyset() {
        Set<Long> kmerSet = new HashSet<>();
        for (Character character : indexMap.keySet()) {
            kmerSet.addAll(indexMap.get(character).keySet());
        }
        return kmerSet;
    }

    public static void main(String[] args) {

    }


    private void initializeReport() {
        report = new Report();
        report.addLong(Report.ReportId.START_TIME, System.currentTimeMillis());
    }

    private void finalizeReport() {
        report.addLong(Report.ReportId.END_TIME, System.currentTimeMillis());
        report.addLong(Report.ReportId.PROCESSED_ITEMS, processedFiles);
    }

    private void sendUpdate(Report report) {
        if (receiverList != null) {
            for (UpdateReceiver updateReceiver : receiverList) {
                updateReceiver.receiveUpdate(report);
            }
        }
    }

    private void endUpdate() {
        if (report == null) return;
        finalizeReport();
        if (receiverList !=  null) {
            for (UpdateReceiver updateReceiver : receiverList) {
                updateReceiver.receiveEndUpdate(report);
            }
        }
        cancelTimer();
    }

    public boolean addUpdateReceiver(UpdateReceiver receiver) {
        if (receiverList == null) receiverList = new ArrayList<>();
        if (!receiverList.contains(receiver)) {
            receiverList.add(receiver);
            return true;
        }
        return false;
    }

    public boolean removeUpdateReceiver(UpdateReceiver receiver) {
        if (receiverList != null && receiverList.contains(receiver))
            return receiverList.remove(receiver);
        return false;
    }

    private double getProgress() {
        double fileSize = this.fileSize/1000d;
        double curPos = currentPos/1000d;
        double ratio = ((long)((curPos/fileSize)*100000))/1000d;
        return ratio;
    }


    public void setTimer(long millis) {
        initializeReport();
        if (this.timer == null) this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Report report = new Report();
                report.addLong(Report.ReportId.TIME_STAMP, System.currentTimeMillis());
                report.addDouble(Report.ReportId.PROGRESS, getProgress());
                report.addLong(Report.ReportId.PROCESSED_ITEMS, processedFiles);
                sendUpdate(report);
            }
        }, 0L, millis);
    }

    @Override
    public int getK() {
        return k;
    }

    @Override
    public String getPath() {
        return folder;
    }

    public void cancelTimer() {
        this.timer.cancel();
    }
}
