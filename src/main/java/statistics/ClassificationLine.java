package statistics;

import taxonomy.NCBITaxonomy;
import taxonomy.TAXONOMIC_RANK;

/**
 * Created by joachim on 04.09.19.
 */
public class ClassificationLine {
    // Line field accessors
    private static String separator;
    private static String headerSeparator;

    private static int classifiedField;
    private static int headerField;
    private static int idField;
    private static int correctIdField;

    private String split[];

    //Line variables
    private boolean classified;
    private String header;
    private int id;
    private int correctId;

    //TAXONOMY

    private NCBITaxonomy taxonomy = null;

    public ClassificationLine(String separator, int classifiedField, int headerField, int idField, int correctIdField) {
        this.separator = separator;
        this.classifiedField = classifiedField;
        this.headerField = headerField;
        this.idField = idField;
        this.correctIdField = correctIdField;
        this.headerSeparator = null;
    }

    public ClassificationLine(String separator, int classifiedField, int headerField, int idField, String headerSeparator) {
        this.separator = separator;
        this.classifiedField = classifiedField;
        this.headerField = headerField;
        this.idField = idField;
        this.correctIdField = -1;
        this.headerSeparator = headerSeparator;
    }

    public ClassificationLine(String separator, int classifiedField, int headerField, int idField) {
        this.separator = separator;
        this.classifiedField = classifiedField;
        this.headerField = headerField;
        this.idField = idField;
        this.correctIdField = -1;
        this.headerSeparator = null;
    }

    public void provideTaxonomy(NCBITaxonomy taxonomy) {
        this.taxonomy = taxonomy;
    }

    private void clearFields() {
        this.classified = false;
        this.header = null;
        this.id = -1;
        this.correctId = -1;
    }

    public void readNewLine(String line) {
        clearFields();
        this.split = line.split(separator);

        this.classified = this.split[classifiedField].equals("C");
        this.header = this.split[headerField];
        this.id = Integer.parseInt(this.split[idField]);

        if (hasCorrectId())
            this.correctId = this.correctIdField != -1 ? Integer.parseInt(this.split[correctIdField]) : Integer.parseInt(this.header.split(headerSeparator)[1]);
    }

    public boolean hasCorrectId() {
        return this.correctId != -1 || this.headerSeparator != null;
    }

    public boolean isClassified() {
        return this.classified;
    }

    public String getHeader() {
        return header;
    }

    public int getId() {
        return id;
    }

    public int getCorrectId() {
        return correctId;
    }

    public int getId(TAXONOMIC_RANK rank) {
        if (id > 0 && taxonomy != null && taxonomy.getTaxon(id) != null)
            return taxonomy.getParentOfRank(id, rank);
        return -1;
    }

    public int getCorrectId(TAXONOMIC_RANK rank) {
        if (correctId > 0 && taxonomy != null && taxonomy.getTaxon(correctId) != null)
            return taxonomy.getParentOfRank(correctId, rank);
        return -1;
    }
}
