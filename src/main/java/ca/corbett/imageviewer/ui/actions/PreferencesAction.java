package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Shows the application preferences dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class PreferencesAction extends EnhancedAction {

    private static final String NAME = "Application preferences...";

    public PreferencesAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public PreferencesAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconSettings(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (AppConfig.getInstance().showPropertiesDialog(MainWindow.getInstance())) {
            ReloadUIAction.getInstance().actionPerformed(e);
        }
    }
}
