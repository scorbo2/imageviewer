package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ReloadAction extends AbstractAction {

    public ReloadAction() {
        super("Reload current directory");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().reloadCurrentDirectory();
    }

}
