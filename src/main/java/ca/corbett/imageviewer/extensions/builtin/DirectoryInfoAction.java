package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.ui.MainWindow;

import java.awt.event.ActionEvent;
import java.io.File;

/**
 * Launches the directory information dialog for the currently selected directory, assuming
 * we are in file system browse mode.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.4
 */
public class DirectoryInfoAction extends EnhancedAction {

    public DirectoryInfoAction() {
        super("Directory information");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (MainWindow.getInstance().getBrowseMode() != MainWindow.BrowseMode.FILE_SYSTEM) {
            MainWindow.getInstance().showMessageDialog("Directory information",
                                  "Directory information is only available in file system browse mode.");
            return;
        }

        File selectedDir = MainWindow.getInstance().getCurrentDirectory();
        if (selectedDir == null || ! selectedDir.isDirectory()) {
            MainWindow.getInstance().showMessageDialog("Directory information", "No directory selected.");
            return;
        }

        new DirectoryInfoDialog(MainWindow.getInstance(), selectedDir).setVisible(true);
    }
}
