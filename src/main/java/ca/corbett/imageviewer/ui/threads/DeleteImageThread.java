package ca.corbett.imageviewer.ui.threads;

import ca.corbett.extras.io.FileSearchListener;
import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.ImageOperationHandler;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import org.apache.commons.io.FileUtils;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A worker thread to delete either a list of images, or an entire directory.
 *
 * @author scorbo2
 * @since 2017-11-25
 */
public final class DeleteImageThread implements Runnable {

    private final static Logger logger = Logger.getLogger(DeleteImageThread.class.getName());
    private final List<File> fileList;
    private final File directory;
    private ProgressMonitor monitor;

    /**
     * Creates a new thread for deleting the specified list of files.
     *
     * @param list The list of files to be deleted.
     */
    public DeleteImageThread(List<File> list) {
        fileList = list;
        directory = null;
        initialize();
    }

    /**
     * Creates a new thread for deleting the specified directory.
     *
     * @param dir The directory to be deleted.
     */
    public DeleteImageThread(File dir) {
        fileList = null;
        directory = dir;
        initialize();
    }

    /**
     * Invoked internally to initialize the worker thread.
     */
    private void initialize() {
        int min = 0;
        int max = 100;
        if (fileList != null && !fileList.isEmpty()) {
            max = fileList.size();
        }
        monitor = new ProgressMonitor(MainWindow.getInstance(),
                                      "Deleting...", "Please wait", min, max);
        monitor.setMillisToDecideToPopup(200);
        monitor.setMillisToPopup(200);
    }

    @Override
    public void run() {
        if (directory != null) {
            monitor.setNote("Deleting " + directory.getName());
            int currentProgress = 0;
            boolean okay = true;
            monitor.setMaximum(1000); // a completely unfounded guess in the absence of "indeterminate".
            try {
                FileSearchListener listener = new FileSearchListener() {
                    private int currentProgress = 0;

                    @Override
                    public boolean fileFound(File file) {
                        monitor.setNote("Evaluating: " + file.getName());
                        monitor.setProgress(currentProgress++); // this is ugly but whatever
                        return !monitor.isCanceled();
                    }

                };

                List<File> filesToDelete = FileSystemUtil.findFiles(directory, true, listener);
                if (!monitor.isCanceled()) {
                    List<File> subdirs = FileSystemUtil.findSubdirectories(directory, true, listener);
                    monitor.setMaximum(filesToDelete.size() + subdirs.size());

                    for (File file : filesToDelete) {
                        if (monitor.isCanceled()) {
                            okay = false;
                            break;
                        }

                        long startTime = System.currentTimeMillis();
                        ImageViewerExtensionManager.getInstance()
                                                   .preImageOperation(ImageOperation.Type.DELETE, file, null);
                        long elapsedTimeLog = System.currentTimeMillis() - startTime;
                        startTime = System.currentTimeMillis();

                        // Make note of any companion files that our extensions want to accompany this image file:
                        List<File> companions = ImageViewerExtensionManager.getInstance().getCompanionFiles(file);

                        logger.log(Level.INFO, "deleteImage: {0}", file.getAbsolutePath());
                        okay = okay && file.delete();
                        if (okay) {
                            // Note: extensions may do stuff like delete companion files, which may confuse us
                            // when we go to delete a file that has already been deleted by an extension.
                            // It's okay, as the call to deleteDirectory() down below ensures that we
                            // leave behind no stragglers.
                            ImageViewerExtensionManager.getInstance()
                                                       .postImageOperation(ImageOperation.Type.DELETE, file);
                        }

                        for (File f : companions) {
                            logger.log(Level.INFO, "deleteImage (companion): {0}", file.getAbsolutePath());
                            f.delete();
                        }

                        long elapsedTimeDelete = System.currentTimeMillis() - startTime;
                        currentProgress++;
                        monitor.setProgress(currentProgress);
                        monitor.setNote("Deleting " + file.getName());
                        logger.log(Level.FINE, "Deletion of {0}: {1}ms for logging, {2}ms for deletion",
                                   new Object[]{file.getAbsolutePath(), elapsedTimeLog, elapsedTimeDelete});
                    }

                    Collections.reverse(subdirs); // we want them deepest first
                    for (File dir : subdirs) {
                        if (monitor.isCanceled()) {
                            okay = false;
                            break;
                        }

                        logger.log(Level.INFO, "deleteDirectory: {0}", dir.getAbsolutePath());
                        okay = okay && dir.delete();
                        currentProgress++;
                        monitor.setProgress(currentProgress);
                        monitor.setNote("Deleting " + dir.getName());
                    }

                    if (!monitor.isCanceled()) {
                        logger.log(Level.INFO, "deleteDirectory: {0}", directory.getAbsolutePath());
                        FileUtils.deleteDirectory(directory);
                    }
                    else {
                        okay = false;
                    }
                }
                else {
                    okay = false;
                }
            }
            catch (IOException ioe) {
                logger.log(Level.SEVERE, "DeleteImageThread: problem deleting directory.", ioe);
                okay = false;
            }
            final boolean allDeletedOkay = okay;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ImageOperationHandler.deleteDirectoryCallback(allDeletedOkay);
                }

            });
        }

        else if (!fileList.isEmpty()) {
            int i = 1;
            boolean okay = true;
            for (File file : fileList) {
                if (monitor.isCanceled()) {
                    okay = false;
                    break;
                }
                long startTime = System.currentTimeMillis();
                ImageViewerExtensionManager.getInstance().preImageOperation(ImageOperation.Type.DELETE, file, null);
                long elapsedTimeLog = System.currentTimeMillis() - startTime;
                startTime = System.currentTimeMillis();
                logger.log(Level.INFO, "deleteImage: {0}", file.getAbsolutePath());
                okay = okay && file.delete();
                if (okay) {
                    ImageViewerExtensionManager.getInstance().postImageOperation(ImageOperation.Type.DELETE, file);
                }
                long elapsedTimeDelete = System.currentTimeMillis() - startTime;
                monitor.setProgress(i++);
                monitor.setNote("Deleting " + file.getName());
                logger.log(Level.FINE, "Deletion of {0}: {1}ms for logging, {2}ms for deletion",
                           new Object[]{file.getAbsolutePath(), elapsedTimeLog, elapsedTimeDelete});
            }
            final boolean allDeletedOkay = okay;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ImageOperationHandler.deleteAllImagesCallback(allDeletedOkay);
                }

            });
        }

        monitor.close();
    }

}
