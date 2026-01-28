package ca.corbett.imageviewer;

import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.AboutAction;
import ca.corbett.imageviewer.ui.actions.ExitAction;
import ca.corbett.imageviewer.ui.actions.ImageOperationAction;
import ca.corbett.imageviewer.ui.actions.ImageSetAddAllImagesAction;
import ca.corbett.imageviewer.ui.actions.ImageSetAddImageAction;
import ca.corbett.imageviewer.ui.actions.ImageSetBrowseToSourceDirAction;
import ca.corbett.imageviewer.ui.actions.ImageSetCreateAction;
import ca.corbett.imageviewer.ui.actions.ImageSetDeleteAction;
import ca.corbett.imageviewer.ui.actions.ImageSetMoveImageAction;
import ca.corbett.imageviewer.ui.actions.ImageSetRemoveImageAction;
import ca.corbett.imageviewer.ui.actions.LogConsoleAction;
import ca.corbett.imageviewer.ui.actions.ManageExtensionsAction;
import ca.corbett.imageviewer.ui.actions.NextImageAction;
import ca.corbett.imageviewer.ui.actions.PreferencesAction;
import ca.corbett.imageviewer.ui.actions.PreviousImageAction;
import ca.corbett.imageviewer.ui.actions.QuickMoveEditAction;
import ca.corbett.imageviewer.ui.actions.ReloadAction;
import ca.corbett.imageviewer.ui.actions.RenameAction;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class that can build up menus for the main window menu bar and the image panel popup menu.
 * <p>
 * <b>Menus change based on browse mode!</b> - in the main window, we can browse images from the
 * file system or from an image set. The available options in our menus may change depending on
 * the browse mode given to setBrowseMode(). The setBrowseMode() method will update the main
 * menu bar automatically, but if you have created a popup menu to be used anywhere else
 * (main image panel, toolbar buttons, etc), then you should rebuild those menus using the
 * facilities in this class whenever the browse mode changes.
 * </p>
 * <p>
 * <b>Extensions can supply menu items!</b> - extensions will be queried for top-level menus,
 * if they supply any, and extensions can also optionally supply extra menu items for the File,
 * Edit, View, and Help menus.
 * </p>
 *
 * @author scorbo2
 * @since 2017-11-12
 */
public final class MenuManager {

    /**
     * If any menu actions use an icon, we will unconditionally scale them to this size.
     * Unlike the toolbar icons, this size is not user-configurable.
     */
    public static final int MENU_ICON_SIZE = 18;

    private final JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu viewMenu;
    private JMenu settingsMenu;
    private JMenu helpMenu;
    private MainWindow.BrowseMode browseMode;

    /**
     * Creates and builds the main menu bar using the default browse mode of FILE_SYSTEM.
     * The result can be retrieved using getMainMenuBar().
     */
    public MenuManager() {
        browseMode = MainWindow.BrowseMode.FILE_SYSTEM;
        menuBar = new JMenuBar();
        rebuildAll();
    }

    /**
     * Clears the menu bar and rebuilds it using the given browse mode.
     */
    public void setBrowseMode(MainWindow.BrowseMode mode) {
        browseMode = mode;
        rebuildAll();
    }

    /**
     * Returns the current menu bar without rebuilding it.
     */
    public JMenuBar getMainMenuBar() {
        return menuBar;
    }

    /**
     * Forces a clean and rebuild of the menu bar using the current browse mode.
     */
    public void rebuildAll() {
        rebuildMenuBar();
        rebuildFileMenu();
        rebuildEditMenu();
        rebuildViewMenu();
        rebuildSettingsMenu();
        rebuildHelpMenu();
    }

    /**
     * Builds and returns a new JPopupMenu for use on the main image panel, based on
     * the current browse mode. Unlike the main menu, this popup menu is recreated
     * every time this method is called.
     */
    public JPopupMenu buildImagePanelPopupMenu() {
        JPopupMenu imagePanelPopupMenu = new JPopupMenu();

        // Quick Move:
        List<JMenuItem> menuItems = buildImageMovementMenuItems();
        for (Component c : menuItems) {
            imagePanelPopupMenu.add(c);
        }

        imagePanelPopupMenu.addSeparator();
        menuItems = buildImageRemovalMenuItems();
        for (Component c : menuItems) {
            imagePanelPopupMenu.add(c);
        }

        imagePanelPopupMenu.addSeparator();

        if (browseMode == MainWindow.BrowseMode.FILE_SYSTEM) {
            imagePanelPopupMenu.add(buildImageSetMenu());
            imagePanelPopupMenu.add(new ImageSetAddAllImagesAction());
        }

        if (browseMode == MainWindow.BrowseMode.IMAGE_SET) {
            imagePanelPopupMenu.add(new JMenuItem(new ImageSetBrowseToSourceDirAction()));
        }

        imagePanelPopupMenu.add(new JMenuItem(new RenameAction()));

        // Add any menu items from our extensions, if any:
        List<JMenuItem> extensionItems = ImageViewerExtensionManager.getInstance().getPopupMenuItems(browseMode);
        for (JMenuItem extensionItem : extensionItems) {
            imagePanelPopupMenu.add(extensionItem);
        }

        return imagePanelPopupMenu;
    }

    /**
     * Invoked internally to clean and rebuild the top-level menus.
     */
    private void rebuildMenuBar() {
        menuBar.removeAll();

        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(editMenu);

        // Any extension-provided top-level menu can go in between Edit and View:
        List<JMenu> extensionMenus = ImageViewerExtensionManager.getInstance().getTopLevelMenus(browseMode);
        if (!extensionMenus.isEmpty()) {
            for (JMenu extensionMenu : extensionMenus) {
                menuBar.add(extensionMenu);
            }
        }

        viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(viewMenu);

        settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(settingsMenu);

        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);
    }

    /**
     * Invoked internally to clean and rebuild the File menu.
     */
    private void rebuildFileMenu() {
        fileMenu.removeAll();

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> items = ImageViewerExtensionManager.getInstance().getMenuItems("File", browseMode);
        if (!items.isEmpty()) {
            for (JMenuItem item : items) {
                fileMenu.add(item);
            }
            fileMenu.addSeparator();
        }

        JMenuItem exitItem = new JMenuItem(new ExitAction());
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        fileMenu.add(exitItem);
    }

    /**
     * Invoked internally to clean and rebuild the Edit menu.
     */
    private void rebuildEditMenu() {
        editMenu.removeAll();

        List<JMenuItem> items = buildImageMovementMenuItems();
        for (JMenuItem item : items) {
            editMenu.add(item);
        }
        editMenu.addSeparator();

        items = buildImageRemovalMenuItems();
        for (JMenuItem item : items) {
            editMenu.add(item);
        }

        editMenu.addSeparator();
        editMenu.add(new JMenuItem(new RenameAction()));

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> extensionItems = ImageViewerExtensionManager.getInstance().getMenuItems("Edit", browseMode);
        for (JMenuItem extensionItem : extensionItems) {
            editMenu.add(extensionItem);
        }
        editMenu.addSeparator();

        editMenu.add(new JMenuItem(new PreferencesAction(MENU_ICON_SIZE)));

    }

    /**
     * Invoked internally to clean and rebuild the View menu.
     */
    private void rebuildViewMenu() {
        viewMenu.removeAll();

        JMenuItem item = new JMenuItem(new PreviousImageAction(MENU_ICON_SIZE));
        item.setMnemonic(KeyEvent.VK_P);
        viewMenu.add(item);

        item = new JMenuItem(new NextImageAction(MENU_ICON_SIZE));
        item.setMnemonic(KeyEvent.VK_N);
        viewMenu.add(item);

        item = new JMenuItem(new ReloadAction(MENU_ICON_SIZE));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        viewMenu.add(item);

        if (browseMode == MainWindow.BrowseMode.IMAGE_SET) {
            viewMenu.add(new JMenuItem(new ImageSetBrowseToSourceDirAction()));
        }

        viewMenu.addSeparator();

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> items = ImageViewerExtensionManager.getInstance().getMenuItems("View", browseMode);
        if (!items.isEmpty()) {
            for (JMenuItem extensionItem : items) {
                viewMenu.add(extensionItem);
            }
            viewMenu.addSeparator();
        }

        item = new JMenuItem(new LogConsoleAction());
        item.setMnemonic(KeyEvent.VK_L);
        viewMenu.add(item);
    }

    /**
     * Invoked internally to clean and rebuild the Settings menu.
     */
    private void rebuildSettingsMenu() {
        settingsMenu.removeAll();

        // Note... we don't currently allow extensions to add menu items here, but we could.
        // For now, it's just Application preferences and Manage extensions.
        JMenuItem prefsItem = new JMenuItem(new PreferencesAction(MENU_ICON_SIZE));
        prefsItem.setMnemonic(KeyEvent.VK_P);
        settingsMenu.add(prefsItem);

        JMenuItem extensionsItem = new JMenuItem(new ManageExtensionsAction(MENU_ICON_SIZE));
        extensionsItem.setMnemonic(KeyEvent.VK_E);
        settingsMenu.add(extensionsItem);
    }

    /**
     * Invoked internally to clean and rebuild the Help menu.
     */
    private void rebuildHelpMenu() {
        helpMenu.removeAll();

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> items = ImageViewerExtensionManager.getInstance().getMenuItems("Help", browseMode);
        if (!items.isEmpty()) {
            for (JMenuItem extensionItem : items) {
                helpMenu.add(extensionItem);
            }
            helpMenu.addSeparator();
        }

        JMenuItem aboutItem = new JMenuItem(new AboutAction(MENU_ICON_SIZE));
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        helpMenu.add(aboutItem);
    }

    /**
     * Invoked internally to build the image movement menu items based on the current browse mode.
     * In filesystem mode, these options can be quite complex, and include options for moving,
     * copying, and linking images or directories to pre-configured locations. In image set mode,
     * the options are much simpler, just allowing moving or copying the current image to another
     * image set. In filesystem mode, the individual operations of move, copy, and link can all
     * be enabled or disabled in application settings. Image set operations currently
     * cannot be disabled.
     */
    public List<JMenuItem> buildImageMovementMenuItems() {
        List<JMenuItem> menuList = new ArrayList<>();

        if (browseMode == MainWindow.BrowseMode.FILE_SYSTEM) {
            JMenu moveImageMenu = new JMenu("Quick Move this image...");
            JMenu moveAllImagesMenu = new JMenu("Quick Move all images in this directory...");
            JMenu moveDirMenu = new JMenu("Quick Move this directory...");
            JMenu copyImageMenu = new JMenu("Copy this image...");
            JMenu copyAllImagesMenu = new JMenu("Copy all images in this directory...");
            JMenu copyDirMenu = new JMenu("Copy this directory...");
            JMenu linkImageMenu = new JMenu("Link this image...");
            JMenu linkAllImagesMenu = new JMenu("Link all images in this directory...");
            JMenu linkDirMenu = new JMenu("Link this directory...");

            menuList.add(moveImageMenu);
            menuList.add(moveAllImagesMenu);
            menuList.add(moveDirMenu);
            moveImageMenu.setVisible(AppConfig.getInstance().isQuickMoveEnabled());
            moveAllImagesMenu.setVisible(AppConfig.getInstance().isQuickMoveEnabled());
            moveDirMenu.setVisible(AppConfig.getInstance().isQuickMoveEnabled());

            menuList.add(copyImageMenu);
            menuList.add(copyAllImagesMenu);
            menuList.add(copyDirMenu);
            copyImageMenu.setVisible(AppConfig.getInstance().isQuickCopyEnabled());
            copyAllImagesMenu.setVisible(AppConfig.getInstance().isQuickCopyEnabled());
            copyDirMenu.setVisible(AppConfig.getInstance().isQuickCopyEnabled());

            menuList.add(linkImageMenu);
            menuList.add(linkAllImagesMenu);
            menuList.add(linkDirMenu);
            linkImageMenu.setVisible(AppConfig.getInstance().isQuickLinkEnabled());
            linkAllImagesMenu.setVisible(AppConfig.getInstance().isQuickLinkEnabled());
            linkDirMenu.setVisible(AppConfig.getInstance().isQuickLinkEnabled());

            QuickMoveManager.TreeNode rootNode = QuickMoveManager.getInstance().getRootNode();

            if (rootNode != null && rootNode.getChildCount() > 0) {

                if (AppConfig.getInstance().isQuickMoveEnabled()) {
                    buildImageOperationMenuRecursive(rootNode, rootNode, moveImageMenu,
                                                     ImageOperation.moveSingleImage());
                    buildImageOperationMenuRecursive(rootNode, rootNode, moveAllImagesMenu,
                                                     ImageOperation.moveAllImages());
                    buildImageOperationMenuRecursive(rootNode, rootNode, moveDirMenu, ImageOperation.moveDirectory());
                }

                if (AppConfig.getInstance().isQuickCopyEnabled()) {
                    buildImageOperationMenuRecursive(rootNode, rootNode, copyImageMenu,
                                                     ImageOperation.copySingleImage());
                    buildImageOperationMenuRecursive(rootNode, rootNode, copyAllImagesMenu,
                                                     ImageOperation.copyAllImages());
                    buildImageOperationMenuRecursive(rootNode, rootNode, copyDirMenu, ImageOperation.copyDirectory());
                }

                if (AppConfig.getInstance().isQuickLinkEnabled()) {
                    buildImageOperationMenuRecursive(rootNode, rootNode, linkImageMenu,
                                                     ImageOperation.linkSingleImage());
                    buildImageOperationMenuRecursive(rootNode, rootNode, linkAllImagesMenu,
                                                     ImageOperation.linkAllImages());
                    buildImageOperationMenuRecursive(rootNode, rootNode, linkDirMenu, ImageOperation.linkDirectory());
                }
            }

            menuList.add(new JMenuItem(new QuickMoveEditAction()));
        }

        else {
            menuList.add(new JMenuItem(new ImageSetMoveImageAction(true)));
            menuList.add(new JMenuItem(new ImageSetMoveImageAction(false)));
        }

        return menuList;
    }

    /**
     * Creates and returns a list of "image removal" menu items. What we mean by "removal" depends on the
     * current browse mode. In filesystem mode, removal means deleting files or directories from disk.
     * In image set mode, removal means removing the image from the image set, or deleting the entire image set.
     */
    public List<JMenuItem> buildImageRemovalMenuItems() {
        List<JMenuItem> menuList = new ArrayList<>();

        if (browseMode == MainWindow.BrowseMode.FILE_SYSTEM) {
            menuList.add(
                    new JMenuItem(new ImageOperationAction("Delete this image", ImageOperation.deleteSingleImage())));
            menuList.add(new JMenuItem(
                    new ImageOperationAction("Delete all images in this directory", ImageOperation.deleteAllImages())));
            menuList.add(
                    new JMenuItem(new ImageOperationAction("Delete this directory", ImageOperation.deleteDirectory())));
        }

        else {
            menuList.add(new JMenuItem(new ImageSetRemoveImageAction(MENU_ICON_SIZE)));
            menuList.add(new JMenuItem(new ImageSetDeleteAction(MENU_ICON_SIZE)));
        }

        return menuList;
    }

    /**
     * Interrogates the current list of ImageSets to build out an "add to image set" menu.
     */
    public static JMenu buildImageSetMenu() {
        JMenu menu = new JMenu("Add to image set...");
        List<DefaultMutableTreeNode> topLevelNodes = MainWindow.getInstance().getImageSetPanel().getTopLevelNodes();
        for (DefaultMutableTreeNode topLevelNode : topLevelNodes) {
            buildImageSetMenuRecursive(topLevelNode, menu);
        }
        menu.add(new JMenuItem(new ImageSetCreateAction()));
        return menu;
    }

    /**
     * Invoked internally to recurse through the given tree node and generate menu items as
     * appropriate for the given ImageOperation into the given JMenu. This is only invoked
     * in filesystem browse mode.
     *
     * @param node    The QuickMoveManager.TreeNode in question
     * @param menu    The JMenu which will receive all menu items.
     * @param imageOp The ImageOperation to perform when a menu item is selected.
     */
    private static void buildImageOperationMenuRecursive(QuickMoveManager.TreeNode rootNode, QuickMoveManager.TreeNode node, JMenu menu, ImageOperation imageOp) {
        if (node != null && node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                QuickMoveManager.TreeNode childNode = (QuickMoveManager.TreeNode)node.getChildAt(i);
                if (childNode.getChildCount() > 0) {
                    JMenu subMenu = new JMenu(childNode.toString());
                    buildImageOperationMenuRecursive(rootNode, childNode, subMenu, imageOp);
                    menu.add(subMenu);
                }
                else if (childNode.getDirectory() != null) {
                    ImageOperation op = imageOp.copy();
                    op.setDestination(childNode.getDirectory());
                    menu.add(new JMenuItem(new ImageOperationAction(childNode.toString(), op)));
                }
            }

            // If we're at the root level, also add an option for performing this operation
            // to some arbitrary directory:
            if (node == rootNode) {
                menu.add(new JMenuItem(new ImageOperationAction("To directory...", imageOp)));
            }
        }
    }

    /**
     * Invoked internally to recurse through the given tree node and generate menu items
     * for adding images to image sets. This is only invoked in image set browse mode.
     */
    private static void buildImageSetMenuRecursive(DefaultMutableTreeNode node, JMenu menu) {
        if (node != null && node.getChildCount() > 0) {
            JMenu subMenu = new JMenu(node.getUserObject().toString());
            menu.add(subMenu);
            for (int i = 0; i < node.getChildCount(); i++) {
                buildImageSetMenuRecursive((DefaultMutableTreeNode)node.getChildAt(i), subMenu);
            }
        }
        else if (node != null && (node.getUserObject() instanceof ImageSet)) {
            menu.add(new ImageSetAddImageAction((ImageSet)node.getUserObject()));
        }
    }
}
