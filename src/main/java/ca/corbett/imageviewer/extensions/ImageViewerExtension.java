package ca.corbett.imageviewer.extensions;

import ca.corbett.extensions.AppExtension;
import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.logging.LogConsoleStyle;
import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ThumbPanel;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

/**
 * The starting point for ImageViewer extensions. Extend this adapter class and place the
 * resulting class file with dependencies in a jar file, and stick that in the extensions
 * directory, either in the app install location, or in the user settings dir.
 * There, it can be detected and picked up by ImageViewer.
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public abstract class ImageViewerExtension extends AppExtension {

    public enum ExtraPanelPosition {
        Left, Top, Right, Bottom
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
     * <p>
     *     If your actions specify icons, please use MenuManager.MENU_ICON_SIZE
     *     to size your icons appropriately.
     * </p>
     *
     * @param topLevelMenu The name of the top-level menu being built: File, Edit, View, or Help.
     * @param browseMode Whether we're currently browsing from the file system or from an ImageSet.
     * @return an optional list of EnhancedActions to insert into the given menu, or null for nothing.
     */
    public List<EnhancedAction> getMenuActions(String topLevelMenu, MainWindow.BrowseMode browseMode) {
        return null;
    }

    /**
     * Invoked when the application wants to know if the extension has its own top-level
     * menu to add to the MainWindow's main menu. Every name that is returned here will be
     * added as a top-level menu. It will also be supplied to getMenuActions() calls when
     * building that menu.
     *
     * @param browseMode Whether we're currently browsing from the file system or from an ImageSet.
     * @return an optional list of top-level menu names, or null for none.
     */
    public List<String> getTopLevelMenus(MainWindow.BrowseMode browseMode) {
        return null;
    }

    /**
     * Invoked when the application is building the popup menu for the MainWindow's image
     * panel, also used in the toolbar and in the MainMenu. Extensions can add EnhancedActions
     * here if they pertain to the current image or current directory of images.
     * <p>
     *     If your actions specify icons, please use MenuManager.MENU_ICON_SIZE
     *     to size your icons appropriately.
     * </p>
     *
     * @param browseMode Whether we're currently browsing from the file system or from an ImageSet.
     * @return An optional list of EnhancedActions to insert into the popup menu, or null for none.
     */
    public List<EnhancedAction> getPopupMenuActions(MainWindow.BrowseMode browseMode) {
        return null;
    }

    /**
     * Invoked when the application is building the MainWindow ToolBar - extensions can add
     * buttons to this toolbar by returning EnhancedActions here.
     *
     * @return A List of EnhancedActions, or null.
     */
    public List<EnhancedAction> getMainToolBarActions() {
        return null;
    }

    /**
     * Invoked when the application is building the ImageSetPanel ToolBar - extensions can add
     * buttons to this toolbar by returning EnhancedActions here.
     *
     * @return A List of EnhancedActions, or null.
     */
    public List<EnhancedAction> getImageSetToolBarActions() {
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
     * <p>
     *     Note that some operations, like delete, do not have a "destination" per se.
     *     The destFile parameter will be null in those cases.
     * </p>
     *
     * @param opType   The type of operation that is about to happen
     * @param srcFile  The File which is about to be operated on.
     * @param destFile For operations that have a destination only (otherwise null).
     */
    public void preImageOperation(ImageOperation.Type opType, File srcFile, File destFile) {
    }

    /**
     * Informational message that an ImageOperation has just been conducted on the given image.
     * This message is sent AFTER the operation completes, so the srcFile may no longer exist
     * (as in the case of an image move). If you want to be notified BEFORE the operation is
     * completed, so you can do something with the srcFile, you can use preImageOperation() instead.
     * <p>
     *     Note that some operations, like delete, do not have a "destination" per se.
     *     The destFile parameter will be null in those cases.
     * </p>
     *
     * @param opType   The type of operation that was conducted.
     * @param srcFile  The original File which was operating on (may no longer exist).
     * @param destFile The File in its current, post-operation state (may be null).
     */
    public void postImageOperation(ImageOperation.Type opType, File srcFile, File destFile) {
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
     * Informational message to inform extensions that the current browse mode has changed.
     * Extensions can use this to re-render whatever UI component may need to change as a result.
     */
    public void browseModeChanged(MainWindow.BrowseMode newBrowseMode) {
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
     * Invoked when the application is trying to decide what type of file it's looking
     * at, and therefore what to do with it. ImageViewer works with four broad types of files:
     * <ol>
     *     <li><b>Images</b> - any file in a supported image format</li>
     *     <li><b>Companion files</b> - not images, but files that nonetheless belong together
     *     with an image. For example, a text file, json file, or xml file that describes
     *     the image or contains additional information about the image. Extensions can
     *     register support for companion files. Out of the box (i.e. without extensions),
     *     ImageViewer does not recognize any file as a companion file.</li>
     *     <li><b>Known files</b> - extensions can store extra files (configuration, metadata,
     *     whatever) in an image directory, and regard those files as "known". These files are
     *     ignored for file-based image operations.</li>
     *     <li><b>Aliens</b> - an alien file is any file that is not positively identified
     *     either as a supported image type, a companion file, or a "known" file.</li>
     * </ol>
     * If an extension wishes to consider a given File as a companion file, it can return
     * true here. The default return is false, indicating that the extension does not recognize
     * the given file as a companion. If all extensions return false, the file will be
     * considered an alien file.
     *
     * @param candidateFile The file in question.
     * @return true if the extension recognizes the file, false otherwise (default false).
     */
    public boolean isCompanionFile(File candidateFile) {
        return false;
    }

    /**
     * Given an image file, return a list of any companion files that the extension sees for that
     * image file. The default return is an empty list, indicating the image has no companions.
     *
     * @param imageFile Any image file.
     * @return A List of zero or more companion files that should be moved/copied/linked with the image.
     */
    public List<File> getCompanionFiles(File imageFile) {
        return List.of();
    }

    /**
     * Extensions can store extra metadata or configuration files in image directories, and mark
     * them as known files. These are not the same as companion files! Companion files are associated
     * with individual images, whereas known files are associated with the directory as a whole.
     * These files will therefore NOT be included with image operations (except for operations
     * that move or copy the entire directory). But, they will also not be marked as "alien" files.
     * This causes ImageViewer to just ignore them - they do not appear in the application's UI.
     * Extensions can provide their own UI to display or edit these files, if they wish.
     * <p>
     * Don't include "known" files in your list of companion files! Also, don't return
     * true here for companion files! They are different concepts.
     * </p>
     *
     * @param candidateFile The file in question.
     * @return true if the extension recognizes the file as a known file, false otherwise (default false).
     */
    public boolean isKnownFile(File candidateFile) {
        return false;
    }

    /**
     * Invoked when the application is building the main ImagePanel display - there are four
     * positions for extra components that can go around the main image panel, indicated by the
     * ExtraPanelPosition value of TOP, RIGHT, BOTTOM, or LEFT. Each loaded and enabled extension
     * will be queried to see if they have an extra component to offer for each position.
     * Your extension may return one JComponent for each position. Returning null is fine
     * here, if your extension has nothing to supply.
     * <p>
     * It is recommended that extensions expose a config property to allow users to select where they want
     * their extra component to show up, to help mitigate conflicts with other extensions.
     * For example, I set extension A to use the LEFT position, and set extension B to use the RIGHT position.
     * That way they can coexist on screen at the same time with no conflicts.
     * </p>
     * <p>
     * <b>Hint:</b> use setName() on your component to give it a meaningful name. If more than one
     * extension supplies an extra component for the same position, the component name will be used
     * as a tab header in the tabbed pane that will be created to hold them all. Keep the name
     * brief but distinctive. If no name is supplied, your tab will get a numeric name based on load order.
     * </p>
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
     * An informational message that is sent whenever the main image panel is loaded with
     * a new image.
     *
     * @param selectedImage An ImageInstance containing the new image, if there is one.
     */
    public void imageSelected(ImageInstance selectedImage) {
    }

    /**
     * Extensions may return zero or more JPanel instances which will be added as tabs
     * to the main image tab panel. The default return here is an empty list. If no
     * extension returns anything from this method, the main image tab panel is hidden.
     * Note: use setName() on your panel to give the resulting tab a meaningful name.
     */
    public List<JPanel> getImageTabPanels() {
        return List.of();
    }

}
