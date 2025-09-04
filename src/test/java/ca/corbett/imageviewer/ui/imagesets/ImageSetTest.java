package ca.corbett.imageviewer.ui.imagesets;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageSetTest {

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
            String[] result = ImageSet.parsePathNodes(input);

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
            String[] result = ImageSet.parsePathNodes(input);

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
            String[] result = ImageSet.parsePathNodes(input);

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
            String[] result = ImageSet.parsePathNodes(input);

            // THEN we should get just the element name:
            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals("test", result[0]);
        }
    }

    @Test
    public void findImageSet_withGarbageInput_returnsEmpty() {
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
            Optional<ImageSet> result = ImageSet.getImageSet(input);

            // THEN we should get nothing:
            assertFalse(result.isPresent());
        }
    }

    @Test
    public void findImageSet_withValidInput_shouldCreateNodes() {
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
            Optional<ImageSet> result = ImageSet.getImageSet(input);

            // THEN we should get a valid node with the expected name:
            assertTrue(result.isPresent());
            assertEquals("hello", result.get().getName());
        }
    }

    @Test
    public void findImageSet_withMultiPath_shouldCreatePath() {
        //GIVEN input that describes a path:
        String[] multiElementInput = {
                "hello/there",
                "/hello/there/",
                "/hello/there",
                "    / / / / / /  / / / hello / / / / / / there / / / / "
        };

        // WHEN we try to get an ImageSet out of it:
        for (String input : multiElementInput) {
            Optional<ImageSet> result = ImageSet.getImageSet(input);

            // THEN we should get the leaf node:
            assertTrue(result.isPresent());
            assertEquals("there", result.get().getName());

            // AND the root node should be present:
            assertEquals(1, ImageSet.rootNodes.size());
            assertEquals("hello", ImageSet.rootNodes.get(0).getName());

            // AND that root node should have the leaf as a child:
            assertEquals(1, ImageSet.rootNodes.get(0).getChildSets().size());
            assertEquals("there", ImageSet.rootNodes.get(0).getChildSets().get(0).getName());
        }
    }
}