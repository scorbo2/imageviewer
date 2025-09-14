package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.ImageSetDeleteAction;
import ca.corbett.imageviewer.ui.actions.ImageSetEditAction;
import ca.corbett.imageviewer.ui.actions.ImageSetLoadAction;
import ca.corbett.imageviewer.ui.actions.ImageSetSaveAction;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Optional;

public class ImageSetPanel extends JPanel {

    private final ImageSetTree imageSetTree;

    public ImageSetPanel() {
        imageSetTree = new ImageSetTree();
        imageSetTree.getTree().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                handleTreeSelectionChanged(treeSelectionEvent);
            }
        });
        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(imageSetTree.getTree()), BorderLayout.CENTER);
    }

    public List<DefaultMutableTreeNode> getTopLevelNodes() {
        return imageSetTree.getTopLevelNodes();
    }

    public Optional<DefaultMutableTreeNode> getSelectedNode() {
        return imageSetTree.getSelectedNode();
    }

    public String getSelectedPath() {
        return imageSetTree.getSelectedPath();
    }

    public Optional<ImageSet> getSelectedImageSet() {
        return imageSetTree.getSelectedImageSet();
    }

    public String getPathForNode(DefaultMutableTreeNode node) {
        return imageSetTree.getPathForNode(node);
    }

    public void selectAndScrollTo(ImageSet set) {
        imageSetTree.selectAndScrollTo(set);
    }

    public void resync() {
        imageSetTree.resync();
    }

    public DefaultMutableTreeNode addImageSet(ImageSet set) {
        return imageSetTree.addImageSet(set);
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
        // TODO give this button an icon instead of a text label, make it pretty
        JButton button = new JButton(new ImageSetEditAction("Edit"));
        button.setPreferredSize(new Dimension(60, 23));
        toolbar.add(button);

        button = new JButton(new ImageSetSaveAction("Save"));
        button.setPreferredSize(new Dimension(60, 23));
        toolbar.add(button);

        button = new JButton(new ImageSetLoadAction("Load"));
        button.setPreferredSize(new Dimension(60, 23));
        toolbar.add(button);

        button = new JButton(new ImageSetDeleteAction("Delete"));
        button.setPreferredSize(new Dimension(60, 23));
        toolbar.add(button);

        return toolbar;
    }

    private void handleTreeSelectionChanged(TreeSelectionEvent event) {
        // Passing null is allowed here, it will clear the thumb panel:
        MainWindow.getInstance().setImageSet(getSelectedImageSet().orElse(null));
    }
}
