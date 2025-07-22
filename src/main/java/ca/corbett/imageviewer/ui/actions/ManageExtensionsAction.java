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
            ReloadUIAction.getInstance().actionPerformed(e);
        }
    }
}
