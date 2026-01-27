package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.UIReloadable;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Sets the main image panel zoom to best fit.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImagePanelBestFitAction extends EnhancedAction implements UIReloadable {

    private static final String NAME = "Best fit";

    public ImagePanelBestFitAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public ImagePanelBestFitAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconBestFit(iconSize)));
        ReloadUIAction.getInstance().registerReloadable(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().zoomBestFit();
    }

    @Override
    public void reloadUI() {
        if (getIcon() == null) {
            return;
        }

        // Our icon size may have changed:
        setIcon(new ImageIcon(ImageViewerResources.getIconBestFit(AppConfig.getInstance().getToolbarIconSize())));
    }
}
