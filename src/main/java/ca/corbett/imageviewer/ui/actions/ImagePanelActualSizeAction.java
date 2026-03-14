package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Sets the main image panel zoom to actual size.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImagePanelActualSizeAction extends EnhancedAction {

    private static final String NAME = "Actual size";

    public ImagePanelActualSizeAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public ImagePanelActualSizeAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconActualSize(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().zoomActualSize();
    }
}
