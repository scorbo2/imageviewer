package ca.corbett.imageviewer.ui.imagesets;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
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
public class ImageSet extends DefaultMutableTreeNode {

    private static final Logger log = Logger.getLogger(ImageSet.class.getName());

    private final List<String> imageFiles = new ArrayList<>();
    private final String name;

    @JsonIgnore
    private boolean isDirty; // TODO do something with this

    public ImageSet(String name) {
        super(name);
        this.name = name;
    }

    public List<File> getImageFiles() {
        List<File> list = new ArrayList<>();
        for (String imageFile : imageFiles) {
            list.add(new File(imageFile));
        }
        return list;
    }

    public void clearImages() {
        imageFiles.clear();
    }

    public boolean addImageFile(File imageFile) {
        if (imageFile == null) {
            log.warning("Ignoring attempt to add null image to image set.");
            return false;
        }
        if (imageFiles.contains(imageFile.getAbsolutePath())) {
            log.warning("Ignoring attempt to add duplicate image to image set: " + imageFile.getAbsolutePath());
            return false;
        }
        imageFiles.add(imageFile.getAbsolutePath());
        return true;
    }

    public String getName() {
        return name;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setIsDirty(boolean dirty) {
        isDirty = dirty;
    }

    /**
     * Returns the fully-qualified path and name of this node as a formatted string using the PATH_DELIMITER
     * constant in ImageSetPanel.
     */
    public String getPathString() {
        TreeNode[] treePath = getPath();
        StringBuilder sb = new StringBuilder();
        for (TreeNode node : treePath) {
            if (node instanceof ImageSet imageSet) {
                sb.append(imageSet.getName());

                if (imageSet != this) {
                    sb.append(ImageSetPanel.PATH_DELIMITER);
                }
            }
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImageSet imageSet)) { return false; }
        return Objects.equals(getPathString(), imageSet.getPathString()) && Objects.equals(name, imageSet.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPathString(), name);
    }

    public static List<ImageSet> getRootNodes() {
        List<ImageSet> list = new ArrayList<>();
        list.add(new ImageSet("Favorites"));
        return list;
    }
}
