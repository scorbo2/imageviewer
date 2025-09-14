package ca.corbett.imageviewer.ui;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.dirtree.DirTree;
import ca.corbett.extras.dirtree.DirTreeListener;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.logging.LogConsole;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ImageOperationHandler;
import ca.corbett.imageviewer.KeyboardManager;
import ca.corbett.imageviewer.LogConsoleManager;
import ca.corbett.imageviewer.MenuManager;
import ca.corbett.imageviewer.QuickMoveManager;
import ca.corbett.imageviewer.ToolBarManager;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.actions.ReloadUIAction;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetManager;
import ca.corbett.imageviewer.ui.imagesets.ImageSetPanel;
import org.apache.commons.io.FileUtils;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the main window for the application.
 * Formerly (up to and including ImageViewer 1.3), most of the application logic was
 * here in this class. Starting with ImageViewer 2.0, this has been smartened up considerably,
 * and now most of the logic is contained within the various *Manager classes in
 * the main ca.corbett.imageviewer package. Also in 2.0, huge chunks of functionality were
 * extracted out of this project altogether and placed into extensions that can be loaded
 * dynamically. This makes the code base MUCH more flexible and easily extensible, and
 * also makes the code here in MainWindow a lot cleaner.
 *
 * @author scorbett
 * @since 2017-11-08
 */
public final class MainWindow extends JFrame implements UIReloadable {

    private static final Logger logger = Logger.getLogger(MainWindow.class.getName());

    public enum BrowseMode {
        FILE_SYSTEM,
        IMAGE_SET
    }

    public static final int MIN_WIDTH = 640;
    public static final int MIN_HEIGHT = 480;

    private MessageUtil messageUtil;
    private static MainWindow instance;
    private static JFileChooser fileChooser;

    private final ImageSetManager imageSetManager;
    private final MenuManager menuManager;
    private JToolBar toolBar;

    private BrowseMode browseMode;
    private JTabbedPane imgSrcTabPane;
    private final ImgSrcTabPaneListener imgSrcTabPaneListener;
    private ToggleableTabbedPane imageTabPane;
    private DirTree dirTree;
    private final DirTreeChangeListener dirTreeChangeListener;
    private ImageSetPanel imageSetPanel;
    private ThumbContainerPanel thumbContainerPanel;
    private JPanel mainWrapperPanel;
    private ImagePanel imagePanel;
    private ImagePanelConfig imagePanelProperties;
    private JLabel statusLabel1;
    private JLabel statusLabel2;
    private JSplitPane sideSplitPane;
    private JSplitPane mainSplitPane;

    // Optionally set via cmdline args in Main, this will override our saved startup dir if set:
    private File startupDir;

    /**
     * Constructor is private to force singleton access.
     */
    private MainWindow() {
        menuManager = new MenuManager();
        imageSetManager = new ImageSetManager();
        imgSrcTabPaneListener = new ImgSrcTabPaneListener();
        dirTreeChangeListener = new DirTreeChangeListener();
    }

    /**
     * Singleton accessor.
     *
     * @return The single instance of this window.
     */
    public static MainWindow getInstance() {
        if (instance == null) {
            instance = new MainWindow();

            logger.log(Level.INFO, "Loaded {0} extensions ({1} enabled).",
                       new Object[]{
                               ImageViewerExtensionManager.getInstance().getLoadedExtensionCount(),
                               ImageViewerExtensionManager.getInstance().getEnabledLoadedExtensions().size()});

            java.net.URL url = MainWindow.class.getResource("/ca/corbett/imageviewer/images/logo_IV.jpg");
            if (url != null) {
                Image image = Toolkit.getDefaultToolkit().createImage(url);
                instance.setIconImage(image);
                LogConsole.getInstance().setIconImage(image);
            }
            else {
                logger.warning("MainWindow: unable to load logo image from resources.");
            }
            LogConsole.getInstance().setTitle(Version.APPLICATION_NAME + " log console");

            instance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            instance.setTitle(Version.APPLICATION_NAME);
            instance.initComponents();
            KeyboardManager.addGlobalKeyListener(instance);

            instance.addWindowListener(new WindowAdapter() {
                /**
                 * Invoked when the user manually closes a window by clicking its X button
                 * or using a keyboard shortcut like Ctrl+Q or whatever. This event handler
                 * is NOT invoked when you manually dispose() the window (at least in my
                 * testing on linux mint).
                 */
                @Override
                public void windowClosing(WindowEvent e) {
                    instance.saveUIState();
                    ImageViewerExtensionManager.getInstance().deactivateAll();
                    QuickMoveManager.getInstance().close();
                    instance.imageSetManager.save();
                    logger.info("Application windowClosing(): finished cleanup.");
                }

                /**
                 * Invoked when you programmatically dispose() of the window. Note that the
                 * user manually closing the window via the OS does NOT invoke this handler
                 * (at least in my testing on linux mint).
                 */
                @Override
                public void windowClosed(WindowEvent e) {
                    instance.saveUIState();
                    ImageViewerExtensionManager.getInstance().deactivateAll();
                    QuickMoveManager.getInstance().close();
                    instance.imageSetManager.save();
                    logger.info("Application windowClosed(): finished cleanup.");
                }

            });

            // Custom LogConsole theme:
            LogConsoleManager.setCustomTheme();

            instance.imageSetManager.load();

            ReloadUIAction.getInstance().registerReloadable(instance);
        }

        return instance;
    }

    /**
     * Can be invoked before showing the form to set a starting directory, which will
     * override the starting directory saved in preferences. This will also override
     * the saved lock directory, if set, such that if the given startup dir is outside
     * our normal lock directory, we'll unlock the tree and show it anyway.
     * If set, this won't be persisted, and it won't overwrite the stored preference
     * for startup dir and/or lock dir. This means the next time you start the app with
     * no cmdline args, the startup dir and lock dir will be whatever they were before.
     * Invoking this after the form is shown does nothing. Must therefore be invoked from Main.
     *
     * @param dir The directory to show on startup.
     */
    public void setStartupDir(File dir) {
        startupDir = dir;
    }

    /**
     * Will force a reload of whatever image is currently showing in our image panel, if any.
     * Does nothing if no image is showing.
     * This is currently only invoked after one of the tool dialogs has modified an image and
     * we want to show the results (eg. CropDialog, ResizeDialog, etc).
     * TODO this won't update the thumbnail panel... maybe that's okay as it will
     * be reloaded next time navigating to this directory (at least for image crops).
     */
    public void reloadCurrentImage() {
        File imgFile = (File)imagePanel.getExtraAttribute("srcFile");
        if (imgFile == null) {
            return;
        }
        if (imagePanel.getImage() != null) {
            imagePanel.getImage().flush();
        }
        // Load the new image:
        try {
            if (imgFile.getName().toLowerCase().endsWith(".gif")) {
                ImageIcon icon = ImageUtil.loadImageIcon(imgFile);
                imagePanel.setImageIcon(icon);
            }
            else {
                BufferedImage image = ImageUtil.loadImage(imgFile);
                imagePanel.setImage(image);
            }
            updateStatusBar();
            ImageViewerExtensionManager.getInstance().imageSelected(getSelectedImage());
        }
        catch (IOException ioe) {
            getMessageUtil().error("Image load error", "Unable to load image.", ioe);
        }
        updateStatusBar();
    }

    /**
     * MainWindow maintains a JFileChooser that can be shared across other parts of the code
     * that may need to pop one. The advantage of using a shared JFileChooser instead of
     * creating a new one as needed is that it will remember the last directory it was
     * in, which saves you from having to continually renavigate back to the same place.
     *
     * @param dialogTitle   The title for the dialog
     * @param selectionMode one of the JFileChooser selection modes, like DIRECTORIES_ONLY.
     * @return A shared JFileChooser instance
     */
    public JFileChooser getFileChooser(String dialogTitle, int selectionMode) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setFileSelectionMode(selectionMode);
        return fileChooser;
    }

    public BrowseMode getBrowseMode() {
        return browseMode;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    public ImageSetManager getImageSetManager() {
        return imageSetManager;
    }

    public void setBrowseMode(BrowseMode mode) {
        setBrowseMode(mode, true);
    }

    public void setBrowseMode(BrowseMode mode, boolean autoLoad) {
        // Reject no-op requests:
        if (mode == browseMode) {
            return;
        }

        imgSrcTabPane.removeChangeListener(imgSrcTabPaneListener);
        browseMode = mode;
        switch (browseMode) {
            case FILE_SYSTEM:
                imgSrcTabPane.setSelectedIndex(0);
                break;
            case IMAGE_SET:
                imgSrcTabPane.setSelectedIndex(1);
                break;
        }
        imgSrcTabPane.addChangeListener(imgSrcTabPaneListener);

        menuManager.setBrowseMode(mode);
        rebuildMenus();

        // if autoLoad is set, we will force a load of whatever is selected in the new tab.
        // Callers can set this to false if they want to load something else.
        if (!autoLoad) {
            return;
        }

        switch (browseMode) {
            case FILE_SYSTEM:
                if (imgSrcTabPane.getSelectedIndex() != 0) {
                    imgSrcTabPane.setSelectedIndex(0);
                }
                setDirectory(dirTree.getCurrentDir());
                break;

            case IMAGE_SET:
                if (imgSrcTabPane.getSelectedIndex() != 1) {
                    imgSrcTabPane.setSelectedIndex(1);
                }
                setImageSet(imageSetPanel.getSelectedImageSet().orElse(null));
                break;
        }
    }

    public void rebuildMenus() {
        menuManager.rebuildAll();
        imagePanel.setPopupMenu(menuManager.buildImagePanelPopupMenu());
        ToolBarManager.rebuildMenus();
    }

    /**
     * Invoke this to inform the MainWindow when the currently selected image has been
     * removed (for example, from a move or delete action). The thumbpanel and the status
     * bar will be updated, and the next available image will be selected (if there is one).
     */
    public void selectedImageRemoved() {
        thumbContainerPanel.removeSelected();
        updateStatusBar();
    }

    /**
     * Invoke this to inform the MainWindow when the currently selected image file has
     * been renamed. The thumbpanel will be updated.
     *
     * @param newFile The File object representing the new image file. Assumed to be in same dir.
     */
    public void selectedImageRenamed(File newFile) {
        thumbContainerPanel.renameSelected(newFile);
        imagePanel.setExtraAttribute("srcFile", newFile);
    }

    /**
     * Returns the list of files currently being shown, whether they have been loaded yet or not.
     *
     * @return A List of File objects representing all the images in the current directory.
     */
    public List<File> getCurrentFileList() {
        return thumbContainerPanel.getImageFiles();
    }

    /**
     * Returns the list of unrecognized files in the current directory.
     *
     * @return A List of File objects representing all unknown files in the current directory.
     */
    public List<File> getCurrentAlienFileList() {
        return thumbContainerPanel.getAliens();
    }

    public void redrawImagePanel() {
        mainWrapperPanel.invalidate();
        mainWrapperPanel.revalidate();
        mainWrapperPanel.repaint();
    }

    /**
     * Internal meethod to set up the main window and all its components.
     */
    private void initComponents() {
        setSize(MIN_WIDTH, MIN_HEIGHT);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

        dirTree = DirTree.createDirTree();
        dirTree.setMinimumSize(new Dimension(180, 100));
        dirTree.setPreferredSize(new Dimension(180, 200));
        dirTree.addDirTreeListener(dirTreeChangeListener);

        imageSetPanel = new ImageSetPanel();

        browseMode = BrowseMode.FILE_SYSTEM;
        imgSrcTabPane = new JTabbedPane();
        imgSrcTabPane.addTab("File system", dirTree);
        imgSrcTabPane.addTab("Image sets", imageSetPanel);
        imgSrcTabPane.addChangeListener(imgSrcTabPaneListener);

        thumbContainerPanel = ThumbContainerPanel.createThumbContainer();
        thumbContainerPanel.setMinimumSize(new Dimension(180, 100));
        thumbContainerPanel.addListener(new ThumbContainerPanelListener() {
            @Override
            public void thumbnailSelected(ThumbContainerPanel source, ThumbPanel pn) {
                // Flush the old image if present:
                if (imagePanel.getImage() != null) {
                    imagePanel.getImage().flush();
                }
                // Load the new image:
                try {
                    File imgFile = pn.getFile();
                    if (imgFile.getName().toLowerCase().endsWith(".gif")) {
                        ImageIcon icon = ImageUtil.loadImageIcon(imgFile);
                        imagePanel.setImageIcon(icon);
                    }
                    else {
                        BufferedImage image = ImageUtil.loadImage(pn.getFile());
                        imagePanel.setImage(image);
                    }
                }
                catch (IOException ioe) {
                    getMessageUtil().error("Image load error", "Unable to load image.", ioe);
                }
                imagePanel.setExtraAttribute("srcFile", pn.getFile());
                ImageViewerExtensionManager.getInstance().imageSelected(getSelectedImage());
                updateStatusBar();
            }

            @Override
            public void selectionCleared(ThumbContainerPanel source) {
                imagePanel.setImage(null);
                imagePanel.setExtraAttribute("srcFile", null);
                ImageViewerExtensionManager.getInstance().imageSelected(getSelectedImage());
                updateStatusBar();
            }

            @Override
            public void loadStarting(ThumbContainerPanel source) {
                dirTree.setEnabled(false);
            }

            @Override
            public void loadCompleted(ThumbContainerPanel source) {
                dirTree.setEnabled(true);
            }

        });
        JScrollPane thumbScrollPane = new JScrollPane(thumbContainerPanel);
        thumbScrollPane.getVerticalScrollBar().setUnitIncrement(20);

        imagePanelProperties = ImagePanelConfig.createDefaultProperties();
        imagePanelProperties.setMagnifierCursor(null);
        imagePanelProperties.setEnableZoomOnMouseClick(false); // Need mouse events for popup menu
        imagePanelProperties.setZoomFactorIncrement(0.02);
        imagePanel = new ImagePanel(imagePanelProperties);

        mainWrapperPanel = new JPanel();
        mainWrapperPanel.setLayout(new BorderLayout());

        // Add extra panels, if any are supplied by our extensions:
        JComponent westComponent = ImageViewerExtensionManager.getInstance().getExtraPanelComponent(
                ImageViewerExtension.ExtraPanelPosition.Left);
        JComponent eastComponent = ImageViewerExtensionManager.getInstance().getExtraPanelComponent(
                ImageViewerExtension.ExtraPanelPosition.Right);
        JComponent northComponent = ImageViewerExtensionManager.getInstance().getExtraPanelComponent(
                ImageViewerExtension.ExtraPanelPosition.Top);
        JComponent southComponent = ImageViewerExtensionManager.getInstance().getExtraPanelComponent(
                ImageViewerExtension.ExtraPanelPosition.Bottom);
        if (westComponent != null) {
            mainWrapperPanel.add(westComponent, BorderLayout.WEST);
        }
        if (eastComponent != null) {
            mainWrapperPanel.add(eastComponent, BorderLayout.EAST);
        }
        if (northComponent != null) {
            mainWrapperPanel.add(northComponent, BorderLayout.NORTH);
        }
        if (southComponent != null) {
            mainWrapperPanel.add(southComponent, BorderLayout.SOUTH);
        }

        imageTabPane = new ToggleableTabbedPane();
        imageTabPane.addTab("Image", imagePanel);
        mainWrapperPanel.add(imageTabPane, BorderLayout.CENTER);

        // See if extensions have any image tab panes for us:
        List<JPanel> imageTabs = ImageViewerExtensionManager.getInstance().getImageTabPanels();
        if (!imageTabs.isEmpty()) {
            int tabNumber = 1;
            for (JPanel tabPanel : imageTabs) {
                String name = tabPanel.getName() == null ? "Tab " + tabNumber : tabPanel.getName();
                imageTabPane.add(name, tabPanel);
                tabNumber++;
            }
        }

        // If no extension supplies any image tabs, then don't show a tab pane:
        else {
            imageTabPane.setTabHeaderVisible(false);
        }

        sideSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, imgSrcTabPane, thumbScrollPane);
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideSplitPane, mainWrapperPanel);
        //TODO this behaves horribly in certain look and feels: mainSplitPane.setOneTouchExpandable(true);

        setLayout(new BorderLayout());
        add(mainSplitPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusLabel1 = new JLabel("Ready.");
        statusLabel2 = new JLabel("");
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new FlowLayout(FlowLayout.LEFT));
        wrapper.add(statusLabel1);
        statusPanel.add(wrapper, BorderLayout.CENTER);
        wrapper = new JPanel();
        wrapper.setLayout(new FlowLayout(FlowLayout.RIGHT));
        wrapper.add(statusLabel2);
        statusPanel.add(wrapper, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);

        toolBar = ToolBarManager.buildToolBar();
        add(toolBar, BorderLayout.PAGE_START);

        // Build up our various menus:
        rebuildMenus();
        setJMenuBar(menuManager.getMainMenuBar());
        ImageViewerExtensionManager.getInstance().quickMoveTreeChanged(); // TODO why are we invoking this here???
    }

    /**
     * Selects the previous image. If there are no images in the current directory, or
     * if the currently selected image is the first in the list, this does nothing.
     */
    public void selectPreviousImage() {
        thumbContainerPanel.selectPrevious();
    }

    /**
     * Selects the next image. If there are no images in the current directory, or
     * if the currently selected image is the last in the list, this does nothing.
     */
    public void selectNextImage() {
        thumbContainerPanel.selectNext();
    }

    /**
     * Zooms in on the currently selected image, if there is one.
     */
    public void zoomIn() {
        imagePanel.zoomIn();
    }

    /**
     * Zooms out in the currently selected image, if there is one.
     */
    public void zoomOut() {
        imagePanel.zoomOut();
    }

    /**
     * Zooms the currently selected image to best fit the display.
     */
    public void zoomBestFit() {
        imagePanel.zoomBestFit();
    }

    /**
     * Zooms the currently selected image to actual size.
     */
    public void zoomActualSize() {
        imagePanel.setZoomFactor(1.0);
    }

    /**
     * Overridden here so we can set certain GUI preferences that can't be set until
     * the controls are visible (JSplitPane positions and such).
     *
     * @param visible Whether to show or hide the form.
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            // Don't invoke this on this thread as our loadUIState() below seems to get into a race
            // condition with the call to super.setVisible() above. Sometimes we try to set the
            // window dimensions before the window is actually visible, and it has no effect.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    loadUIState();

                    // Let all enabled extensions know we're starting up:
                    ImageViewerExtensionManager.getInstance().activateAll();

                    logger.info("MainWindow ready.");
                }

            });
        }
    }

    /**
     * Disables the DirTree to prevent changing the current selection.
     */
    public void disableDirTree() {
        dirTree.setEnabled(false);
    }

    /**
     * Enables the DirTree and allows changing current selection.
     */
    public void enableDirTree() {
        dirTree.setEnabled(true);
    }

    /**
     * Internal method to load our UI state.
     */
    private void loadUIState() {
        logger.fine("MainWindow.loadUIState()");

        AppConfig prefs = AppConfig.getInstance();
        sideSplitPane.setDividerLocation(prefs.getSideSplitPanePosition());
        mainSplitPane.setDividerLocation(prefs.getMainSplitPanePosition());
        this.setSize(prefs.getMainWindowWidth(), prefs.getMainWindowHeight());

        File effectiveStartupDir = (startupDir == null) ? prefs.getStartupDirectory() : startupDir;

        File lockDir = prefs.getLockDirectory();
        if (lockDir == null) {
            lockDir = new File("/");
        }
        if (effectiveStartupDir == null) {
            dirTree.lock(lockDir); // no startup dir was given; go ahead with the lock.
        }
        else if (effectiveStartupDir.getAbsolutePath().startsWith(lockDir.getAbsolutePath())) {
            dirTree.lock(lockDir); // startupDir is inside lockDir, we're fine. Else don't lock it.
        }
        dirTree.selectAndScrollTo(effectiveStartupDir); // okay if null

        setImageBackgroundColor(UIManager.getDefaults().getColor("ColorPalette.primaryBackground"));
        setZoomFactorIncrement(prefs.getImagePanelZoomIncrement());
        setAutoBestFit(prefs.getImagePanelAutoBestFit());
    }

    /**
     * Internal method to save current UI state.
     */
    private void saveUIState() {
        logger.fine("MainWindow.saveUIState()");

        AppConfig prefs = AppConfig.getInstance();
        prefs.setSideSplitPanePosition(sideSplitPane.getDividerLocation());
        prefs.setMainSplitPanePosition(mainSplitPane.getDividerLocation());
        prefs.setMainWindowWidth(this.getWidth());
        prefs.setMainWindowHeight(this.getHeight());

        // Only save lock dir and current dir if we were NOT given a startup
        // directory on the command line. Otherwise our settings were transient.
        if (startupDir == null) {
            prefs.setLockDirectory(dirTree.getRootDir());
            prefs.setStartupDirectory(dirTree.getCurrentDir());
        }

        prefs.save();
    }

    /**
     * Reloads all extension-configurable elements of the UI, which is most of them.
     * The main menu will be rebuilt, all popup menus (ImagePanel and ToolBar) will be
     * rebuilt, the AppPreferences dialog will be updated, and the current directory
     * will be reloaded. This is intended to be invoked after an extension is modified
     * or disabled at runtime (at any point after the initial load of MainWindow).
     */
    @Override
    public void reloadUI() {

        // Nuke the toolbar and rebuild it from scratch:
        remove(toolBar);
        toolBar = ToolBarManager.buildToolBar();
        add(toolBar, BorderLayout.PAGE_START);

        // Build up our various menus:
        rebuildMenus();
        ImageViewerExtensionManager.getInstance().quickMoveTreeChanged(); // TODO why do we call this here?

        // Rebuild the image tab pane:
        imageTabPane.removeAll();
        imageTabPane.addTab("Image", imagePanel);
        // See if extensions have any image tab panes for us:
        List<JPanel> imageTabs = ImageViewerExtensionManager.getInstance().getImageTabPanels();
        if (!imageTabs.isEmpty()) {
            imageTabPane.setTabHeaderVisible(true);
            int tabNumber = 1;
            for (JPanel tabPanel : imageTabs) {
                String name = tabPanel.getName() == null ? "Tab " + tabNumber : tabPanel.getName();
                imageTabPane.add(name, tabPanel);
                tabNumber++;
            }
        }

        // If no extension supplies any image tabs, then don't show a tab pane:
        else {
            imageTabPane.setTabHeaderVisible(false);
        }

        reload();
    }

    public void reload() {
        if (browseMode == BrowseMode.FILE_SYSTEM) {
            reloadCurrentDirectory();
        }
        else {
            setImageSet(imageSetPanel.getSelectedImageSet().orElse(null));
        }
    }

    public void reloadCurrentDirectory() {
        thumbContainerPanel.removeAll();
        thumbContainerPanel.reloadThumbSizePreference();
        if (dirTree.getCurrentDir() != null) {
            dirTreeChangeListener.selectionChanged(dirTree, dirTree.getCurrentDir());
        }
        updateStatusBar();
    }

    public void setImageBackgroundColor(Color color) {
        imagePanelProperties.setBgColor(color);
        thumbContainerPanel.setBackground(color);
        imagePanel.applyProperties(imagePanelProperties);
        ImageViewerExtensionManager.getInstance().imagePanelBackgroundChanged(color);
    }

    public void setZoomFactorIncrement(double increment) {
        imagePanelProperties.setZoomFactorIncrement(increment);
        imagePanel.applyProperties(imagePanelProperties);
    }

    public void setAutoBestFit(boolean bestFit) {
        imagePanelProperties.setDisplayMode(ImagePanelConfig.DisplayMode.BEST_FIT);
        imagePanel.applyProperties(imagePanelProperties);
    }

    /**
     * Returns the directory currently being browsed.
     *
     * @return A File object representing the current directory.
     */
    public File getCurrentDirectory() {
        return dirTree.getCurrentDir();
    }

    public ImageSetPanel getImageSetPanel() {
        return imageSetPanel;
    }

    /**
     * Flips the image panel tab pane back to the first tab (the main image panel).
     */
    public void resetSelectedImagePanelTab() {
        setSelectedImagePanelTab(null);
    }

    /**
     * Flips the image panel tab pane to the given tab name, if it exists.
     * If the given name is null or blank, this is equivalent to resetSelectedImagePanelTab.
     * If the given name is not found, nothing happens.
     */
    public void setSelectedImagePanelTab(String name) {
        if (name == null || name.isBlank()) {
            imageTabPane.setSelectedIndex(0);
            return;
        }
        for (Component component : imageTabPane.getComponents()) {
            if (name.equals(component.getName())) {
                imageTabPane.setSelectedComponent(component);
                return;
            }
        }
    }

    /**
     * Deletes the currently selected image, if there is one selected.
     * This is shorthand for ImageOperationHandler.deleteImage(), which does the same thing.
     */
    public void deleteSelectedImage() {
        ImageOperationHandler.deleteImage();
    }

    /**
     * Returns an informational object about the currently selected image, if any.
     *
     * @return An ImageInstance object containing information about the current image.
     */
    public ImageInstance getSelectedImage() {
        return new ImageInstance((File)imagePanel.getExtraAttribute("srcFile"),
                                 imagePanel.getImage(),
                                 imagePanel.getImageIcon());
    }

    /**
     * Updates the status label with details about the currently selected image.
     */
    private void updateStatusBar() {
        String status1 = "Ready.";
        String status2 = "";
        int index = thumbContainerPanel.getSelectionIndex();
        if (index != -1) {
            int size = thumbContainerPanel.getCount();
            status2 = (index + 1) + " of " + size + "    ";

            File srcFile = ((File)imagePanel.getExtraAttribute("srcFile"));
            long fileSize = srcFile.length();
            status1 = FileUtils.byteCountToDisplaySize(fileSize);

            long memorySize = imagePanel.getImageWidth()
                    * imagePanel.getImageHeight() * 3;
            status1 += " (" + FileUtils.byteCountToDisplaySize(memorySize) + " in memory), ";

            status1 += imagePanel.getImageWidth() + "x" + imagePanel.getImageHeight();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            status1 += ", " + format.format(new java.util.Date(srcFile.lastModified()));
        }

        statusLabel1.setText(status1);
        statusLabel2.setText(status2);

        final JPanel panel = (JPanel)statusLabel1.getParent();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                panel.invalidate();
                panel.revalidate();
                panel.repaint();
            }

        });
    }

    /**
     * Navigates to the specified directory (can be null for no selection), and switches
     * browse mode to FILE_SYSTEM. This will update all menus and the window title as needed.
     */
    public void setDirectory(File selectedDir) {
        setBrowseMode(BrowseMode.FILE_SYSTEM, false);

        // Select it in the tree without triggering a change event:
        dirTree.removeDirTreeListener(dirTreeChangeListener);
        dirTree.selectAndScrollTo(selectedDir);
        dirTree.addDirTreeListener(dirTreeChangeListener);

        if (selectedDir == null) {
            setTitle(Version.NAME);
        }
        else {
            setTitle(Version.NAME + " [File system] " + selectedDir.getAbsolutePath());
        }

        thumbContainerPanel.setDirectory(selectedDir); // handles nulls
    }

    /**
     * Selects and displays the given ImageSet (can be null for no selection), and switches
     * browse mode to IMAGE_SET. This will update all menus and the window title as needed.
     */
    public void setImageSet(ImageSet set) {
        setBrowseMode(BrowseMode.IMAGE_SET, false);

        if (set == null) {
            setTitle(Version.NAME);
        }
        else {
            setTitle(Version.NAME + " [Image set] " + set.getFullyQualifiedName());
        }

        thumbContainerPanel.setImageSet(set); // handles nulls
    }

    public void showMessageDialog(String title, String message) {
        getMessageUtil().info(title, message);
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(this, logger);
        }
        return messageUtil;
    }

    private static class DirTreeChangeListener implements DirTreeListener {

        @Override
        public void selectionChanged(DirTree source, File selectedDir) {
            MainWindow.getInstance().setDirectory(selectedDir);
        }

        @Override
        public void treeLocked(DirTree source, File lockDir) {
        }

        @Override
        public void treeUnlocked(DirTree source) {
        }
    }

    private static class ImgSrcTabPaneListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            final MainWindow mw = MainWindow.getInstance();
            mw.thumbContainerPanel.clear();
            switch (mw.imgSrcTabPane.getSelectedIndex()) {
                case 0:
                    mw.setBrowseMode(BrowseMode.FILE_SYSTEM);
                    break;
                case 1:
                    mw.setBrowseMode(BrowseMode.IMAGE_SET);
                    break;
            }
        }
    }
}
