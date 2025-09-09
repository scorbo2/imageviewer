package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.ImageSetEditAction;
import ca.corbett.imageviewer.ui.actions.ImageSetLoadAction;
import ca.corbett.imageviewer.ui.actions.ImageSetSaveAction;

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
import javax.swing.tree.TreeNode;
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
        rootNode = new DefaultMutableTreeNode("Root", true);
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

    public Optional<ImageSet> getSelectedImageSet() {
        DefaultMutableTreeNode selectedNode = getSelectedNode().orElse(null);
        if (selectedNode != null && (selectedNode.getUserObject() instanceof ImageSet)) {
            return Optional.of((ImageSet)selectedNode.getUserObject());
        }
        return Optional.empty();
    }

    public void resync() {
        DefaultMutableTreeNode selectedNode = getSelectedNode().orElse(null);

        rootNode.removeAllChildren();
        List<ImageSet> imageSets = ImageSetManager.getInstance().getImageSets();
        for (ImageSet set : imageSets) {
            addImageSet(set);
        }
        treeModel.reload();

        // Re-select whatever was selected if there was a selection:
        if (selectedNode != null) {
            TreePath selectionPath = new TreePath(selectedNode.getPath()); // TODO this path is no longer valid
            tree.scrollPathToVisible(selectionPath); // TODO so this code will fail
            tree.setSelectionPath(selectionPath); // TODO we need therefore a findNodeByImageSet or selectByPath
        }
    }

    public DefaultMutableTreeNode addImageSet(ImageSet set) {
        String[] nodes = ImageSetManager.parsePathNodes(set.getFullyQualifiedName());
        if (nodes.length == 0) {
            return null;
        }

        return addImageSetInNode(rootNode, set, nodes);
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

    /**
     * Wonky swing nonsense - adding/removing nodes doesn't update the tree until you tell it explicitly
     * about the changes.
     */
    public void refresh() {
        treeModel.nodeStructureChanged((TreeNode)treeModel.getRoot());
        // TODO can't we just do treeModel.reload() here? is that the same thing?
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        // TODO give this button an icon instead of a text label, make it pretty
        JButton button = new JButton(new ImageSetEditAction("Edit"));
        button.setPreferredSize(new Dimension(90, 23));
        toolbar.add(button);

        button = new JButton(new ImageSetSaveAction("Save"));
        button.setPreferredSize(new Dimension(90, 23));
        toolbar.add(button);

        button = new JButton(new ImageSetLoadAction("Load"));
        button.setPreferredSize(new Dimension(90, 23));
        toolbar.add(button);

        return toolbar;
    }

    private void handleTreeSelectionChanged(TreeSelectionEvent event) {
        // Passing null is allowed here, it will clear the thumb panel:
        MainWindow.getInstance().setImageSet(getSelectedImageSet().orElse(null));
    }
}
