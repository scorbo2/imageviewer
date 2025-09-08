package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.FavoritesEditListAction;
import ca.corbett.imageviewer.ui.actions.FavoritesLoadAction;
import ca.corbett.imageviewer.ui.actions.FavoritesSaveAction;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ImageSetPanel extends JPanel {

    public static char PATH_DELIMITER = '/';

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    public ImageSetPanel() {
        rootNode = new DefaultMutableTreeNode("Image sets", true);
        List<ImageSet> nodes = ImageSet.getRootNodes();
        nodes.addAll(ImageViewerExtensionManager.getInstance().getImageSetRootNodes());
        for (ImageSet node : nodes) {
            rootNode.add(node);
        }
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                handleTreeSelectionChanged(treeSelectionEvent);
            }
        });
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        TreeCellRenderer renderer = tree.getCellRenderer();
        ((DefaultTreeCellRenderer)renderer).setClosedIcon(null);
        ((DefaultTreeCellRenderer)renderer).setLeafIcon(null);
        ((DefaultTreeCellRenderer)renderer).setOpenIcon(null);
        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    /**
     * Returns the selected ImageSet, or null if nothing is selected.
     */
    public ImageSet getSelectedImageSet() {
        TreePath path = tree.getSelectionPath();
        if (path == null || path.getLastPathComponent() == null) {
            // nothing selected
            return null;
        }

        // If whatever was selected is somehow not an ImageSet, we're done:
        if (!(path.getLastPathComponent() instanceof ImageSet)) {
            return null;
        }

        return (ImageSet)path.getLastPathComponent();
    }

    /**
     * Returns the top-level favorites nodes, which may have child nodes and grandchild nodes and so on.
     */
    public List<ImageSet> getFavorites() {
        List<ImageSet> topLevelNodes = new ArrayList<>();
        for (int i = 0; i < getFavoritesRoot().getChildCount(); i++) {
            topLevelNodes.add((ImageSet)getFavoritesRoot().getChildAt(i));
        }
        return topLevelNodes;
    }

    public void addToFavorites(ImageSet imageSet) {
        addToRootNode("Favorites", imageSet);
    }

    public void addToRootNode(String rootNodeName, ImageSet imageSet) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ImageSet node = (ImageSet)rootNode.getChildAt(i);
            if (node.getName().equals(rootNodeName)) {
                node.add(imageSet);
                return;
            }
        }
    }

    public ImageSet getFavoritesRoot() {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ImageSet node = (ImageSet)rootNode.getChildAt(i);
            if (node.getName().equals("Favorites")) {
                return node;
            }
        }
        return null;
    }

    public boolean favoriteSetExists(String path) {
        String[] nodes = parsePathNodes(path);
        if (nodes.length == 0) {
            return false;
        }

        // Find the first path node:
        ImageSet firstPathNode = null;
        for (ImageSet candidate : getFavorites()) {
            if (candidate.getName().equalsIgnoreCase(nodes[0])) {
                firstPathNode = candidate;
                break;
            }
        }
        if (firstPathNode == null) {
            return false; // not found
        }

        // If there was no sub-path, we're done:
        if (nodes.length == 1) {
            return true;
        }

        // Otherwise, recursively search that path node until we have the target:
        return pathExistsInNode(firstPathNode, Arrays.stream(nodes).skip(1).toArray(String[]::new));
    }

    /**
     * Finds or creates an ImageSet with the given name.
     * You can use PATH_DELIMITER to create a nested path, like "parent/child/grandchild".
     * Or use a non-delimited string to just create/search for a top level ImageSet.
     */
    public Optional<ImageSet> findOrCreateFavoritesSet(String name) {
        String[] nodes = parsePathNodes(name);
        if (nodes.length == 0) {
            return Optional.empty();
        }

        // Find or create the root node:
        ImageSet rootNode = null;
        for (ImageSet candidate : getFavorites()) {
            if (candidate.getName().equalsIgnoreCase(nodes[0])) {
                rootNode = candidate;
                break;
            }
        }
        if (rootNode == null) {
            rootNode = new ImageSet(nodes[0]);
            getFavoritesRoot().add(rootNode);
        }

        // If there was no sub-path, we're done:
        if (nodes.length == 1) {
            return Optional.of(rootNode);
        }

        // Otherwise, recursively search that root node until we have the target:
        return Optional.of(findOrCreateImageSetInNode(rootNode, Arrays.stream(nodes).skip(1).toArray(String[]::new)));
    }

    private boolean pathExistsInNode(ImageSet rootNode, String[] path) {
        ImageSet nextNode = null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ImageSet candidate = (ImageSet)rootNode.getChildAt(i);
            if (candidate.getName().equalsIgnoreCase(path[0])) {
                nextNode = candidate;
            }
        }
        if (nextNode == null) {
            return false;
        }

        // If there are no more path elements, this is the final leaf node:
        if (path.length == 1) {
            return true;
        }

        // Otherwise, recurse further:
        return pathExistsInNode(nextNode, Arrays.stream(path).skip(1).toArray(String[]::new));

    }

    /**
     * Recursively searches the given rootNode using the given nodes array as a path element.
     * For example, given a root node named "root" and a nodes array of {"1","2","3"}, this
     * method will find or create a child node of "1" inside the given root node, a grandchild
     * node of "2" inside of "1", and a great-grandchild node of "3" inside of "2". The return
     * value is the last leaf node (in the example above, node "3" would be returned). All parent
     * nodes of the returned node will have been created if necessary.
     */
    private ImageSet findOrCreateImageSetInNode(ImageSet rootNode, String[] nodes) {
        ImageSet nextNode = null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ImageSet candidate = (ImageSet)rootNode.getChildAt(i);
            if (candidate.getName().equalsIgnoreCase(nodes[0])) {
                nextNode = candidate;
            }
        }
        if (nextNode == null) {
            nextNode = new ImageSet(nodes[0]);
            rootNode.add(nextNode);
        }

        // If there are no more path elements, this is the final leaf node:
        if (nodes.length == 1) {
            return nextNode;
        }

        // Otherwise, recurse further:
        return findOrCreateImageSetInNode(nextNode, Arrays.stream(nodes).skip(1).toArray(String[]::new));
    }

    /**
     * Given a delimited String, will return a String array containing all the non-blank elements
     * in the input. Leading and trailing delimiters are ignored, as are repeated delimiters.
     * So: "hello/////there", "hello//there", "/hello/there/" and "hello/there" all return
     * an array of length 2 with the elements "hello" and "there". If the input String is
     * empty or null, you get an empty array.
     */
    protected String[] parsePathNodes(String input) {
        if (input == null || input.isBlank()) {
            return new String[0];
        }

        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == PATH_DELIMITER) {
                // If we have accumulated characters, add them as a part
                if (!current.isEmpty()) {
                    String candidate = current.toString().trim();
                    if (!candidate.isBlank()) {
                        parts.add(candidate);
                    }
                    current.setLength(0); // Clear the StringBuilder
                }
                // Otherwise, ignore the delimiter (handles consecutive delimiters)
            }
            else {
                current.append(c);
            }
        }

        // Add the last part if it's not empty
        if (!current.isEmpty()) {
            String candidate = current.toString().trim();
            if (!candidate.isBlank()) {
                parts.add(candidate);
            }
        }

        return parts.toArray(new String[0]);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        // TODO give this button an icon instead of a text label, make it pretty
        JButton button = new JButton(new FavoritesEditListAction("Edit"));
        button.setPreferredSize(new Dimension(90, 23));
        toolbar.add(button);

        button = new JButton(new FavoritesSaveAction("Save"));
        button.setPreferredSize(new Dimension(90, 23));
        toolbar.add(button);

        button = new JButton(new FavoritesLoadAction("Load"));
        button.setPreferredSize(new Dimension(90, 23));
        toolbar.add(button);

        return toolbar;
    }

    private void handleTreeSelectionChanged(TreeSelectionEvent event) {
        MainWindow.getInstance().setImageSet(getSelectedImageSet()); // if null / nothing selected, this will clear it
    }
}
