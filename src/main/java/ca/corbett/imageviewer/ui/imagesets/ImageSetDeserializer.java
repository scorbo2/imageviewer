package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.ui.MainWindow;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageSetDeserializer extends JsonDeserializer<ImageSet> {


    @Override
    public ImageSet deserialize(JsonParser parser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        String name = null;
        List<String> filePaths = new ArrayList<>();
        String nodePath = null;
        List<ImageSet> childNodes = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextToken(); // Move to the value

            switch (fieldName) {
                case "name":
                    if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
                        name = parser.getValueAsString();
                    }
                    break;

                case "fullPath":
                    if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
                        nodePath = parser.getValueAsString();
                    }
                    break;

                case "imageFiles":
                    if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
                                filePaths.add(parser.getValueAsString());
                            } // skip nulls
                        }
                    }
                    break;

                case "childNodes":
                    if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                        // Process each child and add via parent's add() method
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            if (parser.getCurrentToken() != JsonToken.VALUE_NULL) {
                                // Recursively deserialize child objects
                                ImageSet child = deserialize(parser, deserializationContext);
                                childNodes.add(child);
                            }
                        }
                    }
                    break;
            }
        }

        if (name == null || nodePath == null) {
            throw new IOException("Found no name or full path for ImageSet.");
        }

        // TODO clean up this terrible hack
        if (nodePath.startsWith("/Favorites")) {
            nodePath = nodePath.substring("/Favorites".length());
        }
        else if (nodePath.startsWith("Favorites")) {
            nodePath = nodePath.substring("Favorites".length());
        }

        ImageSet imageSet = MainWindow.getInstance().getImageSetPanel().findOrCreateFavoritesSet(nodePath).orElse(null);
        if (imageSet == null) {
            throw new IOException("Unable to restore image set with path: "+nodePath);
        }
        for (String filePath : filePaths) {
            imageSet.addImageFile(new File(filePath)); // TODO dead file handling goes here
        }
        //for (ImageSet child : childNodes) { // No! don't add them here as it is already done above by findOrCreate
        //    imageSet.add(child);
        //}

        return new ImageSet(name);
    }
}
