package ca.corbett.imageviewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A special case of ImageOperation that can track which file(s) were affected
 * by the operation - this is used for "undo last action".
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
class LastImageOperation extends ImageOperation {

    private final File source;
    private final List<File> createdFiles;

    public LastImageOperation(Type op, Payload payload, File destination, File source) {
        super(op, payload, destination);
        this.source = source;
        this.createdFiles = new ArrayList<>();

        if (destination == null || source == null) {
            throw new RuntimeException("Attempted to create a null LastImageOperation.");
        }
    }

    public File getSource() {
        return source;
    }

    public void addCreatedFile(File f) {
        createdFiles.add(f);
    }

    public List<File> getCreatedFiles() {
        List<File> copy = new ArrayList<>();
        copy.addAll(createdFiles);
        return copy;
    }

}
