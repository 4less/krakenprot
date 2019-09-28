package accession;

import utils.SQLiteMapping;

/**
 * Created by joachim on 16.03.19.
 */
public class SQLiteAccessionMap implements AccessionMap {
    private SQLiteMapping sqLiteMapping;
    public SQLiteAccessionMap(String databaseFile) {
        this.sqLiteMapping = new SQLiteMapping(databaseFile, "acc2taxid", "accession", "taxid");
    }

    @Override
    public Integer getTaxId(String identifier) {
        return sqLiteMapping.getTaxId(identifier);
    }

    public static void main(String[] args) {
        AccessionMap map = new SQLiteAccessionMap("accession2tid.db");
        System.out.println(map.getTaxId("AEJ77532"));
    }
}
