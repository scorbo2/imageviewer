package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads all persisted ImageSets from the configured save location.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetLoadAction extends AbstractAction {

    private static Logger log = Logger.getLogger(ImageSetLoadAction.class.getName());

    public ImageSetLoadAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // TODO move this to prefs
        File saveDir = new File("/home/scorbett/tmp/test.json");

        // TODO nuke the current contents of the tree?
        //      what if they're dirty?
        //      should there be a prompt here to confirm discarding current tree?

        ObjectMapper mapper = new ObjectMapper();
        try {
            ImageSet[] favorites = mapper.readValue(saveDir, ImageSet[].class);
            for (ImageSet set : favorites) {
                ImageSetManager.getInstance().addImageSet(set);
            }

            MainWindow.getInstance().getImageSetPanel().resync();
            // TODO reload menus... but DON'T do a full ReloadUIAction.getInstance().actionPerformed(actionEvent);
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "Error saving image sets: " + e.getMessage(), e);
        }
    }
}
