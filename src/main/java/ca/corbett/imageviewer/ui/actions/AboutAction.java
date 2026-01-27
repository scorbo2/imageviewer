package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.about.AboutDialog;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

/**
 * Shows an About dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class AboutAction extends EnhancedAction {

    private static final String NAME = "About...";

    public AboutAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public AboutAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconAbout(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        AboutDialog dialog = new AboutDialog(MainWindow.getInstance(), Version.aboutInfo);
        dialog.setSize(new Dimension(560, 640));
        dialog.setVisible(true);
    }
}
