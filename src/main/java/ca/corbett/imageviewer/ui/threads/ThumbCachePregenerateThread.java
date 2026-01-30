package ca.corbett.imageviewer.ui.threads;

import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ThumbCacheManager;
import ca.corbett.imageviewer.ui.ThumbContainerPanel;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * A worker thread to go through a directory recursively, automatically generating
 * thumbnails for all images found, and adding them to the ThumbnailCache.
 * <P>
 *     This thread will immediately exit if thumbnail caching is disabled in preferences.
 *     But, the action to launch this thread is hidden if caching is disabled,
 *     so it should not normally be possible to launch it in that case.
 * </P>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 1.1
 */
public class ThumbCachePregenerateThread implements Runnable {

    private static final Logger log = Logger.getLogger(ThumbCachePregenerateThread.class.getName());

    private final File rootDir;
    private final MultiProgressDialog progress;

    public ThumbCachePregenerateThread(File dir) {
        this.rootDir = dir;
        progress = new MultiProgressDialog(MainWindow.getInstance(), "Pregenerating thumbnails...");
    }

    @Override
    public void run() {
        // If caching is disabled in preferences, just exit:
        if (!AppConfig.getInstance().isThumbCacheEnabled()) {
            log.warning("Ignoring request to pregenerate thumbnails: thumbnail caching is disabled in preferences.");
            return;
        }

        List<File> alldirs = FileSystemUtil.findSubdirectories(rootDir, true);
        alldirs.add(0, rootDir); // above list does not include the root itself
        progress.setMajorProgressBounds(0, alldirs.size());
        progress.setInitialShowDelayMS(500); // Don't show for very fast operations
        try {
            progress.setVisible(true);
            int majorProgress = 1; // 1-based progress looks better

            for (File dir : alldirs) {
                if (progress.isCanceled()) {
                    break;
                }
                progress.setMajorProgress(majorProgress, dir.getAbsolutePath());
                List<File> images = FileSystemUtil.findFiles(dir, false, ThumbContainerPanel.imageExtensions);
                progress.setMinorProgressBounds(0, images.size());
                int minorProgress = 1; // 1-based progress looks better
                for (File image : images) {
                    if (progress.isCanceled()) {
                        break;
                    }
                    progress.setMinorProgress(minorProgress, image.getName());
                    ThumbCacheManager.add(image);
                    minorProgress++;
                }
                majorProgress++;
            }

        }
        finally {
            // Ensure progress dialog is closed no matter what happens above
            progress.setVisible(false);
            progress.dispose();
        }
    }
}
