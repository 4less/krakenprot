package utils;

import java.io.*;
import java.util.*;

/**
 * Created by joachim on 16.07.19.
 */
public class Utilities {
    public static String makeNewDir (String folder) {
        if (folder.endsWith("/"))
            folder = folder.substring(0, folder.length()-1);
        String newFolder = folder;

        File file = new File(newFolder);
        int num = 1;

        while (file.exists()) {
            newFolder = folder+"_"+num;
            file = new File(newFolder);
            num++;
        }
        file.mkdirs();

        return newFolder+"/";
    }

    public static String getFileNameFrom(String folder, String filename, String extension) {
        return folder+"/"+filename+"."+extension;
    }

    public static String getUniqueFilePath(String folder, String filename, String extension) {
        String newFile = getFileNameFrom(folder, filename, extension);
        File file = new File(newFile);
        int num = 1;

        while (file.exists()) {
            newFile = getFileNameFrom(folder, filename + "_" + num, extension);
            file = new File(newFile);
            num++;
        }

        return newFile;
    }

    public static String getUniqueDirPath(String folder) {
        String newFolder = folder;
        File file = new File(folder);
        int num = 1;

        while (file.exists()) {
            newFolder = folder + "_" + num;
            file = new File(folder + "_" + num);
            num++;
        }

        return newFolder;
    }

    public static List<Long> loadLongList(String file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<Long> list = new ArrayList<>();
            String item;

            while ((item = reader.readLine()) != null) {
                if (!item.isEmpty())
                    list.add(Long.parseLong(item));
            }
            return list;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String millisToDate(long millis) {
        //omit ns

        long days = millis / 86_400_000;
        long hours = (millis%86_400_000) / 3_600_000;
        long mins = (millis%3_600_000) / 60_000;
        long secs = (millis%60_000) / 1_000;
        long ms = (millis%1_000);


        return (days + "d " + hours + "h " + mins + "m " + secs + "s " + ms + "ms");
    }

    public static String nanoToDate(long nano) {
        long days = nano / 86_400_000_000_000L;
        long hours = (nano%86_400_000_000_000L) / 3_600_000_000_000L;
        long mins = (nano%3_600_000_000_000L) / 60_000_000_000L;
        long secs = (nano%60_000_000_000L) / 1_000_000_000L;
        long ms = (nano%1_000_000_000L) / 1_000_000L;


        return (days + "d " + hours + "h " + mins + "m " + secs + "s " + ms + "ms");
    }

    public static <T> void writeList(String file, List<T> objects) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            for (T object : objects) {
                writer.write(object.toString() + "\n");
            }

            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeIntIntMap(Map<Integer, Integer> map, String output) throws IOException {
        File file = new File(output);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));

        for (Integer i : map.keySet()) {
            bw.write(i + ": " + map.get(i) + "\n");
        }
        bw.close();
    }

    public static void writeStringIntMap(Map<String, Integer> map, String output) throws IOException {
        File file = new File(output);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output));

        for (String i : map.keySet()) {
            bw.write(i + ": " + map.get(i) + "\n");
        }
        bw.close();
    }

    public static int getIndexOfLargest( float[] array )
    {
        if ( array == null || array.length == 0 ) return -1; // null or empty

        int largest = 0;
        for ( int i = 1; i < array.length; i++ )
        {
            if ( array[i] > array[largest] ) largest = i;
        }
        return largest; // position of the first largest found
    }


    public static List<Integer> getSortedListFromIntSet(Set<Integer> set) {
        List<Integer> list = new ArrayList<>();
        for (Integer i : set)
            list.add(i);

        Collections.sort(list);

        return list;
    }

    public static void main(String[] args) {
        String folder = "output/k10";
        System.out.println(makeNewDir(folder));
        System.out.println(makeNewDir(folder));
        System.out.println(makeNewDir(folder));

        File file = new File("test.txt");
    }
}
