package sequence.encoding;

import sequence.utils.UnknownAAException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joachim on 01.09.19.
 */
public class Encoding implements IEncoding {
    private String encoding;
    private int[] aaIntMap;
    private char[] intAaMap;

    int bit;

    public int aaToInt(char aa) {
        return aaIntMap[letterAsInt(aa)];
    }
    public static int letterAsInt(char aa) {
        return ((int) aa) - 0x41;
    }
    public char intToAA(int aa) { return intAaMap[aa]; }


    public Encoding(String mapping) {
        Map<Character, Integer> temp = new HashMap<>();
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        this.encoding = mapping;
        String[] split;
        if (mapping.contains(","))
            split = mapping.split(",");
        else {
            split = new String[mapping.length()];
            for (int i = 0; i < split.length; i++) {
                split[i] = String.valueOf(mapping.charAt(i));
            }
        }

        char[] group;
        intAaMap = new char[split.length];
        for (int i = 0; i < split.length; i++) {
            group = split[i].toCharArray();
            intAaMap[i] = group[0];
            for (char c : group)
                temp.put(c, i);
        }
        aaIntMap = new int[alphabet.length];

        for (int j = 0; j < alphabet.length; j++)
            if (temp.containsKey(alphabet[j]))
                aaIntMap[j] = temp.get(alphabet[j]);
            else
                aaIntMap[j] = -1;

        bit = getBit(intAaMap.length);
    }

    private int getBit(int size) {
        for (int i = 1; i <= 5; i++) {
            if (Math.pow(2, i) >= size)
                return i;
        }
        return -1;
    }



    /**
     * Reduces a given AASequence to a smaller aa alphabet
     * @param aaSequence
     * @return reduced aaSequence
     */
    public String reduce (String aaSequence) {
        char[] seq = aaSequence.toCharArray();
        reduce(seq);
        return new String(seq);
    }

    public char reduce (char aa) {
        return intAaMap[aaIntMap[letterAsInt(aa)]];
    }

    /**
     * Reduces a given AASequence to a smaller aa alphabet
     * @param seq
     * @return reduced aaSequence
     */
    public void reduce (char[] seq) {
        for (int i = 0; i < seq.length; i++) {
            int aminoAcid = ((int) seq[i]) - 0x41;
            if (aminoAcid < 0 || aminoAcid > 25)
                throw new UnknownAAException("Amino acid " + seq[i] + " not found in alphabet.");
            seq[i] = intAaMap[aaIntMap[aminoAcid]];
        }
    }



    /**
     * Converts kmer in char array to Long representation
     * @param kmer
     * @return long representation of kmer
     */
    public long kmerToLong(char[] kmer, int index, int k) {
        long res = 0;
        int shift;

        for (int i = 0; i < k; i++) {
            shift = (k - i - 1);
            res |= (long) aaToInt(kmer[index+i]) << shift*bit;
        }
        return res;
    }


    /**
     * Converts kmer to long representation
     * @param kmer
     * @return
     */
    public long kmerToLong(String kmer) {
        return kmerToLong(kmer.toCharArray(), 0, kmer.length());
    }

    /**
     * Converts a kmers long representation back to its String representation
     * @param kmer
     * @param k
     * @return
     */
    public String longToKmer(long kmer, int k) {
        char[] res = new char[k];
        int shift;
        for (int i = 0; i < k; i++) {
            shift = (k - i - 1);
            res[i] = intToAA((int) (kmer >> ((shift*bit)) & (long)(Math.pow(2,bit)-1)));
        }
        return new String(res);
    }

    @Override
    public char[] getAlphabet() {
        return intAaMap;
    }

    public char getFirstChar(long kmer, int k) {
        return intToAA((int) (kmer >> ((k-1)*bit) & (long)(Math.pow(2,bit)-1)));
    }

    public void print() {
        System.out.println("bit: " + bit);
        System.out.println("aaIntMap");
        for (int i : aaIntMap) {
            System.out.print(i+", ");
        }
        System.out.println();
        System.out.println("intAaMap");
        for (char c : intAaMap) {
            System.out.print(c+", ");
        }
        System.out.println();
    }


    public String getEncodingString() {
        return encoding;
    }



    public static void main(String[] args) {
        String kmer = "ADCLEYIDNTDKIIYLYYQDDKCVGKVKLRKNWNRYAYIEDIAVCKDFRGQGIGSALINI";

        //                           A  B   C  D   E  F  G  H   I  J  K   L  M  N   O  P  Q   R   S  T  U  V  W  X   Y  Z
        int[] aaIntMap = new int[] { 2, 10, 1, 10, 9, 7, 3, 14, 0, 0, 13, 0, 0, 11, 0, 6, 12, 13, 4, 5, 1, 0, 8, 15, 7, 9};
        char[] intAaMap = new char[] { 'L', 'C', 'A', 'G', 'S', 'T', 'P', 'F', 'W', 'E', 'D', 'N', 'Q', 'K', 'H', 'X'};

        String encoding = "LIJMVO,CU,A,G,S,T,P,FY,W,EZ,DB,N,Q,KR,H,X";

        Encoding e = new Encoding(encoding);
        e.print();

        long kmerl;

        System.out.println(kmer);
        System.out.println(e.reduce(kmer));
        System.out.println("ADCLEYIDNTDK");
        kmerl = e.kmerToLong(kmer.toCharArray(), 0, 12);
        System.out.println(kmerl);
        System.out.println(e.longToKmer(kmerl, 12));

        e = new Encoding("ACDEFGHIKLMNPQRSTVWY");
        e.print();

        System.out.println(kmer);
        System.out.println(e.reduce(kmer));
        System.out.println("DCLEYIDNTDKI");
        kmerl = e.kmerToLong("DCLEYIDNTDKI");
        System.out.println(kmerl);
        System.out.println(e.longToKmer(kmerl, 12));

        kmerl = e.kmerToLong(kmer.toCharArray(),1, 12);
        System.out.println(kmerl);
        System.out.println(e.longToKmer(kmerl, 12));

        System.out.println("ALPHABET");
        for (char c : e.getAlphabet()) {
            System.out.print(c);
        }

    }

}
