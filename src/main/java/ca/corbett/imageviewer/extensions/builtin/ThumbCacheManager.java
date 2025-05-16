package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.Version;
import org.apache.commons.io.FileUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a cache on disk of thumbnail images. Provides methods for adding,
 * updating, or removing items in the cache, as well as clearing it.
 * <p>
 * The cache is kept in the USER_SETTINGS_DIR/thumbnails directory, and the
 * directory hierarchy matches the file system, almost like a chroot situation.
 * For example:
 * </p>
 * <blockquote>
 * USER_SETTINGS_DIR/thumbnails/some/path/image_thumb32.jpg
 * </blockquote>
 * <p>
 * The above represents the 32x32 thumbnail for /some/path/image.jpg.
 * </p>
 * <p>
 * Caching can be enabled or disabled for the entire application via the extension
 * manager dialog, by just disabling this extension. Disabling the extension does not
 * clear the cache dir, so re-enabling it later will allow the use of whatever
 * thumbnails were already cached before it was disabled.
 * </p>
 *
 * @author scorbo2
 * @since ImageViewer 1.1
 */
final class ThumbCacheManager {

    private static final File CACHE_DIR = new File(Version.APPLICATION_HOME, "thumbnails");
    private static final Logger logger = Logger.getLogger(ThumbCacheManager.class.getName());

    static {
        CACHE_DIR.mkdirs();
    }

    public static class CacheStats {

        int thumbnailCount;
        int fileCount;
        long totalSize;

        @Override
        public String toString() {
            return thumbnailCount + " thumbnails in " + fileCount + " files, " + getPrintableSize(
                    totalSize) + " total.";
        }

        public String getPrintableSize(long size) {
            String suffix = " bytes";
            if (size > 1024) {
                size /= 1024;
                suffix = "KB";

                if (size > 1024) {
                    size /= 1024;
                    suffix = "MB";

                    if (size > 1024) {
                        size /= 1024;
                        suffix = "GB";

                        if (size > 1024) {
                            size /= 1024;
                            suffix = "TB";
                        }
                    }
                }
            }
            return size + suffix;
        }

    }

    private ThumbCacheManager() {
    }

    /**
     * Creates and returns a CacheStats object describing current cache statistics.
     * This may take a while to execute depending on cache size;
     *
     * @return A populated CacheStats object.
     */
    public static CacheStats gatherCacheStats() {
        CacheStats stats = new CacheStats();
        List<File> list = FileSystemUtil.findFiles(CACHE_DIR, true, "jpg");
        stats.fileCount = list.size();
        stats.thumbnailCount = list.size() / AppConfig.ThumbSize.values().length;
        for (File f : list) {
            stats.totalSize += f.length();
        }
        return stats;
    }

    /**
     * Attempts to find and load a thumbnail matching the given file and the given thumbnail
     * dimensions.
     *
     * @param srcFile The image file for which we want to find a thumbnail.
     * @param size    The desired thumbnail size.
     * @return If found, the image is loaded and returned. otherwise, null.
     */
    public static BufferedImage get(File srcFile, int size) {
        File thumbFile = generateThumbnailPath(srcFile, size);
        if (thumbFile.exists()) {
            try {
                logger.log(Level.FINE, "Returning thumbnail {1} from cache for {0}",
                           new Object[]{srcFile.getAbsolutePath(), size});
                return ImageUtil.loadImage(thumbFile);
            }
            catch (IOException | ArrayIndexOutOfBoundsException ioe) {
                logger.log(Level.SEVERE, "Unable to load thumbnail from cache for " + srcFile.getAbsolutePath(), ioe);
            }
        }
        logger.log(Level.FINE, "No thumbnail in cache for {0}", srcFile.getAbsolutePath());
        return null;
    }

    /**
     * Loads an image from the given srcFile, generates thumbnails from it, and
     * stores them into the cache. If thumbnails already existed for the given file,
     * they will be overwritten. Note that a thumbnail will be pregenerated at each
     * supported thumbnail size. Rather than throw an exception on I/O error, the
     * method simply returns false. Thumbnails are a fire-and-forget situation.
     * All errors will be logged here.
     *
     * @param srcFile The source file for which thumbnails should be generated.
     * @return Returns true if all went well, or false if images couldn't be stored.
     */
    public static boolean add(File srcFile) {
        return add(srcFile, null);
    }

    /**
     * Generates thumbnails for the given source image and stores them in the cache.
     * If thumbnails already existed for the given image, they will be overwritten.
     * Note that a thumbnail will be pregenerated at each supported thumbnail size.
     * Rather than throw an exception on I/O error, the method simply returns false.
     * Thumbnails are a fire-and-forget situation. All errors will be logged here.
     *
     * @param srcFile  The File from which srcImage was loaded.
     * @param srcImage If you have already loaded an image from srcFile, supply it here.
     * @return Returns true if all went well, or false if images couldn't be stored.
     */
    public static boolean add(File srcFile, BufferedImage srcImage) {
        for (AppConfig.ThumbSize size : AppConfig.ThumbSize.values()) {
            File thumbFile = generateThumbnailPath(srcFile, size.getDimensions());
            logger.log(Level.FINE, "Generating thumbnail for {0} in {1}",
                       new Object[]{srcFile.getAbsolutePath(), thumbFile.getAbsolutePath()});
            try {
                boolean cleanupRequired = false;
                if (srcImage == null) {
                    srcImage = ImageUtil.loadImage(srcFile);
                    cleanupRequired = true;
                }

                // If the image is still null, it's not a valid image file:
                if (srcImage == null) {
                    return false;
                }

                BufferedImage thumb = ImageUtil.generateThumbnail(srcImage, size.getDimensions(), size.getDimensions());
                thumbFile.getParentFile().mkdirs();
                ImageUtil.saveImage(thumb, thumbFile);
                thumb.flush();
                if (cleanupRequired) {
                    srcImage.flush();
                }
            }
            catch (IOException | ArrayIndexOutOfBoundsException ioe) {
                logger.log(Level.SEVERE, "Error adding thumbnail for " + srcFile.getAbsolutePath() + " to cache.", ioe);
                return false;
            }
        }
        return true;
    }

    /**
     * Removes all thumbnails for the given srcFile if any were present.
     *
     * @param srcFile The source image in question.
     */
    public static void remove(File srcFile) {
        for (AppConfig.ThumbSize size : AppConfig.ThumbSize.values()) {
            File thumbFile = generateThumbnailPath(srcFile, size.getDimensions());
            if (thumbFile.exists()) {
                FileUtils.deleteQuietly(thumbFile);
            }
        }
    }

    /**
     * Copies any associated cache entries for the given srcFile to the matching
     * location for destFile. If there were no cache entries for srcFile, do nothing.
     *
     * @param srcFile  The source file of the operation.
     * @param destFile The new location of the source file.
     */
    public static void copy(File srcFile, File destFile) {
        copy(srcFile, destFile, false);
    }

    /**
     * Moves any associated cache entries for the given srcFile to the matching
     * location for destFile. If there were no cache entries for srcFile, do nothing.
     *
     * @param srcFile  The source file of the operation.
     * @param destFile The new location of the source file.
     */
    public static void move(File srcFile, File destFile) {
        copy(srcFile, destFile, true);
    }

    /**
     * Invoked internally from copy(File,File) and move(File,File) to do the actual
     * file movement and optionally delete the source files depending on removeOriginal.
     *
     * @param srcFile        The source file.
     * @param destFile       The destination file.
     * @param removeOriginal True if we're moving, false if we're copying.
     */
    private static void copy(File srcFile, File destFile, boolean removeOriginal) {
        String action = removeOriginal ? "move" : "copy";
        for (AppConfig.ThumbSize size : AppConfig.ThumbSize.values()) {
            File srcThumb = generateThumbnailPath(srcFile, size.getDimensions());
            if (srcThumb.exists()) {
                File destThumb = generateThumbnailPath(destFile, size.getDimensions());
                destThumb.getParentFile().mkdirs();
                if (destThumb.exists()) {
                    FileUtils.deleteQuietly(destThumb);
                }
                try {
                    if (removeOriginal) {
                        FileUtils.moveFile(srcThumb, destThumb);
                    }
                    else {
                        FileUtils.copyFile(srcThumb, destThumb);
                    }
                }
                catch (IOException ioe) {
                    logger.log(Level.SEVERE, "Unable to " + action + " associated image thumbnail.", ioe);
                    if (removeOriginal) {
                        FileUtils.deleteQuietly(srcThumb);
                    }
                }
            }
        }

    }

    /**
     * Copies all cache entries for the given source directory to the matching location
     * for the given dest directory. If there were no cache entries for srcDir, do nothing.
     *
     * @param srcDir  The source directory in question.
     * @param destDir The new location for srcDir.
     */
    public static void copyDirectory(File srcDir, File destDir) {
        File thumbSrcDir = new File(CACHE_DIR.getAbsolutePath() + srcDir.getAbsolutePath());
        if (thumbSrcDir.exists()) {
            File thumbDestDir = new File(CACHE_DIR.getAbsolutePath() + destDir.getAbsolutePath());
            thumbDestDir.getParentFile().mkdirs();
            try {
                FileUtils.copyDirectory(thumbSrcDir, thumbDestDir);
            }
            catch (IOException ioe) {
                logger.log(Level.SEVERE, "Unable to copy associated thumbnail directory.", ioe);
            }
        }
    }

    /**
     * Moves all cache entries for the given source directory to the matching location
     * for the given dest directory. If there were no cache entries for srcDir, do nothing.
     *
     * @param srcDir  The source directory in question.
     * @param destDir The new location for srcDir.
     */
    public static void moveDirectory(File srcDir, File destDir) {
        File thumbSrcDir = new File(CACHE_DIR.getAbsolutePath() + srcDir.getAbsolutePath());
        if (thumbSrcDir.exists()) {
            File thumbDestDir = new File(CACHE_DIR.getAbsolutePath() + destDir.getAbsolutePath());
            thumbDestDir.getParentFile().mkdirs();
            try {
                FileUtils.moveDirectory(thumbSrcDir, thumbDestDir);
            }
            catch (IOException ioe) {
                logger.log(Level.SEVERE, "Unable to relocate associated thumbnail directory.", ioe);
            }
        }
    }

    /**
     * Removes all cache contents.
     */
    public static void clear() {
        try {
            FileUtils.deleteDirectory(CACHE_DIR);
            CACHE_DIR.mkdirs();
        }
        catch (IOException ioe) {
            logger.log(Level.SEVERE, "Unable to clear cache.", ioe);
        }
    }

    /**
     * For a given file, generate A File reference which represents the full path
     * of the equivalent thumbnail file in the cache directory. For example, given
     * a source file of /some/path/image.bmp, and a size of 50, this method would return
     * CACHE_DIR/some/path/image_50.jpg. Note that all thumbnails
     * will have the ".jpg" extension regardless of source file extension.
     *
     * @param srcFile The source file on the file system somewhere.
     * @param size    The desired thumbnail size.
     * @return A File object representing the equivalent thumbnail for the given srcFile and size.
     */
    private static File generateThumbnailPath(File srcFile, int size) {
        return new File(CACHE_DIR.getAbsolutePath() + srcFile.getParentFile().getAbsolutePath(),
                        ThumbCacheManager.generateThumbnailFilename(srcFile, size));
    }

    /**
     * For a given file, generate a filename suitable for thumbnail representation.
     * For example, given a file with name "someImage.jpeg", and given a thumbnail
     * size of 150, this image will return "someImage.jpeg_150.jpg".
     * Yes, we keep the entire original filename including extension, and append the
     * size and an unconditional ".jpg" extension on top of that. This is to avoid the case
     * where you have "someImage.jpeg" and "someImage.png" in the same directory, in which
     * case their thumbnails would conflict if we stripped the original extension.
     *
     * @param file The source image file.
     * @param size The size string to embed in the filename.
     * @return A filename suitable for thumbnail representation at the given size.
     */
    private static String generateThumbnailFilename(File file, int size) {
        return file.getName() + "_" + size + ".jpg";

        // The old flawed approach:
        //String originalName = file.getName();
        //if (!originalName.contains(".")) {
        //  return originalName + "_" + size + ".jpg";
        //}
        //String newName = originalName.substring(0, originalName.lastIndexOf("."));
        //return newName + "_" + size + ".jpg";
    }

}
