package ca.corbett.imageviewer.ui.imagesets;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a list which can contain either other ImageSets or individual image files.
 * To be used with ImageSetPanel.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSet extends DefaultMutableTreeNode {

    protected static final List<ImageSet> rootNodes = new ArrayList<>();

    private final UUID id;
    private final List<ImageSet> childSets = new ArrayList<>();
    private final List<String> imageFiles = new ArrayList<>();
    private final String name;

    public ImageSet(String name) {
        super(name);
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public List<File> getImageFiles() {
        List<File> list = new ArrayList<>();
        for (String imageFile : imageFiles) {
            list.add(new File(imageFile));
        }
        return list;
    }

    public void addImageFile(File imageFile) {
        imageFiles.add(imageFile.getAbsolutePath());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImageSet imageSet)) { return false; }
        return Objects.equals(id, imageSet.id) && Objects.equals(name, imageSet.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public static List<ImageSet> getRootNodes() {
        List<ImageSet> list = new ArrayList<>();
        list.add(new ImageSet("Favorites"));
        return list;
    }
}
