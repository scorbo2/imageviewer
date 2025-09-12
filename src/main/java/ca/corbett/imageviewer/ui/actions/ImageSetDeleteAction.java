package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;

import javax.swing.AbstractAction;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * Represents an action to delete the currently selected ImageSet, if any.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImageSetDeleteAction extends AbstractAction {

    public ImageSetDeleteAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Optional<DefaultMutableTreeNode> selectedNode = MainWindow.getInstance().getImageSetPanel().getSelectedNode();
        if (selectedNode.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Delete image set", "Nothing selected.");
            return;
        }

        // Is this branch locked?
        ImageSetManager imageSetManager = MainWindow.getInstance().getImageSetManager();
        String nodePath = MainWindow.getInstance().getImageSetPanel().getPathForNode(selectedNode.get());
        if (MainWindow.getInstance().getImageSetPanel().isBranchLocked(selectedNode.get()) ||
            imageSetManager.isBranchLocked(nodePath)) {
            MainWindow.getInstance().showMessageDialog("Delete image set",
                                                       "One ore more image sets in this branch of "
                                                       + "the tree are locked and cannot be deleted.");
            return;
        }

        // Nuke it:
        imageSetManager.remove(nodePath);

        // Reload the tree
        MainWindow.getInstance().getImageSetPanel().resync();
        MainWindow.getInstance().rebuildMenus();
        MainWindow.getInstance().setImageSet(null);
    }
}
