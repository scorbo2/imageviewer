package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
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


    /**
     * Finds or creates an ImageSet with the given name.
     * You can use PATH_DELIMITER to create a nested path, like "parent/child/grandchild".
     * Or use a non-delimited string to just create/search for a top level ImageSet.
     */
    public Optional<ImageSet> getFavoritesSet(String name) {
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
        return Optional.of(findImageSet(rootNode, Arrays.stream(nodes).skip(1).toArray(String[]::new)));
    }

    /**
     * Recursively searches the given rootNode using the given nodes array as a path element.
     * For example, given a root node named "root" and a nodes array of {"1","2","3"}, this
     * method will find or create a child node of "1" inside the given root node, a grandchild
     * node of "2" inside of "1", and a great-grandchild node of "3" inside of "2". The return
     * value is the last leaf node (in the example above, node "3" would be returned). All parent
     * nodes of the returned node will have been created if necessary.
     */
    private ImageSet findImageSet(ImageSet rootNode, String[] nodes) {
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
        return findImageSet(nextNode, Arrays.stream(nodes).skip(1).toArray(String[]::new));
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
        return toolbar;
    }

    private void handleTreeSelectionChanged(TreeSelectionEvent event) {
        if (event.getNewLeadSelectionPath() == null
                || event.getNewLeadSelectionPath().getLastPathComponent() == null) {
            // nothing selected
            return;
        }

        ImageSet selectedNode = (ImageSet)event.getNewLeadSelectionPath().getLastPathComponent();
        List<File> imageFiles = selectedNode.getImageFiles();
        if (!imageFiles.isEmpty()) {
            // TODO do something with this
            MainWindow.getInstance().setImageSet(selectedNode);
        }
    }
}
