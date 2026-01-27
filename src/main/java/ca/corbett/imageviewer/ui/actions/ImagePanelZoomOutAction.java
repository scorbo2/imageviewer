package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Sets the main image panel zoom to zoom out one step.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImagePanelZoomOutAction extends EnhancedAction {

    private static final String NAME = "Zoom out";

    public ImagePanelZoomOutAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public ImagePanelZoomOutAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconZoomOut(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().zoomOut();
    }
}
