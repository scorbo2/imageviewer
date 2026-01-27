package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ToolBarManager;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
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
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * A Panel that can be used very similarly to the way DirTree is used to browse the file system,
 * except that this panel will browse the configured ImageSet(s) instead. Controls are provided
 * to edit, rename, move, or delete the selected ImageSet. Here, you can also reload or save
 * the current ImageSet list (changes are saved automatically on application exit, but you can
 * force a save here).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(ImageSetPanel.class.getName());

    private static final int ICON_SIZE = 18; // shoulld this use AppConfig's toolbar size? No, this toolbar should be smaller than the main toolbar

    private final ImageSetTree imageSetTree;
    private JToolBar toolBar;

    public ImageSetPanel() {
        imageSetTree = new ImageSetTree();
        imageSetTree.getTree().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                handleTreeSelectionChanged(treeSelectionEvent);
            }
        });
        setLayout(new BorderLayout());
        toolBar = buildToolbar();
        add(toolBar, BorderLayout.NORTH);
        add(new JScrollPane(imageSetTree.getTree()), BorderLayout.CENTER);
    }

    public void rebuildToolbar() {
        remove(toolBar);
        toolBar = buildToolbar();
        add(toolBar, BorderLayout.NORTH);
        revalidate();
        repaint();
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

    public void resync(ImageSet selectAndScrollTo) {
        imageSetTree.resync(selectAndScrollTo);
    }

    public DefaultMutableTreeNode addImageSet(ImageSet set) {
        return imageSetTree.addImageSet(set);
    }

    private JToolBar buildToolbar() {
        int toolbarSize = AppConfig.getInstance().getMiniToolbarIconSize()
                + AppConfig.getInstance().getMiniToolbarIconMargin();
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        JPanel wrapper = new JPanel();
        wrapper.setBackground(UIManager.getDefaults().getColor("Button.background"));
        wrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        wrapper.setPreferredSize(new Dimension(500, toolbarSize + 8));

        wrapper.add(ToolBarManager.buildMiniToolbarButton(new ImageSetEditAction()));
        wrapper.add(ToolBarManager.buildMiniToolbarButton(new ImageSetRenameAction()));
        wrapper.add(ToolBarManager.buildMiniToolbarButton(new ImageSetDeleteAction()));
        wrapper.add(new JLabel(" "));

        // Allow extensions to insert their own buttons here:
        List<EnhancedAction> extActions = ImageViewerExtensionManager.getInstance().getImageSetToolBarActions();
        if (!extActions.isEmpty()) {
            for (EnhancedAction action : extActions) {
                wrapper.add(ToolBarManager.buildMiniToolbarButton(action));
            }
            wrapper.add(new JLabel(" "));
        }

        wrapper.add(ToolBarManager.buildMiniToolbarButton(new ImageSetLoadAction(true)));

        wrapper.add(ToolBarManager.buildMiniToolbarButton(new ImageSetSaveAction()));

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
}
