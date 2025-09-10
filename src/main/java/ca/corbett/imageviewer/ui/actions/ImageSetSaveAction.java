package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Loads all persisted image sets from the configured save location.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetSaveAction extends AbstractAction {

    public ImageSetSaveAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageSetManager.getInstance().save();
    }
}
