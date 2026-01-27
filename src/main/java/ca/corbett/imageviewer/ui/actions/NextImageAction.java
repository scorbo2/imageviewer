package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Moves to the next image in the current directory or image set.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class NextImageAction extends EnhancedAction {

    private static final String NAME = "Next image";

    public NextImageAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public NextImageAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconNext(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().selectNextImage();
    }
}
