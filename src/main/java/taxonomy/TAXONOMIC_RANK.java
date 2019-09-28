package taxonomy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by joachim on 10.07.19.
 */
public enum TAXONOMIC_RANK {
    EMPIRE("empire", 1),

    SUPERKINGDOM("superkingdom", 2),
    KINGDOM("kingdom", 3),
    SUBKINGDOM("subkingdom", 4),

    SUPERPHYLUM("superphylum", 5),
    PHYLUM("phylum", 6),
    SUBPHYLUM("subphylum", 7),

    SUPERCLASS("superclass", 8),
    CLASS("class", 9),
    SUBCLASS("subclass", 10),
    INFRACLASS("infraclass", 11),

    COHORT("cohort", 12),
    SUBCOHORT("subcohort", 13),

    SUPERORDER("superorder", 14),
    ORDER("order", 15),
    SUBORDER("suborder", 16),
    INFRAORDER("infraorder", 17),
    PARVORDER("parvorder", 18),

    SUPERFAMILY("superfamily", 19),
    FAMILY("family", 20),
    SUBFAMILY("subfamily", 21),

    TRIBE("tribe", 22),
    SUBTRIBE("subtribe", 23),

    GENUS("genus", 24),
    SUBGENUS("subgenus", 25),

    SECTION("section", 26),
    SUBSECTION("subsection", 27),

    SERIES("series", 28),

    SPECIES_GROUP("species group", 29),
    SPECIES_SUBGROUP("species subgroup", 30),
    SPECIES("species", 31),

    SUBSPECIES("subspecies", 32),
    FORMA("forma", 32),
    VARIETAS("varietas", 32),

    NOT_VALID("", 33),
    NO_RANK("no rank", 33);

    private int numVal;
    private String rank;

    // Reverse-lookup map for getting a day from an abbreviation
    private static final Map<String, TAXONOMIC_RANK> lookup = new HashMap<String, TAXONOMIC_RANK>();
    private static final Map<Integer, TAXONOMIC_RANK> intLookup = new HashMap<Integer, TAXONOMIC_RANK>();
    public static Set<String> unknowntaxonids = new HashSet<>();

    static {
        for (TAXONOMIC_RANK r : TAXONOMIC_RANK.values()) {
            lookup.put(r.getString(), r);
        }
        for (TAXONOMIC_RANK r : TAXONOMIC_RANK.values()) {
            intLookup.put(r.getNumVal(), r);
        }

    }

    TAXONOMIC_RANK(String rank, int numVal) {
        this.numVal = numVal;
        this.rank = rank;
    }

    public int getNumVal() {
        return numVal;
    }

    public String getString() {
        return rank;
    }

    public static TAXONOMIC_RANK get(String rank) {
        if (!lookup.containsKey(rank)) {
            System.out.println("rank: " + rank + " not valid");
            unknowntaxonids.add(rank);
            return null;
        }
        return lookup.get(rank);
    }

    public static TAXONOMIC_RANK get(int rank) {
        if (!intLookup.containsKey(rank)) {
            System.out.println("rank: " + rank + " not valid");
            return null;
        }
        return intLookup.get(rank);
    }
}
