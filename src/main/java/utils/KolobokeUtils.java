package utils;

import com.koloboke.collect.map.LongIntMap;
import com.koloboke.collect.map.hash.HashIntLongMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by joachim on 15.07.19.
 */
public class KolobokeUtils {
    public static void writeLongIntMap(LongIntMap map, String name, String folder) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/" + name + ".idx"));

            for (Long kmer : map.keySet()) {
                writer.write(kmer + "\t" + map.get(kmer.longValue()));
                writer.write("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Integer> getListFromIntIntMap(final HashIntLongMap map) {
        List<Integer> list = new ArrayList<>();
        for (Integer i : map.keySet())
            list.add(i);

        return list;
    }

    public static List<Integer> getSortedListFromIntLongMap(final HashIntLongMap map) {
        List<Integer> list = getListFromIntIntMap(map);

        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer first, Integer second) {
                long count1 = map.get(first.intValue());
                long count2 = map.get(second.intValue());
                if (count1 > count2)
                    return 1;
                if (count1 < count2)
                    return -1;
                return 0;
            }
        });
        Collections.reverse(list);
        return list;
    }
}
