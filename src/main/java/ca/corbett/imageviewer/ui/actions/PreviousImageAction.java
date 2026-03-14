package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Selects the previous image in the main window.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class PreviousImageAction extends EnhancedAction {

    private static final String NAME = "Previous image";

    public PreviousImageAction() {
        this(AppConfig.getInstance().getToolbarIconSize());
    }

    public PreviousImageAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconPrevious(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().selectPreviousImage();
    }
}
