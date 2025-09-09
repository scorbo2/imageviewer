package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persists all ImageSets to the configured save location.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetSaveAction extends AbstractAction {

    private static final Logger log = Logger.getLogger(ImageSetSaveAction.class.getName());

    public ImageSetSaveAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // TODO move this to prefs
        File saveDir = new File("/home/scorbett/tmp/test.json");

        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        List<ImageSet> favorites = ImageSetManager.getInstance().getImageSets();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writer(prettyPrinter).writeValue(saveDir, favorites);
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "Error saving image sets: " + e.getMessage(), e);
        }
    }
}
