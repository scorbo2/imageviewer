package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Reloads the current directory/image set in the main window.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ReloadAction extends EnhancedAction {

    private static final String NAME = "Reload";

    public ReloadAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public ReloadAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconReload(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().reload();
    }
}
