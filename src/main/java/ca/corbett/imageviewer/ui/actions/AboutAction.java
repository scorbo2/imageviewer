package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.about.AboutDialog;
import ca.corbett.extras.about.AboutInfo;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class AboutAction extends AbstractAction {

    private final AboutInfo aboutInfo;

    public AboutAction() {
        super("About...");

        aboutInfo = new AboutInfo();
        aboutInfo.applicationName = Version.APPLICATION_NAME;
        aboutInfo.applicationVersion = Version.VERSION;
        aboutInfo.copyright = "Copyright © 2017 Steve Corbett";
        aboutInfo.shortDescription = "Fast and extensible image viewer and sorter.";
        aboutInfo.license = Version.LICENSE;
        aboutInfo.projectUrl = Version.PROJECT_URL;
        aboutInfo.logoImageLocation = "/ca/corbett/imageviewer/images/logo_wide.jpg";
        aboutInfo.releaseNotesLocation = "/ca/corbett/imageviewer/ReleaseNotes.txt";
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        new AboutDialog(MainWindow.getInstance(), aboutInfo).setVisible(true);
    }
}
