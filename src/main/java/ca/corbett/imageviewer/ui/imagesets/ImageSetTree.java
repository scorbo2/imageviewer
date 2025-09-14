package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ImageSetTree {

    private final JTree tree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;

    public ImageSetTree() {
        rootNode = new DefaultMutableTreeNode("Root", true);
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        TreeCellRenderer renderer = tree.getCellRenderer();
        ((DefaultTreeCellRenderer)renderer).setClosedIcon(null);
        ((DefaultTreeCellRenderer)renderer).setLeafIcon(null);
        ((DefaultTreeCellRenderer)renderer).setOpenIcon(null);
        resync();
    }

    public JTree getTree() {
        return tree;
    }

    public List<DefaultMutableTreeNode> getTopLevelNodes() {
        List<DefaultMutableTreeNode> list = new ArrayList<>();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            list.add((DefaultMutableTreeNode)rootNode.getChildAt(i));
        }
        return list;
    }

    public Optional<DefaultMutableTreeNode> getSelectedNode() {
        TreePath path = tree.getSelectionPath();
        if (path == null || path.getLastPathComponent() == null) {
            // nothing selected
            return Optional.empty();
        }

        // If whatever was selected is somehow not an ImageSet, we're done:
        DefaultMutableTreeNode selected = (DefaultMutableTreeNode)path.getLastPathComponent();
        if (!(selected.getUserObject() instanceof ImageSet)) {
            return Optional.empty();
        }

        return Optional.of(selected);
    }

    public String getSelectedPath() {
        DefaultMutableTreeNode selectedNode = getSelectedNode().orElse(null);
        if (selectedNode == null) {
            return null;
        }
        return getPathForNode(getSelectedNode().get());
    }

    public Optional<ImageSet> getSelectedImageSet() {
        DefaultMutableTreeNode selectedNode = getSelectedNode().orElse(null);
        if (selectedNode != null && (selectedNode.getUserObject() instanceof ImageSet)) {
            return Optional.of((ImageSet)selectedNode.getUserObject());
        }
        return Optional.empty();
    }

    public String getPathForNode(DefaultMutableTreeNode node) {
        if (node == null || node == rootNode) {
            return String.valueOf(ImageSetManager.PATH_DELIMITER);
        }

        List<String> pathNodes = new ArrayList<>();
        DefaultMutableTreeNode nextNode = node;
        while (nextNode != rootNode && nextNode != null) {
            pathNodes.add(0, nextNode.getUserObject().toString());
            nextNode = (DefaultMutableTreeNode)nextNode.getParent();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ImageSetManager.PATH_DELIMITER);
        while (!pathNodes.isEmpty()) {
            sb.append(pathNodes.get(0));
            pathNodes.remove(0);
            if (!pathNodes.isEmpty()) {
                sb.append(ImageSetManager.PATH_DELIMITER);
            }
        }
        return sb.toString();
    }

    public void selectAndScrollTo(ImageSet set) {
        String[] pathNodes = ImageSetManager.parsePathNodes(set.getFullyQualifiedName());
        if (pathNodes.length == 0) {
            return;
        }

        DefaultMutableTreeNode nodeToSelect = findChildNode(rootNode, pathNodes);
        if (nodeToSelect == null) {
            return;
        }

        TreePath path = new TreePath(nodeToSelect.getPath());
        tree.scrollPathToVisible(path);
        tree.setSelectionPath(path);
    }

    public void resync() {
        DefaultMutableTreeNode selectedNode = getSelectedNode().orElse(null);

        rootNode.removeAllChildren();
        List<ImageSet> imageSets = MainWindow.getInstance().getImageSetManager().getImageSets();
        for (ImageSet set : imageSets) {
            addImageSet(set);
        }
        treeModel.reload();

        // Re-select whatever was selected if there was a selection:
        if (selectedNode != null && (selectedNode.getUserObject() instanceof ImageSet)) {
            selectAndScrollTo((ImageSet)selectedNode.getUserObject());
        }
    }

    public DefaultMutableTreeNode addImageSet(ImageSet set) {
        String[] nodes = ImageSetManager.parsePathNodes(set.getFullyQualifiedName());
        if (nodes.length == 0) {
            return null;
        }

        return addImageSetInNode(rootNode, set, nodes);
    }

    private DefaultMutableTreeNode findChildNode(DefaultMutableTreeNode parentNode, String[] nodes) {
        DefaultMutableTreeNode nextNode = null;
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            DefaultMutableTreeNode candidate = (DefaultMutableTreeNode)parentNode.getChildAt(i);
            if (nodes[0].equalsIgnoreCase(candidate.getUserObject().toString())) {
                nextNode = candidate;
                break;
            }
        }

        if (nextNode == null) {
            return null;
        }

        if (nodes.length == 1) {
            return nextNode;
        }

        return findChildNode(nextNode, Arrays.stream(nodes).skip(1).toArray(String[]::new));
    }

    private DefaultMutableTreeNode addImageSetInNode(DefaultMutableTreeNode rootNode, ImageSet set, String[] nodes) {
        DefaultMutableTreeNode nextNode = null;
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode candidate = (DefaultMutableTreeNode)rootNode.getChildAt(i);
            String nodeName = candidate.getUserObject().toString();
            if (nodeName.equalsIgnoreCase(nodes[0])) {
                nextNode = candidate;
                break;
            }
        }
        if (nextNode == null) {
            nextNode = new DefaultMutableTreeNode(nodes[0]);
            rootNode.add(nextNode);
        }

        // If there was no sub-path, we're done:
        if (nodes.length == 1) {
            nextNode.setUserObject(set);
            return nextNode;
        }

        // Otherwise, recursively search that root node until we have the target:
        return addImageSetInNode(nextNode, set, Arrays.stream(nodes).skip(1).toArray(String[]::new));
    }
}
