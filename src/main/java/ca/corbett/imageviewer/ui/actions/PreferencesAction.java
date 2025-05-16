package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class PreferencesAction extends AbstractAction {

    public PreferencesAction() {
        super("Application preferences...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (AppConfig.getInstance().showPropertiesDialog(MainWindow.getInstance())) {
            MainWindow.getInstance().reloadUI(); // TODO this should be an action and not in MainWindow
        }
    }

}
