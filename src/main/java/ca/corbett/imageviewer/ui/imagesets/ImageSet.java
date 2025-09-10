package ca.corbett.imageviewer.ui.imagesets;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Represents a list which can contain either other ImageSets or individual image files.
 * To be used with ImageSetPanel.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSet {

    private static final Logger log = Logger.getLogger(ImageSet.class.getName());

    private final List<String> imageFilePaths = new ArrayList<>();
    private String fullyQualifiedName;

    @JsonIgnore
    private String name;

    @JsonIgnore
    private String path;

    private boolean isTransient;

    @JsonIgnore
    private boolean isDirty;

    public ImageSet() {
        isTransient = false;
    }

    public List<String> getImageFilePaths() {
        return new ArrayList<>(imageFilePaths);
    }

    public void clearImages() {
        imageFilePaths.clear();
        isDirty = true;
    }

    public void setImageFilePaths(List<String> imageFilePaths) {
        for (String imageFile : imageFilePaths) {
            addImageFilePath(imageFile);
        }
    }

    public void removeImageFilePath(String pathToRemove) {
        imageFilePaths.remove(pathToRemove);
    }

    public boolean addImageFilePath(String imageFilePath) {
        if (imageFilePath == null) {
            log.warning("Ignoring attempt to add null image to image set.");
            return false;
        }
        if (imageFilePaths.contains(imageFilePath)) {
            log.warning("Ignoring attempt to add duplicate image to image set: " + imageFilePath);
            return false;
        }
        imageFilePaths.add(imageFilePath);
        isDirty = true;
        return true;
    }

    /**
     * Returns just the name of this ImageSet, without regard to its path within the tree.
     * See also getPath() and getFullyQualifiedName().
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the path of this ImageSet, which is the fully qualified name minus our name.
     */
    public String getPath() {
        return path;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
        isDirty = true;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    /**
     * Accepts the fully qualified path and name of this ImageSet.
     * As a convenience, will parse out the name (last delimited item in
     * the path string) and the path (all items before the last one).
     * For example, setFullyQualifiedName("a/b/c") will set the fully
     * qualified name to "/a/b/c", the name to "c", and the path to "/a/b/".
     * Note that paths always start with the delimiter (though it's not
     * a requirement to pass it in that way here - "a/b/c" and "/a/b/c"
     * will yield the same results).
     */
    public void setFullyQualifiedName(String newName) {
        this.fullyQualifiedName = ImageSetManager.parseFullyQualifiedName(newName);
        this.path = ImageSetManager.parsePath(newName);
        this.name = ImageSetManager.parseName(newName);
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImageSet imageSet)) { return false; }
        return Objects.equals(getFullyQualifiedName(), imageSet.getFullyQualifiedName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFullyQualifiedName());
    }

    @Override
    public String toString() {
        return name; // Not fully qualified name! This will appear in the JTree as the node name.
    }
}
