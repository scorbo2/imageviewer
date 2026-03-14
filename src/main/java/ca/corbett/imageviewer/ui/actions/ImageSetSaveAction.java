package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;

/**
 * Saves all image sets to the configured save location.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetSaveAction extends EnhancedAction {

    private static final String NAME = "Save changes";

    public ImageSetSaveAction() {
        this(AppConfig.getInstance().getMiniToolbarIconSize());
    }

    public ImageSetSaveAction(int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconSave(iconSize)));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        MainWindow.getInstance().getImageSetManager().save();
    }
}
