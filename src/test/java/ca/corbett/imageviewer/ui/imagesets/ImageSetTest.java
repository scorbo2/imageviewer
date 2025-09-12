package ca.corbett.imageviewer.ui.imagesets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageSetTest {

    @Test
    public void getPathString_withEmptyPath_shouldReturnNameOnly() {
        // GIVEN a node with no path:
        ImageSet imageSet = new ImageSet();
        imageSet.setFullyQualifiedName("test");

        // WHEN we ask it for its path string:
        String actual = imageSet.getFullyQualifiedName();

        // THEN it should be empty:
        assertEquals("/test", actual);
    }

    @Test
    public void getPathString_withLongPath_shouldReturnFullyQualified() {
        // GIVEN a node with parents and grandparents:
        ImageSet grandparent = new ImageSet();
        grandparent.setFullyQualifiedName("grandpappy");
        ImageSet parent = new ImageSet();
        parent.setFullyQualifiedName("grandpappy/pa");
        ImageSet child = new ImageSet();
        child.setFullyQualifiedName("grandpappy/pa/test");

        // WHEN we ask each node for its path string:
        String grandparentActual = grandparent.getFullyQualifiedName();
        String parentActual = parent.getFullyQualifiedName();
        String childActual = child.getFullyQualifiedName();

        // THEN each should be fully qualified:
        char delim = ImageSetPanel.PATH_DELIMITER;
        assertEquals("/grandpappy", grandparentActual);
        assertEquals("/grandpappy" + delim + "pa", parentActual);
        assertEquals("/grandpappy" + delim + "pa" + delim + "test", childActual);
    }
}