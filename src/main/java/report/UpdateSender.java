package report;

/**
 * Created by joachim on 15.07.19.
 */
public interface UpdateSender {
    boolean addUpdateReceiver(UpdateReceiver receiver);
    boolean removeUpdateReceiver(UpdateReceiver receiver);
    void setTimer(long timer);
    void cancelTimer();
}
