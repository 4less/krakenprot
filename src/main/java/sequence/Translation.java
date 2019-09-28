package sequence;

import java.util.HashMap;

/**
 * Created by joachim on 28.09.18.
 */
public class Translation {
    public static HashMap<Character, Character> complement = new HashMap<Character, Character>();
    public static HashMap<String, Character> codon = new HashMap<String, Character>();

    static {
        complement.put('A', 'T');
        complement.put('C', 'G');
        complement.put('G', 'C');
        complement.put('T', 'A');

        codon.put("AAA", 'K');
        codon.put("AAC", 'N');
        codon.put("AAG", 'K');
        codon.put("AAT", 'N');

        codon.put("ACA", 'T');
        codon.put("ACC", 'T');
        codon.put("ACG", 'T');
        codon.put("ACT", 'T');
        codon.put("ACN", 'T');

        codon.put("AGA", 'R');
        codon.put("AGC", 'S');
        codon.put("AGG", 'R');
        codon.put("AGT", 'S');

        codon.put("ATA", 'I');
        codon.put("ATC", 'I');
        codon.put("ATG", 'M');
        codon.put("ATT", 'I');


        codon.put("CAA", 'Q');
        codon.put("CAC", 'H');
        codon.put("CAG", 'Q');
        codon.put("CAT", 'H');

        codon.put("CCA", 'P');
        codon.put("CCC", 'P');
        codon.put("CCG", 'P');
        codon.put("CCT", 'P');
        codon.put("CCN", 'P');

        codon.put("CGA", 'R');
        codon.put("CGC", 'R');
        codon.put("CGG", 'R');
        codon.put("CGT", 'R');
        codon.put("CGN", 'R');

        codon.put("CTA", 'L');
        codon.put("CTC", 'L');
        codon.put("CTG", 'L');
        codon.put("CTT", 'L');
        codon.put("CTN", 'L');


        codon.put("GAA", 'E');
        codon.put("GAC", 'D');
        codon.put("GAG", 'E');
        codon.put("GAT", 'D');

        codon.put("GCA", 'A');
        codon.put("GCC", 'A');
        codon.put("GCG", 'A');
        codon.put("GCT", 'A');
        codon.put("GCN", 'A');

        codon.put("GGA", 'G');
        codon.put("GGC", 'G');
        codon.put("GGG", 'G');
        codon.put("GGT", 'G');
        codon.put("GGN", 'G');

        codon.put("GTA", 'V');
        codon.put("GTC", 'V');
        codon.put("GTG", 'V');
        codon.put("GTT", 'V');
        codon.put("GTN", 'V');


        codon.put("TAA", '#');
        codon.put("TAC", 'Y');
        codon.put("TAG", '#');
        codon.put("TAT", 'Y');

        codon.put("TCA", 'S');
        codon.put("TCC", 'S');
        codon.put("TCG", 'S');
        codon.put("TCT", 'S');
        codon.put("TCN", 'S');

        codon.put("TGA", '#');
        codon.put("TGC", 'C');
        codon.put("TGG", 'W');
        codon.put("TGT", 'C');

        codon.put("TTA", 'L');
        codon.put("TTC", 'F');
        codon.put("TTG", 'L');
        codon.put("TTT", 'F');
    }


    public static char[][] extract6ReadingFrames(char[] read) {
        //System.out.println("get triplet counts: ");

        char[] rf1 = new char[read.length / 3];
        char[] rf2 = new char[(read.length - 1) / 3];
        char[] rf3 = new char[(read.length - 2) / 3];
        char[] rf4 = new char[read.length / 3];
        char[] rf5 = new char[(read.length - 1) / 3];
        char[] rf6 = new char[(read.length - 2) / 3];

        Character aa;

        for (int i = 0; i < Math.max(Math.max(rf1.length, rf2.length), rf3.length); i++) {

            int j = 3 * i;

            rf1[i] = getAA("" + read[j] + read[j + 1] + read[j + 2]);

            if (i < rf2.length) {
                rf2[i] = getAA("" + read[j + 1] + read[j + 2] + read[j + 3]);
            }
            if (i < rf3.length) {
                rf3[i] = getAA("" + read[j + 2] + read[j + 3] + read[j + 4]);
            }

            int k = read.length - 1 - j;

            rf4[i] = getAA("" + complement.get(read[k]) + complement.get(read[k - 1]) + complement.get(read[k - 2]));

            if (i < rf2.length) {
                rf5[i] = getAA("" + complement.get(read[k - 1]) + complement.get(read[k - 2]) + complement.get(read[k - 3]));
            }
            if (i < rf3.length) {
                rf6[i] = getAA("" + complement.get(read[k - 2]) + complement.get(read[k - 3]) + complement.get(read[k - 4]));
            }

        }

        return new char[][]{
            rf1, rf2, rf3, rf4, rf5, rf6
        };

    }

    public static void print(String[] triplets) {
        for (String triplet :
                triplets) {
            System.out.print(triplet + " ");
        }
        System.out.println();
    }

    public static void print(char[] aas) {
        for (char aa :
                aas) {
            System.out.print(aa);
        }
        System.out.println();
    }

    public static char getAA(String triplet) {
        return codon.get(triplet) == null ? '#' : codon.get(triplet);
    }

    public static void main(String[] args) {
        String read = "GACTCAGACGATTAACGNATTANCACTACAACGATTACTACACTACACTAACACTACACGACTCAGACGATTAACGNATTANCACTACAACGATTACTACACTACACTAACACTACACGACGTACGTACGACGT";
        String read2 = "GACTCAGACGATTAACGNATTANCACTACAACGATTACTACACTACACTAACACTACACGACTCAGACGA";
        
        System.out.println("read: " + read);
        System.out.println("length: " + read.length());

        char[][] rfs = Translation.extract6ReadingFrames(read.toCharArray());

        for (char[] arg : rfs) {
            System.out.println(new String(arg));
        }
    }
}
