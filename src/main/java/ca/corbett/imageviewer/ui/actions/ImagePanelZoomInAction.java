package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Sets the main image panel zoom to zoom in one step.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImagePanelZoomInAction extends EnhancedAction {

    private static final String NAME = "Zoom in";

    public ImagePanelZoomInAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public ImagePanelZoomInAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconZoomIn(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().zoomIn();
    }
}
