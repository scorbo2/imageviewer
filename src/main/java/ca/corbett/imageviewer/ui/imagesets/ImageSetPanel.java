package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.extras.image.ImageUtil;
import ca.corbett.imageviewer.ToolBarManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.ImageSetDeleteAction;
import ca.corbett.imageviewer.ui.actions.ImageSetEditAction;
import ca.corbett.imageviewer.ui.actions.ImageSetLoadAction;
import ca.corbett.imageviewer.ui.actions.ImageSetRenameAction;
import ca.corbett.imageviewer.ui.actions.ImageSetSaveAction;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageSetPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(ImageSetPanel.class.getName());

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

    private JToolBar buildToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JPanel wrapper = new JPanel();
        wrapper.setBackground(UIManager.getDefaults().getColor("Button.background"));
        wrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        wrapper.setPreferredSize(new Dimension(500, 28));

        try {
            wrapper.add(ToolBarManager.buildButton(
                    loadIconImage("icon-document-edit.png"),
                    "Edit image set",
                    new ImageSetEditAction("Edit image set"), 22));

            wrapper.add(ToolBarManager.buildButton(
                    loadIconImage("icon-document-upload.png"),
                    "Rename/move this image set",
                    new ImageSetRenameAction("Rename/move this image set"), 22));

            wrapper.add(ToolBarManager.buildButton(
                    loadIconImage("icon-x.png"),
                    "Delete this image set",
                    new ImageSetDeleteAction("Delete this image set"), 22));

            wrapper.add(new JLabel(" "));

            wrapper.add(ToolBarManager.buildButton(
                    loadIconImage("icon-reboot.png"),
                    "Discard changes and reload all image sets",
                    new ImageSetLoadAction("Discard changes and reload all image sets", true), 22));

            wrapper.add(ToolBarManager.buildButton(
                    loadIconImage("icon-save.png"),
                    "Save changes",
                    new ImageSetSaveAction("Save changes"), 22));
        }
        catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error loading icon image: " + ioe.getMessage(), ioe);
        }

        toolbar.add(wrapper);
        return toolbar;
    }

    private void handleTreeSelectionChanged(TreeSelectionEvent event) {
        if (MainWindow.getInstance().getBrowseMode() == MainWindow.BrowseMode.FILE_SYSTEM) {
            return; // don't need to do anything if we're not browsing image sets
        }

        // Passing null is allowed here, it will clear the thumb panel:
        MainWindow.getInstance().setImageSet(getSelectedImageSet().orElse(null));
    }

    /**
     * Invoked internally to load an icon image from resources, scale it if needed, and return it.
     */
    private static BufferedImage loadIconImage(String resourceName) throws IOException {
        final int iconSize = 18;
        return ImageUtil.loadFromResource(MainWindow.class,
                                          "/ca/corbett/imageviewer/images/" + resourceName,
                                          iconSize,
                                          iconSize);
    }
}
