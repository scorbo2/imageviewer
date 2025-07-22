package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.dialogs.RenameDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Handles renaming of the currently selected image in MainWindow, if there is one.
 * Wired up with F2 as a keyboard shortcut, or you can find it in the main menu.
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public class RenameAction extends AbstractAction {

    public RenameAction() {
        super("Rename...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Rename", "Nothing selected.");
            return;
        }
        File file = currentImage.getImageFile();
        new RenameDialog(file).setVisible(true);
    }

}
