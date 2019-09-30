package main;

import accession.AccessionMap;
import accession.SQLiteAccessionMap;
import assessment.IndexAssessment;
import assessment.IndexAssessmentTask;
import classifier.Classifier;
import classifier.stats.ClassificationOutput;
import classifier.stats.ClassificationOutputSimple;
import com.koloboke.collect.map.LongIntMap;
import com.koloboke.collect.map.hash.HashLongIntMaps;
import index.indexbuilder.IndexBuilder;
import index.indexbuilder.SimpleUpdateReceiver;
import index.indexdb.IndexLoader;
import index.indexdb.IndexStore;
import index.indexdb.SplitLongIndex;
import org.apache.commons.cli.*;
import report.UpdateSender;
import sequence.BufferedFastaReader;
import sequence.BufferedFastqReader;
import sequence.FastxReader;
import sequence.encoding.Encoding;
import sequence.encoding.IEncoding;
import sequence.tools.Filter;
import statistics.OutputStatistics;
import taxonomy.*;
import utils.Utilities;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by joachim on 01.09.19.
 */
public class Main {
    /**
     * Enum for handling different run modes
     */
    public enum MODE {
        BUILD_INDEX ("build-index", "index building mode"),
        //PREP_NR ("prepare-nr", "prepare nr reference file for database creation"),
        FILTER("filter", "filter nr reference fasta and remove sequences that do contain characters other than that contained in the provided alphabet"),
        CLASSIFY("classify", "classify a query"),
        MULTI_CLASSIFY("multi-classify", "classify multiple files individually using the same index with a given file of commands."),
        STATISTICS("get-stats", "get statistics for a KrakenProt classification result"),
        ASSESS_INDEX("assess-index", "assess the information content for a KrakenProt index by classifiying "),
        TAXA("taxa", "Add taxon information to tab delimited file with a column containing taxonomic ids."),
        HELP("help", "print help");

        private String name;
        private String description;

        // Reverse-lookup map
        private static final Map<String, MODE> lookup = new HashMap<>();

        static {
            for (MODE m : MODE.values()) {
                lookup.put(m.getName(), m);
            }
        }

        MODE (String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return description;
        }

        public static MODE get(String mode) {
            if (!lookup.containsKey(mode))
                return null;
            return lookup.get(mode);
        }

        private static String padWithWhitspace(String s, int length) {
            StringBuilder builder = new StringBuilder(s);

            for (int i = s.length(); i < length; i++) {
                builder.append(" ");
            }
            return builder.toString();
        }


        private static String breakLine(String line, int len, int leadingWhitespace) {
            List<String> s = new ArrayList<>();
            for (int i = 0; i < line.length(); i++) {
                if ((i % len) == 0) {
                    int start = (i / len) * len;
                    int end = start + len;
                    if (end < line.length())
                        s.add(line.substring(start,end));
                }
            }

            s.add(line.substring((line.length()/len)*len, line.length()));

            StringBuilder builder = new StringBuilder(s.get(0));
            if (s.size() > 1)
                builder.append("\n");

            for (int i = 1; i < s.size(); i++) {
                builder.append(padWithWhitspace("",leadingWhitespace));
                builder.append(s.get(i));
                if (i < s.size()-1)
                    builder.append("\n");
            }


            return builder.toString();
        }

        public static void printModes() {
            System.out.println("Please specify a mode: java -jar pkraken.jar [mode] [options] ...\n");
            System.out.println(padWithWhitspace("",5) + "Available modes:");
            for (MODE mode : MODE.values()) {
                System.out.print(padWithWhitspace("", 5) + padWithWhitspace(mode.getName(), 20));
                System.out.println(breakLine(mode.getDescription(),50,25));
            }
            System.out.println();
        }
    }

    private static void buildIndex(String fasta, int k, String nodes, String acc, String output, String encoding, int threads) {
        SimpleTaxonomy taxonomy = new SimpleNCBITaxonomy(nodes);
        AccessionMap accession = acc == null ? null : new SQLiteAccessionMap(acc);
        BufferedFastaReader reader = null;
        try {
            reader = new BufferedFastaReader(new File(fasta));
            reader.addUpdateReceiver(new SimpleUpdateReceiver());
            reader.setTimer(3000L);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!new File(output).exists())
            Utilities.makeNewDir(output);
        String folder = output + "/k" + k;
        folder = Utilities.makeNewDir(folder);
        IEncoding encoder = new Encoding(encoding);
        IndexStore index = new SplitLongIndex(taxonomy, folder, encoder, k);

        IndexBuilder indexBuilder = new IndexBuilder(taxonomy, accession, reader, index, k);
        indexBuilder.build(threads);
    }

    private static void filter(String fastaFile, String outfile, String alphabet) {
        Set<Character> alphabetSet = new HashSet();
        for (char c : alphabet.toCharArray())
            alphabetSet.add(c);

        BufferedFastaReader reader = null;
        try {
            reader = new BufferedFastaReader(new File(fastaFile));
            reader.addUpdateReceiver(new SimpleUpdateReceiver());
            reader.setTimer(3000L);

        } catch (IOException e) {
            e.printStackTrace();
        }


        Filter filter = new Filter(reader, outfile, alphabetSet);
        try {
            filter.convert();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void classifyWithIndex(IndexLoader index, NCBITaxonomy taxonomy, String nodes, String names, String fastx1, String fastx2, String folder, int threads, boolean best, int threshold) {
        // TEST

        //Log.getInstance().init();


        FastxReader reader1 = null;
        FastxReader reader2 = null;

        boolean fastq = fastx1.endsWith(".fastq") || fastx1.endsWith(".fq") || fastx1.endsWith(".fnq");

        try {
            if (fastq) reader1 = new BufferedFastqReader(new File(fastx1));
            else reader1 = new BufferedFastaReader(new File(fastx1));
            if (fastx2 != null)
                if (fastq) reader2 = new BufferedFastqReader(new File(fastx2));
                else reader2 = new BufferedFastaReader(new File(fastx2));

            reader1.addUpdateReceiver(new SimpleUpdateReceiver());
            reader1.setTimer(20000L);

        } catch (IOException e) {
            e.printStackTrace();
        }

        String type = reader2 != null ? "PE" : "SE";
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy_HH:mm");

        String indexName;
        String[] splitIndexPath = index.getPath().split("/");
        if (splitIndexPath.length == 0)
            indexName = Integer.toString(index.getK());
        else
            indexName = splitIndexPath[splitIndexPath.length-1];


        // Create unique and remarkable folder name.
        StringBuilder newFolderPath = new StringBuilder(folder).append("/");
        newFolderPath.append(indexName);
        if (best || threshold > 0) {
            newFolderPath.append("_");
            if (best) newFolderPath.append("b");
            if (threshold > 0) newFolderPath.append(threshold);
        }

        String outputFolder = Utilities.makeNewDir(folder);
        //String outputFolder = Utilities.makeNewDir(Utilities.getUniqueDirPath(newFolderPath.toString()));

        ClassificationOutput output = null;
        try {
            output = new ClassificationOutputSimple(new BufferedWriter(new FileWriter(outputFolder + "/results.tsv")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Classifier classifier = new Classifier (taxonomy, index, reader1, reader2, output, best, threshold);
        classifier.run(threads);

        //System.out.println("print results yooo");
        //Log.getInstance().printResult();


        //String nodes, String names, String indexPath, String fastx1, String fastx2, String folder, int threads, boolean best
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder+"/parameters.txt"))) {
            writer.write("nodes: " + nodes);
            writer.newLine();
            writer.write("names: " + names);
            writer.newLine();
            writer.write("indexPath: " + index.getPath());
            writer.newLine();
            writer.write("readfile1: " + fastx1);
            writer.newLine();
            writer.write("readfile2: " + (fastx2));
            writer.newLine();
            writer.write("threads: " + threads);
            writer.newLine();
            writer.write("best: " + (best ? "true" : "false"));
            writer.newLine();
            writer.write("threshold: " + threshold);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Test
        //Log.getInstance().printResult();
    }

    private static String[] addToArgs(String[] args, String... add) {
        String[] newargs = new String[args.length + add.length];

        for (int i = 0; i < args.length; i++)
            newargs[i] = args[i];
        for (int i = args.length; i < newargs.length; i++) {
            newargs[i] = add[i-args.length];
        }
        return newargs;
    }

    public static void classify(String nodes, String names, String indexPath, String fastx1, String fastx2, String folder, int threads, boolean best, int threshold) {

        IndexLoader index = loadIndex(indexPath);
        NCBITaxonomy taxonomy = new NCBITaxonomy(nodes, names, true);
        classifyWithIndex(index, taxonomy, nodes, names, fastx1, fastx2, folder, threads, best, threshold);
    }

    public static IndexLoader loadIndex(String indexPath) {
        IndexLoader index = new SplitLongIndex();

        ((UpdateSender) index).addUpdateReceiver(new SimpleUpdateReceiver());
        ((UpdateSender) index).setTimer(20000L);

        index.load(indexPath);

        return index;
    }

    public static void assess(String nodes, String names, String indexPath, String accessionPath, String output, String fasta, int threads) {
        NCBITaxonomy taxonomy = new NCBITaxonomy(nodes, names);
        AccessionMap accession = new SQLiteAccessionMap(accessionPath);
        IndexLoader index = new SplitLongIndex();

        ((UpdateSender) index).addUpdateReceiver(new SimpleUpdateReceiver());
        ((UpdateSender) index).setTimer(20000L);


        index.load(indexPath);

        try (BufferedFastaReader reader = new BufferedFastaReader(new File(fasta))) {
            reader.addUpdateReceiver(new SimpleUpdateReceiver());
            reader.setTimer(3000L);

            IndexAssessment assessment = new IndexAssessment(index, taxonomy, reader, accession, output);
            assessment.run(threads);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void getStatistics (String nodes, String names, String input, String output, int classifiedField, int headerField, int classificationField, int realTaxIdField, String separator, String headerSeparator, int expectedId, String realCountsFile, TAXONOMIC_RANK... ranks) {
        NCBITaxonomy taxonomy = new NCBITaxonomy(nodes, names);

        OutputStatistics outputStatistics = null;
        if (headerSeparator != null)
            outputStatistics = new OutputStatistics(taxonomy,input,output, separator, classifiedField, headerField, classificationField, headerSeparator, ranks);
        else if (realTaxIdField != -1)
            outputStatistics = new OutputStatistics(taxonomy,input,output, separator, classifiedField, headerField, classificationField, realTaxIdField, ranks);
        else
            outputStatistics = new OutputStatistics(taxonomy,input,output, separator, classifiedField, headerField, classificationField, ranks);

        if (expectedId != -1)
            outputStatistics.provideExpectedId(expectedId);
        if (realCountsFile != null)
            outputStatistics.provideRealCount(realCountsFile);

        outputStatistics.processFile();
        outputStatistics.print();
        if (output != null)
            outputStatistics.write();
    }

    public static void getInformationOn(String nodes, String names, int... taxids) {
        NCBITaxonomy taxonomy = new NCBITaxonomy(nodes, names);

        for (int taxid : taxids) {
            Taxon taxon = taxonomy.getTaxon(taxid);
            if (taxon != null ) {
                System.out.println("taxid: " + taxid);
                System.out.println("name: " + taxon.getScientificName());
                System.out.println("rank: " + taxon.getRank().getString());
                System.out.println("isLeaf: " + (taxon.isLeaf() ? "true" : "false"));
                System.out.println("parent: " + taxon.getParent().getId() + " (" + taxon.getParent().getScientificName() + ")");
                System.out.println("_____________________________");
            }
        }

        for (int taxid : taxids) {
            TAXONOMIC_RANK rank = IndexAssessmentTask.getClosestRelevantRank(taxonomy.getTaxon(taxid));
            System.out.println(taxid + "\t" + rank.getString());
        }
    }

    public static void addTaxonNameToCSV(String csv, String out, String nodes, String names, int taxonfield, boolean rank) {
        NCBITaxonomy taxonomy = new NCBITaxonomy(nodes, names);

        try (BufferedReader reader = new BufferedReader(new FileReader(csv))) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(out));

            String line;
            String[] split;
            int tid;
            String name, ranks;
            Taxon t;

            while ((line = reader.readLine()) != null && !line.equals("")) {
                split = line.split("\t");
                tid = Integer.parseInt(split[taxonfield]);
                t = taxonomy.getTaxon(tid);
                name = t == null ? "null" : t.getScientificName();
                ranks = rank && t == null ? "null" : t.getRank().getString();

                for (String s : split)
                    writer.write(s + "\t");
                writer.write(name);
                if (rank)
                    writer.write("\t" + ranks);

                writer.newLine();
            }


            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] omitMode(String[] args) {
        String[] newargs = new String[0];
        if (args.length > 1) {
            newargs = new String[args.length-1];
            for (int i = 0; i < args.length-1; i++)
                newargs[i] = args[i+1];
        }
        return newargs;
    }

    public static void main(String[] args) {
        final HelpFormatter formatter = new HelpFormatter();
        final CommandLineParser parser = new DefaultParser();
        final PrintWriter writer = new PrintWriter(System.out);
        String program = "-jar pkraken.jar ";


        Option referenceOption = Option.builder("r")
                .longOpt("reference")
                .desc("specify file with reference sequences. (For index building only pass sequences with headers starting with the accession like >accession.version...")
                .required(true)
                .hasArg(true)
                .build();
        Option kOption = Option.builder("k")
                .longOpt("ksize")
                .desc("specify k-mer size")
                .required(true)
                .hasArg(true)
                .build();
        Option nodesOption = Option.builder("n")
                .longOpt("nodes")
                .desc("specify nodes.dmp for taxonomic information")
                .required(true)
                .hasArg(true)
                .build();
        Option namesOption = Option.builder("m")
                .longOpt("names")
                .desc("specify names.dmp for taxonomic information")
                .required(true)
                .hasArg(true)
                .build();
        Option accessionOption = Option.builder("a")
                .longOpt("accession")
                .desc("specify sqlite3 file (.db) for mapping nr accessions to taxonomic identifiers")
                .required(true)
                .hasArg(true)
                .build();
        Option outputFolderOption = Option.builder("o")
                .longOpt("outputFolder")
                .desc("KrakenProt outputs all files to the specified folder. If you provide k10_result/ then the folder k10_result will be created if it does not exist, an all output files will be in the folder.")
                .required(true)
                .hasArg(true)
                .build();
        Option encodingOption = Option.builder("e")
                .longOpt("encoding")
                .desc("Specify amino acid alphabet. Groups are separated by commata. Amino acids of the same group are mapped to and represented by the first amino acid of the respective group. Example: LIJMVO,CU,A,G,S,T,P,FY,W,EZ,DB,N,Q,KR,H,X. If you do not want to use a reduced alphabet pass ACDEFGHIKLMNPQRSTVWY")
                .required(true)
                .hasArg(true)
                .build();
        Option threadsOption = Option.builder("t")
                .longOpt("threads")
                .desc("Number of threads used (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option outputFileOption = Option.builder("o")
                .longOpt("output")
                .desc("path to output file")
                .required(true)
                .hasArg(true)
                .build();
        Option alphabetOption = Option.builder("A")
                .longOpt("alphabet")
                .desc("provide amino acid alphabet (e.g. ACDEFGHIKLMNPQRSTVWY")
                .required(true)
                .hasArg(true)
                .build();
        Option read1Option = Option.builder("1")
                .longOpt("first")
                .desc("Path to fasta/fastq file. If data is paired-end, this is the first of two files")
                .required(true)
                .hasArg(true)
                .build();
        Option read2Option = Option.builder("2")
                .longOpt("second")
                .desc("Path to second fasta/fastq file. Only specify if your data is paired-end. (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option indexOption = Option.builder("i")
                .longOpt("index")
                .desc("Path to folder containing KrakenProt index.")
                .required(true)
                .hasArg(true)
                .build();
        Option inputOption = Option.builder("i")
                .longOpt("input")
                .desc("Input file.")
                .required(true)
                .hasArg(true)
                .build();
        Option headerFieldOption = Option.builder("h")
                .longOpt("headerField")
                .desc("index of header field in result file (first field is 0). (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option classificationFieldOption = Option.builder("c")
                .longOpt("classificationField")
                .desc("index of classification field in result file (first field is 0). (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option separatorFieldOption = Option.builder("s")
                .longOpt("separator")
                .desc("index of classification field in result file (first field is 0). (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option headerSeparatorOption = Option.builder("hs")
                .longOpt("hseparator")
                .desc("index of classification field in result file (first field is 0). (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option rankOption = Option.builder("r")
                .longOpt("rank")
                .desc("provide linnean rank, e.g. species or genus (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option correctTaxIdFieldOption = Option.builder("cf")
                .longOpt("correctTaxIdField")
                .desc("Provide field that contains correct taxid. First field is (1) (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option bestFlag = Option.builder("b")
                .longOpt("best")
                .desc("Only take the reading frame with the most hits in the index for classification. Naive mode pools hits from all reading frames. (optional)")
                .required(false)
                .hasArg(false)
                .build();
        Option thresholdOption = Option.builder("T")
                .longOpt("threshold")
                .desc("leave reads with a number of index hits lower than the specified threshold unclassified. (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option expectedCountsOption = Option.builder("ec")
                .longOpt("expectedCounts")
                .desc("provide list of taxonomic ids (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option expectedIdOption = Option.builder("ei")
                .longOpt("expectedId")
                .desc("provide expected id that applies to all reads (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option classifiedFieldOption = Option.builder("cf")
                .longOpt("classifiedField")
                .desc("provide field num that contains C for classified or U for unclassified (optional)")
                .required(false)
                .hasArg(true)
                .build();
        Option cmdInputOption = Option.builder("cmds")
                .longOpt("commands")
                .desc("provide file with commands each line exactly the way they should be used with classify without specifying the classify mode. Do not provide index, names and nodes. Lines starting with a hashtag are considered comments.")
                .required(true)
                .hasArg(true)
                .build();
        Option addRankOption = Option.builder("rank")
                .longOpt("add-rank")
                .desc("Also add rank to column")
                .required(false)
                .hasArg(false)
                .build();
        Option taxonFieldOption = Option.builder("tf")
                .longOpt("taxon-field")
                .desc("specify which column contains the taxon ids")
                .required(true)
                .hasArg(true)
                .build();


        /**
         * Set options for build-index mode
         */
        Options idxOptions = new Options();
        idxOptions.addOption(referenceOption);
        idxOptions.addOption(kOption);
        idxOptions.addOption(nodesOption);
        idxOptions.addOption(accessionOption);
        idxOptions.addOption(outputFolderOption);
        idxOptions.addOption(encodingOption);
        idxOptions.addOption(threadsOption);

        /**
         * Set options for filter mode
         */
        Options filterOptions = new Options();
        filterOptions.addOption(inputOption);
        filterOptions.addOption(outputFileOption);
        filterOptions.addOption(alphabetOption);

        /**
         * Set options for classify mode
         */
        Options classifyOptions = new Options();
        classifyOptions.addOption(nodesOption);
        classifyOptions.addOption(namesOption);
        classifyOptions.addOption(read1Option);
        classifyOptions.addOption(read2Option);
        classifyOptions.addOption(indexOption);
        classifyOptions.addOption(outputFolderOption);
        classifyOptions.addOption(threadsOption);
        classifyOptions.addOption(bestFlag);
        classifyOptions.addOption(thresholdOption);

        /**
         * Set options for assess mode
         */
        Options assessOptions = new Options();
        assessOptions.addOption(nodesOption);
        assessOptions.addOption(namesOption);
        assessOptions.addOption(accessionOption);
        assessOptions.addOption(indexOption);
        assessOptions.addOption(threadsOption);
        assessOptions.addOption(referenceOption);
        assessOptions.addOption(outputFileOption);

        /**
         * Set options for get-stats mode
         */
        Options resultProcessOptions = new Options();
        resultProcessOptions.addOption(nodesOption);
        resultProcessOptions.addOption(namesOption);

        // Input file -i
        resultProcessOptions.addOption(inputOption);

        //optional
        resultProcessOptions.addOption(outputFolderOption);

        // which information is found in which column
        resultProcessOptions.addOption(classifiedFieldOption);
        resultProcessOptions.addOption(classificationFieldOption);
        resultProcessOptions.addOption(headerFieldOption);
        resultProcessOptions.addOption(classificationFieldOption);
        resultProcessOptions.addOption(separatorFieldOption);

        //provide information on provenance
        resultProcessOptions.addOption(headerSeparatorOption);
        resultProcessOptions.addOption(correctTaxIdFieldOption);
        resultProcessOptions.addOption(expectedCountsOption);
        resultProcessOptions.addOption(expectedIdOption);

        // classification on which ranks
        resultProcessOptions.addOption(rankOption);


        /**
         * Set options for get taxonomic ids
         */
        Options taxonomicIdsOptions = new Options();
        taxonomicIdsOptions.addOption(nodesOption);
        taxonomicIdsOptions.addOption(namesOption);
        taxonomicIdsOptions.addOption(outputFileOption);
        taxonomicIdsOptions.addOption(inputOption);
        taxonomicIdsOptions.addOption(addRankOption);
        taxonomicIdsOptions.addOption(taxonFieldOption);


        /**
         * Set options for classify-multiple
         */
        Options classifyMultipleOptions = new Options();
        classifyMultipleOptions.addOption(indexOption);
        classifyMultipleOptions.addOption(cmdInputOption);
        classifyMultipleOptions.addOption(nodesOption);
        classifyMultipleOptions.addOption(namesOption);


        // Determine mode
        if (args.length == 0)  {
            MODE.printModes();
            System.exit(0);
        }

        MODE mode = MODE.get(args[0]);


        CommandLine cmd;


        // DEFAULTS
        int threadDefault = 1;
        int thresholdDefault = 0;

        //Statistics default
        int classifiedFieldDefault = 0;
        int headerFieldDefault = 1;
        int classificationFieldDefault = 2;
        int correctTaxIdFieldDefault = -1;
        String separatorDefault = "\t";
        String headerSeparatorDefault = null;
        String expectedCountsDefault = null;
        TAXONOMIC_RANK defaultRank = TAXONOMIC_RANK.GENUS;


        if (mode == null) {
            MODE.printModes();
            formatter.printHelp(program + MODE.BUILD_INDEX.name, idxOptions);
            System.out.println();
            formatter.printHelp(program + MODE.FILTER.name, filterOptions);
            System.out.println();
            formatter.printHelp(program + MODE.ASSESS_INDEX.name, assessOptions);
            System.out.println();
            formatter.printHelp(program + MODE.CLASSIFY.name, classifyOptions);
            System.out.println();
        } else {
            switch (mode) {
                case BUILD_INDEX:
                    try {
                        System.out.println("build Index");
                        args = omitMode(args);
                        cmd = parser.parse(idxOptions, args);

                        String dbfolder = cmd.getOptionValue("o");
                        int k = Integer.parseInt(cmd.getOptionValue("k"));
                        String accession = cmd.hasOption('a') ? accession = cmd.getOptionValue('a') : null;
                        String fastaFile = cmd.getOptionValue("r");
                        String nodes = cmd.getOptionValue("n");
                        String encoding = cmd.getOptionValue("e");
                        int threads = cmd.hasOption('t') ? Integer.parseInt(cmd.getOptionValue('t')) : threadDefault;

                        buildIndex(fastaFile, k, nodes, accession, dbfolder, encoding, threads);

                    } catch (ParseException e) {
                        String syntax = program + MODE.BUILD_INDEX.name;
                        System.out.println(e.getMessage());
                        formatter.printUsage(writer, 80, syntax, filterOptions);
                        formatter.printHelp(syntax, idxOptions);
                    }
                    break;
                case CLASSIFY:
                    try {
                        System.out.println("classify");
                        args = omitMode(args);
                        cmd = parser.parse(classifyOptions, args);

                        String fastx1 = cmd.getOptionValue('1');
                        String fastx2 = cmd.hasOption('2') ? cmd.getOptionValue('2') : null;
                        String indexPath = cmd.getOptionValue('i');
                        String nodes = cmd.getOptionValue('n');
                        String names = cmd.getOptionValue('m');
                        String outputFolder = cmd.getOptionValue('o');
                        int threads = cmd.hasOption('t') ? Integer.parseInt(cmd.getOptionValue('t')) : threadDefault;
                        int threshold = cmd.hasOption('T') ? Integer.parseInt(cmd.getOptionValue('T')) : thresholdDefault;
                        boolean best = cmd.hasOption('b');

                        classify(nodes, names, indexPath, fastx1, fastx2, outputFolder, threads, best, threshold);

                    } catch (ParseException e) {
                        String syntax = program + MODE.CLASSIFY.name;
                        System.out.println(e.getMessage());
                        formatter.printUsage(writer, 80, syntax, filterOptions);
                        formatter.printHelp(syntax, classifyOptions);
                    }
                    break;

                case MULTI_CLASSIFY:
                    try {
                        args = omitMode(args);
                        cmd = parser.parse(classifyMultipleOptions, args);

                        String indexPath = cmd.getOptionValue('i');
                        String cmdFile = cmd.getOptionValue("cmds");
                        String nodes = cmd.getOptionValue("n");
                        String names = cmd.getOptionValue("m");

                        BufferedReader reader = new BufferedReader(new FileReader(cmdFile));
                        String line;

                        IndexLoader index = loadIndex(indexPath);
                        NCBITaxonomy taxonomy = new NCBITaxonomy(nodes, names);

                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("#"))  {
                                System.out.println(line);
                                continue;
                            }

                            args = line.split(" ");
                            args = addToArgs(args, "-n", "nodes", "-m", "names", "-i", "index");

                            cmd = parser.parse(classifyOptions, args);

                            String fastx1 = cmd.getOptionValue('1');
                            String fastx2 = cmd.hasOption('2') ? cmd.getOptionValue('2') : null;
                            String outputFolder = cmd.getOptionValue('o');
                            int threads = cmd.hasOption('t') ? Integer.parseInt(cmd.getOptionValue('t')) : threadDefault;
                            int threshold = cmd.hasOption('T') ? Integer.parseInt(cmd.getOptionValue('T')) : thresholdDefault;
                            boolean best = cmd.hasOption('b');

                            classifyWithIndex(index, taxonomy, nodes, names, fastx1, fastx2, outputFolder, threads, best, threshold);
                        }

                    } catch (ParseException e) {
                        String syntax = program + MODE.MULTI_CLASSIFY.name;
                        System.out.println(e.getMessage());
                        formatter.printUsage(writer, 80, syntax, classifyMultipleOptions);
                        formatter.printHelp(syntax, classifyMultipleOptions);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case FILTER:
                    try {
                        System.out.println("filter sequences");
                        args = omitMode(args);
                        cmd = parser.parse(filterOptions, args);

                        String fastaFile = cmd.getOptionValue("i");
                        String outputFile = cmd.getOptionValue("o");
                        String alphabet = cmd.getOptionValue("A");

                        filter(fastaFile, outputFile, alphabet);

                    } catch (ParseException e) {
                        String syntax = program + MODE.FILTER.name;
                        System.out.println(e.getMessage());
                        formatter.printUsage(writer, 80, syntax, filterOptions);
                        formatter.printHelp(syntax, filterOptions);
                    }
                    break;
                case ASSESS_INDEX:
                    try {
                        System.out.println("assess index");
                        args = omitMode(args);
                        cmd = parser.parse(assessOptions, args);

                        String reference = cmd.getOptionValue("r");
                        String indexPath = cmd.getOptionValue('i');
                        String nodes = cmd.getOptionValue('n');
                        String names = cmd.getOptionValue('m');
                        String outputFile = cmd.getOptionValue("o");
                        int threads = cmd.hasOption('t') ? Integer.parseInt(cmd.getOptionValue('t')) : threadDefault;
                        String accession = cmd.getOptionValue('a');

                        assess(nodes, names, indexPath, accession, outputFile, reference, threads);

                    } catch (ParseException e) {
                        String syntax = program + MODE.ASSESS_INDEX.name;
                        System.out.println(e.getMessage());
                        formatter.printUsage(writer, 80, syntax, assessOptions);
                        formatter.printHelp(syntax, assessOptions);
                    }
                    break;
                case HELP:
                    MODE.printModes();
                    System.out.println("To get help on a mode, run java -jar krakenprot <mode>\n");

                    break;
                case STATISTICS:
                    try {
                        System.out.println("statistics");
                        args = omitMode(args);
                        cmd = parser.parse(resultProcessOptions, args);

                        String nodes = cmd.getOptionValue('n');
                        String names = cmd.getOptionValue('m');
                        String input = cmd.getOptionValue('i');
                        String output = cmd.getOptionValue('o');

                        String separator = cmd.hasOption('s') ? cmd.getOptionValue('s') : separatorDefault;

                        int classifiedField = cmd.hasOption("cf") ? Integer.parseInt(cmd.getOptionValue("cf")) : classifiedFieldDefault;
                        int headerField = cmd.hasOption('h') ? Integer.parseInt(cmd.getOptionValue('h')) : headerFieldDefault;
                        int classificationField = cmd.hasOption('c') ? Integer.parseInt(cmd.getOptionValue('c')) : classificationFieldDefault;
                        int correctTaxIdField = cmd.hasOption("cf") ? Integer.parseInt(cmd.getOptionValue("cf")) : correctTaxIdFieldDefault;


                        String expectedCounts = cmd.hasOption("ec") ? cmd.getOptionValue("ec") : expectedCountsDefault;
                        int expectedId = cmd.hasOption("ei") ? Integer.parseInt(cmd.getOptionValue("ei")) : -1;
                        String rank = cmd.hasOption('r') ? cmd.getOptionValue('r') : null;


                        // Get RAnk or ranks right
                        TAXONOMIC_RANK[] ranklist;
                        if (rank != null) {
                            if (rank.contains(",")) {
                                String[] split = rank.split(",");
                                ranklist = new TAXONOMIC_RANK[split.length];
                                for (int i = 0; i < split.length; i++)
                                    ranklist[i] = TAXONOMIC_RANK.get(split[i]);
                            }
                            else
                                ranklist =  new TAXONOMIC_RANK[] {TAXONOMIC_RANK.get(rank)};
                        } else {
                            ranklist = new TAXONOMIC_RANK[] { defaultRank };
                        }

                        String headerSeparator = cmd.hasOption("hs") ? cmd.getOptionValue("hs") : headerSeparatorDefault;

                        getStatistics(nodes, names, input, output, classifiedField, headerField, classificationField, correctTaxIdField, separator, headerSeparator, expectedId, expectedCounts, ranklist);

                    } catch (ParseException e) {
                        String syntax = program + MODE.STATISTICS.getName();
                        System.out.println(e.getMessage());
                        formatter.printUsage(writer, 80, syntax, resultProcessOptions);
                        formatter.printHelp(syntax, resultProcessOptions);
                    }

                    break;
                case TAXA:
                    try {
                        args = omitMode(args);
                        cmd = parser.parse(taxonomicIdsOptions, args);

                        String nodes = cmd.getOptionValue('n');
                        String names = cmd.getOptionValue('m');
                        String csv = cmd.getOptionValue('i');
                        String output = cmd.getOptionValue('o');
                        int field = Integer.parseInt(cmd.getOptionValue("tf"));
                        boolean rank = cmd.hasOption("rank");

                        addTaxonNameToCSV(csv, output, nodes, names, field, rank);

                        //getInformationOn(nodes, names, ids);

                    } catch (ParseException e) {
                        String syntax = program + MODE.TAXA.getName();
                        System.out.println(e.getMessage());
                        formatter.printUsage(writer, 80, syntax, taxonomicIdsOptions);
                        formatter.printHelp(syntax, taxonomicIdsOptions);
                    }
                    break;
                default:

                    break;
            }
        }
    }
}
