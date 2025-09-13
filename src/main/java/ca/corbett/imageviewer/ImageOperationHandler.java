package ca.corbett.imageviewer;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.ImageOperationAction;
import ca.corbett.imageviewer.ui.dialogs.NameConflictDialog;
import ca.corbett.imageviewer.ui.threads.DeleteImageThread;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles image operations such as moves, copies, symlinks, and deletes.
 *
 * @author scorbo2
 * @since ImageViewer 2.0 (all this used to live in MainWindow)
 */
public final class ImageOperationHandler {

    private static final Logger logger = Logger.getLogger(ImageOperationHandler.class.getName());
    private static MessageUtil messageUtil;
    private static LastImageOperation lastOperation;

    private ImageOperationHandler() {

    }

    /**
     * Repeats the last action, whatever it was, to whatever the last destination
     * directory was. This will show an error if there was no last action.
     * This will also fail if nothing is selected. If there is something selected,
     * it will be copied, moved, or symlinked to the last destination directory,
     * depending on whatever the last action was.
     */
    public static void repeatLastOperation() {
        if (lastOperation == null) {
            getMessageUtil().error("No previous operation to repeat.");
            return;
        }
        File lastDestination = lastOperation.getDestination();
        if (!lastDestination.exists() || !lastDestination.isDirectory()) {
            getMessageUtil().error("Previous destination \""
                                           + lastDestination.getAbsolutePath()
                                           + "\" does not exist or is not a directory");
            return;
        }
        ImageInstance imageInstance = MainWindow.getInstance().getSelectedImage();
        if (imageInstance.isEmpty()) {
            getMessageUtil().error("No image selected.");
            return;
        }

        // Whatever it was, play it again:
        new ImageOperationAction("Repeat last operation", lastOperation).actionPerformed(null);
    }

    /**
     * Undoes the last action, whatever it was, if the action was undoable.
     * For move operations, this will move the image file(s) back to where they started.
     * For copy or symlink operations, this will delete the copies or symlinks that were created.
     * Delete operations cannot be undone, so an error will be shown.
     * Note that the undo is not guaranteed to succeed! If an image is moved by this application
     * but then deleted from its new location by some other application, then obviously
     * we can't conjure it back into existence. Also, the user has the option of renaming
     * file during operations to avoid name clashes, so the result of this might get ugly.
     * A best attempt will be made to complete the undo.
     */
    public static void undoLastOperation() {
        // Make sure we can proceed:
        if (lastOperation == null) {
            getMessageUtil().error("No previous operation to undo.");
            return;
        }
        if (!lastOperation.isUndoable()) {
            getMessageUtil().error("The previous operation cannot be undone.");
            return;
        }
        if (lastOperation.getPayload() == ImageOperation.Payload.DIRECTORY) {
            undoDirectoryOperation();
        }
        else {
            undoFileOperation();
        }
    }

    /**
     * Internal method invoked by the various move/copy/link methods to handle a single image file
     * to the given destination directory. This method handles validation of the source
     * and destination to ensure the operation makes sense, and will also use the
     * NameConflictDialog if needed to handle naming conflicts.
     *
     * @param srcFile   The file in question.
     * @param destDir   The destination directory.
     * @param batchMode True if this is part of a batch move operation, false otherwise.
     * @param operation Specifies what to do: move the image, copy it, or symlink it.
     * @return Result.SUCCESS if all is well, CANCEL or CANCEL_ALL on error or cancel.
     */
    private static NameConflictDialog.Result handleSingleFileOperation(File srcFile, File destDir, boolean batchMode, ImageOperation.Type operation) {
        String opName = "moveSingleFile";
        switch (operation) {
            case COPY:
                opName = "copySingleFile";
                break;
            case SYMLINK:
                opName = "linkSingleFile";
                break;
        }

        // Sanity check - make sure the source file exists:
        if (srcFile == null || !srcFile.exists()) {
            getMessageUtil().info("No image selected", opName + ": Nothing selected.");
            return NameConflictDialog.Result.CANCEL;
        }

        // Sanity check - make sure the destination exists:
        if (destDir == null || !destDir.exists() || !destDir.isDirectory()) {
            getMessageUtil().error("Move error", opName + ": Invalid destination.");
            return NameConflictDialog.Result.CANCEL_ALL;
        }

        // Another sanity check:
        if (destDir.equals(srcFile.getParentFile())) {
            getMessageUtil().error("Move error", opName + ": Source and destination directories are the same.");
            return NameConflictDialog.Result.CANCEL_ALL;
        }

        // Check for name conflicts:
        File destFile = new File(destDir, srcFile.getName());
        if (destFile.exists()) {
            logger.log(Level.INFO, "{0}: name conflict!", opName);
            NameConflictDialog dialog = NameConflictDialog.getInstance();
            dialog.setConflict(srcFile, destDir, batchMode);
            dialog.setVisible(true);
            switch (dialog.getResult()) {
                case RENAME:
                    logger.log(Level.INFO, "{0}: name conflict resolved by user input.", opName);
                    destFile = dialog.getDestFile();
                    break;
                case CANCEL:
                    logger.log(Level.INFO, "{0}: canceled.", opName);
                    return NameConflictDialog.Result.CANCEL; // do nothing
                case CANCEL_ALL:
                    logger.log(Level.INFO, "{0}: canceled all.", opName);
                    return NameConflictDialog.Result.CANCEL_ALL; // do nothing
                default:
                    break;
            }
        }

        // Make sure destination is writable:
        if ((destFile.exists() && !destFile.canWrite()) || !destDir.canWrite()) {
            getMessageUtil().error("Permissions error", opName + ": can't write to destination.");
            return NameConflictDialog.Result.CANCEL_ALL;
        }

        // Make a note of the destination file for undo purposes (note that we do this after
        // the user has had an opportunity to rename it above, so we capture the new name):
        if (lastOperation != null) {
            lastOperation.addCreatedFile(destFile);
        }

        // Make note of any companion files that our extensions want to accompany this image file:
        List<File> companions = ImageViewerExtensionManager.getInstance().getCompanionFiles(srcFile);

        try {
            // Make a note of the date/time on this file in case we're moving or copying it:
            BasicFileAttributes view = Files.getFileAttributeView(srcFile.toPath(), BasicFileAttributeView.class)
                                            .readAttributes();
            FileTime srcFileCreationTime = view.creationTime();

            // If the destination file exists, nuke it (user had a chance to deal with this above):
            if (destFile.exists()) {
                logger.log(Level.INFO, "{0}: dest file exists; deleting.", opName);
                destFile.delete();
            }
            logger.log(Level.INFO, "{0}: {1} -> {2}",
                       new Object[]{opName, srcFile.getAbsolutePath(), destFile.getAbsolutePath()});
            for (File f : companions) {
                logger.log(Level.INFO, "{0} (companion): {1} -> {2}",
                           new Object[]{opName, f.getAbsolutePath(), new File(destDir, f.getName())});
            }

            // Notify extensions of what's about to happen:
            ImageViewerExtensionManager.getInstance().preImageOperation(operation, srcFile, destFile);

            switch (operation) {
                case MOVE: {
                    FileUtils.moveFile(srcFile, destFile);
                    for (File f : companions) {
                        FileUtils.moveFile(f, new File(destDir, f.getName()));
                    }
                }
                break;

                case COPY: {
                    FileUtils.copyFile(srcFile, destFile);
                    for (File f : companions) {
                        FileUtils.copyFile(f, new File(destDir, f.getName()));
                    }
                }
                break;

                case SYMLINK: {
                    Path target = FileSystems.getDefault().getPath(srcFile.getAbsolutePath());
                    Path link = FileSystems.getDefault().getPath(destFile.getAbsolutePath());
                    java.nio.file.Files.createSymbolicLink(link, target);

                    for (File f : companions) {
                        target = FileSystems.getDefault().getPath(f.getAbsolutePath());
                        link = FileSystems.getDefault().getPath(new File(destDir, f.getName()).getAbsolutePath());
                        java.nio.file.Files.createSymbolicLink(link, target);
                    }
                }
                break;
            }

            // Modify the target file to have the same creation time as the source file:
            if (AppConfig.getInstance().isPreserveDateTimeEnabled()
                    && (operation == ImageOperation.Type.MOVE || operation == ImageOperation.Type.COPY)) {
                Files.setLastModifiedTime(destFile.toPath(), srcFileCreationTime);
                for (File f : companions) {
                    Files.setLastModifiedTime(new File(destDir, f.getName()).toPath(), srcFileCreationTime);
                }
            }

            // Notify extensions of what just happened:
            ImageViewerExtensionManager.getInstance().postImageOperation(operation, destFile);
        }
        catch (IOException ex) {
            getMessageUtil().error("File transfer error", "Error transferring file, probably permissions related.", ex);
            return NameConflictDialog.Result.CANCEL;
        }

        return NameConflictDialog.Result.SUCCESS;
    }

    /**
     * Moves the current image (if any) to the specified directory. Name conflicts in the destination
     * will prompt an intelligent rename dialog.
     *
     * @param destDir The destination directory.
     */
    public static void moveImage(File destDir) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            getMessageUtil().info("No image selected", "Move image: Nothing selected.");
            return;
        }

        // Record this as the last operation even before we begin processing it.
        // This is debatable, but even if the operation fails or is canceled via the
        // name conflict dialog, I think it should still count as the last
        // thing you did (or tried to do):
        lastOperation = new LastImageOperation(ImageOperation.Type.MOVE,
                                               ImageOperation.Payload.SINGLE_IMAGE,
                                               destDir,
                                               MainWindow.getInstance().getCurrentDirectory());

        // Get the currently showing image:
        if (handleSingleFileOperation(currentImage.getImageFile(), destDir, false,
                                      ImageOperation.Type.MOVE) == NameConflictDialog.Result.SUCCESS) {
            MainWindow.getInstance().selectedImageRemoved();
        }
    }

    /**
     * Moves all images in the current directory (if any) to the specified directory. Name conflicts
     * will prompt an intelligent rename dialog.
     *
     * @param destination The destination directory.
     */
    public static void moveAllImages(File destination) {
        List<File> imageFiles = MainWindow.getInstance().getCurrentFileList();

        if (imageFiles.isEmpty()) {
            getMessageUtil().info("No images to move", "Move all images: No images to move.");
            return;
        }

        // Record this as the last operation even before we begin processing it.
        // This is debatable, but even if the operation fails or is canceled via the
        // name conflict dialog, I think it should still count as the last
        // thing you did (or tried to do):
        lastOperation = new LastImageOperation(ImageOperation.Type.MOVE,
                                               ImageOperation.Payload.ALL_IMAGES,
                                               destination,
                                               MainWindow.getInstance().getCurrentDirectory());

        for (File file : imageFiles) {
            NameConflictDialog.Result result = handleSingleFileOperation(file, destination, true,
                                                                         ImageOperation.Type.MOVE);
            if (result == NameConflictDialog.Result.CANCEL_ALL) {
                break;
            }
        }

        // Reload the thumbnails:
        MainWindow.getInstance().reloadCurrentDirectory();
    }

    /**
     * Moves the current directory to the specified destination. A rename dialog will pop to
     * offer an opportunity to rename the directory as part of the move.
     *
     * @param destination The destination directory.
     */
    public static void moveDirectory(File destination) {
        File srcDir = MainWindow.getInstance().getCurrentDirectory();
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            getMessageUtil().error("Move error", "Move directory: Source directory is null or nonexistent.");
            return;
        }
        File parentDir = srcDir.getParentFile();

        // Sanity check - make sure the destination exists:
        if (destination == null || !destination.exists() || !destination.isDirectory()) {
            getMessageUtil().error("Move error", "Move directory: Destination directory is null or nonexistent.");
            return;
        }

        // Another sanity check:
        if (destination.equals(srcDir)) {
            getMessageUtil().error("Move error", "Move directory: Source and destination directories are the same.");
            return;
        }

        File newDir = new File(destination, srcDir.getName());
        String msg = "Optionally rename this directory while moving:";
        do {
            String newName = JOptionPane.showInputDialog(MainWindow.getInstance(), msg, newDir.getName());

            if (newName == null) {
                logger.info("moveDirectory: canceled by user.");
                return; // canceled
            }

            newDir = new File(destination, newName);
            if (newDir.exists()) {
                msg = "That directory already exists. Rename:";
            }
        } while (newDir.exists());

        // Record this as the last operation.
        lastOperation = new LastImageOperation(ImageOperation.Type.MOVE, ImageOperation.Payload.DIRECTORY, newDir,
                                               parentDir);

        try {
            logger.log(Level.INFO, "moveDirectory: {0} -> {1}",
                       new Object[]{srcDir.getAbsolutePath(), newDir.getAbsolutePath()});
            FileUtils.moveDirectory(srcDir, newDir);
            ImageViewerExtensionManager.getInstance().directoryWasMoved(srcDir, newDir);
        }
        catch (IOException ex) {
            getMessageUtil().error("Move error", "Error moving directory.", ex);
            return;
        }

        // Change selection to the parent dir and remove the child (below call causes a DirTree reload):
        MainWindow.getInstance().setDirectory(parentDir);
    }

    /**
     * Copies the current image (if any) to the specified directory. Name conflicts in the destination
     * will prompt an intelligent rename dialog.
     *
     * @param destDir The destination directory.
     */
    public static void copyImage(File destDir) {
        // Get the currently showing image:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            getMessageUtil().info("No image selected", "Copy image: Nothing selected.");
            return;
        }

        // Record this as the last operation even before we begin processing it.
        // This is debatable, but even if the operation fails or is canceled via the
        // name conflict dialog, I think it should still count as the last
        // thing you did (or tried to do):
        lastOperation = new LastImageOperation(ImageOperation.Type.COPY,
                                               ImageOperation.Payload.SINGLE_IMAGE,
                                               destDir,
                                               MainWindow.getInstance().getCurrentDirectory());

        File srcFile = currentImage.getImageFile();
        if (handleSingleFileOperation(srcFile, destDir, false,
                                      ImageOperation.Type.COPY) == NameConflictDialog.Result.SUCCESS) {
            getMessageUtil().info("Image copied successfully.");
        }
    }

    /**
     * Copies all images in the current directory (if any) to the specified directory. Name conflicts
     * will prompt an intelligent rename dialog.
     *
     * @param destination The destination directory.
     */
    public static void copyAllImages(File destination) {
        List<File> imageFiles = MainWindow.getInstance().getCurrentFileList();

        if (imageFiles.isEmpty()) {
            getMessageUtil().info("No images to copy", "Copy all images: No images to copy.");
            return;
        }

        // Record this as the last operation even before we begin processing it.
        // This is debatable, but even if the operation fails or is canceled via the
        // name conflict dialog, I think it should still count as the last
        // thing you did (or tried to do):
        lastOperation = new LastImageOperation(ImageOperation.Type.COPY,
                                               ImageOperation.Payload.ALL_IMAGES,
                                               destination,
                                               MainWindow.getInstance().getCurrentDirectory());

        int filesCopied = 0;
        for (File file : imageFiles) {
            NameConflictDialog.Result result = handleSingleFileOperation(file, destination, true,
                                                                         ImageOperation.Type.COPY);
            if (result == NameConflictDialog.Result.CANCEL_ALL) {
                break;
            }
            else if (result != NameConflictDialog.Result.CANCEL) {
                filesCopied++;
            }
        }

        if (filesCopied > 0) {
            getMessageUtil().info("Copied " + filesCopied + " images to " + destination.getAbsolutePath());
        }
    }

    /**
     * Copies the current directory to the specified destination. A rename dialog will pop to
     * offer an opportunity to rename the directory as part of the copy.
     *
     * @param destination The destination directory.
     */
    public static void copyDirectory(File destination) {
        File srcDir = MainWindow.getInstance().getCurrentDirectory();
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            getMessageUtil().error("Copy error", "Copy directory: Source directory is null or nonexistent.");
            return;
        }
        File parentDir = srcDir.getParentFile();

        // Sanity check - make sure the destination exists:
        if (destination == null || !destination.exists() || !destination.isDirectory()) {
            getMessageUtil().error("Copy error", "Copy directory: Destination directory is null or nonexistent.");
            return;
        }

        // Another sanity check:
        if (destination.equals(srcDir)) {
            getMessageUtil().error("Copy error", "Copy directory: Source and destination directories are the same.");
            return;
        }

        File newDir = new File(destination, srcDir.getName());
        String msg = "Optionally rename this directory while copying:";
        do {
            String newName = JOptionPane.showInputDialog(MainWindow.getInstance(), msg, newDir.getName());

            if (newName == null) {
                logger.info("copyDirectory: canceled by user.");
                return; // canceled
            }

            newDir = new File(destination, newName);
            if (newDir.exists()) {
                msg = "That directory already exists. Rename:";
            }
        } while (newDir.exists());

        // Record this as the last operation.
        lastOperation = new LastImageOperation(ImageOperation.Type.COPY, ImageOperation.Payload.DIRECTORY, newDir,
                                               parentDir);

        try {
            logger.log(Level.INFO, "copyDirectory: {0} -> {1}",
                       new Object[]{srcDir.getAbsolutePath(), newDir.getAbsolutePath()});
            FileUtils.copyDirectory(srcDir, newDir);
            ImageViewerExtensionManager.getInstance().directoryWasCopied(srcDir, newDir);
        }
        catch (IOException ex) {
            getMessageUtil().error("Copy error", "Error copying directory.", ex);
            return;
        }

        MainWindow.getInstance().setDirectory(srcDir);
        getMessageUtil().info("Copy complete",
                              "The directory has been copied:\nFrom: " + srcDir.getAbsolutePath() + "\nTo: " + newDir.getAbsolutePath());
    }

    /**
     * Symlinks the current image (if any) to the specified directory. Name conflicts in the
     * destination
     * will prompt an intelligent rename dialog.
     *
     * @param destDir The destination directory.
     */
    public static void linkImage(File destDir) {
        // Get the currently showing image:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            getMessageUtil().info("No image selected", "Link image: Nothing selected.");
            return;
        }

        // Record this as the last operation even before we begin processing it.
        // This is debatable, but even if the operation fails or is canceled via the
        // name conflict dialog, I think it should still count as the last
        // thing you did (or tried to do):
        lastOperation = new LastImageOperation(ImageOperation.Type.COPY,
                                               ImageOperation.Payload.SINGLE_IMAGE,
                                               destDir,
                                               MainWindow.getInstance().getCurrentDirectory());

        File srcFile = currentImage.getImageFile();
        if (handleSingleFileOperation(srcFile, destDir, false,
                                      ImageOperation.Type.SYMLINK) == NameConflictDialog.Result.SUCCESS) {
            getMessageUtil().info("Image linked successfully.");
        }
    }

    /**
     * Links all images in the current directory (if any) to the specified directory. Name conflicts
     * will prompt an intelligent rename dialog.
     *
     * @param destination The destination directory.
     */
    public static void linkAllImages(File destination) {
        List<File> imageFiles = MainWindow.getInstance().getCurrentFileList();

        if (imageFiles.isEmpty()) {
            getMessageUtil().info("No images to link", "Link all images: No images to link.");
            return;
        }

        // Record this as the last operation even before we begin processing it.
        // This is debatable, but even if the operation fails or is canceled via the
        // name conflict dialog, I think it should still count as the last
        // thing you did (or tried to do):
        lastOperation = new LastImageOperation(ImageOperation.Type.SYMLINK,
                                               ImageOperation.Payload.ALL_IMAGES,
                                               destination,
                                               MainWindow.getInstance().getCurrentDirectory());

        int filesLinked = 0;
        for (File file : imageFiles) {
            NameConflictDialog.Result result = handleSingleFileOperation(file, destination, true,
                                                                         ImageOperation.Type.SYMLINK);
            if (result == NameConflictDialog.Result.CANCEL_ALL) {
                break;
            }
            else if (result != NameConflictDialog.Result.CANCEL) {
                filesLinked++;
            }
        }

        if (filesLinked > 0) {
            getMessageUtil().info("Linked " + filesLinked + " images in " + destination.getAbsolutePath());
        }
    }

    /**
     * Links the current directory to the specified destination. A rename dialog will pop to
     * offer an opportunity to rename the directory as part of the copy.
     *
     * @param destination The destination directory.
     */
    public static void linkDirectory(File destination) {
        File srcDir = MainWindow.getInstance().getCurrentDirectory();
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            getMessageUtil().error("Link error", "Link directory: Source directory is null or nonexistent.");
            return;
        }
        File parentDir = srcDir.getParentFile();

        // Sanity check - make sure the destination exists:
        if (destination == null || !destination.exists() || !destination.isDirectory()) {
            getMessageUtil().error("Link error", "Link directory: Destination directory is null or nonexistent.");
            return;
        }

        // Another sanity check:
        if (destination.equals(srcDir)) {
            getMessageUtil().error("Link error", "Link directory: Source and destination directories are the same.");
            return;
        }

        File newDir = new File(destination, srcDir.getName());
        String msg = "Optionally rename this directory while linking:";
        do {
            String newName = JOptionPane.showInputDialog(MainWindow.getInstance(), msg, newDir.getName());

            if (newName == null) {
                logger.info("linkDirectory: canceled by user.");
                return; // canceled
            }

            newDir = new File(destination, newName);
            if (newDir.exists()) {
                msg = "That directory already exists. Rename:";
            }
        } while (newDir.exists());

        // Record this as the last operation.
        lastOperation = new LastImageOperation(ImageOperation.Type.SYMLINK, ImageOperation.Payload.DIRECTORY, newDir,
                                               parentDir);

        try {
            logger.log(Level.INFO, "linkDirectory: {0} -> {1}",
                       new Object[]{srcDir.getAbsolutePath(), newDir.getAbsolutePath()});
            Path target = FileSystems.getDefault().getPath(srcDir.getAbsolutePath());
            Path link = FileSystems.getDefault().getPath(newDir.getAbsolutePath());
            java.nio.file.Files.createSymbolicLink(link, target);
        }
        catch (IOException ex) {
            getMessageUtil().error("Link error", "Error linking directory.", ex);
            return;
        }

        MainWindow.getInstance().setDirectory(srcDir);
        getMessageUtil().info("Link complete",
                              "The directory has been linked:\nOriginal: " + srcDir.getAbsolutePath() + "\nSymlink: " + newDir.getAbsolutePath());
    }

    /**
     * Renames the currently showing image.
     *
     * @param newName The new filename. Only basic validation will be done here.
     */
    public static void renameImage(String newName) {
        // Get the currently showing image:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        File srcFile = currentImage.getImageFile();
        if (srcFile == null || !srcFile.exists()) {
            logger.info("renameImage: source file is null or nonexistent.");
            return;
        }

        if (newName == null || newName.isEmpty()) {
            getMessageUtil().error("Attempted to rename image with an empty new name.");
            return;
        }

        File newFile = new File(srcFile.getParentFile(), newName);
        if (newFile.exists()) {
            getMessageUtil().error("Attempted to rename image to a name that is in use.");
            return;
        }

        // See if our extensions have any companion files for this image:
        List<File> companions = ImageViewerExtensionManager.getInstance().getCompanionFiles(srcFile);

        logger.log(Level.INFO, "renameImage: {0} -> {1}", new Object[]{srcFile.getAbsolutePath(), newName});
        ImageViewerExtensionManager.getInstance().preImageOperation(ImageOperation.Type.MOVE, srcFile, newFile);

        // Rename the image:
        srcFile.renameTo(newFile);

        // Rename any companion files:
        for (File f : companions) {
            File renamedCompanion = new File(srcFile.getParentFile(),
                                             FilenameUtils.getBaseName(newFile.getName())
                                                     + "."
                                                     + FilenameUtils.getExtension(f.getName()));
            if (renamedCompanion.exists()) {
                logger.log(Level.WARNING, "renameImage: companion file {0} already exists. Skipping.",
                           new Object[]{renamedCompanion.getAbsolutePath()});
            }
            else {
                logger.log(Level.INFO, "renameImage (companion): {0} -> {1}",
                           new Object[]{f.getAbsolutePath(), renamedCompanion.getAbsolutePath()});
                f.renameTo(new File(srcFile.getParentFile(), renamedCompanion.getName()));
            }
        }
        ImageViewerExtensionManager.getInstance().postImageOperation(ImageOperation.Type.MOVE, newFile);

        MainWindow.getInstance().selectedImageRenamed(newFile);
    }

    /**
     * Deletes the currently showing image.
     */
    public static void deleteImage() {
        logger.entering("ImageOperationHandler", "deleteImage");

        // Get the currently showing image:
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        File srcFile = currentImage.getImageFile();
        if (srcFile == null || !srcFile.exists()) {
            logger.info("deleteImage: source file is null or nonexistent.");
            return;
        }

        // See if our extensions have any companion files for this image:
        List<File> companions = ImageViewerExtensionManager.getInstance().getCompanionFiles(srcFile);

        // Notify extensions that we're about to delete this file:
        ImageViewerExtensionManager.getInstance().preImageOperation(ImageOperation.Type.DELETE, srcFile, null);

        // Delete the image:
        logger.log(Level.INFO, "deleteImage: {0}", srcFile.getAbsolutePath());
        if (!srcFile.delete()) {
            getMessageUtil().error("Delete error",
                                   "Error deleting " + srcFile.getAbsolutePath() + " - check permissions.");
            return;
        }
        for (File f : companions) {
            logger.log(Level.INFO, "deleteImage (companion): {0}", f.getAbsolutePath());
            f.delete();
        }

        // Notify extensions that we deleted the file (this is debatable since srcFile no longer exists, but eh.
        ImageViewerExtensionManager.getInstance().postImageOperation(ImageOperation.Type.DELETE, srcFile);

        // Update the container panel and currently showing image:
        MainWindow.getInstance().selectedImageRemoved();

        logger.exiting("MainWindow", "deleteImage");
    }

    /**
     * Deletes all images in the current directory.
     */
    public static void deleteAllImages() {
        // Give a chance to abort this in case the menu was clicked by accident:
        int result = JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                                   "Really delete all images here?",
                                                   "Confirm delete all", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) {
            return;
        }

        DeleteImageThread thread = new DeleteImageThread(MainWindow.getInstance().getCurrentFileList());
        MainWindow.getInstance().disableDirTree();
        new Thread(thread).start();
    }

    /**
     * Deletes the current directory and everything in it. Will prompt to confirm if there
     * are alien files present.
     */
    public static void deleteDirectory() {
        List<File> imageFiles = MainWindow.getInstance().getCurrentFileList();
        List<File> alienFiles = MainWindow.getInstance().getCurrentAlienFileList();
        File currentDir = MainWindow.getInstance().getCurrentDirectory();
        List<File> subdirs = FileSystemUtil.findSubdirectories(currentDir, true);
        String confirmMsg = "";
        if (!imageFiles.isEmpty()) {
            confirmMsg += imageFiles.size() + " images will be deleted!\n";
        }
        if (!alienFiles.isEmpty()) {
            confirmMsg += alienFiles.size() + " alien files will be deleted!\n";
        }
        if (!subdirs.isEmpty()) {
            confirmMsg += subdirs.size() + " subdirectories (total) will be deleted!\n";
        }

        // Deleting empty dirs is a special case:
        if (imageFiles.isEmpty() && alienFiles.isEmpty() && subdirs.isEmpty()) {
            confirmMsg = "Delete this empty directory?";
        }

        int result = JOptionPane.showConfirmDialog(MainWindow.getInstance(), confirmMsg, "Confirm deletion",
                                                   JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            MainWindow.getInstance().disableDirTree();
            DeleteImageThread thread = new DeleteImageThread(currentDir);
            new Thread(thread).start();
        }
    }

    /**
     * Invoked by our worker thread when all images are deleted in the current directory.
     *
     * @param allDeletedOkay Indicates whether a deletion error occurred (file permissions).
     */
    public static void deleteAllImagesCallback(boolean allDeletedOkay) {
        MainWindow.getInstance().enableDirTree();
        MainWindow.getInstance().reloadCurrentDirectory();

        if (!allDeletedOkay) {
            getMessageUtil().info("Deletion problem",
                                  "Not all images were deleted. Either canceled by user, or permissions error encountered.");
        }
    }

    /**
     * Invoked by our worker thread upon directory deletion.
     *
     * @param ok Indicates whether we encountered a permissions problem.
     */
    public static void deleteDirectoryCallback(boolean ok) {
        MainWindow.getInstance().enableDirTree();
        //
        // Extensions can give misleading results here, so we ignore "ok" in this callback now.
        // Example: companion file extension deletes companion files, while the directory
        // deletion thread enumerates all files and tries to delete them. So, if the extension
        // deletes a file before the directory deletion thread gets to it, the directory deletion
        // thread thinks something went wrong. We can safely ignore it 99% of the time.
        //
        //if (!ok) {
        //  getMessageUtil().info("Deletion problem",
        //                        "Not all files and directories were deleted. Either canceled by user, or permissions error encountered.");
        //  return;
        //}

        File currentDir = MainWindow.getInstance().getCurrentDirectory();
        File parentDir = currentDir.getParentFile();
        MainWindow.getInstance().setDirectory(parentDir);
    }

    /**
     * Invoked from undoLastOperation if the operation in question was a directory-based
     * operation. If it was a move, we move it back. If it was a symlink or a copy, we delete
     * the directory or symlink that was created.
     */
    private static void undoDirectoryOperation() {
        // Check to make sure the destination directory still exists (may have been deleted
        // by something outside of this app):
        File targetDir = lastOperation.getDestination();
        if (!targetDir.exists()) {
            getMessageUtil().error("Undo directory operation",
                                   "The target directory " + targetDir.getAbsolutePath() + " seems to no longer exist. Unable to proceed.");
            return;
        }

        File sourceDir = lastOperation.getSource();
        String confirmMsg = null;
        switch (lastOperation.getType()) {
            case MOVE:
                confirmMsg = "The directory " + targetDir.getAbsolutePath() + "\nwill be moved back to where it came from: " + sourceDir.getAbsolutePath();
                break;

            case COPY:
                confirmMsg = "The directory " + targetDir.getAbsolutePath() + "\nwhich was copied from: " + sourceDir.getAbsolutePath() + "\nwill be deleted.";
                break;

            case SYMLINK:
                confirmMsg = "The symlink " + targetDir.getAbsolutePath() + "\nwhich links to source dir: " + sourceDir.getAbsolutePath() + "\nwill be deleted.";
                break;
        }
        if (confirmMsg == null) {
            getMessageUtil().error("Undo directory operation",
                                   "Unable to determine details of last action. Unable to proceed.");
            return;
        }

        if (JOptionPane.showConfirmDialog(MainWindow.getInstance(), confirmMsg + "\n\nProceed?",
                                          "Undo directory operation", JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
            return;
        }

        switch (lastOperation.getType()) {
            case MOVE:
                if (!sourceDir.exists()) {
                    getMessageUtil().error(
                            "The source dir " + sourceDir.getAbsolutePath() + " seems to no longer exist; unable to proceed.");
                    return;
                }
                File newDir = new File(sourceDir, targetDir.getName());
                String msg = "Optionally rename this directory while moving it back:";
                do {
                    String newName = JOptionPane.showInputDialog(MainWindow.getInstance(), msg, newDir.getName());

                    if (newName == null) {
                        logger.info("moveDirectory: canceled by user.");
                        return; // canceled
                    }

                    newDir = new File(sourceDir, newName);
                    if (newDir.exists()) {
                        msg = "That directory already exists. Rename:";
                    }
                } while (newDir.exists());

                logger.log(Level.INFO, "undo: moveDirectory: {0} -> {1}",
                           new Object[]{targetDir.getAbsolutePath(), newDir.getAbsolutePath()});
                try {
                    FileUtils.moveDirectory(targetDir, newDir);
                    ImageViewerExtensionManager.getInstance().directoryWasMoved(targetDir, newDir);
                }
                catch (IOException ioe) {
                    getMessageUtil().error("Caught exception while moving directory: " + ioe.getMessage(), ioe);
                    return;
                }

                MainWindow.getInstance().setDirectory(MainWindow.getInstance().getCurrentDirectory());
                getMessageUtil().info("The directory operation has been undone.");
                break;

            case COPY:
                MainWindow.getInstance().disableDirTree();
                DeleteImageThread thread = new DeleteImageThread(targetDir);
                new Thread(thread).start();
                break;

            case SYMLINK:
                try {
                    Files.deleteIfExists(Paths.get(targetDir.getAbsolutePath()));
                }
                catch (IOException ioe) {
                    getMessageUtil().error("Caught exception while removing symlink: " + ioe.getMessage(), ioe);
                }
                MainWindow.getInstance().setDirectory(MainWindow.getInstance().getCurrentDirectory());
                getMessageUtil().info("The symlink has been removed.");
                break;
        }
    }

    /**
     * Invoked internally from undoLastOperation if the operation was not directory-related.
     * If it was a move, we'll move the affected file(s) back where they came from. If it
     * was a copy or a symlink, we'll delete the copies or symlinks that were created.
     */
    private static void undoFileOperation() {
        List<File> affectedFiles = lastOperation.getCreatedFiles();
        if (affectedFiles.isEmpty()) {
            getMessageUtil().error("Undo last operation",
                                   "The list of affected files from the previous operation is empty. Unable to proceed.");
            return;
        }

        boolean anyExists = false;
        for (File file : affectedFiles) {
            if (file.exists()) {
                anyExists = true;
                break;
            }
        }
        if (!anyExists) {
            getMessageUtil().error("Undo last operation",
                                   "The file(s) affected by the previous operation seem to no longer exist. Unable to proceed.");
            return;
        }

        String confirmMsg = null;
        File sourceDir = lastOperation.getSource();
        File targetDir = lastOperation.getDestination();

        if (!targetDir.exists()) {
            getMessageUtil().error("Undo last operation",
                                   "The target directory " + targetDir.getAbsolutePath() + " seems to no longer exist.");
            return;
        }

        switch (lastOperation.getType()) {
            case MOVE:
                confirmMsg = affectedFiles.size() + " images which were moved\n  from dir: " + sourceDir.getAbsolutePath() + "\n  to dir: " + targetDir.getAbsolutePath() + "\nwill be moved back.";
                break;

            case COPY:
                confirmMsg = affectedFiles.size() + " images which were copied\n  from dir: " + sourceDir.getAbsolutePath() + "\n  to dir: " + targetDir.getAbsolutePath() + "\nwill be deleted.";
                break;

            case SYMLINK:
                confirmMsg = affectedFiles.size() + " symlinks which were created in " + targetDir.getAbsolutePath() + " will be removed.";
                break;
        }

        if (confirmMsg == null) {
            getMessageUtil().error("Undo last operation",
                                   "Unable to determine details of last operation. Unable to proceed.");
            return;
        }

        // Grammar check:
        if (affectedFiles.size() == 1) {
            confirmMsg = confirmMsg.replace("images which were", "image which was");
            confirmMsg = confirmMsg.replace("symlinks which were", "symlink which was");
        }

        if (JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                          confirmMsg + "\n\nProceed?", "Undo last operation",
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
            return;
        }

        switch (lastOperation.getType()) {
            case MOVE:
                String opName = "undo move";
                if (!sourceDir.exists()) {
                    getMessageUtil().error(
                            "The source dir " + sourceDir.getAbsolutePath() + " seems to no longer exist; unable to proceed.");
                    return;
                }

                for (File file : affectedFiles) {
                    if (!file.exists()) {
                        logger.log(Level.INFO, "{0}: file \"{1}\" seems to no longer exist; skipping.",
                                   new Object[]{opName, file.getAbsolutePath()});
                        continue;
                    }
                    // Check for name conflicts:
                    File destFile = new File(sourceDir, file.getName());
                    if (destFile.exists()) {
                        logger.log(Level.INFO, "{0}: name conflict!", opName);
                        NameConflictDialog dialog = NameConflictDialog.getInstance();
                        dialog.setConflict(file, sourceDir, true);
                        dialog.setVisible(true);
                        switch (dialog.getResult()) {
                            case RENAME:
                                logger.log(Level.INFO, "{0}: name conflict resolved by user input.", opName);
                                destFile = dialog.getDestFile();
                                break;
                            case CANCEL:
                                logger.log(Level.INFO, "{0}: canceled.", opName);
                                continue;
                            case CANCEL_ALL:
                                logger.log(Level.INFO, "{0}: canceled all.", opName);
                                return;
                            default:
                                break;
                        }
                    }

                    // Make note of any companion files that our extensions want to accompany this image file:
                    List<File> companions = ImageViewerExtensionManager.getInstance().getCompanionFiles(file);

                    try {
                        ImageViewerExtensionManager.getInstance()
                                                   .preImageOperation(ImageOperation.Type.MOVE, file, destFile);
                        FileUtils.moveFile(file, destFile);
                        for (File f : companions) {
                            FileUtils.moveFile(f, new File(destFile.getParentFile(), f.getName()));
                        }
                        ImageViewerExtensionManager.getInstance()
                                                   .postImageOperation(ImageOperation.Type.MOVE, destFile);
                    }
                    catch (IOException ioe) {
                        getMessageUtil().error("Undo last operation",
                                               "Caught exception while moving files back: " + ioe.getMessage(), ioe);
                        return;
                    }
                }
                break;

            case COPY:
                for (File file : affectedFiles) {
                    ImageViewerExtensionManager.getInstance().preImageOperation(ImageOperation.Type.DELETE, file, null);
                    List<File> companions = ImageViewerExtensionManager.getInstance().getCompanionFiles(file);
                    FileUtils.deleteQuietly(file);
                    for (File f : companions) {
                        FileUtils.deleteQuietly(f);
                    }
                    ImageViewerExtensionManager.getInstance().postImageOperation(ImageOperation.Type.DELETE, file);
                }
                break;

            case SYMLINK:
                try {
                    for (File file : affectedFiles) {
                        List<File> companions = ImageViewerExtensionManager.getInstance().getCompanionFiles(file);
                        Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
                        for (File f : companions) {
                            Files.deleteIfExists(Paths.get(f.getAbsolutePath()));
                        }
                    }
                }
                catch (IOException ioe) {
                    getMessageUtil().error("Caught exception while removing symlink: " + ioe.getMessage(), ioe);
                }
        }

        MainWindow.getInstance().reloadCurrentDirectory();
        getMessageUtil().info("The last operation has been undone.");
    }

    private static MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), logger);
        }
        return messageUtil;
    }

}
