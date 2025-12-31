package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.MessageUtil;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.logging.Logger;

/**
 * Launches the directory information dialog for the currently selected directory, assuming
 * we are in file system browse mode.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.4
 */
public class DirectoryInfoAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(DirectoryInfoAction.class.getName());
    private MessageUtil messageUtil;

    public DirectoryInfoAction() {
        super("Directory information");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (MainWindow.getInstance().getBrowseMode() != MainWindow.BrowseMode.FILE_SYSTEM) {
            getMessageUtil().info("Directory information",
                                  "Directory information is only available in file system browse mode.");
            return;
        }

        File selectedDir = MainWindow.getInstance().getCurrentDirectory();
        if (selectedDir == null || ! selectedDir.isDirectory()) {
            getMessageUtil().info("Directory information", "No directory selected.");
            return;
        }

        new DirectoryInfoDialog(MainWindow.getInstance(), selectedDir).setVisible(true);
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), logger);
        }
        return messageUtil;
    }
}
