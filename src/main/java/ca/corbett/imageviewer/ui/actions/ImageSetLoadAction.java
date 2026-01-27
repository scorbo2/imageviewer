package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageViewerResources;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

/**
 * Loads all persisted ImageSets from the configured save location.
 * An optional confirmation dialog can be enabled to warn the user that
 * any unsaved changes made since the last load or save will be discarded.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetLoadAction extends EnhancedAction {

    private static final String NAME = "Discard changes and reload all image sets";

    private final boolean isWarningEnabled;

    public ImageSetLoadAction(boolean enableWarning) {
        this(enableWarning, AppConfig.getInstance().getMiniToolbarIconSize());
    }

    public ImageSetLoadAction(boolean enableWarning, int iconSize) {
        super(NAME);
        setTooltip(NAME);
        setIcon(new ImageIcon(ImageViewerResources.getIconImageSetReload(iconSize)));
        this.isWarningEnabled = enableWarning;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (isWarningEnabled) {
            int result = JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                                       "Really discard all changes and reload image sets?",
                                                       "Confirm reload", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.NO_OPTION) {
                return;
            }
        }

        MainWindow.getInstance().getImageSetManager().load();
        MainWindow.getInstance().getImageSetPanel().resync();
    }
}
