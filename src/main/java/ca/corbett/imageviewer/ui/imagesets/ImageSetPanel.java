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
import java.util.List;

public class ImageSetPanel extends JPanel {

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
