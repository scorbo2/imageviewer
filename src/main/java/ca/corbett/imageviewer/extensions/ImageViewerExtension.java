package ca.corbett.imageviewer.extensions;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extras.dirtree.DirTree;
import ca.corbett.extras.logging.LogConsoleStyle;
import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.ThumbPanel;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * The starting point for ImageViewer extensions. Extend this adapter class and place the
 * resulting class file with dependencies in a jar file, and stick that in the dependencies
 * directory, either in the app install location, or in the user settings dir.
 * There, it can be detected and picked up by ImageViewer.
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public abstract class ImageViewerExtension implements AppExtension {

    public enum ExtraPanelPosition {
        Left, Top, Right, Bottom
    }

    /**
     * Invoked when an extension is enabled or when the application is starting up.
     * Extensions should ideally avoid long-running operations here, but some initialization
     * is fine. This method is optional, and the default implementation here does nothing.
     */
    @Override
    public void onActivate() {
    }

    /**
     * Invoked when an extension is disabled or when the application is shutting down.
     * This method is optional, and the default implementation here does nothing.
     */
    @Override
    public void onDeactivate() {
    }

    /**
     * Invoked when the application wants to generate a thumbnail for the given image
     * file at the given size. If this method returns an image, it will be used, otherwise
     * the application will fall back on default thumbnail generation.
     *
     * @param imageFile The file containing the image in question.
     * @param thumbSize The pixel width/height (square) of the desired thumbnail.
     * @return A BufferedImage, or null - this default implementation returns null.
     */
    public BufferedImage getThumbnail(File imageFile, int thumbSize) {
        return null;
    }

    /**
     * Any extension that had cached a thumbnail for the given image file should discard it.
     * This is invoked in cases where the image has changed in-place, so any cached thumbnail
     * is no longer valid.
     *
     * @param imageFile The image file in question.
     */
    public void removeThumbnail(File imageFile) {
    }

    /**
     * Invoked when the application is building the MainWindow's main menu and wants
     * to know if the extension has anything to add to one of the built-in top-level
     * menus.
     *
     * @param topLevelMenu The name of the top-level menu being built: File, Edit, View, or Help.
     * @return an optional list of menu items to insert into the given menu, or null for nothing.
     */
    public List<JMenuItem> getMenuItems(String topLevelMenu) {
        return null;
    }

    /**
     * Invoked when the application wants to know if the extension has its own top-level
     * menu to add to the MainWindow's main menu.
     *
     * @return an optional list of JMenu objects for the main menu, or null for none.
     */
    public List<JMenu> getTopLevelMenus() {
        return null;
    }

    /**
     * Invoked when the application is building the popup menu for the MainWindow's image
     * panel, also used in the toolbar and in the MainMenu. Extensions can add menu items
     * here if they pertain to the current image or current directory of images.
     *
     * @return An optional list of menu items to insert into the popup menu, or null for none.
     */
    public List<JMenuItem> getPopupMenuItems() {
        return null;
    }

    /**
     * Invoked when the application is building the MainWindow ToolBar - extensions can add
     * buttons to this toolbar by returning them here. Use ToolBarManager.buildButton() to
     * generate buttons of the correct size.
     *
     * @return A List of JButtons, or null.
     */
    public List<JButton> getToolBarButtons() {
        return null;
    }

    /**
     * Invoked when the application receives a keyboard shortcut. Extensions can take some
     * action in response to this keyboard shortcut. Processing does not stop when an extension
     * handles this call, so in theory multiple extensions could do something with the same
     * shortcut.
     *
     * @param e the KeyEvent in question.
     * @return true if this extension did something with this event, false otherwise.
     */
    public boolean handleKeyboardShortcut(KeyEvent e) {
        return false;
    }

    /**
     * Invoked when MainWindow wants to create the main DirTree - an extension can return
     * some customized DirTree which will be used in place of the default one.
     *
     * @return A DirTree instance, or null.
     */
    public DirTree buildDirTree() {
        return null;
    }

    /**
     * Extensions can return a list of LogConsoleStyles to be applied to the ImageViewer
     * LogConsole theme.
     *
     * @return A List of LogConsoleStyle objects, or null.
     */
    public List<LogConsoleStyle> getLogConsoleStyles() {
        return null;
    }

    /**
     * Informational message than an ImageOperation is about to be conducted on a single image.
     * This message is sent BEFORE the operation happens, in case extensions want to do something
     * with the original file, as it may or may not still exist after the operation.
     * To receive notification AFTER the operation has completed, you can use
     * postImageOperation() instead.
     *
     * @param opType   The type of operation that is about to happen
     * @param srcFile  The File which is about to be operated on.
     * @param destFile For operations that have a destination only (otherwise null).
     */
    public void preImageOperation(ImageOperation.Type opType, File srcFile, File destFile) {
    }

    /**
     * Informational message that an ImageOperation has just been conducted on the given image.
     * This message is sent AFTER the operation completes, and the given image file is the file
     * as it exists now (i.e. if it was moved, the given file is the new location, not the
     * original location). To receive notification BEFORE the operation happens, you can
     * use preImageOperation instead.
     *
     * @param opType    The type of operation that was conducted.
     * @param imageFile The resulting image file now that the operation has completed.
     */
    public void postImageOperation(ImageOperation.Type opType, File imageFile) {
    }

    /**
     * Informational message that an image directory has been copied. This message is sent
     * AFTER the copy has complete. There is no pre-notification for this operation.
     *
     * @param oldLocation The original directory
     * @param newLocation The new copy of this directory
     */
    public void directoryWasCopied(File oldLocation, File newLocation) {
    }

    /**
     * Informational message that an image directory has been moved. This message is sent
     * AFTER the move has complete, so the given oldLocation no longer exists. There is
     * no pre-notification for this operation.
     *
     * @param oldLocation The previous location of this directory
     * @param newLocation The new location of this directory
     */
    public void directoryWasMoved(File oldLocation, File newLocation) {
    }

    /**
     * Informational message sent out each time a ThumbPanel has been created to represent
     * an image - extensions can use this to add custom stuff to the ThumbPanel.
     *
     * @param thumbPanel The newly created ThumbPanel instance.
     */
    public void thumbPanelCreated(ThumbPanel thumbPanel) {
    }

    /**
     * Invoked when a ThumbPanel is selected or deselected. This method is invoked after
     * the selection is changed, so isSelected describes the new state.
     *
     * @param thumbPanel The ThumbPanel in question.
     * @param isSelected whether the ThumbPanel is selected.
     */
    public void thumbPanelSelectionChanged(ThumbPanel thumbPanel, boolean isSelected) {
    }

    /**
     * Invoked when a ThumbPanel receives a rename request.
     * Note that this is invoked after the rename has occurred on the file system.
     *
     * @param thumbPanel The ThumbPanel in question.
     * @param newFile    The new File representing the image that this ThumbPanel represents.
     */
    public void thumbPanelRenamed(ThumbPanel thumbPanel, File newFile) {
    }

    /**
     * Invoked when the application is trying to decide if a given non-image file is
     * an "alien" or not (that is, not handled by this application). The default return
     * is true. If any extension returns false, the file will not be treated as an alien.
     *
     * @param alienFile The suspected alien
     * @return true if the extension does not recognize the file, or false otherwise.
     */
    public boolean isFileAlien(File alienFile) {
        return true;
    }

    /**
     * Invoked when the application is building the main ImagePanel display - there are four
     * extra components that can go around the main image panel, indicated by the
     * ExtraPanelPosition value of TOP, RIGHT, BOTTOM, or LEFT. The first extension that returns
     * a non-null component for each of these positions will be allowed to use that area to
     * display extra information or controls. It is recommended that extensions expose a config
     * property to allow users to select where they want that extension to show up, to help
     * mitigate conflicts with other extensions. For example, I set extension A to use the LEFT
     * position, and set extension B to use the RIGHT position.
     *
     * @param position LEFT, TOP, RIGHT, or BOTTOM, relative to the main ImagePanel.
     * @return Any JComponent, or null for none.
     */
    public JComponent getExtraPanelComponent(ExtraPanelPosition position) {
        return null;
    }

    /**
     * Invoked when the application is building the Quick Move dialog - any actions returned
     * here will be added as buttons on that dialog.
     *
     * @return A list of actions to add to the quick move dialog, or null for none.
     */
    public List<AbstractAction> getQuickMoveDialogActions() {
        return null;
    }

    /**
     * Informational message that will be sent to all extensions when the quick move tree
     * changes - extensions can do what they need in order to refresh based on the new
     * tree (and QuickMoveDialog getSelectedNode() can tell you what, if anything,
     * is currently selected).
     */
    public void quickMoveTreeChanged() {
    }

    /**
     * An informational message that will be sent to all extensions when the image panel
     * background color changes. Extensions that supply an extra panel around the main image
     * panel may want to react to this by changing their own background colour to match.
     *
     * @param newColor The new background color.
     */
    public void imagePanelBackgroundChanged(Color newColor) {
    }

    /**
     * An informational message that is sent whenever the main image panel is loaded with
     * a new image.
     *
     * @param selectedImage An ImageInstance containing the new image, if there is one.
     */
    public void imageSelected(ImageInstance selectedImage) {
    }

}
