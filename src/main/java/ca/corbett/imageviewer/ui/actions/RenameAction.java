package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.MenuManager;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.dialogs.RenameDialog;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Handles renaming of the currently selected image in MainWindow, if there is one.
 * Wired up with F2 as a keyboard shortcut by default, or you can find it in the main menu.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.0
 */
public class RenameAction extends EnhancedAction {

    private static final String NAME = "Rename...";

    public RenameAction() {
        this(MenuManager.MENU_ICON_SIZE);
    }

    public RenameAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconRename(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog(NAME, "Nothing selected.");
            return;
        }
        File file = currentImage.getImageFile();
        new RenameDialog(file).setVisible(true);
    }

}
