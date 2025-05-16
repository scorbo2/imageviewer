package ca.corbett.imageviewer.ui;

import org.apache.commons.io.FileUtils;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wraps all the stuff that represents a single displayed image.
 * Namely, out of necessity, we have to differentiate between regular images
 * and animated GIFs, which are represented by different classes.
 * But this class also contains convenient methods for getting the
 * image dimensions, file size, and file date in a handy way.
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public class ImageInstance {

    private final File imageFile;
    private final BufferedImage regularImage;
    private final ImageIcon gifImage;

    /**
     * Package access constructor as this will generally only be created by MainWindow.
     * Generally, only one of image or gifImage should/will be present.
     *
     * @param srcFile  The File from which the image was loaded.
     * @param image    Optionally, a BufferedImage containing a static image.
     * @param gifImage Optionally, an ImageIcon containing an animated GIF image.
     */
    ImageInstance(File srcFile, BufferedImage image, ImageIcon gifImage) {
        this.imageFile = srcFile;
        this.regularImage = image;
        this.gifImage = gifImage;
    }

    /**
     * If no image was supplied to the constructor, the image is considered empty.
     *
     * @return True if there's no image here.
     */
    public boolean isEmpty() {
        return regularImage == null && gifImage == null;
    }

    /**
     * Reports whether a regular static image is contained here.
     *
     * @return True if this is a static image and not an animated gif.
     */
    public boolean isRegularImage() {
        return regularImage != null;
    }

    /**
     * Reports whether an animated gif is contained here.
     *
     * @return True if this is an animated gif and not a static image.
     */
    public boolean isAnimatedGIF() {
        return gifImage != null;
    }

    /**
     * Returns the File from which this image was loaded. May be null.
     *
     * @return A File object, or null if no image is here.
     */
    public File getImageFile() {
        return imageFile;
    }

    /**
     * Returns the static image contained here, or null if there isn't one.
     *
     * @return A BufferedImage instance, or null.
     */
    public BufferedImage getRegularImage() {
        return regularImage;
    }

    /**
     * Returns the ImageIcon containing the animated gif contained here, or null if there isn't one.
     *
     * @return An ImageIcon instance, or null.
     */
    public ImageIcon getGifImage() {
        return gifImage;
    }

    /**
     * Will interrogate either the static image or the animated gif contained here, depending
     * on which one is set, and return its width.
     *
     * @return The width of the contained image, or 0 if there isn't one.
     */
    public int getImageWidth() {
        if (isRegularImage()) {
            return regularImage.getWidth();
        }
        else if (isAnimatedGIF()) {
            return gifImage.getIconWidth();
        }
        return 0;
    }

    /**
     * Will interrogate either the static image or the animated gif contained here, depending
     * on which one is set, and return its height.
     *
     * @return The height of the contained image, or 0 if there isn't one.
     */
    public int getImageHeight() {
        if (isRegularImage()) {
            return regularImage.getHeight();
        }
        else if (isAnimatedGIF()) {
            return gifImage.getIconHeight();
        }
        return 0;
    }

    /**
     * If an image is contained here, this will return the size of the File from whence it came.
     *
     * @return A file size, or 0 if there is no image here.
     */
    public long getFileSize() {
        return imageFile != null ? imageFile.length() : 0;
    }

    /**
     * Returns the image file size in a human-readable String form, or "n/a" if there
     * is no image here.
     *
     * @return A human-readable representation of the file size, complete with units.
     */
    public String getFileSizePrintable() {
        return imageFile == null ? "n/a" : FileUtils.byteCountToDisplaySize(imageFile.length());
    }

    /**
     * Returns the date of the image file in a human-readable string form, based on
     * the supplied SimpleDateFormat.
     *
     * @param format Any instance of SimpleDateFormat.
     * @return The formatted date of the image file, or "n/a" if no image is contained here.
     */
    public String getFileDatePrintable(SimpleDateFormat format) {
        return imageFile == null ? "n/a" : format.format(new Date(imageFile.lastModified()));
    }

    /**
     * Returns the filename (without path) of the file from which this image was loaded.
     *
     * @return A file name, or "n/a" if no image is contained here.
     */
    public String getImageFileName() {
        return getImageFileName(0);
    }

    /**
     * Will return the image file name (without path) up to the specified lengthLimit.
     * If the file name is longer than the length limit, it will be truncated and
     * the string "..." will be appended.
     *
     * @param lengthLimit How many characters to allow in the return (0 for no limit)
     * @return The file name, truncated as directed, or "" if no image is contained here.
     */
    public String getImageFileName(int lengthLimit) {
        String filename = "";
        if (imageFile != null) {
            filename = imageFile.getName();
        }

        // Truncate filename if requested:
        if (lengthLimit > 0) {
            if (filename.length() > lengthLimit) {
                filename = filename.substring(0, lengthLimit) + "...";
            }
        }
        return filename;
    }

}
