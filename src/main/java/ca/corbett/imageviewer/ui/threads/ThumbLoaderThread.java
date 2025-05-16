package ca.corbett.imageviewer.ui.threads;

import ca.corbett.extras.image.ImageUtil;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ThumbContainerPanel;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A thread to load thumbnails for the given image file list and render them into
 * the given JPanel.
 *
 * @author scorbo2
 * @since 2017-11-11
 */
public final class ThumbLoaderThread implements Runnable {

    private static final Logger logger = Logger.getLogger(ThumbLoaderThread.class.getName());

    private final List<File> fileList;
    private final ThumbContainerPanel thumbContainerPanel;
    private final ProgressMonitor monitor;
    private final int offset;
    private int pageSize;

    /**
     * To construct, you must supply a List of File objects (assumed to be image files)
     * and a JPanel into which to render the results. You must also supply an offset and
     * a page size, indicating where to start loading in the list of files, and how many to
     * load. If the list continues past index offset+pageSize, a "load more" control will
     * be shown at the end of the list. Before invoking run() you can optionally also
     * call setThumbSize() to set the thumbnail width and height (defaults to 80x80).
     * Note: the thumbnail page size, thumbnail width, and thumbnail height are taken from
     * the current preferences and so do not need to be explicitly supplied.
     *
     * @param list   A List of File objects which will be loaded.
     * @param panel  A JPanel into which the results will be rendered.
     * @param offset An offset into the file list stating where to start loading.
     */
    public ThumbLoaderThread(List<File> list, ThumbContainerPanel panel, int offset) {
        fileList = list;
        thumbContainerPanel = panel;
        this.offset = offset;
        pageSize = AppConfig.getInstance().getThumbnailPageSize();

        monitor = new ProgressMonitor(MainWindow.getInstance(),
                                      "Loading...", "Please wait", offset, offset + pageSize);
        monitor.setMillisToDecideToPopup(100);
        monitor.setMillisToPopup(200);
    }

    /**
     * You can override the preference for thumb page size by invoking this before
     * starting the thread. This is useful if for example you wish to load all remaining
     * images instead of just the next page worth.
     *
     * @param size The number of images to load in the next batch - overrides thumb page size.
     */
    public void setCustomPageSize(int size) {
        pageSize = size;
        monitor.setMaximum(offset + pageSize);
    }

    /**
     * Executes the thread with current parameters.
     */
    @Override
    public void run() {
        // If the offset is past the end of the list, there's nothing for us to do:
        if (offset >= fileList.size()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    thumbContainerPanel.notifyLoadFinished();
                    thumbContainerPanel.addAlienControl(); // show or hide as needed
                    thumbContainerPanel.revalidate();
                }

            });
            return;
        }

        // If the pageSize is invalid, we're done:
        if (pageSize <= 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    thumbContainerPanel.notifyLoadFinished();
                    thumbContainerPanel.addAlienControl(); // show or hide as needed
                    thumbContainerPanel.revalidate();
                }

            });
            return;
        }

        // Generate a list of ThumbPanel instances:
        int i = offset;
        int newOffset = offset;
        int limit = offset + pageSize;
        if (limit >= fileList.size()) {
            limit = fileList.size();
        }
        for (; i < limit; i++) {
            final File file = fileList.get(i);
            BufferedImage thumbImage = null;
            try {
                if (monitor.isCanceled()) {
                    break;
                }

                // track how many we've successfully loaded as we have to hand this to the container:
                newOffset++;

                // Attempt to load the image:
                String note = "Loading image " + (i + 1) + " of " + limit;
                monitor.setNote(note);
                monitor.setProgress(i);
                int thumbSize = AppConfig.getInstance().getThumbnailSize();

                // Give extensions a chance to return a thumbnail for this image:
                thumbImage = ImageViewerExtensionManager.getInstance().getThumbnail(file, thumbSize);

                // Generate if nothing came back:
                if (thumbImage == null) {
                    BufferedImage srcImage = ImageUtil.loadImage(file);
                    if (srcImage != null) {
                        thumbImage = ImageUtil.generateThumbnail(srcImage, thumbSize, thumbSize);
                    }
                    else {
                        logger.log(Level.WARNING, "ThumbLoaderThread: encountered null image from file: {0}",
                                   file.getName());
                    }
                }
            }
            catch (ArrayIndexOutOfBoundsException oobe) {
                logger.log(Level.WARNING, "ThumbLoaderThread: skipped malformed file: {0}", file.getName());
            }
            catch (IOException ioe) {
                logger.log(Level.SEVERE, "ThumbLoaderThread: IOException on file: " + file.getName(), ioe);
            }

            // Update the container panel with this image (may be null if image didn't load...
            //   that's okay as ThumbPanel will handle this with a default "unknown" icon)
            final BufferedImage thumb = thumbImage;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    thumbContainerPanel.addThumb(file, thumb);
                    thumbContainerPanel.revalidate();
                }

            });
        }

        final int finalOffset = newOffset; // might not be == i if some load error(s) occurred
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                thumbContainerPanel.notifyLoadFinished();
                thumbContainerPanel.setLoadOffset(finalOffset);
                thumbContainerPanel.addLoadMoreControl(); // will only show if necessary.
                thumbContainerPanel.addAlienControl(); // will only show if necessary.
                thumbContainerPanel.revalidate();
            }

        });

        monitor.close();

    }

}
