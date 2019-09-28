package taxonomy;

import report.Report;
import report.UpdateReceiver;
import report.UpdateSender;
import utils.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by joachim on 11.07.19.
 */
public class NCBITaxonomy implements Taxonomy, UpdateSender, UpdateReceiver {
    private Map<Integer, Taxon> taxonMap = new HashMap<>();

    private List<UpdateReceiver> receivers = null;
    private Timer timer;
    private Report report;

    private boolean suppressOutput = false;

    public NCBITaxonomy(File nodes, File names) {
        initUpdates();
        loadNodes(nodes);
        loadNames(names);
        inferLayer();
        endUpdates();
    }

    public NCBITaxonomy(String nodes, String names) {
        initUpdates();
        loadNodes(new File(nodes));
        loadNames(new File(names));
        inferLayer();
        endUpdates();
    }

    public NCBITaxonomy(String nodes, String names, boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
        if (!suppressOutput) initUpdates();
        loadNodes(new File(nodes));
        loadNames(new File(names));
        inferLayer();
        if (!suppressOutput) endUpdates();
    }


    private void initUpdates() {
        addUpdateReceiver(this);
        initReport();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Report report = new Report();
                report.addLong(Report.ReportId.TIME_STAMP, System.currentTimeMillis());
                report.addLong(Report.ReportId.PROCESSED_ITEMS, taxonMap.size());
                send(report);
            }
        }, 0L, 1000L);
    }

    private void endUpdates() {
        sendEnd(this.report);
        timer.cancel();
    }

    private void send(Report report) {
        if (receivers != null) {
            for (UpdateReceiver receiver : receivers) {
                receiver.receiveUpdate(report);
            }
        }
    }

    private void sendEnd(Report report) {
        endReport();
        if (receivers != null) {
            for (UpdateReceiver receiver : receivers) {
                receiver.receiveEndUpdate(report);
            }
        }
    }

    private void initReport() {
        this.report = new Report();
        this.report.addLong("START", System.currentTimeMillis());
    }

    private void endReport() {
        if (report != null) {
            report.addLong("END", System.currentTimeMillis());
        }
    }

    public Taxon getRoot() {
        return taxonMap.get(1);
    }

    private void loadNodes(File nodes) {
        FileReader fr = null;
        BufferedReader br = null;

        int taxonId;
        int parentId;
        TAXONOMIC_RANK rank;

        Taxon taxon;
        Taxon parentTaxon;

        try {
            fr = new FileReader(nodes);
            br = new BufferedReader(fr);

            String curLine;
            int linenr = -1;

            while ((curLine = br.readLine()) != null) {
                linenr++;

                String[] fields = curLine.split("\t\\|\t|\t\\|");

                taxonId = Integer.parseInt(fields[0]);
                parentId = Integer.parseInt(fields[1]);
                rank = TAXONOMIC_RANK.get(fields[2]);

                taxon = getOrPut(taxonId);
                parentTaxon = getOrPut(parentId);

                taxon.setRank(rank);
                taxon.setParent(parentTaxon);

                if (taxonId != parentId) {
                    parentTaxon.addChild(taxon);
                    taxon.setParentId(parentId);
                }
            }

        } catch(IOException e) {
            e.printStackTrace();
        }

    }

    private Taxon getOrPut(int taxonId) {
        Taxon taxon;
        if (!taxonMap.containsKey(taxonId)) {
            taxon = new Taxon (taxonId);
            taxonMap.put(taxonId, taxon);
        } else {
            taxon = taxonMap.get(taxonId);
        }
        return taxon;
    }

    private void loadNames(File names) {
        FileReader fr = null;
        BufferedReader br = null;
        Taxon taxon;
        String line;
        int lineCount = 0;

        try {
            fr = new FileReader(names);
            br = new BufferedReader(fr);

            while ((line = br.readLine()) != null) {
                lineCount++;

                String[] fields = line.split("\t\\|\t|\t\\|");
                int currentId = Integer.parseInt(fields[0]);

                taxon = getTaxon(currentId);

                taxon.addName(new TaxonName (
                        fields[1],
                        fields[2],
                        fields[3]
                ));
            }

        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public int getParentOfRank(int taxid, String rank) {
        TAXONOMIC_RANK trank = TAXONOMIC_RANK.get(rank);
        if (trank == null) return -1;
        return getParentOfRank(taxid, trank);
    }

    public int getParentOfRank(int taxid, TAXONOMIC_RANK rank) {
        Taxon node = getTaxon(taxid);
        if (node == null) {
            System.out.println(taxid + " not in tax tree");
            return -1;
        }

        if (node.getRank() == null) {
            System.out.println(node.getId() + " -> " + rank);
            for (String unknowntaxonid : TAXONOMIC_RANK.unknowntaxonids) {
                System.out.println(unknowntaxonid);
            }
        }

        int rankNum = node.getRank().getNumVal();
        int desRankNum = rank.getNumVal();

        while (rankNum > desRankNum && node.getId() != 1) {
            node = node.getParent();
            rankNum = node.getRank().getNumVal();
        }
        if (rankNum < desRankNum) return 0;
        return node.getId();
    }

    private void inferLayer() {
        inferLayerWorker(getRoot(), 0);
    }

    private void inferLayerWorker(Taxon taxon, int layer) {
        taxon.setLayer(++layer);

        for (Taxon taxonomyNode : taxon.getChildren())
            inferLayerWorker(taxonomyNode, layer);
    }

    public int getParent(int taxid) {
        return taxonMap.get(taxid).getParentId();
    }

    public int getParent(Taxon taxon) {
        return getParent(taxon.getId());
    }

    public Taxon getParentTaxon(int taxid) {
        return taxonMap.get(getParent(taxid));
    }

    public Taxon getParentTaxon(Taxon taxon) {
        return taxonMap.get(taxon.getParentId());
    }

    public Taxon getLCA(Taxon t1, Taxon t2) {
        if (t1.getLayer() > t2.getLayer())
            while (t1.getLayer() > t2.getLayer())
                t1 = t1.getParent();
        else if (t2.getLayer() > t1.getLayer())
            while (t2.getLayer() > t1.getLayer())
                t2 = t2.getParent();

        while (t1.getId() != t2.getId()) {
            t1 = t1.getParent();
            t2 = t2.getParent();
        }
        return t1;
    }

    public Taxon getTaxon(int taxid) {
        return taxonMap.get(taxid);
    }

    public List<TaxonName> getTaxonNames(int taxid) {
        return taxonMap.get(taxid).getNames();
    }

    public Taxon getTaxon(String name) {
        return null;
    }

    public int getLCA(int taxid1, int taxid2) {
        Taxon t1 = getTaxon(taxid1);
        Taxon t2 = getTaxon(taxid2);
        return getLCA(t1,t2).getId();
    }

    @Override
    public boolean contains(int id) {
        return taxonMap.containsKey(id);
    }


    @Override
    public boolean addUpdateReceiver(UpdateReceiver receiver) {
        if (receivers == null)
            receivers = new ArrayList<>();

        if (!receivers.contains(receiver)) {
            receivers.add(receiver);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeUpdateReceiver(UpdateReceiver receiver) {
        if (receivers != null && receivers.contains(receiver)) {
            receivers.remove(receiver);
            return true;
        }
        return false;
    }

    @Override
    public void setTimer(long timer) {
        return;
    }

    @Override
    public void cancelTimer() {
        this.timer.cancel();
    }

    @Override
    public void receiveUpdate(Report report) {
        System.out.println("Loading tree... " + report.getLong(Report.ReportId.PROCESSED_ITEMS) + " cats saved.");
    }

    @Override
    public void receiveEndUpdate(Report report) {
        System.out.println("Loading finished in " + (report.getLong("END") - report.getLong("START"))/1000 + " seconds");
    }

    public int getOffset(Taxon taxon, Taxon ancestor) {
        int offset = 0;
        if (taxon == null) {
            System.out.println("taxon is null");
            return -1;
        }
        if (ancestor == null) {
            System.out.println("ancestor is null");
            return -1;
        }

        while (taxon.getId() != ancestor.getId()) {
            if (taxon.getId() == 1) return -1;
            taxon = taxon.getParent();
            offset++;
        }
        return offset;
    }

    public static void main(String[] args) {

        NCBITaxonomy taxonomy = new NCBITaxonomy(new File("nodes.dmp"), new File("names.dmp"));


        List<Long> kraken = Utilities.loadLongList("server_data/mbp.gen.taxid.txt");
        List<Long> pkraken = Utilities.loadLongList("server_data/mbp.prot.taxid.txt");

        System.out.println("size kraken tids: " + kraken.size());
        System.out.println("size pkraken tids: " + pkraken.size());

        for (int i = 0; i < pkraken.size(); i++) {
            Taxon t = taxonomy.getTaxon(pkraken.get(i).intValue());

            if (t == null) {
                System.out.print(pkraken.get(i));
                System.out.println(",    length: " + pkraken.size());
            }
            if (t == null || !t.isLeaf()) {
                pkraken.remove(i);
                i--;
            }
        }

        System.out.println("AFTER");
        System.out.println("size kraken tids: " + kraken.size());
        System.out.println("size pkraken tids: " + pkraken.size());

        for (Long aLong : pkraken) {
            Taxon taxon = taxonomy.getTaxon(aLong.intValue());
        }

    }
}
