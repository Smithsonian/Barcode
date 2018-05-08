package smithsonian.merlin.util;

import com.google.common.io.Files;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by albesmn on 8/8/2016.
 */
public class DBController {

    private static final DBController dbcontroller = new DBController();
    private static Connection connection;
    private static String DB_PATH_LOCAL = "res/databases/";
    private static String DB_PATH_SHARED = Options.shared_folder_path;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Fehler beim Laden des JDBC-Treibers");
            e.printStackTrace();
        }
    }

    public static DBController getInstance() {
        return dbcontroller;
    }

    public static void initDBConnection() {
        try {
            // System.out.println("Creating Connection to Database...");
            System.out.println(DB_PATH_SHARED);
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH_SHARED);
            if (!connection.isClosed()) System.out.println("Connected to Database.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    if (!connection.isClosed() && connection != null) {
                        connection.close();
                        if (connection.isClosed())
                            System.out.println("Connection to Database closed.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void closeDBConnection() {
        try {
            connection.close();
            System.out.println("Connection to Database closed.");
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query");
            e.printStackTrace();
        }
    }

    public static void useDatabase(Session session) {
        String timeStamp = session.getTimeStamp();
        String cartNumber = session.getCart();

        // declare all attributes
        String date = timeStamp.replace('.', '-').replace('/', '-');
        String outputName = "CART" + cartNumber + "_" + date + ".db";
        String folder = Options.shared_folder_path;
        String museum = Options.museum.toLowerCase();

        String finalPath = "/" + museum + "/databases/" + outputName;

        System.out.println(finalPath);
        DB_PATH_SHARED = folder + finalPath;
        DB_PATH_LOCAL = "res/sessions" + finalPath;
    }

    public static void createDB(Session session) {
        try {

            String timeStamp = session.getTimeStamp();
            String cartNumber = session.getCart();
            // declare all attributes
            String date = timeStamp.replace('.', '-').replace('/', '-');
            String outputName = "CART" + cartNumber + "_" + date + ".db";
            String folder = Options.shared_folder_path;
            String museum = Options.museum.toLowerCase();

            DB_PATH_SHARED = folder + "/" + museum + "/databases/";
            DB_PATH_LOCAL = "res/sessions/" + museum + "/databases/";

            File f = new File(DB_PATH_SHARED);
            f.mkdirs();
            f = new File(DB_PATH_LOCAL);
            f.mkdirs();

            DB_PATH_SHARED += outputName;
            DB_PATH_LOCAL += outputName;

            f = new File(DB_PATH_SHARED);
            if (f.exists()) f.delete();
            else f.createNewFile();

            // stores a local copy of the database
            Files.copy(f, new File(DB_PATH_LOCAL));

            initDBConnection();
            Statement stmt = connection.createStatement(); // new Statement for queries

            /* Drop all tables */
            stmt.executeUpdate("DROP TABLE IF EXISTS locations");
            stmt.executeUpdate("DROP TABLE IF EXISTS items");

            /* Create locations table */
            stmt.executeUpdate("CREATE TABLE locations" +
                    "(location_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    " level0 varchar(32) DEFAULT NULL," +
                    " level1 varchar(32) DEFAULT NULL," +
                    " level2 varchar(32) DEFAULT NULL," +
                    " level3 varchar(32) DEFAULT NULL," +
                    " level4 varchar(32) DEFAULT NULL," +
                    " level5 varchar(32) DEFAULT NULL" +
                    ")");

            /* Create items table */
            stmt.executeUpdate("CREATE TABLE items" +
                    "(barcode varchar(32) PRIMARY KEY," +
                    " timeStamp varchar(32) DEFAULT NULL," +
                    " location INTEGER DEFAULT NULL," +
                    " FOREIGN KEY (location) REFERENCES locations (location_id)" +
                    ")");

            System.out.println("Database setup successful.");
            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void saveItemsInCurrentLocation(ObservableList<Item> items) {
        try {
            initDBConnection();
            PreparedStatement ps = connection.prepareStatement("INSERT OR IGNORE INTO locations (level0, level1, level2, level3, level4, level5) values(?, ?, ?, ?, ?, ?)");

            Location[] itemLocs = items.get(0).getLocation();
            for (int i = 0; i < itemLocs.length; i++) {
                if (itemLocs[i] != null)
                    ps.setString(i + 1, itemLocs[i].getValue());
                else
                    ps.setNull(i + 1, Types.VARCHAR);
            }
            ps.addBatch();

            ps.executeBatch();

            int lastID = ps.getGeneratedKeys().getInt(1);
            ps = connection.prepareStatement("INSERT INTO items (barcode, timeStamp, location) values(?, ?, ?)");

            for (int i = 0; i < items.size(); i++) {
                ps.setString(1, items.get(i).getBarcode());
                if (items.get(i).getTimeStamp() != null)
                    ps.setString(2, items.get(i).getTimeStamp());
                else
                    ps.setNull(2, Types.VARCHAR);
                ps.setInt(3, lastID);

                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();

            closeDBConnection();
            Files.copy(new File(DB_PATH_SHARED), new File(DB_PATH_LOCAL));
        } catch (Exception e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }
    }

    public static ObservableList<Item> getAllItems() {
        List<Item> list = new ArrayList<>();
        ObservableList<Item> result = FXCollections.observableList(list);
        try {
            initDBConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT *, locations.level0, " +
                    "locations.level1, locations.level2, locations.level3, " +
                    "locations.level4, locations.level5 " +
                    "FROM items " +
                    "INNER JOIN locations " +
                    "ON (items.location = locations.location_id)");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Item item = new Item(rs.getString("barcode"));

                Location[] locations = FileManager.loadLocations();
                Location[] itemLoc = new Location[6];
                for (int i = 0; i < itemLoc.length; i++) {
                    if (rs.getString("level" + i) != null && !rs.getString("level" + i).trim().isEmpty())
                        itemLoc[i] = new Location(locations[i].getName(), rs.getString("level" + i), i);
                }
                item.setLocation(itemLoc);
                if (rs.getString("timeStamp") != null && !rs.getString("timeStamp").trim().isEmpty())
                    item.setTimeStamp(rs.getString("timeStamp"));
                result.add(item);
            }

            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }

        return result;
    }

    public static void checkInItem(Item item) {
        try {
            initDBConnection();
            PreparedStatement ps = connection.prepareStatement("UPDATE items SET timeStamp=" + item.getTimeStamp() + " WHERE barcode='" + item.getBarcode() + "'");
            ps.execute();

            closeDBConnection();

            // stores a local copy of the database
            Files.copy(new File(DB_PATH_SHARED), new File(DB_PATH_LOCAL));
        } catch (Exception e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }
    }

    public static Location[] getLocationFromID(int id) {
        Location[] result = new Location[6];
        try {
            initDBConnection();
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM locations WHERE (location_id = " + id + ")");
            Location[] locations = FileManager.loadLocations();

            for (int i = 0; i < result.length; i++) {
                if (rs.getString("level" + i) != null && !rs.getString("level" + i).trim().isEmpty())
                    result[i] = new Location(locations[i].getName(), rs.getString("level" + i), i);
            }

            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }
        return result;
    }

    public static int[] getAllLocationIDs() {
        int[] result = null;
        try {
            initDBConnection();
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT location_id FROM locations");
            int counter = 0;
            while (rs.next()) {
                counter++;
            }
            result = new int[counter];

            counter = 0;
            rs = stmt.executeQuery("SELECT location_id FROM locations");
            while (rs.next()) {
                result[counter] = rs.getInt("location_id");
                counter++;
            }

            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }
        return result;
    }

    public static ObservableList<Item> getItemsInLocation(int locationID) {
        List<Item> list = new ArrayList<>();
        ObservableList<Item> result = FXCollections.observableList(list);
        try {
            initDBConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT *, locations.level0, " +
                    "locations.level1, locations.level2, locations.level3, " +
                    "locations.level4, locations.level5 " +
                    "FROM items " +
                    "INNER JOIN locations " +
                    "ON (items.location = locations.location_id)" +
                    "WHERE (location_id = " + locationID + ") ");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Item item = new Item(rs.getString("barcode"));

                Location[] itemLoc = new Location[6];
                Location[] locations = FileManager.loadLocations();

                for (int i = 0; i < itemLoc.length; i++) {
                    if (rs.getString("level" + i) != null && !rs.getString("level" + i).trim().isEmpty())
                        itemLoc[i] = new Location(locations[i].getName(), rs.getString("level" + i), i);
                }
                item.setLocation(itemLoc);
                if (rs.getString("timeStamp") != null && !rs.getString("timeStamp").trim().isEmpty())
                    item.setTimeStamp(rs.getString("timeStamp"));
                result.add(item);
            }

            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }

        return result;
    }

    public static int getNumberOfSameLevels() {
        int counter = 0;
        try {
            initDBConnection();
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS rowcount FROM locations");
            int entries = rs.getInt("rowcount");
            int count;

            do {
                rs = stmt.executeQuery("SELECT level" + counter + ", COUNT(*) AS c FROM locations GROUP BY level" + counter);
                if (!rs.isClosed()) {
                    count = rs.getInt("c");
                    System.out.println(count);
                    // debug
                    // System.out.println(entries + ": " + count);
                    counter++;
                } else break;
            } while (count == entries);

            if (--counter < 0) counter = 0; // no negative values

            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }
        return counter;
    }

    public static void outputAllItems() {
        try {
            initDBConnection();
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT barcode, location FROM items");
            while (rs.next()) System.out.println(rs.getString("barcode") + ": " + rs.getInt("location"));

            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }
    }

    public static void outputAllLocations() {
        try {
            initDBConnection();
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM locations");
            while (rs.next()) System.out.println(rs.getInt("location_id") + ": " + rs.getString("level0")
                    + "," + rs.getString("level1") + "," + rs.getString("level2") + "," + rs.getString("level3")
                    + "," + rs.getString("level4") + "," + rs.getString("level5"));

            closeDBConnection();
        } catch (SQLException e) {
            System.err.println("Couldn't handle DB-Query.");
            e.printStackTrace();
        }
    }


}