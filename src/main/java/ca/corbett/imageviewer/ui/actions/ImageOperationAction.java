package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.ImageOperationHandler;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * This action provides a convenient shortcut wrapper around the various move, copy,
 * symlink, and delete methods exposed by ImageOperationHandler.
 * If you supply a destination with your ImageOperation, this action will defer to
 * the appropriate ImageOperationHandler method. If you supply a null destination,
 * a directory chooser will be shown (if necessary) to select the destination directory.
 * (Note that delete actions obviously do not require a destination).
 *
 * @author scorbett
 * @since ImageViewer 2.0
 */
public class ImageOperationAction extends AbstractAction {

    private final ImageOperation operation;

    /**
     * Creates a new MoveAction with the specified ImageOperation. The destination
     * within this ImageOperation can be null, in which case we will prompt for
     * a destination, or it can be set to some directory, in which case we'll use it.
     *
     * @param label     The String label for this Action
     * @param operation The ImageOperation to perform.
     */
    public ImageOperationAction(String label, ImageOperation operation) {
        super(label);
        this.operation = operation;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // If it's a delete action, just do it, no need to worry about destination:
        if (operation.isDeleteOperation()) {
            handleDelete();
            return;
        }

        File destination = operation.getDestination();

        // If we weren't given a destination, prompt for one:
        if (destination == null) {
            JFileChooser fileChooser = MainWindow.getInstance()
                                                 .getFileChooser("Choose directory", JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showDialog(MainWindow.getInstance(), "Select");
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }
            destination = fileChooser.getSelectedFile();
        }

        switch (operation.getType()) {
            case COPY:
                handleCopy(destination);
                break;
            case MOVE:
                handleMove(destination);
                break;
            case SYMLINK:
                handleSymLink(destination);
                break;
        }
    }

    private void handleCopy(File destination) {
        switch (operation.getPayload()) {
            case SINGLE_IMAGE:
                ImageOperationHandler.copyImage(destination);
                break;
            case ALL_IMAGES:
                ImageOperationHandler.copyAllImages(destination);
                break;
            case DIRECTORY:
                ImageOperationHandler.copyDirectory(destination);
                break;
        }
    }

    private void handleMove(File destination) {
        switch (operation.getPayload()) {
            case SINGLE_IMAGE:
                ImageOperationHandler.moveImage(destination);
                break;
            case ALL_IMAGES:
                ImageOperationHandler.moveAllImages(destination);
                break;
            case DIRECTORY:
                ImageOperationHandler.moveDirectory(destination);
                break;
        }
    }

    private void handleSymLink(File destination) {
        switch (operation.getPayload()) {
            case SINGLE_IMAGE:
                ImageOperationHandler.linkImage(destination);
                break;
            case ALL_IMAGES:
                ImageOperationHandler.linkAllImages(destination);
                break;
            case DIRECTORY:
                ImageOperationHandler.linkDirectory(destination);
                break;
        }
    }

    private void handleDelete() {
        switch (operation.getPayload()) {
            case SINGLE_IMAGE:
                ImageOperationHandler.deleteImage();
                break;
            case ALL_IMAGES:
                ImageOperationHandler.deleteAllImages();
                break;
            case DIRECTORY:
                ImageOperationHandler.deleteDirectory();
                break;
        }
    }
}
