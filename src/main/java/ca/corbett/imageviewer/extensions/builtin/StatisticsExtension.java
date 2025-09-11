package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An extension for keeping track of how many images have been deleted/moved/copied
 * in the app, and can generate reports on statistics over time.
 * TODO track more than just deletions! track all operations
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public class StatisticsExtension extends ImageViewerExtension {

    private static final Logger logger = Logger.getLogger(StatisticsExtension.class.getName());
    private static final String STATS_FILENAME = "stats.sqlite";

    public enum DateRange {
        TODAY, LAST_MONTH, THIS_MONTH
    }

    ;

    private final AppExtensionInfo extInfo;
    private Connection statsConn = null;
    private final File statsFile;
    private boolean dbAvailable;

    public StatisticsExtension() {
        extInfo = new AppExtensionInfo.Builder("Statistics tracker")
                .setAuthor("steve@corbett.ca")
                .setVersion("1.0")
                .setTargetAppName(Version.APPLICATION_NAME)
                .setTargetAppVersion(Version.VERSION)
                .setShortDescription("Tracks statistics on ImageViewer operations.")
                .setLongDescription("Keeps track of how many deletions/moves/copies are performed "
                                            + "and can report on these statistics.")
                .setReleaseNotes("1.0 - extracted from ImageViewer 1.3")
                .build();
        statsFile = new File(Version.SETTINGS_DIR, STATS_FILENAME);

        // Make sure our classpath contains what we need:
        dbAvailable = true;
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ignored) {
            logger.severe("SQLite driver not available - Statistics extension will do nothing.");
            dbAvailable = false;
        }

    }

    @Override
    public AppExtensionInfo getInfo() {
        return extInfo;
    }

    @Override
    protected List<AbstractProperty> createConfigProperties() {
        return List.of();
    }

    @Override
    public void onActivate() {
        if (!dbAvailable || statsConn != null) {
            return;
        }
        boolean statsFilePresent = statsFile.exists();
        statsConn = null;
        try {
            statsConn = DriverManager.getConnection("jdbc:sqlite:" + statsFile.getAbsolutePath());
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem getting connection to " + statsFile.getName(), sqe);
        }

        // It's not an error condition if the file doesn't exist. Just create it if needed.
        if (!statsFilePresent) {
            logger.info("Creating new stats db");
            createStatsDatabase();
        }

    }

    @Override
    public void onDeactivate() {
        if (!dbAvailable) {
            return;
        }
        try {
            if (statsConn != null) {
                statsConn.close();
                statsConn = null;
            }
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem closing db connections.", sqe);
        }
    }

    @Override
    public List<JMenuItem> getMenuItems(String topLevelMenu, MainWindow.BrowseMode browseMode) {
        if ("View".equals(topLevelMenu)) {
            List<JMenuItem> list = new ArrayList<>();
            JMenuItem item = new JMenuItem("Deletion statistics");
            item.setMnemonic(KeyEvent.VK_D);
            final StatisticsExtension thisInstance = this;
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new StatisticsDialog(thisInstance).setVisible(true);
                }

            });
            list.add(item);
            return list;
        }

        return null;
    }

    @Override
    public void preImageOperation(ImageOperation.Type opType, File srcFile, File destination) {
        if (opType == ImageOperation.Type.DELETE) {
            logDeletion(srcFile);
        }
    }

    /**
     * Logs a deletion in the stats database.
     *
     * @param fileDeleted The file to be deleted. Must not be deleted yet so we can get file size.
     */
    private void logDeletion(File fileDeleted) {
        if (fileDeleted == null || !fileDeleted.exists() || !dbAvailable) {
            return;
        }

        long size = fileDeleted.length();
        String[] arr = fileDeleted.getName().split("\\.");
        String extension = arr[arr.length - 1].toLowerCase();

        String sql = "insert into stats (ext, date, size) values (?, date('now'), ?)";
        try (PreparedStatement statement = statsConn.prepareStatement(sql)) {
            statement.setString(1, extension);
            statement.setLong(2, size);
            statement.execute();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to log deletion.", sqe);
        }
    }

    /**
     * Returns an array of unique file extensions in the deletion stats database.
     * Extensions are converted to lower case to make comparisons consistent.
     * If the database is empty, the array will be empty.
     *
     * @return An array of distinct file extensions that have been deleted.
     */
    String[] getDeletedFileExtensions() {
        List<String> extList = new ArrayList<>();
        String sql = "select distinct ext from stats order by ext";
        try (Statement st = statsConn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                extList.add(rs.getString(1));
            }
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to query stats database.", sqe);
        }

        return extList.toArray(new String[]{});
    }

    /**
     * Returns the total size of all deleted files of the given extension in the given
     * date range.
     *
     * @param extension The file extension, case insensitive. If null, all file types are searched.
     * @param dateRange The date range to search. If null, no date filter is applied.
     * @return The sum of bytes deleted for all files matching the given filters.
     */
    long getDeletionTotal(String extension, DateRange dateRange) {
        long size = 0l;
        List<String> params = new ArrayList<>();

        try {
            String sql = "select sum(size) from stats ";
            if (extension != null || dateRange != null) {
                sql += "where ";

                if (extension != null) {
                    sql += "ext=? ";
                    params.add(extension.toLowerCase());
                }

                if (dateRange != null) {
                    if (extension != null) {
                        sql += "and ";
                    }

                    // This is cleaner code but doesn't seem to work (string concatenation maybe?)
//          sql += "date like ? || '%'";
//          switch (dateRange) {
//            case TODAY: params.add("date('now')"); break;
//            case LAST_MONTH: params.add("substr(date('now','-1 month'),0,8)"); break;
//            case THIS_MONTH: params.add("substr(date('now'),0,8)"); break;
//            default: break;
//          }
                    // This is ugly as sin, but works:
                    switch (dateRange) {
                        case TODAY:
                            sql += "date = date('now')";
                            break;
                        case LAST_MONTH:
                            sql += "date like substr(date('now','-1 month'),0,8) || '%'";
                            break;
                        case THIS_MONTH:
                            sql += "date like substr(date('now'),0,8) || '%'";
                            break;
                        default:
                            break;
                    }
                }
            }

            logger.log(Level.FINE, "Database.getDeletionTotal: executing \"{0}\"", sql);
            PreparedStatement statement = statsConn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                statement.setString(i + 1, params.get(i));
                logger.log(Level.FINE, "Parameter {0} = \"{1}\"", new Object[]{i, params.get(i)});
            }
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                size = rs.getLong(1);
            }

            rs.close();
            statement.close();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to query stats database.", sqe);
        }

        return size;
    }

    /**
     * Clears the deletion stats database. Can be invoked at any time.
     */
    void clearStats() {
        if (!dbAvailable) {
            return;
        }

        try (Statement statement = statsConn.createStatement()) {
            statement.executeUpdate("delete from stats");
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to clear stats database.", sqe);
        }
    }

    /**
     * Invoked internally to create the stats database if it does not yet exist.
     */
    private void createStatsDatabase() {
        if (!dbAvailable) {
            return;
        }

        try (Statement statement = statsConn.createStatement()) {
            statement.executeUpdate("create table stats (ext text, date text, size integer)");
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to create initial stats database.", sqe);
        }
    }

}
