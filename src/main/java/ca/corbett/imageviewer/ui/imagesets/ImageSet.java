package ca.corbett.imageviewer.ui.imagesets;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
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

    private boolean isLocked;

    @JsonIgnore
    private boolean isDirty;

    public ImageSet() {
        isTransient = false;
        isLocked = false;
    }

    public ImageSet(String fullyQualifiedName) {
        isTransient = false;
        isLocked = false;
        setFullyQualifiedName(fullyQualifiedName);
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
        isDirty = true;
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

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        this.isLocked = locked;
        isDirty = true;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public void imageMoved(String srcFilePath, String destFilePath) {
        for (int i = 0; i < imageFilePaths.size(); i++) {
            if (Objects.equals(imageFilePaths.get(i), srcFilePath)) {
                imageFilePaths.set(i, destFilePath);
                isDirty = true;
            }
        }
    }

    public void imageDeleted(String srcFilePath) {
        while (imageFilePaths.contains(srcFilePath)) {
            imageFilePaths.remove(srcFilePath);
            isDirty = true;
        }
    }

    public void directoryMoved(String srcDirPath, String destDirPath) {
        // Our paths must end with a separator.
        // Why? Consider moving /example/hello to /somewhere/else
        // We go through all paths looking for any that begin with /example/hello
        // But there could be other sibling dirs like /example/hello1, /example/hello2, etc
        // We DON'T want to update those.
        // So, we convert /example/hello to /example/hello/ so our startsWith() is more accurate.
        if (!srcDirPath.endsWith(File.separator)) {
            srcDirPath += File.separator; // trailing separator is important!
        }
        if (!destDirPath.endsWith(File.separator)) {
            destDirPath += File.separator; // trailing separator is important!
        }

        for (int i = 0; i < imageFilePaths.size(); i++) {
            if (imageFilePaths.get(i).startsWith(srcDirPath)) {
                imageFilePaths.set(i, imageFilePaths.get(i).replace(srcDirPath, destDirPath));
                isDirty = true;
            }
        }
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
        this.path = ImageSetManager.parseParent(newName);
        this.name = ImageSetManager.parseName(newName);
        this.isDirty = true;
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
