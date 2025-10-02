package ca.corbett.imageviewer.ui.imagesets;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        char delim = ImageSetManager.PATH_DELIMITER;
        assertEquals("/grandpappy", grandparentActual);
        assertEquals("/grandpappy" + delim + "pa", parentActual);
        assertEquals("/grandpappy" + delim + "pa" + delim + "test", childActual);
    }

    @Test
    public void imageMoved_withNoMatch_shouldChangeNothing() {
        // GIVEN an ImageSet with a couple of images:
        ImageSet testSet = new ImageSet("test");
        testSet.addImageFilePath("/test/1");
        testSet.addImageFilePath("/test/2");
        testSet.setDirty(false);

        // WHEN we tell it about an image move that doesn't affect our images:
        testSet.imageMoved("/some/other/path/1", "/some/other/path/2");

        // THEN nothing should have changed:
        assertFalse(testSet.isDirty());
        assertEquals(2, testSet.getImageFilePaths().size());
        assertTrue(testSet.getImageFilePaths().contains("/test/1"));
        assertTrue(testSet.getImageFilePaths().contains("/test/2"));
        assertFalse(testSet.getImageFilePaths().contains("/some/other/path/1"));
        assertFalse(testSet.getImageFilePaths().contains("/some/other/path/2"));
    }

    @Test
    public void imageMoved_withMatch_shouldMove() {
        // GIVEN an ImageSet with a few images:
        ImageSet testSet = new ImageSet("test");
        testSet.addImageFilePath("/hello/1");
        testSet.addImageFilePath("/helloThere/1");
        testSet.addImageFilePath("/hello/2");
        testSet.setDirty(false);

        // WHEN we move an image:
        testSet.imageMoved("/hello/1", "/some/where/else/1");

        // THEN only that one image should have moved:
        assertTrue(testSet.isDirty());
        assertEquals(3, testSet.getImageFilePaths().size());
        assertTrue(testSet.getImageFilePaths().contains("/some/where/else/1"));
        assertTrue(testSet.getImageFilePaths().contains("/helloThere/1"));
        assertTrue(testSet.getImageFilePaths().contains("/hello/2"));
        assertFalse(testSet.getImageFilePaths().contains("/hello/1"));
    }

    @Test
    public void imageDeleted_withNoMatch_shouldDoNothing() {
        // GIVEN an ImageSet with a couple of images:
        ImageSet testSet = new ImageSet("test");
        testSet.addImageFilePath("/test/1");
        testSet.addImageFilePath("/test/2");
        testSet.setDirty(false);

        // WHEN we tell it about an image delete that doesn't affect our images:
        testSet.imageDeleted("/some/other/path/1");

        // THEN nothing should have changed:
        assertFalse(testSet.isDirty());
        assertEquals(2, testSet.getImageFilePaths().size());
        assertTrue(testSet.getImageFilePaths().contains("/test/1"));
        assertTrue(testSet.getImageFilePaths().contains("/test/2"));
    }

    @Test
    public void imageDeleted_withMatch_shouldDelete() {
        // GIVEN an ImageSet with a few images:
        ImageSet testSet = new ImageSet("test");
        testSet.addImageFilePath("/hello/1");
        testSet.addImageFilePath("/helloThere/1");
        testSet.addImageFilePath("/hello/2");
        testSet.setDirty(false);

        // WHEN we delete an image:
        testSet.imageDeleted("/hello/1");

        // THEN only that one image should have moved:
        assertTrue(testSet.isDirty());
        assertEquals(2, testSet.getImageFilePaths().size());
        assertTrue(testSet.getImageFilePaths().contains("/helloThere/1"));
        assertTrue(testSet.getImageFilePaths().contains("/hello/2"));
        assertFalse(testSet.getImageFilePaths().contains("/hello/1"));
    }

    @Test
    public void directoryMoved_withNoMatch_shouldDoNothing() {
        // GIVEN an ImageSet with a couple of images:
        ImageSet testSet = new ImageSet("test");
        testSet.addImageFilePath("/test/1");
        testSet.addImageFilePath("/test/2");
        testSet.setDirty(false);

        // WHEN we tell it about a directory move that does not affect us:
        testSet.directoryMoved("/blah1", "/blah2");

        // THEN nothing should have changed:
        assertFalse(testSet.isDirty());
        assertEquals(2, testSet.getImageFilePaths().size());
        assertTrue(testSet.getImageFilePaths().contains("/test/1"));
        assertTrue(testSet.getImageFilePaths().contains("/test/2"));
    }

    @Test
    public void directoryMoved_withMatch_shouldUpdateImages() {
        // GIVEN an ImageSet with a few images:
        ImageSet testSet = new ImageSet("test");
        testSet.addImageFilePath("/hello/1");
        testSet.addImageFilePath("/helloThere/1");
        testSet.addImageFilePath("/hello/2");
        testSet.addImageFilePath("/hello/childPath/nested/3");
        testSet.setDirty(false);

        // WHEN we move a directory:
        testSet.directoryMoved("/hello", "/foobar");

        // THEN all images only in that directory should be updated:
        assertTrue(testSet.isDirty());
        assertEquals(4, testSet.getImageFilePaths().size());
        assertTrue(testSet.getImageFilePaths().contains("/foobar/1"));
        assertTrue(testSet.getImageFilePaths().contains("/helloThere/1"));
        assertTrue(testSet.getImageFilePaths().contains("/foobar/2"));
        assertTrue(testSet.getImageFilePaths().contains("/foobar/childPath/nested/3"));
    }

    @Test
    public void sort_byName_shouldSort() {
        // GIVEN a bunch of image paths with sortable names:
        ImageSet set = new ImageSet("test");
        set.addImageFilePath("/a/5.jpg");
        set.addImageFilePath("/b/3.jpg");
        set.addImageFilePath("/c/4.jpg");
        set.addImageFilePath("/d/2.jpg");
        set.addImageFilePath("/e/1.jpg");

        // WHEN we sort the list by filename:
        set.sort(Comparator.comparing(File::getName));

        // THEN it should ignore paths and sort by just filename:
        assertEquals(5, set.getImageFilePaths().size());
        assertEquals("/e/1.jpg", set.getImageFilePaths().get(0));
        assertEquals("/d/2.jpg", set.getImageFilePaths().get(1));
        assertEquals("/b/3.jpg", set.getImageFilePaths().get(2));
        assertEquals("/c/4.jpg", set.getImageFilePaths().get(3));
        assertEquals("/a/5.jpg", set.getImageFilePaths().get(4));
    }

    @Test
    public void sort_byPath_shouldSort() {
        // GIVEN a bunch of image paths with sortable paths:
        ImageSet set = new ImageSet("test");
        set.addImageFilePath("/5/a.jpg");
        set.addImageFilePath("/4/c.jpg");
        set.addImageFilePath("/3/d.jpg");
        set.addImageFilePath("/2/b.jpg");
        set.addImageFilePath("/1/e.jpg");

        // WHEN we sort the list by path:
        set.sort(Comparator.comparing(File::getAbsolutePath));

        // THEN it should sort by full path+name:
        assertEquals(5, set.getImageFilePaths().size());
        assertEquals("/1/e.jpg", set.getImageFilePaths().get(0));
        assertEquals("/2/b.jpg", set.getImageFilePaths().get(1));
        assertEquals("/3/d.jpg", set.getImageFilePaths().get(2));
        assertEquals("/4/c.jpg", set.getImageFilePaths().get(3));
        assertEquals("/5/a.jpg", set.getImageFilePaths().get(4));
    }

    @Test
    public void sort_byFileDate_shouldSort() throws Exception {
        // GIVEN files with ascending
        File file1 = File.createTempFile("aaa", ".tmp");
        file1.setLastModified(30);
        File file2 = File.createTempFile("zzz", ".tmp");
        file2.setLastModified(10);
        File file3 = File.createTempFile("ttt", ".tmp");
        file3.setLastModified(20);
        ImageSet set = new ImageSet("test");
        set.addImageFilePath(file1.getAbsolutePath());
        set.addImageFilePath(file2.getAbsolutePath());
        set.addImageFilePath(file3.getAbsolutePath());

        // WHEN we sort by file time:
        set.sort(Comparator.comparing(File::lastModified));

        // THEN it should ignore names and sort by file time:
        assertEquals(3, set.getImageFilePaths().size());
        assertEquals(file2.getAbsolutePath(), set.getImageFilePaths().get(0)); // oldest
        assertEquals(file3.getAbsolutePath(), set.getImageFilePaths().get(1));
        assertEquals(file1.getAbsolutePath(), set.getImageFilePaths().get(2)); // newest

        // Cleanup:
        file1.delete();
        file2.delete();
        file3.delete();
    }
}