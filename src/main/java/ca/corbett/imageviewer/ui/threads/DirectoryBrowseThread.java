package ca.corbett.imageviewer.ui.threads;

import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.progress.SimpleProgressWorker;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;

import javax.swing.SwingUtilities;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A worker thread that will scan the given directory non-recursively.
 * There are two goals for the search:
 * <ol>
 *     <li>Find all image files in the given directory.</li>
 *     <li>Find all alien files in the given directory.</li>
 * </ol>
 * <p>
 * This thread fires a progress event on each file found, so it can
 * be wired up to a MultiProgressDialog. Unfortunately, we can't
 * enumerate the number of files in advance, so the progress bar
 * will be an indeterminate one. A proper fix for this is waiting on
 * <a href="https://github.com/scorbo2/swing-extras/issues/266">issue 266</a>
 * in the swing-extras library.
 * </p>
 * <p>
 * Upon successful completion, the supplied Callback will be notified
 * of whatever results we have.
 * </p>
 * <p>
 * The search can be canceled by the user via the progress dialog.
 * The search can also be canceled by the caller via the stop() method.
 * If the search is stopped early for any reason, the Callback will NOT be notified
 * of the search results.
 * </p>
 * <p>
 *     <b>NOTE!</b> The Callback will be invoked on the EDT. It is safe
 *     to perform UI updates from the Callback.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 3.0
 */
public class DirectoryBrowseThread extends SimpleProgressWorker {

    @FunctionalInterface
    public interface Callback {
        void onBrowseComplete(DirectoryBrowseThread source, List<File> images, List<File> aliens);
    }

    private final ImageViewerExtensionManager extManager;
    private final File directory;
    private final List<File> images;
    private final List<File> aliens;
    private final Callback callback;
    private volatile boolean isCanceled;

    /**
     * Creates a DirectoryBrowseThread for the given directory.
     * The supplied directory must be a valid existing directory.
     * The supplied ownerPanel will be notified of our search results.
     *
     * @param directory the directory to scan for image files
     * @param callback  the callback to be notified of search results upon completion
     */
    public DirectoryBrowseThread(File directory, Callback callback) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("Directory must be a valid existing directory");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        this.extManager = ImageViewerExtensionManager.getInstance();
        this.directory = directory;
        this.callback = callback;
        this.images = new ArrayList<>();
        this.aliens = new ArrayList<>();
        this.isCanceled = false;
    }

    @Override
    public void run() {
        // Fire progress begins with a dummy step count, just to get the dialog up.
        fireProgressBegins(1);
        FileSystemUtil.findFiles(directory, false, this::fileFound);
        fireProgressComplete(); // close the progress dialog

        // Only notify the Callback if we weren't canceled:
        if (!isCanceled) {
            // Sort while we're still on the worker thread:
            FileSystemUtil.sortFiles(images);
            FileSystemUtil.sortFiles(aliens);

            // Now notify our Callback on the EDT:
            SwingUtilities.invokeLater(() -> callback.onBrowseComplete(this, images, aliens));
        }
    }

    /**
     * Invoke this to abort a search in progress. The callback will be invoked with results so far.
     */
    public void stop() {
        isCanceled = true;
    }

    /**
     * Invoked as the search progresses, once for each file found.
     * Here we'll filter the raw list into images and aliens.
     * If the user cancels via the progress dialog, or if the caller
     * cancels via our stop() method, the search will stop immediately
     * and report results so far.
     *
     * @param file the most recently found File during the search.
     * @return true to continue with the search, false to abort it and report results so far.
     */
    private boolean fileFound(File file) {
        // I want to show a proper progress dialog, but FileSystemUtil
        // doesn't have a mechanism to discover the size of the file
        // list in advance. So, we end up just showing an indeterminate
        // progress dialog. Better than nothing, I guess.
        if (!fireProgressUpdate(0, file.getName()) || isCanceled) {
            // User or caller canceled! We're done here.
            fireProgressCanceled();
            return false;
        }

        // Otherwise, continue processing:
        if (ImageUtil.isImageFile(file)) {
            images.add(file);
        }

        // If it's not an image file, and also not a "known" or "companion" file, then it's an alien:
        else if (!extManager.isKnownFile(file) && !extManager.isCompanionFile(file)) {
            aliens.add(file);
        }

        // "known" and "companion" files are purposefully ignored here.
        // they are typically handled by application extensions.

        // Return true to continue with the search:
        return true;
    }
}
