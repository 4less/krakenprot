package statistics;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import taxonomy.NCBITaxonomy;
import taxonomy.TAXONOMIC_RANK;
import taxonomy.Taxon;
import utils.Utilities;

import javax.rmi.CORBA.Util;
import java.io.*;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;


/**
 * Created by joachim on 06.09.19.
 */
public class OutputStatistics {
    private static ClassificationLine currentLine;

    private boolean hasCorrectId = false;
    private int expectedId = -1;
    private Taxon expectedTaxon = null;

    private String input;
    private String output;

    private TAXONOMIC_RANK[] ranks;

    private NCBITaxonomy taxonomy;

    private Map<Integer, Integer> countMap = new HashMap<>();
    private Map<Integer, Integer> correctCountMap = null;
    private Map<TAXONOMIC_RANK, Map<Integer, Integer>> countRankMap = new HashMap<>();
    private Map<TAXONOMIC_RANK, Map<Integer, Integer>> correctCountRankMap = new HashMap<>();

    private Map<TAXONOMIC_RANK, Map<String, Integer>> statisticsMap = new HashMap<>();

    private static final String CORRECT_AT_RANK = "correctAtRank";
    private static final String TOTAL_AT_RANK = "totalAtRank";
    private static final String ALL_BUT_ANCESTOR = "D+E";

    private int totalCount = 0;
    private int classifiedCount = 0;
    private int unclassifiedCount = 0;

    private int matchExpected = 0;


    public OutputStatistics(NCBITaxonomy taxonomy, String input, String outputFolder, String separator, int classifiedField, int headerField, int idField, int correctIdField, TAXONOMIC_RANK... ranks) {
        this.input = input;
        this.output = outputFolder;
        this.taxonomy = taxonomy;
        this.ranks = ranks;
        this.hasCorrectId = true;
        this.correctCountMap = new HashMap<>();
        currentLine = new ClassificationLine(separator, classifiedField, headerField, idField, correctIdField);
        initialize();

    }

    public OutputStatistics(NCBITaxonomy taxonomy, String input, String outputFolder, String separator, int classifiedField, int headerField, int idField,  String headerSeparator, TAXONOMIC_RANK... ranks) {
        this.input = input;
        this.output = outputFolder;
        this.taxonomy = taxonomy;
        this.ranks = ranks;
        this.hasCorrectId = true;
        this.correctCountMap = new HashMap<>();
        currentLine = new ClassificationLine(separator, classifiedField, headerField, idField, headerSeparator);
        initialize();
    }

    public OutputStatistics(NCBITaxonomy taxonomy, String input, String outputFolder, String separator, int classifiedField, int headerField, int idField, TAXONOMIC_RANK... ranks) {
        this.input = input;
        this.output = outputFolder;
        this.taxonomy = taxonomy;
        this.ranks = ranks;
        currentLine = new ClassificationLine(separator, classifiedField, headerField, idField);
        initialize();
    }

    public void provideExpectedId(int expected) {
        this.expectedId = expected;
        expectedTaxon = taxonomy.getTaxon(expected);
    }

    public void provideRealCount(String countPath) {
        correctCountMap = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(countPath))) {
            String line;
            String[] split;
            int taxid;
            int count;
            while ((line = bufferedReader.readLine()) != null) {
                split = line.split("\t");
                if (split.length != 2) {
                    System.out.println("malformed count file (too many columns) for line: " + line);
                    return;
                }
                taxid = Integer.parseInt(split[0]);
                count = Integer.parseInt(split[1]);

                correctCountMap.put(taxid, count);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (TAXONOMIC_RANK rank : ranks) {
            for (Integer id : correctCountMap.keySet()) {
                int idRank = taxonomy.getParentOfRank(id, rank);
                if (idRank > 0) {
                    if (!correctCountRankMap.containsKey(rank))
                        correctCountRankMap.put(rank, new HashMap<>());

                    Map<Integer,Integer> rankMap = correctCountRankMap.get(rank);
                    if (rankMap.containsKey(idRank))
                        rankMap.put(idRank, correctCountMap.get(id) + rankMap.get(idRank));
                    else
                        rankMap.put(idRank, correctCountMap.get(id));
                }
            }
        }
    }


    private void initialize() {
        currentLine.provideTaxonomy(this.taxonomy);
        for (TAXONOMIC_RANK rank : ranks) {
            countRankMap.put(rank, new HashMap<>());
            statisticsMap.put(rank, new HashMap<>());

            if (hasCorrectId) {
                correctCountRankMap.put(rank, new HashMap<>());
                statisticsMap.get(rank).put(CORRECT_AT_RANK, 0);
                statisticsMap.get(rank).put(TOTAL_AT_RANK, 0);
                statisticsMap.get(rank).put(ALL_BUT_ANCESTOR, 0);
            }
        }
    }

    public void processFile() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(this.input))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                processLine(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processLine(String line) {
        this.currentLine.readNewLine(line);
        totalCount++;

        // If correct id is provided, save correct id stats
        if (hasCorrectId) {
            // Count correct taxa
            incrementIntCount(currentLine.getCorrectId(), correctCountMap);
            // Count correct taxa at rank
            for (TAXONOMIC_RANK rank : ranks) {
                if (currentLine.getCorrectId(rank) > 0) {
                    incrementIntCount(currentLine.getCorrectId(rank), correctCountRankMap.get(rank));
                    incrementStringCount(TOTAL_AT_RANK, statisticsMap.get(rank));
                }
            }
        }

        if (currentLine.isClassified()) {
            classifiedCount++;
            // Count predicted taxa for different Rank levels
            //Without rank
            incrementIntCount(currentLine.getId(), countMap);
            //With rankrank
            for (TAXONOMIC_RANK rank : ranks) {
                if (currentLine.getId(rank) != -1)
                    // add up count if it has the particular rank
                    if (currentLine.getId(rank) != 0)
                        incrementIntCount(currentLine.getId(rank), countRankMap.get(rank));
            }

            // Assess if labeled id is child of assessed id
            if (expectedTaxon != null) {
                Taxon current = taxonomy.getTaxon(currentLine.getId());
                if (current != null && current.isChildOf(expectedTaxon) != -1)
                    matchExpected++;
            }


            if (hasCorrectId) {
                int idForRank;
                int correctIdForRank;

                for (TAXONOMIC_RANK rank : ranks) {
                    idForRank = currentLine.getId(rank);
                    correctIdForRank = currentLine.getCorrectId(rank);

                    // Means correct classification and guessed classification have a genus rank below (towards root)
                    if (correctIdForRank > 0 && idForRank > 0)
                        if (idForRank == correctIdForRank) incrementStringCount(CORRECT_AT_RANK, statisticsMap.get(rank));


                    // If read is labeled and is no ancestor of correct genus
                    if (correctIdForRank > 0) {


                        Taxon correctTaxonForRank = taxonomy.getTaxon(correctIdForRank);
                        Taxon taxon = taxonomy.getTaxon(currentLine.getId());

                        if (taxon != null && (correctTaxonForRank.getParent().isChildOf(taxon) == -1)) {
                            incrementStringCount(ALL_BUT_ANCESTOR, statisticsMap.get(rank));
                        }
                    }
                }
            }
        } else {
            unclassifiedCount++;
        }
    }

    public void print() {
        System.out.println("T\t: " + totalCount);
        System.out.println("C\t: " + classifiedCount);
        System.out.println("U\t: " + unclassifiedCount);
        System.out.println("PC\t: " + ((double) classifiedCount/(double)totalCount));
        if (expectedTaxon != null) {
            System.out.println("Match expected (" + expectedTaxon.getId() + "): " + matchExpected);
            System.out.println("Match expected (" + expectedTaxon.getId() + "): " + ((double)matchExpected/classifiedCount));
        }
        System.out.println("_________________________________________");
        for (TAXONOMIC_RANK rank : ranks) {
            System.out.println(rank.getString());
            if (hasCorrectId) {
                Map<String, Integer> rankStats = statisticsMap.get(rank);
                double sensitivity = (double) rankStats.get(CORRECT_AT_RANK) / rankStats.get(TOTAL_AT_RANK);
                double precision = (double) rankStats.get(CORRECT_AT_RANK) / rankStats.get(ALL_BUT_ANCESTOR);
                System.out.println("\tSe:\t" + sensitivity);
                System.out.println("\tPr:\t" + precision);
            } if (correctCountMap != null && !correctCountMap.isEmpty()) {
                System.out.println("\tIC:\t" + (double) getNumOfIncorrectClassifications(rank) / (double) classifiedCount);
                 System.out.println("\tPC:\t" + getPearsonsCorrelation(rank));
                System.out.println("\tCS:\t" + getChiSquared(rank));
            }
        }
    }

    private void incrementIntCount(int key, Map<Integer, Integer> map) {
        if (map.containsKey(key))
            map.put(key, map.get(key)+1);
        else
            map.put(key, 1);
    }

    private void incrementStringCount(String key, Map<String, Integer> map) {
        if (map.containsKey(key))
            map.put(key, map.get(key)+1);
        else
            map.put(key, 1);
    }

    private int getNumOfIncorrectClassifications(TAXONOMIC_RANK rank) {
        int count = 0;
        Map<Integer,Integer> rankMap = countRankMap.get(rank);
        for (Integer id : rankMap.keySet()) {
            if (!correctCountRankMap.get(rank).containsKey(id))
                count += rankMap.get(id);
        }
        return count;
    }

    private double[][] getArraysForStatistics(TAXONOMIC_RANK rank) {
        Map<Integer, Integer> guessedCounts = countRankMap.get(rank);
        Map<Integer, Integer> correctCounts = correctCountRankMap.get(rank);

        double[] guessed = new double[correctCounts.keySet().size()];
        double[] correct = new double[guessed.length];

        Set<Integer> correctTaxa = correctCounts.keySet();

        int incorrectlyLabelled = 0;
        for (Integer id : guessedCounts.keySet()) {
            if (!correctTaxa.contains(id))
                incorrectlyLabelled += guessedCounts.get(id);
        }

        int arrayIndex = 0;
        for (Integer id : correctTaxa) {
            correct[arrayIndex] = correctCounts.get(id);
            guessed[arrayIndex++] = guessedCounts.containsKey(id) ? guessedCounts.get(id) : Double.MIN_VALUE;
        }
        //guessed[arrayIndex] = incorrectlyLabelled;
        //correct[arrayIndex] = Double.MIN_VALUE;
        return new double[][] { correct, guessed };
    }

    private double getPearsonsCorrelation(TAXONOMIC_RANK rank) {
        double[][] arrays = getArraysForStatistics(rank);
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation();
        return pearsonsCorrelation.correlation(arrays[0], arrays[1]);
    }

    private double getChiSquared(TAXONOMIC_RANK rank) {
        double[][] arrays = getArraysForStatistics(rank);
        long[] observed = new long[arrays[0].length];
        for (int i = 0; i < arrays[0].length; i++) {
            observed[i] = (long) arrays[1][i];
            if (observed[i] == 0) observed[i] = 1L;
            if (arrays[0][i] == 0) arrays[0][i] = 1.0d;
        }
        ChiSquareTest chiSquareTest = new ChiSquareTest();

        return chiSquareTest.chiSquare(arrays[0], observed);
    }

    public void write() {
        if (!(new File(output).exists()))
            output = Utilities.makeNewDir(output);
        //output = Utilities.getUniqueDirPath(output);

        // Write metafile
        String metafile = Utilities.getUniqueFilePath(output, "meta", "csv");
        writeMeta(metafile);

        // Write Rankstats
        if (hasCorrectId || (correctCountMap != null && !correctCountMap.isEmpty())) {
            String outfile = Utilities.getUniqueFilePath(output, "statistics", "csv");
            writeRankStats(outfile);
        }

        // Write guessedCounts
        String countFile = Utilities.getUniqueFilePath(output, "counts", "csv");
        writeCounts(countFile, countMap);

        for (TAXONOMIC_RANK rank : ranks) {
            countFile = Utilities.getUniqueFilePath(output, "counts_" +rank.getString(), "csv");
            writeCounts(countFile, countRankMap.get(rank));
        }

        if (correctCountMap != null && !correctCountMap.isEmpty()) {
            countFile = Utilities.getUniqueFilePath(output, "correct_counts", "csv");
            writeCounts(countFile, correctCountMap);

            for (TAXONOMIC_RANK rank : ranks) {
                countFile = Utilities.getUniqueFilePath(output, "correct_counts_" + rank.getString(), "csv");
                writeCounts(countFile, correctCountRankMap.get(rank));
            }
        }
    }

    public void writeMeta(String file) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("property\tvalue");
            bufferedWriter.newLine();
            bufferedWriter.write("totalCount\t" + totalCount);
            bufferedWriter.newLine();
            bufferedWriter.write("classified\t" + classifiedCount);
            bufferedWriter.newLine();
            bufferedWriter.write("unclassified\t" + unclassifiedCount);
            bufferedWriter.newLine();
            bufferedWriter.write("classified portion\t" + ((double) classifiedCount/(double)totalCount));

            if (expectedTaxon != null) {
                bufferedWriter.newLine();
                bufferedWriter.write("expectedId\t"+expectedId);
                bufferedWriter.newLine();
                bufferedWriter.write("expectedName\t" + expectedTaxon.getScientificName());
                bufferedWriter.newLine();
                bufferedWriter.write("match expected\t" + matchExpected);
                bufferedWriter.newLine();
                bufferedWriter.write("match expected portion\t" + ((double)matchExpected/classifiedCount));
            }


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeCounts(String file, Map<Integer, Integer> map) {
        map = sortMap(map);
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write("taxon\tnames\tcounts");
            bufferedWriter.newLine();

            Taxon taxon;
            String name;

            for (Integer integer : map.keySet()) {
                taxon = taxonomy.getTaxon(integer);
                name = taxon != null ? taxon.getScientificName() : "not available for that rank";
                bufferedWriter.write(integer + "\t" + name + "\t" + map.get(integer));
                bufferedWriter.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer,Integer> sortMap(Map<Integer,Integer> map) {

        // let's sort this map by values first
        Map<Integer, Integer> sorted = map
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        return sorted;
    }

    public void writeRankStats(String file) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {

            //write header
            bufferedWriter.write("rank");
            if (hasCorrectId)
                bufferedWriter.write("\tsensitivity\tprecision\tf1");
            if (correctCountMap != null && !correctCountMap.isEmpty())
                bufferedWriter.write("\tincorrectclassification\tpearsoncorrelation\tchisquared");
            bufferedWriter.newLine();


            for (TAXONOMIC_RANK rank : ranks) {
                bufferedWriter.write(rank.getString());
                if (hasCorrectId) {
                    Map<String, Integer> rankStats = statisticsMap.get(rank);
                    double sensitivity = (double) rankStats.get(CORRECT_AT_RANK) / rankStats.get(TOTAL_AT_RANK);
                    double precision = (double) rankStats.get(CORRECT_AT_RANK) / rankStats.get(ALL_BUT_ANCESTOR);
                    double f1 = 2 * ((precision*sensitivity)/(precision+sensitivity));

                    bufferedWriter.write("\t"+sensitivity);
                    bufferedWriter.write("\t"+precision);
                    bufferedWriter.write("\t"+f1);

                } if (correctCountMap != null && !correctCountMap.isEmpty()) {
                    bufferedWriter.write("\t" + ((double) getNumOfIncorrectClassifications(rank) / (double) classifiedCount));
                    bufferedWriter.write("\t" + getPearsonsCorrelation(rank));
                    bufferedWriter.write("\t" + getChiSquared(rank));
                }
                bufferedWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //NCBITaxonomy treehitcounter = new NCBITaxonomy("nodes.dmp", "names.dmp");
        NCBITaxonomy taxonomy = new NCBITaxonomy("nodes.dmp", "names.dmp", true);

        //OutputStatistics stats = new OutputStatistics(treehitcounter, "classification/results.tsv", "classification/sim20", "\t", 0,1,2,"_", TAXONOMIC_RANK.SPECIES, TAXONOMIC_RANK.GENUS, TAXONOMIC_RANK.PHYLUM);

        //OutputStatistics stats = new OutputStatistics(treehitcounter, "classification/k10n_b5_mbarc.tsv", "classification/mbarc/", "\t", 0,1,2, TAXONOMIC_RANK.SPECIES, TAXONOMIC_RANK.GENUS, TAXONOMIC_RANK.PHYLUM);
        //stats.provideRealCount("mbarc_counts2.csv");

        OutputStatistics stats = new OutputStatistics(taxonomy, "classification/chlamydiia.tsv", "classification/chlam", "\t", 0,1,2, TAXONOMIC_RANK.SPECIES, TAXONOMIC_RANK.GENUS, TAXONOMIC_RANK.PHYLUM);
        //stats.provideExpectedId(204429);

        stats.processFile();
        stats.print();
        stats.write();
    }
}
