package utils;

import java.sql.*;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bagci
 */
public class SQLiteMapping {

    private final String KEY;// = "accession";
    private final String VALUE;// = "taxid";
    private final String DATABASE;

    private String databaseFile;
    private Connection c;
    private Statement stmt;



    public SQLiteMapping(String databaseFile, String database, String key, String value) {
        this.KEY = key;
        this.VALUE = value;
        this.DATABASE = database;

        try {
            this.databaseFile = databaseFile;
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + this.databaseFile);
            stmt = c.createStatement();
        } catch (ClassNotFoundException ex) {
            System.out.println("Databasefile not found");
            Logger.getLogger(SQLiteMapping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException e) {
            System.out.println("Databasefile not found");
            Logger.getLogger(SQLiteMapping.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public HashSet<Integer> getTaxIds(String acc) {
        HashSet<Integer> ret = new HashSet<Integer>();
        String sql = "SELECT " + VALUE + " FROM " + DATABASE + " WHERE " + KEY + "='" + acc + "';";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            boolean exists = rs.next();
            if (exists) {
                String result = rs.getString(1);
                for (String taxidS : result.split(",")) {
                    ret.add(Integer.parseInt(taxidS));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(SQLiteMapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public synchronized Integer getTaxId(String acc) {
        HashSet<Integer> ret = new HashSet<Integer>();
        String sql = "SELECT " + VALUE + " FROM " + DATABASE + " WHERE " + KEY + "='" + acc + "';";

        try {
            ResultSet rs = stmt.executeQuery(sql);
            boolean exists = rs.next();
            if (exists) {
                String result = rs.getString(1);
                for (String taxidS : result.split(",")) {
                    ret.add(Integer.parseInt(taxidS));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erroneous Accession: " + acc);
            Logger.getLogger(SQLiteMapping.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        if (ret.isEmpty()) return 1;
        return ret.iterator().next();
    }

    public HashSet<String> getKeysForValue(int taxId) {
        HashSet<String> ret = new HashSet<String>();
        String sql = "SELECT " + KEY + " FROM " + DATABASE +
                "WHERE " + VALUE + "='%," + taxId + ",%' OR " + VALUE + "='" + taxId + "' OR " + VALUE + "='" + taxId +",%' OR " + VALUE + "='%," + taxId +"'";
        try{
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while(rs.next()) {
                for(int i = 1; i <= columnCount; i++) {
                    ret.add(rs.getString(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public HashSet<String> getKeysForValue(HashSet<String> allProteins, int taxid) {
        HashSet<String> ret = new HashSet<>();
        for(String p : allProteins) {
            HashSet<Integer> taxIds = getTaxIds(p);
            if(taxIds.contains(taxid))
                ret.add(p);
        }
        return ret;
    }


    public String getDatabaseFile() {
        return databaseFile;
    }

    public static void main(String args[]) {
        SQLiteMapping sqlite = new SQLiteMapping("accession2tid.db", "acc2taxid", "accession", "taxid");
        String acc = "NP_044193";
        System.out.println(sqlite.getTaxId(acc));
    }

}
