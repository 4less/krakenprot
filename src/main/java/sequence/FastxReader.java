package sequence;

import report.UpdateSender;

import java.io.IOException;

/**
 * Created by joachim on 15.07.19.
 */
public interface FastxReader extends UpdateSender {
    FastxRecord readRecord() throws IOException;
    long getReadRecordCount();
    long getFileSize();
    long getCurrentPos();
    String getFilePath();
    boolean isClosed();
}
