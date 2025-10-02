package ca.corbett.imageviewer.ui.imagesets;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * An <b>ImageSet</b> is a list of images whose locations are arbitrary. This is as opposed
 * to browsing a directory of images, where all contents reside in the same file system
 * location. ImageSets can be constructed on the fly and can represent any number of
 * images. On the MainWindow, the user can choose to browse based on the file system,
 * in which case the DirTree is used to select a directory of images. But, the user
 * can also opt to browse by ImageSet, in which case an ImageSetPanel is used in
 * conjunction with ImageSetManager to view or edit a tree of ImageSets.
 * <p>
 *     Each ImageSet has a fullyQualifiedName, which can be used to optionally
 *     describe a tree structure. For example, "/Folder1/Folder2/My Image Set"
 *     describes an ImageSet named "My Image Set" inside a nested folder-like structure.
 * </p>
 * <p>
 *     ImageSets can be <b>transient</b>. If so, that ImageSet will not be persisted
 *     when the application exits. This is intended to be used for search results or such.
 *     By default, ImageSets are <b>not</b> transient, meaning they are persisted, and
 *     will be loaded the next time the application starts.
 * </p>
 * <p>
 *     ImageSets can be <b>locked</b>. If so, that ImageSet cannot be deleted, moved,
 *     or renamed by the user. This is intended for extension-specific ImageSets.
 *     By default, ImageSets are <b>not</b> locked, meaning that they can be deleted
 *     or modified at any time by the user.
 * </p>
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

    public int size() {
        return imageFilePaths.size();
    }

    /**
     * If no Comparator is given, then by default we sort by full path + filename.
     */
    public void sort() {
        imageFilePaths.sort(null);
    }

    /**
     * Sorts the items in this set using the given Comparator, which can compare
     * any attribute(s) of the files in the set. If null is provided, then the
     * set is sorted by full path + name and this call is equivalent to sort().
     */
    public void sort(Comparator<File> comparator) {
        if (comparator == null) {
            sort();
            return;
        }

        // If there's nothing to sort, don't bother:
        if (imageFilePaths.size() <= 1) {
            return;
        }

        // This is awkward, but internally we don't store File objects, but rather
        // Strings, to cut down on overhead and to make persistence much easier.
        // But, when given a File Comparator, we have to convert our list to
        // a List of Files in order to sort it.
        List<File> fileList = new ArrayList<>(imageFilePaths.size());
        for (String path : imageFilePaths) {
            fileList.add(new File(path));
        }
        fileList.sort(comparator);

        // And now we convert it back to a list of absolute paths:
        imageFilePaths.clear();
        for (File f : fileList) {
            imageFilePaths.add(f.getAbsolutePath());
        }
        isDirty = true;
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
