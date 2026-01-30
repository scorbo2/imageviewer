package ca.corbett.imageviewer.ui.threads;

import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.progress.MultiProgressWorker;
import ca.corbett.imageviewer.AppConfig;
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
public class ThumbCachePregenerateThread extends MultiProgressWorker {

    private static final Logger log = Logger.getLogger(ThumbCachePregenerateThread.class.getName());

    private final File rootDir;

    public ThumbCachePregenerateThread(File dir) {
        this.rootDir = dir;
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

        boolean wasCanceled = false;
        fireProgressBegins(alldirs.size());
        try {
            int majorProgress = 0;

            for (File dir : alldirs) {
                List<File> images = FileSystemUtil.findFiles(dir, false, ThumbContainerPanel.imageExtensions);
                if (!fireMajorProgressUpdate(majorProgress, images.size(), dir.getAbsolutePath())) {
                    wasCanceled = true;
                    break;
                }
                int minorProgress = 0;
                for (File image : images) {
                    if (!fireMinorProgressUpdate(majorProgress, minorProgress, image.getName())) {
                        wasCanceled = true;
                        break;
                    }
                    ThumbCacheManager.add(image);
                    minorProgress++;
                }
                majorProgress++;
            }

        }
        finally {
            // Ensure progress dialog is closed no matter what happens above
            if (wasCanceled) {
                fireProgressCanceled();
            }
            else {
                fireProgressComplete();
            }
        }
    }
}
