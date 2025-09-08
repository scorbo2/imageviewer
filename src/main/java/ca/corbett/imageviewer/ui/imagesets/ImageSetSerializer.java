package ca.corbett.imageviewer.ui.imagesets;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.File;
import java.io.IOException;

public class ImageSetSerializer extends StdSerializer<ImageSet> {

    public ImageSetSerializer() {
        this(null);
    }

    public ImageSetSerializer(Class<ImageSet> t) {
        super(t);
    }

    @Override
    public void serialize(ImageSet imageSet, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartObject();

        if (imageSet.getName() != null) {
            jsonGenerator.writeStringField("name", imageSet.getName());
        } else {
            jsonGenerator.writeNullField("name");
        }

        if (imageSet.getPathString() != null) {
            jsonGenerator.writeStringField("fullPath", imageSet.getPathString());
        } else {
            jsonGenerator.writeNullField("fullPath");
        }

        // Serialize the items array
        jsonGenerator.writeArrayFieldStart("imageFiles");
        for (File item : imageSet.getImageFiles()) {
            if (item != null) {
                jsonGenerator.writeString(item.getAbsolutePath());
            } // Note: will skip null array entries, but there shouldn't be any anyways
        }
        jsonGenerator.writeEndArray();

        // Serialize the children array (recursive)
        jsonGenerator.writeArrayFieldStart("childNodes");
        if (imageSet.getChildCount() > 0) {
            for (int i = 0; i < imageSet.getChildCount(); i++) {
                ImageSet child = (ImageSet)imageSet.getChildAt(i);
                // This will recursively call this same serializer
                serialize(child, jsonGenerator, serializerProvider);
            }
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeEndObject();
    }
}
