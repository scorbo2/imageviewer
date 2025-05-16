package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * @author scorbett
 */
public class ManageExtensionsAction extends AbstractAction {

    public ManageExtensionsAction() {
        super("Manage extensions...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (AppConfig.getInstance().showExtensionDialog(MainWindow.getInstance())) {
            // TODO Reload UI
        }
//        ExtensionManagerDialog dialog = new ExtensionManagerDialog(ImageViewerExtensionManager.getInstance(),
//                                                                   MainWindow.getInstance(),
//                                                                   "Manage " + Version.APPLICATION_NAME + " extensions");
//        dialog.setSize(new Dimension(725,
//                                     495)); // the text fields are very slightly taller here than in the util demo app???? freaking weird, but need to increase the dialog height a bit.
//        dialog.setVisible(true);
//
//        // If it was okayed and changes were made, we need to reload the whole UI...
//        if (dialog.wasOkayed() && dialog.wasModified()) {
//            ImageViewerExtensionManager.getInstance().saveEnabledStatus();
//
//            MainWindow.getInstance().showMessageDialog("Reload UI",
//                                                       "One or more extensions have been enabled or disabled.\nThe UI will now reload to reflect this change.");
//            AppPreferences.getInstance()
//                          .load(); // force a reload to exclude any now-disabled extensions and pick up any now-enabled ones
//            MainWindow.getInstance().reloadUI();
//        }
    }

}
