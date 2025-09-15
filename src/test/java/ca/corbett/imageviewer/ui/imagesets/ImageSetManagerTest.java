package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.imageviewer.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageSetManagerTest {

    private ImageSetManager manager;

    @BeforeEach
    public void setup() {
        manager = new ImageSetManager();
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
            String[] result = ImageSetManager.parsePathNodes(input);

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
            String[] result = ImageSetManager.parsePathNodes(input);

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
            String[] result = ImageSetManager.parsePathNodes(input);

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
            String[] result = ImageSetManager.parsePathNodes(input);

            // THEN we should get just the element name:
            assertNotNull(result);
            assertEquals(1, result.length);
            assertEquals("test", result[0]);
        }
    }

    @Test
    public void parseParent_withInvalidInput_shouldReturnRoot() {
        // GIVEN input with garbage values:
        String[] garbageInput = {
                "",
                null,
                "////////////////////",
                "                     "
        };

        // WHEN we try to parse the parent:
        for (String input : garbageInput) {
            String actual = ImageSetManager.parseParent(input);

            // THEN we should get the delimiter:
            assertEquals("/", actual);
        }
    }

    @Test
    public void parseParent_withShortPath_shouldReturnRoot() {
        // GIVEN input path with one element:
        String[] shortInput = {
                "hello",
                "/hello",
                "///hello",
                "/hello/",
                "hello/"
        };

        // WHEN we try to parse the parent:
        for (String input : shortInput) {
            String actual = ImageSetManager.parseParent(input);

            // THEN we should get the delimiter:
            assertEquals("/", actual);
        }
    }

    @Test
    public void parseParent_withLongPath_shouldReturnParent() {
        // GIVEN input with two or more elements in the path:
        String[] longInput = {
                "/parent/",
                "hello/parent/",
                "/////////hello////////parent////////",
                "blah/blah/blah/parent//////////",
                "1/2/3/4/5/parent/"
        };

        // WHEN we try to parse the parent:
        for (String input : longInput) {
            String expectedPath = ImageSetManager.parseFullyQualifiedName(input) + "/";
            String actual = ImageSetManager.parseParent(input + "test");

            // THEN we should get the correct parent every time:
            assertEquals(expectedPath, actual);
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
            ImageSet result = manager.findOrCreateImageSet(input);

            // THEN we should get nothing:
            assertNull(result);
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
            ImageSet result = manager.findOrCreateImageSet(input);

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
            ImageSet result = manager.findOrCreateImageSet(input);

            // THEN we should get the leaf node:
            assertEquals("there", result.getName());

            // AND the root node should be present:
            assertEquals("/hello/there", result.getFullyQualifiedName());
        }
    }

    @Test
    public void isDirty_whenNotDirty_shouldBeFalse() throws IOException {
        // GIVEN an ImageSetManager with no changes:
        File tmpFile = new File(System.getProperty("java.io.tmpdir"), "imageSets.json");
        tmpFile.createNewFile();
        AppConfig.getInstance().setImageSetSaveLocation(tmpFile.getParentFile());

        // WHEN we query it for isDirty:
        boolean actual = manager.isDirty();

        // THEN it should be false:
        assertFalse(actual);
        tmpFile.delete();
    }

    @Test
    public void isDirty_withUnsavedChanges_shouldBeTrue() throws IOException {
        // GIVEN an ImageSetManager with changes:
        File tmpFile = new File(System.getProperty("java.io.tmpdir"), "imageSets.json");
        tmpFile.createNewFile();
        AppConfig.getInstance().setImageSetSaveLocation(tmpFile.getParentFile());
        manager.addImageSet(new ImageSet("test"));

        // WHEN we query it for isDirty:
        boolean actual = manager.isDirty();

        // THEN it should be true:
        assertTrue(actual);
        tmpFile.delete();
    }

    @Test
    public void isDirty_afterSave_shouldBeFalse() throws IOException {
        // GIVEN an ImageSetManager with changes that get saved:
        File tmpFile = new File(System.getProperty("java.io.tmpdir"), "imageSets.json");
        tmpFile.createNewFile();
        AppConfig.getInstance().setImageSetSaveLocation(tmpFile.getParentFile());
        manager.addImageSet(new ImageSet("test1"));
        manager.save();

        // WHEN we query it for isDirty:
        boolean actual = manager.isDirty();

        // THEN it should be false:
        assertFalse(actual);
        tmpFile.delete();
    }

    @Test
    public void isLocked_withoutLock_shouldBeFalse() {
        // GIVEN an ImageSetManager with no locked image sets:
        manager.addImageSet(new ImageSet("test1"));
        manager.addImageSet(new ImageSet("test2"));
        manager.addImageSet(new ImageSet("test3/test4/test5"));

        // WHEN we query for isLocked:
        boolean actual1 = manager.isBranchLocked("/test1");
        boolean actual2 = manager.isBranchLocked("/test2");
        boolean actual3a = manager.isBranchLocked("/test3");
        boolean actual3b = manager.isBranchLocked("/test3/test4");
        boolean actual3c = manager.isBranchLocked("/test3/test4/test5");

        // THEN they should all be false:
        assertFalse(actual1);
        assertFalse(actual2);
        assertFalse(actual3a);
        assertFalse(actual3b);
        assertFalse(actual3c);
    }

    @Test
    public void isLocked_withLockedPath_shouldReturnTrue() {
        // GIVEN an ImageSetManager with a locked image set:
        manager.addImageSet(new ImageSet("test1"));
        ImageSet lockedSet = new ImageSet("test2/locked");
        lockedSet.setLocked(true);
        manager.addImageSet(lockedSet);

        // WHEN we query for isLocked:
        boolean actual1 = manager.isBranchLocked("/test1");
        boolean actual2a = manager.isBranchLocked("/test2");
        boolean actual2b = manager.isBranchLocked("/test2/locked");

        // THEN only the locked branch should show as locked:
        assertFalse(actual1);
        assertTrue(actual2a);
        assertTrue(actual2b);
    }

    @Test
    public void isLocked_withLockedAncestor_shouldReturnLocked() {
        // GIVEN an ImageSetManager with a locked ancestor node high in the tree:
        manager.addImageSet(new ImageSet("testRoot1")); // unlocked root
        ImageSet lockedSet = new ImageSet("testRoot1/lockedNode");
        lockedSet.setLocked(true);
        manager.addImageSet(lockedSet);
        manager.addImageSet(new ImageSet("testRoot1/lockedNode/test1"));
        manager.addImageSet(new ImageSet("testRoot1/lockedNode/test1/test2"));
        manager.addImageSet(new ImageSet("testRoot1/lockedNode/test1/test2/test3"));
        manager.addImageSet(new ImageSet("testRoot2")); // unrelated sibling

        // WHEN we check locked status:
        // THEN everything in testRoot1 should be locked:
        assertTrue(manager.isBranchLocked("testRoot1"));
        assertTrue(manager.isBranchLocked("testRoot1/lockedNode"));
        assertTrue(manager.isBranchLocked("testRoot1/lockedNode/test1"));
        assertTrue(manager.isBranchLocked("testRoot1/lockedNode/test1/test2"));
        assertTrue(manager.isBranchLocked("testRoot1/lockedNode/test1/test2/test3"));

        // But the sibling node should be unlocked:
        assertFalse(manager.isBranchLocked("testRoot2"));
    }

    @Test
    public void remove_withInvalidPath_shouldDoNothing() {
        // GIVEN an ImageSetManager with a few sets:
        manager.addImageSet(new ImageSet("test1"));
        manager.addImageSet(new ImageSet("test2"));
        manager.addImageSet(new ImageSet("test3"));

        // WHEN we try to delete a non-existent path:
        manager.remove("/hello");
        manager.remove((String)null);
        manager.remove("");
        manager.remove("/");
        manager.remove("frinky/glavin/meow/meow");

        // THEN absolutely nothing should have happened:
        assertEquals(3, manager.getImageSets().size());
    }

    @Test
    public void remove_givenPartialPath_shouldRemoveSelectively() {
        // GIVEN an ImageSetManager with a branch of ImageSets:
        manager.addImageSet(new ImageSet("test1"));
        manager.addImageSet(new ImageSet("test1/test2"));
        manager.addImageSet(new ImageSet("test1/test2/test3"));

        // WHEN we remove the middle of the path:
        manager.remove("/test1/test2");

        // THEN only that part of the branch should be gone:
        assertTrue(manager.findImageSet("/test1").isPresent());
        assertFalse(manager.findImageSet("/test1/test2").isPresent());
        assertFalse(manager.findImageSet("/test1/test2/test3").isPresent());
    }

    @Test
    public void remove_givenMiddleNode_shouldRemoveSelectively() {
        // GIVEN an ImageSetManager with a branch of ImageSets:
        ImageSet set1 = new ImageSet("test1");
        ImageSet set2 = new ImageSet("test1/test2");
        ImageSet set3 = new ImageSet("test1/test2/test3");
        manager.addImageSet(set1);
        manager.addImageSet(set2);
        manager.addImageSet(set3);

        // WHEN we remove the middle of the path:
        manager.remove(set2);

        // THEN only that part of the branch should be gone:
        assertTrue(manager.findImageSet("/test1").isPresent());
        assertFalse(manager.findImageSet("/test1/test2").isPresent());
        assertFalse(manager.findImageSet("/test1/test2/test3").isPresent());
    }

    @Test
    public void renameBranch_withNoMatch_shouldDoNothing() {
        // GIVEN an ImageSetManager with a branch of ImageSets:
        ImageSet set1 = new ImageSet("test1");
        ImageSet set2 = new ImageSet("test1/test2");
        ImageSet set3 = new ImageSet("test1/test2/test3");
        manager.addImageSet(set1);
        manager.addImageSet(set2);
        manager.addImageSet(set3);

        // WHEN we rename a branch that does not exist:
        manager.renameBranch("/howdy/pardner", "froobydooby");

        // THEN nothing should have changed:
        assertTrue(manager.findImageSet("/test1").isPresent());
        assertTrue(manager.findImageSet("/test1/test2").isPresent());
        assertTrue(manager.findImageSet("/test1/test2/test3").isPresent());
    }

    @Test
    public void renameBranch_withBranchMatch_shouldRenameBranch() {
        // GIVEN an ImageSetManager with a branch of ImageSets:
        ImageSet set1 = new ImageSet("test1");
        ImageSet set2 = new ImageSet("test1/test2");
        ImageSet set3 = new ImageSet("test1/test2/test3");
        manager.addImageSet(set1);
        manager.addImageSet(set2);
        manager.addImageSet(set3);

        // WHEN we rename a branch in the middle:
        manager.renameBranch("/test1/test2", "hello");

        // THEN everything at that level and under should have been updated
        assertTrue(manager.findImageSet("/test1").isPresent());
        assertTrue(manager.findImageSet("/hello").isPresent());
        assertTrue(manager.findImageSet("/hello/test3").isPresent());
        assertFalse(manager.findImageSet("/test1/test2").isPresent());
        assertFalse(manager.findImageSet("/test1/test3").isPresent());
    }

    @Test
    public void renameBranch_withLeafMatch_shouldRenameLeaf() {
        // GIVEN an ImageSetManager with a branch of ImageSets:
        ImageSet set1 = new ImageSet("test1");
        ImageSet set2 = new ImageSet("test1/test2");
        ImageSet set3 = new ImageSet("test1/test2/test3");
        manager.addImageSet(set1);
        manager.addImageSet(set2);
        manager.addImageSet(set3);

        // WHEN we rename a leaf node:
        manager.renameBranch("/test1/test2/test3", "hello");

        // THEN only that leaf should have been renamed:
        assertTrue(manager.findImageSet("/test1").isPresent());
        assertTrue(manager.findImageSet("/hello").isPresent());
        assertFalse(manager.findImageSet("/hello/test3").isPresent());
        assertTrue(manager.findImageSet("/hello").isPresent());
    }
}