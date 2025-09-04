package ca.corbett.imageviewer.ui.imagesets;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a list which can contain either other ImageSets or individual image files.
 * To be used with ImageSetPanel.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSet {

    public static char PATH_DELIMITER = '/';
    protected static final List<ImageSet> rootNodes = new ArrayList<>();

    private final UUID id;
    private final List<ImageSet> childSets = new ArrayList<>();
    private final List<File> images = new ArrayList<>();
    private final String name;

    protected ImageSet(String name) {
        this.id = UUID.randomUUID();
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
        return Optional.of(findImageSet(rootNode, Arrays.stream(nodes).skip(1).toArray(String[]::new)));
    }

    public UUID getId() {
        return id;
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

    /**
     * Recursively searches the given rootNode using the given nodes array as a path element.
     * For example, given a root node named "root" and a nodes array of {"1","2","3"}, this
     * method will find or create a child node of "1" inside the given root node, a grandchild
     * node of "2" inside of "1", and a great-grandchild node of "3" inside of "2". The return
     * value is the last leaf node (in the example above, node "3" would be returned). All parent
     * nodes of the returned node will have been created if necessary.
     */
    private static ImageSet findImageSet(ImageSet rootNode, String[] nodes) {
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
            return nextNode;
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

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof ImageSet imageSet)) { return false; }
        return Objects.equals(id, imageSet.id) && Objects.equals(name, imageSet.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
