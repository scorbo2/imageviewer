package ca.corbett.imageviewer.ui.imagesets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageSetManagerTest {

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
            String[] result = ImageSetManager.getInstance().parsePathNodes(input);

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
            String[] result = ImageSetManager.getInstance().parsePathNodes(input);

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
            String[] result = ImageSetManager.getInstance().parsePathNodes(input);

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
            String[] result = ImageSetManager.getInstance().parsePathNodes(input);

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
            ImageSet result = ImageSetManager.getInstance().findOrCreateImageSet(input);

            // THEN we should get nothing:
            // TODO is this test useless after the last refactor? assertFalse(result.isPresent());
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
            ImageSet result = ImageSetManager.getInstance().findOrCreateImageSet(input);

            // THEN we should get a valid node with the expected name:
            assertEquals("hello", result.getName());
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
            ImageSet result = ImageSetManager.getInstance().findOrCreateImageSet(input);

            // THEN we should get the leaf node:
            assertEquals("there", result.getName());

            // AND the root node should be present:
            //assertEquals(1, imageSetPanel.getFavoritesRoot().getChildCount());

            // AND that root node should have the leaf as a child:
            //TODO fixme assertEquals("hello", ((ImageSet)imageSetPanel.getFavoritesRoot().getChildAt(0)).getName());
        }
    }
    
}