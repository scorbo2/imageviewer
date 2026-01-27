package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Opens the extension manager dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ManageExtensionsAction extends EnhancedAction {

    private static final String NAME = "Manage extensions...";

    public ManageExtensionsAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public ManageExtensionsAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconExtManager(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (AppConfig.getInstance().showExtensionDialog(MainWindow.getInstance(),
                                                        MainWindow.getInstance().getUpdateManager())) {
            ReloadUIAction.getInstance().actionPerformed(e);
        }
    }
}
