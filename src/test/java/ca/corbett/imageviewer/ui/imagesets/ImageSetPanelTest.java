package ca.corbett.imageviewer.ui.imagesets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageSetPanelTest {

    private ImageSetPanel imageSetPanel;

    @BeforeEach
    public void setup() {
        imageSetPanel = new ImageSetPanel();
    }

    @Test
    public void parsePathNodes_withInvalidInput_shouldReturnEmptyArray() {
        //GIVEN garbage input for parsePathNodes:
        String[] garbageInput = {
                null,
                "",
                " ",
                "/",
                "//////////////"
        };

        //WHEN we try to parse them:
        for (String input : garbageInput) {
            String[] result = imageSetPanel.parsePathNodes(input);

            // THEN we should get a non-null but empty array:
            assertNotNull(result);
            assertEquals(0, result.length);
        }
    }

    @Test
    public void parsePathNodes_withSingleElement_shouldReturnSingleArrayElement() {
        //GIVEN input that only specifies one element:
        String[] singleElementInput = {
                "hello",
                "/hello",
                "hello/",
                "/hello/",
                "/////hello////",
                " hello ",
                "/ hello /"
        };

        //WHEN we try to parse it:
        for (String input : singleElementInput) {
            String[] result = imageSetPanel.parsePathNodes(input);

            // THEN we should get an array of size one with our expected element:
            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals("hello", result[0]);
        }
    }

    @Test
    public void parsePathNodes_withMultipleElements_shouldReturnExpectedElements() {
        //GIVEN input that specifies multiple path elements:
        String[] multiElementInput = {
                "hello/there/turkey",
                "/hello/there/turkey/",
                "//hello//there//turkey//",
                "///////hello/there/turkey",
                "hello/there/turkey//////"
        };

        //WHEN we try to parse it:
        for (String input : multiElementInput) {
            String[] result = imageSetPanel.parsePathNodes(input);

            // THEN we should get an array with all expected elements:
            assertNotNull(result);
            assertEquals(3, result.length);
            assertEquals("hello", result[0]);
            assertEquals("there", result[1]);
            assertEquals("turkey", result[2]);
        }
    }

    @Test
    public void parsePathNodes_withLeadingTrailingWhitespace_shouldTrim() {
        //GIVEN input with leading and/or trailing whitespace
        String[] inputWithSpaces = {
                " test",
                "test ",
                "/test ",
                "/ test ",
                "    /     test    ",
                "test    /     "
        };

        // WHEN we try to parse it:
        for (String input : inputWithSpaces) {
            String[] result = imageSetPanel.parsePathNodes(input);

            // THEN we should get just the element name:
            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals("test", result[0]);
        }
    }

    @Test
    public void findOrCreateImageSet_InNode_withGarbageInput_returnsEmpty() {
        //GIVEN input with garbage values:
        String[] garbageInput = {
                "",
                null,
                "  ",
                "//////",
                "/",
                "     /     ",
                "/ / / / / / / "
        };

        //WHEN we try to get an ImageSet out of it:
        for (String input : garbageInput) {
            Optional<ImageSet> result = imageSetPanel.findOrCreateFavoritesSet(input);

            // THEN we should get nothing:
            assertFalse(result.isPresent());
        }
    }

    @Test
    public void findOrCreateImageSet_InNode_withValidInput_shouldCreateNodes() {
        //GIVEN input with a single element:
        String[] singleElementInput = {
                "hello",
                "/hello",
                "hello/",
                "/hello/",
                " / / / / / / / / / / hello / / / / / / / "
        };

        // WHEN we try to get an ImageSet out of it:
        for (String input : singleElementInput) {
            Optional<ImageSet> result = imageSetPanel.findOrCreateFavoritesSet(input);

            // THEN we should get a valid node with the expected name:
            assertTrue(result.isPresent());
            assertEquals("hello", result.get().getName());
        }
    }

    @Test
    public void findOrCreateImageSet_InNode_withMultiPath_shouldCreatePath() {
        //GIVEN input that describes a path:
        String[] multiElementInput = {
                "hello/there",
                "/hello/there/",
                "/hello/there",
                "    / / / / / /  / / / hello / / / / / / there / / / / "
        };

        // WHEN we try to get an ImageSet out of it:
        for (String input : multiElementInput) {
            Optional<ImageSet> result = imageSetPanel.findOrCreateFavoritesSet(input);

            // THEN we should get the leaf node:
            assertTrue(result.isPresent());
            assertEquals("there", result.get().getName());

            // AND the root node should be present:
            assertEquals(1, imageSetPanel.getFavoritesRoot().getChildCount());

            // AND that root node should have the leaf as a child:
            assertEquals("hello", ((ImageSet)imageSetPanel.getFavoritesRoot().getChildAt(0)).getName());
        }
    }

    @Test
    public void favoriteSetExists_withNoData_shouldReturnFalse() {
        // GIVEN input with no data:

        // WHEN we check for the existence of paths:
        boolean path1Exists = imageSetPanel.favoriteSetExists("hello/there");
        boolean path2Exists = imageSetPanel.favoriteSetExists("Favorites");
        boolean path3Exists = imageSetPanel.favoriteSetExists("a/b/c/d/e");

        // THEN they should report false:
        assertFalse(path1Exists);
        assertFalse(path2Exists);
        assertFalse(path3Exists);
    }

    @Test
    public void favoriteSetExists_withValidPath_shouldReturnTrue() {
        // GIVEN a hierarchical setup:
        ImageSet node1 = new ImageSet("node1");
        ImageSet node2 = new ImageSet("node2");
        ImageSet node3 = new ImageSet("node3");
        node1.add(node2);
        node2.add(node3);
        imageSetPanel.addToFavorites(node1);

        // WHEN we query for the existence of it:
        boolean path1Exists = imageSetPanel.favoriteSetExists("node1/node2/node3");
        boolean path2Exists = imageSetPanel.favoriteSetExists("node1//node2//node3"); // should ignore extras
        boolean path3Exists = imageSetPanel.favoriteSetExists("//////node1/node2/////node3/////");

        // THEN all should be true:
        assertTrue(path1Exists);
        assertTrue(path2Exists);
        assertTrue(path3Exists);
    }
}