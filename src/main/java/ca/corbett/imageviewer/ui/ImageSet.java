package ca.corbett.imageviewer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Represents a list which can contain either other ImageSets or individual image files.
 * To be used with ImageSetPanel.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImageSet {

    public static char PATH_DELIMITER = '/';
    protected static final List<ImageSet> rootNodes = new ArrayList<>();

    private final List<ImageSet> childSets = new ArrayList<>();
    private final List<File> images = new ArrayList<>();
    private final String name;

    protected ImageSet(String name) {
        this.name = name;
    }

    /**
     * Finds or creates an ImageSet with the given name.
     * You can use PATH_DELIMITER to create a nested path, like "parent/child/grandchild".
     * Or use a non-delimited string to just create/search for a top level ImageSet.
     */
    public static Optional<ImageSet> getImageSet(String name) {
        String[] nodes = parsePathNodes(name);
        if (nodes.length == 0) {
            return Optional.empty();
        }

        // Find or create the root node:
        ImageSet rootNode = null;
        for (ImageSet candidate : rootNodes) {
            if (candidate.getName().equalsIgnoreCase(nodes[0])) {
                rootNode = candidate;
                break;
            }
        }
        if (rootNode == null) {
            rootNode = new ImageSet(nodes[0]);
            rootNodes.add(rootNode);
        }

        // If there was no sub-path, we're done:
        if (nodes.length == 1) {
            return Optional.of(rootNode);
        }

        // Otherwise, recursively search that root node until we have the target:
        return findImageSet(rootNode, Arrays.stream(nodes).skip(1).toArray(String[]::new));
    }

    public String getName() {
        return name;
    }

    protected List<ImageSet> getChildSets() {
        return childSets;
    }

    private void addChild(ImageSet set) {
        childSets.add(set);
    }

    private static Optional<ImageSet> findImageSet(ImageSet rootNode, String[] nodes) {
        ImageSet nextNode = null;
        for (ImageSet candidate : rootNode.getChildSets()) {
            if (candidate.getName().equalsIgnoreCase(nodes[0])) {
                nextNode = candidate;
            }
        }
        if (nextNode == null) {
            nextNode = new ImageSet(nodes[0]);
            rootNode.addChild(nextNode);
        }

        // If there are no more path elements, this is the final leaf node:
        if (nodes.length == 1) {
            return Optional.of(nextNode);
        }

        // Otherwise, recurse further:
        return findImageSet(nextNode, Arrays.stream(nodes).skip(1).toArray(String[]::new));
    }

    /**
     * Given a delimited String, will return a String array containing all the non-blank elements
     * in the input. Leading and trailing delimiters are ignored, as are repeated delimiters.
     * So: "hello/////there", "hello//there", "/hello/there/" and "hello/there" all return
     * an array of length 2 with the elements "hello" and "there". If the input String is
     * empty or null, you get an empty array.
     */
    protected static String[] parsePathNodes(String input) {
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
                    if (! candidate.isBlank()) {
                        parts.add(candidate);
                    }
                    current.setLength(0); // Clear the StringBuilder
                }
                // Otherwise, ignore the delimiter (handles consecutive delimiters)
            } else {
                current.append(c);
            }
        }

        // Add the last part if it's not empty
        if (!current.isEmpty()) {
            String candidate = current.toString().trim();
            if (! candidate.isBlank()) {
                parts.add(candidate);
            }
        }

        return parts.toArray(new String[0]);
    }
}
