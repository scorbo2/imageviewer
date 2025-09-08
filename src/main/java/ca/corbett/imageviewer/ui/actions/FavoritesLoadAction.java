package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FavoritesLoadAction extends AbstractAction {

    private static Logger log = Logger.getLogger(FavoritesLoadAction.class.getName());

    public FavoritesLoadAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // TODO move this to prefs
        File saveDir = new File("/home/scorbett/tmp/test.json");

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ImageSet.class, new ImageSetDeserializer());
        mapper.registerModule(module);
        try {
            ImageSet[] favorites = mapper.readValue(saveDir, ImageSet[].class);
            // The deserializer adds the image sets for us... that's pretty tight coupling though TODO fix this
            //for (ImageSet set : favorites) {
            //    MainWindow.getInstance().getImageSetPanel().addToFavorites(set);
            //}

            // TODO how do I force a refresh on the JTree?
            //      if you switch tabs and come back, it looks good, but otherwise it doesn't show the loaded nodes...
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "Error saving favorites tree: "+e.getMessage(), e);
        }
    }
}
