package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetSerializer;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FavoritesSaveAction extends AbstractAction {

    private static final Logger log = Logger.getLogger(FavoritesSaveAction.class.getName());

    public FavoritesSaveAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        // TODO move this to prefs
        File saveDir = new File("/home/scorbett/tmp/test.json");

        // TODO this is a lot of initialization for the ObjectMapper...
        //      can we centralize this somewhere so it can be reused elsewhere?
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        List<ImageSet> favorites = MainWindow.getInstance().getImageSetPanel().getFavorites();
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ImageSet.class, new ImageSetSerializer());
        mapper.registerModule(module);
        try {
            mapper.writer(prettyPrinter).writeValue(saveDir, favorites);
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "Error saving favorites tree: "+e.getMessage(), e);
        }
    }
}
