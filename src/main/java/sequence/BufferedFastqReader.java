package sequence;

import index.indexbuilder.SimpleUpdateReceiver;
import report.Report;
import report.UpdateReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by joachim on 15.07.19.
 */
public class BufferedFastqReader extends BufferedReader implements FastxReader {
    private File file;
    private long readRecordCount = 0L;
    private long currentPos = 0L;

    private Timer timer;
    private Report report;
    private List<UpdateReceiver> receiverList;

    public BufferedFastqReader(File fastq) throws IOException {
        super(new FileReader(fastq));
        this.file = fastq;
    }

    @Override
    public synchronized FastqRecord readRecord() throws IOException {
        FastqRecord fr;

        String header = readLine();
        String sequence =readLine();
        String divider = readLine();
        String quality = readLine();

        if (header == null) {
            endUpdate();
            return null;
        }

        if (!header.startsWith("@") || !divider.startsWith("+")) {
            System.out.println(header);
            System.out.println(sequence);
            System.out.println(divider);
            System.out.println(quality);
            throw new IOException("Corrupted Fastq record");
        }

        fr = new FastqRecord(header, sequence, quality);

        return fr;
    }

    public String readLine() throws IOException {
        String line = super.readLine();
        if (line != null)
            currentPos += line.getBytes().length;
        return line;
    }

    @Override
    public long getReadRecordCount() {
        return readRecordCount;
    }

    @Override
    public long getFileSize() {
        return file.length();
    }

    @Override
    public long getCurrentPos() {
        return currentPos;
    }

    @Override
    public String getFilePath() {
        return file.getPath();
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    private void initializeReport() {
        report = new Report();
        report.addLong(Report.ReportId.START_TIME, System.currentTimeMillis());
    }

    private void finalizeReport() {
        report.addLong(Report.ReportId.END_TIME, System.currentTimeMillis());
        report.addLong(Report.ReportId.PROCESSED_ITEMS, getReadRecordCount());
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
        double fileSize = getFileSize()/1000d;
        double curPos = getCurrentPos()/1000d;
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
                report.addLong(Report.ReportId.PROCESSED_ITEMS, getReadRecordCount());
                sendUpdate(report);
            }
        }, 0L, millis);
    }


    public void cancelTimer() {
        this.timer.cancel();
    }

    public static void main(String[] args) {
        String fasta = "mbarc/mbarc_subset.fastq";

        try {
            final FastxReader reader = new BufferedFastqReader(new File(fasta));

            System.out.println("size: " + reader.getFileSize());

            reader.addUpdateReceiver(new SimpleUpdateReceiver());
            reader.setTimer(3000L);

            FastxRecord record;
            long x;
            while ((record = reader.readRecord()) != null) {
                x = reader.getReadRecordCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
