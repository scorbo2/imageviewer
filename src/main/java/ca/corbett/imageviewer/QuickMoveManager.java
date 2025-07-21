package ca.corbett.imageviewer;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to a local sqlite database for persistent storage of the quick move
 * tree, which is too large and complex to store in AppPreferences.
 *
 * @author scorbo2
 * @since 2017-11-17
 */
public final class QuickMoveManager {

    private static final Logger logger = Logger.getLogger(QuickMoveManager.class.getName());

    private static QuickMoveManager instance;
    private static final String QM_FILENAME = "qm.sqlite";

    private Connection conn = null;
    private boolean dbAvailable;
    private File dbFile;
    private TreeNode rootNode;

    /**
     * Private constructor to enforce singleton access.
     */
    private QuickMoveManager() {
    }

    /**
     * Closes db connections if open.
     */
    public void close() {
        try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem closing db connections.", sqe);
        }
    }

    /**
     * Singleton accessor.
     *
     * @return The single instance of this class.
     */
    public static QuickMoveManager getInstance() {
        if (instance == null) {
            instance = new QuickMoveManager();
            instance.intialize();
        }

        return instance;
    }

    /**
     * Called internally to initialize the QuickMoveManager.
     */
    private void intialize() {
        dbAvailable = true;

        // Make sure our classpath contains what we need:
        try {
            Class.forName("org.sqlite.JDBC");
        }
        catch (ClassNotFoundException ignored) {
            logger.severe("SQLite not available");
            dbAvailable = false;
        }

        logger.info("Initializing QuickMove database");
        dbFile = new File(Version.SETTINGS_DIR, QM_FILENAME);
        boolean qmFilePresent = dbFile.exists();
        conn = getConnection(dbFile);

        // It's not an error condition if these dbs don't exist. Just create them if needed.
        if (!qmFilePresent) {
            logger.info("Creating new QM db");
            createQmDatabase();
        }
    }

    /**
     * Returns the root node of the quick move tree - this method will invoke loadQuickMoveTree()
     * if it has not yet been invoked, otherwise it will return the node that was previously
     * loaded.
     *
     * @return A QuickMoveManager.TreeNode instance representing the root of the tree.
     */
    public TreeNode getRootNode() {
        if (rootNode == null) {
            rootNode = loadQuickMoveTree();
        }
        return rootNode;
    }

    /**
     * Loads the Quick Move tree and returns it. Will return an empty node if the database
     * does not exist or if it is empty. This method forces a reload, even if a root node
     * was previously in memory - you may want to use getRootNode() instead unless you
     * want to force a reload each time.
     *
     * @return The root tree node.
     */
    public TreeNode loadQuickMoveTree() {
        rootNode = new TreeNode("Quick Move destinations");
        if (!dbAvailable) {
            return rootNode;
        }

        try {
            rootNode = loadTreeNode(1);
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to load Quick Move tree.", sqe);
        }

        return rootNode;
    }

    /**
     * Internal helper method to load a QuickMoveTree node by its parent id, and all of its
     * children recursively.
     *
     * @param id The id of the node to be loaded (id==1 for root).
     * @return A TreeNode fully populated with all children.
     */
    private TreeNode loadTreeNode(int id) throws SQLException {
        TreeNode node = null;
        String sql = "select label,dir from qm where id = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            String dirName = rs.getString("dir");
            File dir = null;
            if (!dirName.trim().equals("")) {
                dir = new File(dirName);
            }
            String label = rs.getString("label");
            node = new TreeNode(dir, label);
        }
        rs.close();
        statement.close();

        // Find all children:
        if (node != null) {
            sql = "select id from qm where parent = ?";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            rs = statement.executeQuery();
            while (rs.next()) {
                int childId = rs.getInt("id");
                TreeNode childNode = loadTreeNode(childId);
                if (childNode != null) {
                    node.add(childNode);
                }
            }
            rs.close();
            statement.close();
        }

        return node;
    }

    /**
     * Saves the given Quick Move destination tree. This will delete and then overwrite
     * any previous entries.
     *
     * @param rootNode The top of the tree. Can be null or empty.
     */
    public void saveQuickMoveTree(QuickMoveManager.TreeNode rootNode) {
        if (!dbAvailable) {
            return;
        }

        try {
            clearQuickMove();

            saveTreeNode(rootNode, 1, 0);
            this.rootNode = rootNode;
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to save Quick Move tree.", sqe);
        }
    }

    /**
     * Internal helper method to save the given TreeNode and its children recursively.
     *
     * @param node     The node to be saved.
     * @param nodeId   The database id to use for this node.
     * @param parentId The database id of the parent node (0 for root).
     * @return the next available database id.
     * @throws SQLException if something goes wrong.
     */
    private int saveTreeNode(TreeNode node, int nodeId, int parentId)
            throws SQLException {
        if (node == null) {
            return nodeId + 1;
        }

        String label = (node.getLabel() == null) ? "" : node.getLabel();
        String dir = (node.getDirectory() == null) ? "" : node.getDirectory().getAbsolutePath();

        // We can skip creation of the node if its id is 1:
        // (this is the artificial root node which is inserted by create/clear)
        if (nodeId != 1) {
            String sql = "insert into qm (id, label, dir, parent) values (?,?,?,?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, nodeId);
            statement.setString(2, label);
            statement.setString(3, dir);
            statement.setInt(4, parentId);
            statement.executeUpdate();
            statement.close();
        }

        // Save all children:
        int nextId = nodeId + 1;
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode childNode = (TreeNode)node.getChildAt(i);
            nextId = saveTreeNode(childNode, nextId, nodeId);
        }

        return nextId;
    }

    /**
     * Clears the Quick Move database. This will delete all existing destinations.
     */
    public void clearQuickMove() {
        if (!dbAvailable) {
            return;
        }

        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("delete from qm");
            statement.close();

            String sql = "insert into qm (id, label, dir, parent) values (?,?,?,?)";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, 1);
            st.setString(2, "Quick Move destinations");
            st.setString(3, "");
            st.setInt(4, 0);
            st.executeUpdate();
            st.close();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to insert Quick Move root node.", sqe);
        }
    }

    /**
     * Invoked internally to create the Quick Move database if it does not yet exist.
     */
    private void createQmDatabase() {
        if (!dbAvailable) {
            return;
        }

        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate("create table qm (id integer, label text, dir text, parent integer)");
            statement.close();

            String sql = "insert into qm (id, label, dir, parent) values (?,?,?,?)";
            PreparedStatement st = conn.prepareStatement(sql);
            st.setInt(1, 1);
            st.setString(2, "Quick Move destinations");
            st.setString(3, "");
            st.setInt(4, 0);
            st.executeUpdate();
            st.close();
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Unable to create initial Quick Move database.", sqe);
        }
    }

    /**
     * Invoked internally to open a database and get a connection to it.
     *
     * @param file A File object representing the sqlite file on disk.
     * @return a Connection object which you can use to query this database.
     */
    private Connection getConnection(File file) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        }
        catch (SQLException sqe) {
            logger.log(Level.SEVERE, "Problem getting connection to " + file.getName(), sqe);
        }

        return conn;
    }

    /**
     * A simple container class for storing a single Quick Move destination into a JTree.
     * Each node contains a File object representing the directory it represents, and a String
     * label. If the file object is null, the node represents a container rather than a destination.
     *
     * @author scorbett
     * @since 2017-11-15
     */
    public class TreeNode extends DefaultMutableTreeNode {

        private File directory;
        private String label;

        /**
         * Creates a new node with no directory, and with the specified string label.
         * The node is assumed to be a "category" node (i.e. just a container for other
         * nodes, not a destination).
         *
         * @param txt The label to apply to this node.
         */
        public TreeNode(String txt) {
            super(txt);
            this.label = txt;
            this.directory = null;
        }

        /**
         * Creates a new node with the specified directory and label. Either can be null.
         *
         * @param dir The directory which this node represents.
         * @param txt The string label to apply to this node.
         */
        public TreeNode(File dir, String txt) {
            super(txt);
            this.directory = dir;
            this.label = txt;
        }

        /**
         * Returns the directory associated with this node, or null if this is just
         * a category node.
         *
         * @return A java.io.File object, or null.
         */
        public File getDirectory() {
            return directory;
        }

        /**
         * Sets the directory associated with this node. Null is allowed if this is
         * a category node.
         *
         * @param newDir The directory to associate with this node.
         */
        public void setDirectory(File newDir) {
            directory = newDir;
        }

        /**
         * Sets the text label for this node. If null, the full pathname of the directory is used.
         *
         * @param newLabel The new label for this node.
         */
        public void setLabel(String newLabel) {
            this.label = newLabel;
        }

        /**
         * Returns a String label for this node - either the one that was explicitly set, or
         * the full pathname of the associated directory.
         *
         * @return A string label for this node.
         */
        public String getLabel() {
            String txt = label;
            if (txt == null || txt.equals("")) {
                if (directory != null) {
                    txt = directory.getAbsolutePath();
                }
            }

            if (txt == null || txt.equals("")) {
                txt = "(unknown)";
            }

            return txt;
        }

        /**
         * Equivalent to getLabel().
         *
         * @return getLabel()
         */
        @Override
        public String toString() {
            return getLabel();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Objects.hashCode(this.directory);
            hash = 71 * hash + Objects.hashCode(this.label);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TreeNode other = (TreeNode)obj;
            if (!Objects.equals(this.label, other.label)) {
                return false;
            }
            return Objects.equals(this.directory, other.directory);
        }

    }

}
