package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ui.MainWindow;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages loading and saving of ImageSets, and provides central management for the
 * ImageSet tree.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetManager {

    private static final Logger log = Logger.getLogger(ImageSetManager.class.getName());

    public final static char PATH_DELIMITER = '/';

    private List<ImageSet> imageSets;
    private boolean isDirty;

    public ImageSetManager() {
        imageSets = new ArrayList<>();
        isDirty = false;
    }

    public boolean isDirty() {
        File saveFile = new File(AppConfig.getInstance().getImageSetSaveLocation(), "imageSets.json");
        // If our save destination doesn't exist, the answer is yes with no further checking needed:
        if (!saveFile.exists()) {
            isDirty = true;
            return true;
        }

        // The result is true if we are dirty or if any of our image sets are dirty:
        boolean result = isDirty;
        for (ImageSet set : imageSets) {
            if (set.isTransient()) {
                continue; // don't include transient sets in the check
            }
            result = result || set.isDirty();
        }
        return result;
    }

    private void setDirty(boolean dirty) {
        isDirty = dirty;
        if (!dirty) {
            for (ImageSet set : imageSets) {
                set.setDirty(false);
            }
        }
    }

    public void addImageSet(ImageSet set) {
        // See if we already have this one:
        ImageSet existingSet = findImageSet(set.getFullyQualifiedName()).orElse(null);

        // TODO think about this...
        // If there's an existing set with that path, add all of this new set's images to that existing one.
        // The imageset will weed out duplicates automatically.
        if (existingSet != null) {
            for (String imagePath : set.getImageFilePaths()) {
                existingSet.addImageFilePath(imagePath);
            }
        }

        // If this set wasn't pre-existing, just add it.
        else {
            imageSets.add(set);
        }

        if (!set.isTransient()) {
            isDirty = true;
        }
    }

    public Optional<ImageSet> findImageSet(String fullyQualifiedName) {
        for (ImageSet imageSet : imageSets) {
            if (imageSet.getFullyQualifiedName().equals(parseFullyQualifiedName(fullyQualifiedName))) {
                return Optional.of(imageSet);
            }
        }
        return Optional.empty();
    }

    public ImageSet findOrCreateImageSet(String fullyQualifiedName) {
        String parsedName = parseFullyQualifiedName(fullyQualifiedName);
        if (parsedName.isBlank()) {
            return null;
        }
        Optional<ImageSet> set = findImageSet(parsedName);
        if (set.isEmpty()) {
            ImageSet newSet = new ImageSet();
            newSet.setFullyQualifiedName(fullyQualifiedName);
            imageSets.add(newSet);
            isDirty = true;
            return newSet;
        }

        return set.get();
    }

    /**
     * Remove the given ImageSet and all nodes underneath it in the tree.
     */
    public void remove(ImageSet set) {
        imageSets.remove(set);
        remove(set.getFullyQualifiedName());
    }

    public boolean isBranchLocked(String path) {
        if (path == null || path.length() <= 1) {
            return false;
        }

        // If any image set at or below this path is locked, the branch is locked:
        for (ImageSet candidate : imageSets) {
            if (candidate.getFullyQualifiedName().startsWith(path) && candidate.isLocked()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes all nodes at or under the given tree path.
     */
    public void remove(String path) {
        // Ignore dumb requests:
        if (path == null || path.length() <= 1) {
            return;
        }

        List<ImageSet> survivors = new ArrayList<>(imageSets.size());
        for (ImageSet candidate : imageSets) {
            if (!candidate.getFullyQualifiedName().startsWith(path)) {
                survivors.add(candidate);
            }
        }

        // If nothing got removed, we're done:
        if (survivors.size() == imageSets.size()) {
            return;
        }

        // Otherwise, keep only those ImageSets that survives the deletion:
        imageSets.clear();
        imageSets.addAll(survivors);
    }

    public List<ImageSet> getImageSets() {
        return imageSets.stream().sorted(Comparator.comparing(ImageSet::getFullyQualifiedName)).toList();
    }

    public void save() {
        if (!isDirty()) {
            log.info("Skipping image set save as no save is needed.");
            return; // don't save if not needed
        }

        File saveFile = new File(AppConfig.getInstance().getImageSetSaveLocation(), "imageSets.json");
        log.info("Saving image sets to " + saveFile.getAbsolutePath());
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        List<ImageSet> imageSetsToSave = imageSets
                .stream()
                .filter(item -> !item.isTransient())
                .toList();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writer(prettyPrinter).writeValue(saveFile, imageSetsToSave);
            setDirty(false);
            log.info("Saved " + imageSetsToSave.size() + " image sets.");
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "ImageSetManager.save(): " + e.getMessage(), e);
        }
    }

    public void load() {
        File saveFile = new File(AppConfig.getInstance().getImageSetSaveLocation(), "imageSets.json");
        log.info("Loading image sets from " + saveFile.getAbsolutePath());
        if (!saveFile.exists()) {
            log.info("No image sets to load.");
            return; // not a fatal error - there simply are no favorites
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            ImageSet[] favorites = mapper.readValue(saveFile, ImageSet[].class);
            for (ImageSet set : favorites) {
                // Dead image handling:
                for (String filePath : set.getImageFilePaths()) {
                    if (!new File(filePath).exists()) {
                        log.warning("Image set load: skipping non-existing file "
                                            + filePath
                                            + " in set "
                                            + set.getFullyQualifiedName());
                        set.removeImageFilePath(filePath);
                    }
                }

                addImageSet(set);
            }

            MainWindow.getInstance().getImageSetPanel().resync();
            log.info("Loaded " + imageSets.size() + " saved image sets.");
            MainWindow.getInstance().rebuildMenus();
            setDirty(false);
        }
        catch (IOException e) {
            log.log(Level.SEVERE, "Error saving image sets: " + e.getMessage(), e);
        }
    }

    public static String parseFullyQualifiedName(String input) {
        String[] nodes = parsePathNodes(input);
        if (nodes.length == 0) {
            return "";
        }

        // Our fully qualified path starts with the delimiter and includes
        // each distinct item in the given newName, including the last one:
        StringBuilder sb = new StringBuilder();
        sb.append(PATH_DELIMITER);
        for (int i = 0; i < nodes.length; i++) {
            sb.append(nodes[i]);
            if (i != nodes.length - 1) {
                sb.append(PATH_DELIMITER);
            }
        }
        return sb.toString();
    }

    public static String parseName(String input) {
        String[] nodes = parsePathNodes(input);
        if (nodes.length == 0) {
            return "";
        }

        // Our name is the final element in whatever path we were given:
        // (if we were given just a name with no path, then that is our name)
        return nodes[nodes.length-1];
    }

    public static String parsePath(String input) {
        String[] nodes = parsePathNodes(input);
        if (nodes.length == 0) {
            return "";
        }

        // The path starts with the delimiter and includes each item in the
        // given newName, not including the last one. This string will end
        // with the delimiter character.
        StringBuilder sb = new StringBuilder();
        sb.append(PATH_DELIMITER);
        for (int i = 0; i < nodes.length - 1; i++) {
            sb.append(nodes[i]);
            sb.append(PATH_DELIMITER);
        }
        return sb.toString();
    }

    /**
     * Given a delimited String, will return a String array containing all the non-blank elements
     * in the input. Leading and trailing delimiters are ignored, as are repeated delimiters.
     * So: "hello/////there", "hello//there", "/hello/there/" and "hello/there" all return
     * an array of length 2 with the elements "hello" and "there". If the input String is
     * empty or null, you get an empty array.
     */
    public static String[] parsePathNodes(String input) {
        if (input == null || input.isBlank()) {
            return new String[0];
        }

        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == PATH_DELIMITER) {
                // If we have accumulated characters, add them as a part
                if (!current.isEmpty()) {
                    String candidate = current.toString().trim();
                    if (!candidate.isBlank()) {
                        parts.add(candidate);
                    }
                    current.setLength(0); // Clear the StringBuilder
                }
                // Otherwise, ignore the delimiter (handles consecutive delimiters)
            }
            else {
                current.append(c);
            }
        }

        // Add the last part if it's not empty
        if (!current.isEmpty()) {
            String candidate = current.toString().trim();
            if (!candidate.isBlank()) {
                parts.add(candidate);
            }
        }

        return parts.toArray(new String[0]);
    }
}
