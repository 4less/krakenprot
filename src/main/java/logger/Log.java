package logger;

import taxonomy.TAXONOMIC_RANK;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by joachim on 03.09.19.
 */
public class Log {
    private static final Log instance = new Log();


    public Set<TAXONOMIC_RANK> ranks = new HashSet<>();
    private Map<Integer, Long> rfHitCounts = new HashMap<>();
    private long total = 0;

    private Log() {}

    public void init() {
        total = 0;
        for (int i = 0; i < 6; i++)
            rfHitCounts.put(i, 0L);
    }

    public void incrementHit(List<Long> counts) {
        Collections.sort(counts);
        Collections.reverse(counts);

        for (int i = 0; i < counts.size(); i++)
            rfHitCounts.put(i, rfHitCounts.get(i)+counts.get(i));
    }

    public void incrementRead() {
        total++;
    }

    public void printResult() {
        System.out.println(total);
        for (int i = 0; i < 6; i++) {
            System.out.println(i + ": " + rfHitCounts.get(i) + " -> " + ((double) rfHitCounts.get(i)/total));
        }
    }

    public static Log getInstance() {
        return instance;
    }
}
