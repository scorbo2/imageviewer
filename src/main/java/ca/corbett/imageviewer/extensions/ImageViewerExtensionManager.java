package ca.corbett.imageviewer.extensions;

import ca.corbett.extensions.ExtensionManager;
import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.logging.LogConsoleStyle;
import ca.corbett.extras.properties.KeyStrokeProperty;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ThumbPanel;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * Provides support for scanning and loading extensions of our application extension type.
 *
 * @author scorbo2
 */
public class ImageViewerExtensionManager extends ExtensionManager<ImageViewerExtension> {

    private static ImageViewerExtensionManager instance;

    private ImageViewerExtensionManager() {
    }

    public static ImageViewerExtensionManager getInstance() {
        if (instance == null) {
            instance = new ImageViewerExtensionManager();
        }
        return instance;
    }

    /**
     * Scans our two locations (SYSTEM_EXTENSION_DIR and USER_EXTENSION_DIR) looking for
     * jar files containing classes that extend ImageViewerExtension. All found classes
     * will be instantiated and made available as extensions, enabled by default.
     * Note: this should be called before AppPreferences.load() is invoked, because
     * we first need to load all extensions so they can be queried for their properties.
     */
    public void loadAll() {
        // Built-in extensions:
        addExtension(new ca.corbett.imageviewer.extensions.builtin.ImageInfoExtension(), true);
        addExtension(new ca.corbett.imageviewer.extensions.builtin.StatisticsExtension(), true);
        addExtension(new ca.corbett.imageviewer.extensions.builtin.RepeatUndoExtension(), true);

        // External extensions from jar files on the system:
        try {
            loadExtensions(Version.EXTENSIONS_DIR,
                           ImageViewerExtension.class,
                           Version.APPLICATION_NAME,
                           Version.VERSION);
        }
        catch (LinkageError le) {
            logger.log(Level.SEVERE, "One or more extensions could not be loaded.", le);
        }
    }

    /**
     * Returns all KeyStrokeProperty instances supplied by enabled extensions.
     * Extensions can supply KeyStrokeProperty instances as part of their usual
     * configuration properties. We have a separate getter for them here as a
     * convenience when registering keyboard shortcuts with our KeyStrokeManager.
     * Properties from currently-disabled extensions will not be included.
     *
     * @return A List of KeyStrokeProperty instances supplied by enabled extensions.
     */
    public List<KeyStrokeProperty> getKeyStrokeProperties() {
        return getAllEnabledExtensionProperties()
                .stream()
                .filter(p -> p instanceof KeyStrokeProperty)
                .map(p -> (KeyStrokeProperty)p)
                .toList();
    }

    /**
     * Uses AppPreferences to load the initial enabled/disabled status of each extension.
     * This has to be invoked after AppPreferences.load() so our preferences are available.
     */
    public void loadEnabledStatus() {
        for (ImageViewerExtension extension : getAllLoadedExtensions()) {
            boolean isEnabled = AppConfig.getInstance().isExtensionEnabled(extension.getClass().getName(), true);
            setExtensionEnabled(extension.getClass().getName(), isEnabled, false);
        }
    }

    /**
     * Uses AppPreferences to save the current enabled/disabled status of each extension.
     * This can be called any time after AppPreferences.load()
     */
    public void saveEnabledStatus() {
        for (ImageViewerExtension extension : getAllLoadedExtensions()) {
            AppConfig.getInstance().setExtensionEnabled(extension.getClass().getName(),
                                                        isExtensionEnabled(extension.getClass().getName()));
        }
        AppConfig.getInstance().save();
    }

    /**
     * Lets extensions know that we want to load or generate a thumbnail for the given
     * image file. The first extension that returns a non-null value here will result
     * in that value being used as the thumbnail. If all extensions return null, the
     * default behaviour is to generate a transient thumbnail.
     *
     * @param file      The image file.
     * @param thumbSize The pixel width/height (square) of the desired thumbnail.
     * @return A BufferedImage, or null.
     */
    public BufferedImage getThumbnail(File file, int thumbSize) {
        BufferedImage thumb = null;
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            thumb = extension.getThumbnail(file, thumbSize);
            if (thumb != null) {
                break;
            }
        }
        return thumb;
    }

    /**
     * Any extension that had cached a thumbnail for the given image file should discard it.
     * This is invoked in cases where the image has changed in-place, so any cached thumbnail
     * is no longer valid.
     *
     * @param imageFile The image file in question.
     */
    public void removeThumbnail(File imageFile) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.removeThumbnail(imageFile);
        }
    }

    /**
     * Interrogates extensions to see if they have menu actions that they want to add
     * to one the named top-level menu.
     *
     * @param topLevelMenu The String name of the top-level menu being built: eg. File, Edit, View, Help, or any
     *                     custom top-level menu added by an extension.
     * @param browseMode Whether we're currently browsing from the file system or from an ImageSet.
     * @return A list of 0 or more EnhancedActions supplied by enabled extensions.
     */
    public List<EnhancedAction> getMenuActions(String topLevelMenu, MainWindow.BrowseMode browseMode) {
        List<EnhancedAction> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            List<EnhancedAction> toAdd = extension.getMenuActions(topLevelMenu, browseMode);
            if (toAdd != null) {
                list.addAll(toAdd);
            }
        }
        return list;
    }

    /**
     * Interrogates extensions to see if they have any top-level menus that they want
     * to add to the MainWindow's main menu. Every name that is returned here will be
     * added as a top-level menu. It will also be supplied to getMenuActions() calls when
     * building that menu.
     *
     * @param browseMode Whether we're currently browsing from the file system or from an ImageSet.
     * @return a List of zero or more top-level menu names supplied by enabled extensions.
     */
    public List<String> getTopLevelMenus(MainWindow.BrowseMode browseMode) {
        List<String> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            List<String> toAdd = extension.getTopLevelMenus(browseMode);
            if (toAdd != null) {
                list.addAll(toAdd);
            }
        }
        return list;
    }

    /**
     * Interrogates extensions to see if they have any menu actions to add to the
     * image popup menu.
     *
     * @return A list of 0 or more EnhancedActions supplied by enabled extensions.
     */
    public List<EnhancedAction> getPopupMenuActions(MainWindow.BrowseMode browseMode) {
        List<EnhancedAction> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            List<EnhancedAction> toAdd = extension.getPopupMenuActions(browseMode);
            if (toAdd != null) {
                list.addAll(toAdd);
            }
        }
        return list;
    }

    /**
     * Interrogates extensions to see if they have any actions to add to the main toolbar.
     *
     * @return A list of 0 or more EnhancedActions supplied by enabled extensions.
     */
    public List<EnhancedAction> getMainToolBarActions() {
        List<EnhancedAction> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            List<EnhancedAction> toAdd = extension.getMainToolBarActions();
            if (toAdd != null) {
                list.addAll(toAdd);
            }
        }
        return list;
    }

    /**
     * Interrogates extensions to see if they have any actions to add to the image set panel toolbar.
     *
     * @return A list of 0 or more EnhancedActions supplied by enabled extensions.
     */
    public List<EnhancedAction> getImageSetToolBarActions() {
        List<EnhancedAction> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            List<EnhancedAction> toAdd = extension.getImageSetToolBarActions();
            if (toAdd != null) {
                list.addAll(toAdd);
            }
        }
        return list;
    }

    /**
     * Gives all extensions a chance to register custom LogConsoleStyle objects for use
     * with the ImageViewer theme in our LogConsole.
     *
     * @return A List of LogConsoleStyle instances, may be empty.
     */
    public List<LogConsoleStyle> getLogConsoleStyles() {
        List<LogConsoleStyle> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            List<LogConsoleStyle> toAdd = extension.getLogConsoleStyles();
            if (toAdd != null) {
                list.addAll(toAdd);
            }
        }
        return list;
    }

    /**
     * Informational message than an ImageOperation is about to be conducted on a single image.
     * This message is sent BEFORE the operation happens, in case extensions want to do something
     * with the original file, as it may or may not still exist after the operation.
     * To receive notification AFTER the operation has completed, you can use
     * postImageOperation() instead.
     *
     * @param opType      The type of operation that is about to happen
     * @param imageFile   The File which is about to be operated on.
     * @param destination For operations that have a destination only (otherwise null).
     */
    public void preImageOperation(ImageOperation.Type opType, File imageFile, File destination) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.preImageOperation(opType, imageFile, destination);
        }
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
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.postImageOperation(opType, srcFile, destFile);
        }
    }

    /**
     * Informational message that an image directory has been copied. This message is sent
     * AFTER the copy has complete. There is no pre-notification for this operation.
     *
     * @param oldLocation The original directory
     * @param newLocation The new copy of this directory
     */
    public void directoryWasCopied(File oldLocation, File newLocation) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.directoryWasCopied(oldLocation, newLocation);
        }
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
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.directoryWasMoved(oldLocation, newLocation);
        }
    }

    /**
     * Informational message to inform extensions that the current browse mode has changed.
     * Extensions can use this to re-render whatever UI component may need to change as a result.
     */
    public void browseModeChanged(MainWindow.BrowseMode newBrowseMode) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.browseModeChanged(newBrowseMode);
        }
    }

    /**
     * Informational message sent out each time a ThumbPanel has been created to represent
     * an image - extensions can use this to add custom stuff to the ThumbPanel.
     *
     * @param thumbPanel The newly created ThumbPanel instance.
     */
    public void thumbPanelCreated(ThumbPanel thumbPanel) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.thumbPanelCreated(thumbPanel);
        }
    }

    /**
     * Invoked when a ThumbPanel is selected or deselected. This method is invoked after
     * the selection is changed, so isSelected describes the new state.
     *
     * @param thumbPanel The ThumbPanel in question.
     * @param isSelected whether the ThumbPanel is selected.
     */
    public void thumbPanelSelectionChanged(ThumbPanel thumbPanel, boolean isSelected) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.thumbPanelSelectionChanged(thumbPanel, isSelected);
        }
    }

    /**
     * Invoked when a ThumbPanel receives a rename request.
     * Note that this is invoked after the rename has occurred on the file system.
     *
     * @param thumbPanel The ThumbPanel in question.
     * @param newFile    The new File representing the image that this ThumbPanel represents.
     */
    public void thumbPanelRenamed(ThumbPanel thumbPanel, File newFile) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.thumbPanelRenamed(thumbPanel, newFile);
        }
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
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            if (extension.isCompanionFile(candidateFile)) {
                return true; // first extension that says yes, we're done
            }
        }
        return false;
    }

    /**
     * Given an image file, return a list of any companion files that our extensions see for that
     * image file. The resulting list may be empty if no extensions see any companion files for
     * the given image.
     *
     * @param imageFile Any image file.
     * @return A List of zero or more companion files that should be moved/copied/linked with the image.
     */
    public List<File> getCompanionFiles(File imageFile) {
        // It's conceivable that more than one extension could claim the same
        // companion file, which could result in duplicates in the list.
        // So, we'll use a Set instead of a List while building it up
        // to screen those duplicates out. Otherwise, we would have problems
        // in file move operations, where the handler would try to move
        // the same companion file more than once, which obviously won't work.
        Set<File> companions = new HashSet<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            companions.addAll(extension.getCompanionFiles(imageFile));
        }
        return new ArrayList<>(companions);
    }

    /**
     * Given a candidate non-image file, ask extensions if they "know" this file.
     * Known files are not the same as companion files! Companion files are associated
     * with individual images, whereas known files are associated with the directory as a whole.
     * These files will therefore NOT be included with image operations (except for operations
     * that move or copy the entire directory). But, they will also not be marked as "alien" files.
     * This causes ImageViewer to just ignore them - they do not appear in the application's UI.
     *
     * @param candidateFile The file in question.
     * @return true if any extension recognizes the file, false otherwise (default false).
     */
    public boolean isKnownFile(File candidateFile) {
        // We'll hard-code one application-level "known" file, at least
        // until Darwin has its own ImageViewer extension...
        if (".00darwin-metadata".equals(candidateFile.getName())) {
            return true;
        }

        // For everything else, we'll defer to our enabled extensions:
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            if (extension.isKnownFile(candidateFile)) {
                return true; // first extension that says yes, we're done
            }
        }
        return false;
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
    public JComponent getExtraPanelComponent(ImageViewerExtension.ExtraPanelPosition position) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            JComponent component = extension.getExtraPanelComponent(position);
            if (component != null) {
                return component; // return the first one we find for this position
            }
        }
        return null;
    }

    /**
     * Invoked when the application is building the Quick Move dialog - any actions returned
     * here will be added as buttons on that dialog.
     *
     * @return A list of actions to add to the quick move dialog, or null for none.
     */
    public List<AbstractAction> getQuickMoveDialogActions() {
        List<AbstractAction> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            List<AbstractAction> toAdd = extension.getQuickMoveDialogActions();
            if (toAdd != null) {
                list.addAll(toAdd);
            }
        }
        return list;
    }

    /**
     * Informational message that will be sent to all extensions when the quick move tree
     * changes - extensions can do what they need in order to refresh based on the new
     * tree (and QuickMoveDialog getSelectedNode() can tell you what, if anything,
     * is currently selected).
     */
    public void quickMoveTreeChanged() {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.quickMoveTreeChanged();
        }
    }

    /**
     * An informational message that is sent whenever the main image panel is loaded with
     * a new image.
     *
     * @param selectedImage An ImageInstance containing the new image, if there is one.
     */
    public void imageSelected(ImageInstance selectedImage) {
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            extension.imageSelected(selectedImage);
        }
    }

    /**
     * Returns a combined list of all JPanels returned by all extensions to be displayed as
     * tabs in the main image tab panel. If no extension returns anything, the main image
     * tab panel is hidden.
     */
    public List<JPanel> getImageTabPanels() {
        List<JPanel> list = new ArrayList<>();
        for (ImageViewerExtension extension : getEnabledLoadedExtensions()) {
            list.addAll(extension.getImageTabPanels());
        }
        return list;
    }
}
