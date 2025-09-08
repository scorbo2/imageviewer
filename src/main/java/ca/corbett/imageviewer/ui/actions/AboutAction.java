package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.about.AboutDialog;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

public class AboutAction extends AbstractAction {

    public AboutAction() {
        super("About...");
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        AboutDialog dialog = new AboutDialog(MainWindow.getInstance(), Version.aboutInfo);
        dialog.setSize(new Dimension(560, 640));
        dialog.setVisible(true);
    }
}
