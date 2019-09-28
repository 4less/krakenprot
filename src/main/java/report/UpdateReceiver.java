package report;

/**
 * Created by joachim on 11.07.19.
 */
public interface UpdateReceiver {
    void receiveUpdate(Report report);
    void receiveEndUpdate(Report report);
}
