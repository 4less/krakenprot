package report;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joachim on 11.07.19.
 */
public class Report {
    public enum ReportId {
        TIME_STAMP, PROCESSED_ITEMS, TIME_TO_LAST, START_TIME, END_TIME, PROGRESS
    }

    private Map<String, Long> longMap = new HashMap<>();
    private Map<String, Double> doubleMap = new HashMap<>();

    public void addLong(ReportId id, long num) {
        addLong(id.toString(), num);
    }

    public void addLong(String id, long num) {
        longMap.put (id, num);
    }

    public long getLong(ReportId id) {
        return getLong(id.toString());
    }

    public long getLong(String id) {
        return longMap.get(id);
    }

    public void addDouble(ReportId id, double num) { addDouble(id.toString(), num); }

    public void addDouble(String id, double num) { doubleMap.put(id, num); }

    public double getDouble(String id) { return doubleMap.get(id); }

    public double getDouble(ReportId id) { return getDouble(id.toString()); }
}
