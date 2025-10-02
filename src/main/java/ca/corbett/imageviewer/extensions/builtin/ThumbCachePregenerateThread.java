package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ThumbContainerPanel;

import java.io.File;
import java.util.List;

/**
 * A worker thread to go through a directory recursively, automatically generating
 * thumbnails for all images found, and adding them to the ThumbnailCache.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 1.1 (moved to ThumbCache extension for ImageViewer 2.0)
 */
class ThumbCachePregenerateThread implements Runnable {

    private final File rootDir;
    private final MultiProgressDialog progress;

    public ThumbCachePregenerateThread(File dir) {
        this.rootDir = dir;
        progress = new MultiProgressDialog(MainWindow.getInstance(), "Pregenerating thumbnails...");
    }

    @Override
    public void run() {
        List<File> alldirs = FileSystemUtil.findSubdirectories(rootDir, true);
        alldirs.add(0, rootDir); // above list does not include the root itself
        progress.setMajorProgressBounds(0, alldirs.size());
        progress.setVisible(true);
        int majorProgress = 0;

        for (File dir : alldirs) {
            if (progress.isCanceled()) {
                break;
            }
            progress.setMajorProgress(majorProgress, dir.getAbsolutePath());
            List<File> images = FileSystemUtil.findFiles(dir, false, ThumbContainerPanel.imageExtensions);
            progress.setMinorProgressBounds(0, images.size());
            int minorProgress = 0;
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

        progress.setVisible(false);
        progress.dispose();
    }

}
