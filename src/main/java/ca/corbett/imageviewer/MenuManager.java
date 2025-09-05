package ca.corbett.imageviewer;

import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.AboutAction;
import ca.corbett.imageviewer.ui.actions.ExitAction;
import ca.corbett.imageviewer.ui.actions.ImageOperationAction;
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
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains static helper methods for building up various menus for use in the main window.
 *
 * @author scorbo2
 * @since 2017-11-12
 */
public final class MenuManager {

    private static final String MENU_LABEL_MOVE_ONE = "Quick Move this image...";
    private static final String MENU_LABEL_MOVE_ALL = "Quick Move all images in this directory...";
    private static final String MENU_LABEL_MOVE_DIR = "Quick Move this directory...";
    private static final String MENU_LABEL_COPY_ONE = "Copy this image...";
    private static final String MENU_LABEL_COPY_ALL = "Copy all images in this directory...";
    private static final String MENU_LABEL_COPY_DIR = "Copy this directory...";
    private static final String MENU_LABEL_LINK_ONE = "Link this image...";
    private static final String MENU_LABEL_LINK_ALL = "Link all images in this directory...";
    private static final String MENU_LABEL_LINK_DIR = "Link this directory...";
    private static final String MENU_LABEL_FAVORITE = "Add to favorites...";

    private static JMenuBar menuBar;
    private static JMenu fileMenu;
    private static JMenu editMenu;
    private static JMenu viewMenu;
    private static JMenu settingsMenu;
    private static JMenu helpMenu;

    private MenuManager() {
    }

    /**
     * Builds the JMenuBar with all submenus, and returns it.
     *
     * @return A JMenuBar configured and ready to go.
     */
    public static JMenuBar buildMainMenuBar() {
        menuBar = new JMenuBar();
        menuBar.add(buildFileMenu());
        menuBar.add(buildEditMenu());

        // Any extension-provided top-level menu can go in between Edit and View:
        List<JMenu> extensionMenus = ImageViewerExtensionManager.getInstance().getTopLevelMenus();
        if (!extensionMenus.isEmpty()) {
            for (JMenu extensionMenu : extensionMenus) {
                menuBar.add(extensionMenu);
            }
        }

        menuBar.add(buildViewMenu());
        menuBar.add(buildSettingsMenu());
        menuBar.add(buildHelpMenu());
        return menuBar;
    }

    /**
     * Builds up a JPopupMenu with Quick Move and other options, which can then be attached
     * to an ImagePanel.
     *
     * @return A JPopupMenu suitable for use with an ImagePanel instance.
     */
    public static JPopupMenu buildImagePanelPopupMenu() {
        JPopupMenu imagePanelPopupMenu = new JPopupMenu();

        // Quick Move:
        List<Component> menuItems = buildQuickMoveMenuItems();
        for (Component c : menuItems) {
            imagePanelPopupMenu.add(c);
        }
        imagePanelPopupMenu.add(new JMenuItem(new QuickMoveEditAction()));

        imagePanelPopupMenu.addSeparator();
        menuItems = buildDeleteMenuItems();
        for (Component c : menuItems) {
            imagePanelPopupMenu.add(c);
        }

        imagePanelPopupMenu.addSeparator();
        imagePanelPopupMenu.add(buildFavoritesMenu());
        imagePanelPopupMenu.add(new JMenuItem(new RenameAction()));

        // Add any menu items from our extensions, if any:
        List<JMenuItem> extensionItems = ImageViewerExtensionManager.getInstance().getPopupMenuItems();
        for (JMenuItem extensionItem : extensionItems) {
            imagePanelPopupMenu.add(extensionItem);
        }

        return imagePanelPopupMenu;
    }

    /**
     * Creates the "File" menu and returns it.
     *
     * @return A JMenu containing all "File" menu items.
     */
    private static JMenu buildFileMenu() {
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> extensionItems = ImageViewerExtensionManager.getInstance().getMenuItems("File");
        if (!extensionItems.isEmpty()) {
            for (JMenuItem item : extensionItems) {
                fileMenu.add(item);
            }
            fileMenu.addSeparator();
        }

        JMenuItem exitItem = new JMenuItem(new ExitAction());
        exitItem.setMnemonic(KeyEvent.VK_X);
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
        fileMenu.add(exitItem);

        return fileMenu;
    }

    /**
     * Creates the "Edit" menu and returns it.
     *
     * @return A JMenu containing all "Edit" menu items.
     */
    private static JMenu buildEditMenu() {
        editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        List<Component> menuItems = buildQuickMoveMenuItems();
        for (Component c : menuItems) {
            editMenu.add(c);
        }
        editMenu.add(new JMenuItem(new QuickMoveEditAction()));
        editMenu.addSeparator();

        menuItems = buildDeleteMenuItems();
        for (Component c : menuItems) {
            editMenu.add(c);
        }
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(new RenameAction()));

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> extensionItems = ImageViewerExtensionManager.getInstance().getMenuItems("Edit");
        for (JMenuItem extensionItem : extensionItems) {
            editMenu.add(extensionItem);
        }
        editMenu.addSeparator();

        editMenu.add(new JMenuItem(new PreferencesAction()));

        return editMenu;
    }

    /**
     * Creates the "View" menu and returns it.
     *
     * @return A JMenu containing all "View" menu items.
     */
    private static JMenu buildViewMenu() {
        viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem item = new JMenuItem(new PreviousImageAction());
        item.setMnemonic(KeyEvent.VK_P);
        viewMenu.add(item);

        item = new JMenuItem(new NextImageAction());
        item.setMnemonic(KeyEvent.VK_N);
        viewMenu.add(item);

        item = new JMenuItem(new ReloadAction());
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        viewMenu.add(item);
        viewMenu.addSeparator();

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> extensionItems = ImageViewerExtensionManager.getInstance().getMenuItems("View");
        if (!extensionItems.isEmpty()) {
            for (JMenuItem extensionItem : extensionItems) {
                viewMenu.add(extensionItem);
            }
            viewMenu.addSeparator();
        }

        item = new JMenuItem(new LogConsoleAction());
        item.setMnemonic(KeyEvent.VK_L);
        viewMenu.add(item);

        return viewMenu;
    }

    /**
     * Creates the "Settings" menu and returns it.
     *
     * @return a JMenu containins all "Settings" menu items.
     */
    private static JMenu buildSettingsMenu() {
        settingsMenu = new JMenu("Settings");
        settingsMenu.setMnemonic(KeyEvent.VK_S);

        // Note... we don't currently allow extensions to add menu items here, but we could.
        // For now, it's just Application preferences and Manage extensions.
        JMenuItem prefsItem = new JMenuItem(new PreferencesAction());
        prefsItem.setMnemonic(KeyEvent.VK_P);
        settingsMenu.add(prefsItem);

        JMenuItem extensionsItem = new JMenuItem(new ManageExtensionsAction());
        extensionsItem.setMnemonic(KeyEvent.VK_E);
        settingsMenu.add(extensionsItem);

        return settingsMenu;
    }

    /**
     * Creates the "Help" menu and returns it.
     *
     * @return A JMenu containing all "Help" menu items.
     */
    private static JMenu buildHelpMenu() {
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        // Add any items to this list from our extensions, if any:
        List<JMenuItem> extensionItems = ImageViewerExtensionManager.getInstance().getMenuItems("Help");
        if (!extensionItems.isEmpty()) {
            for (JMenuItem extensionItem : extensionItems) {
                helpMenu.add(extensionItem);
            }
            helpMenu.addSeparator();
        }

        JMenuItem aboutItem = new JMenuItem(new AboutAction());
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        helpMenu.add(aboutItem);

        return helpMenu;
    }

    /**
     * Provides a way to rebuild the quick move menu tree in the Edit menu, if the
     * quick move destination tree has changed at runtime.
     */
    public static void rebuildQuickMoveEditMenu() {
        rebuildQuickMoveMenuItems(editMenu);
    }

    /**
     * Will scan the given JMenu looking for quick move menus, and rebuild them with the
     * quick move destination tree (assuming it has changed and needs to be rescanned).
     *
     * @param srcMenu The menu to be updated. Existing items will be replaced with new ones.
     */
    public static void rebuildQuickMoveMenuItems(JMenu srcMenu) {
        QuickMoveManager.TreeNode rootNode = QuickMoveManager.getInstance().getRootNode();
        for (int i = 0; i < srcMenu.getItemCount(); i++) {
            JMenuItem menuItem = srcMenu.getItem(i);
            if (menuItem instanceof JMenu) {
                JMenu menu = (JMenu)menuItem;
                if (null != menu.getText()) {
                    switch (menu.getText()) {
                        case MENU_LABEL_MOVE_ONE:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu,
                                                             ImageOperation.moveSingleImage());
                            menu.setVisible(AppConfig.getInstance().isQuickMoveEnabled());
                            break;
                        case MENU_LABEL_MOVE_ALL:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu, ImageOperation.moveAllImages());
                            menu.setVisible(AppConfig.getInstance().isQuickMoveEnabled());
                            break;
                        case MENU_LABEL_MOVE_DIR:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu, ImageOperation.moveDirectory());
                            menu.setVisible(AppConfig.getInstance().isQuickMoveEnabled());
                            break;
                        case MENU_LABEL_COPY_ONE:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu,
                                                             ImageOperation.copySingleImage());
                            menu.setVisible(AppConfig.getInstance().isQuickCopyEnabled());
                            break;
                        case MENU_LABEL_COPY_ALL:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu, ImageOperation.copyAllImages());
                            menu.setVisible(AppConfig.getInstance().isQuickCopyEnabled());
                            break;
                        case MENU_LABEL_COPY_DIR:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu, ImageOperation.copyDirectory());
                            menu.setVisible(AppConfig.getInstance().isQuickCopyEnabled());
                            break;
                        case MENU_LABEL_LINK_ONE:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu,
                                                             ImageOperation.linkSingleImage());
                            menu.setVisible(AppConfig.getInstance().isQuickLinkEnabled());
                            break;
                        case MENU_LABEL_LINK_ALL:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu, ImageOperation.linkAllImages());
                            menu.setVisible(AppConfig.getInstance().isQuickLinkEnabled());
                            break;
                        case MENU_LABEL_LINK_DIR:
                            menu.removeAll();
                            buildImageOperationMenuRecursive(rootNode, rootNode, menu, ImageOperation.linkDirectory());
                            menu.setVisible(AppConfig.getInstance().isQuickLinkEnabled());
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * Interrogates QuickMoveManager for its current tree of quick move destinations, and
     * builds up a recursive list of menus that can be inserted into a JMenu.
     *
     * @return A list of menu items containing quick move operations and destinations.
     */
    public static List<Component> buildQuickMoveMenuItems() {
        List<Component> menuList = new ArrayList<>();
        JMenu moveImageMenu = new JMenu(MENU_LABEL_MOVE_ONE);
        JMenu moveAllImagesMenu = new JMenu(MENU_LABEL_MOVE_ALL);
        JMenu moveDirMenu = new JMenu(MENU_LABEL_MOVE_DIR);
        JMenu copyImageMenu = new JMenu(MENU_LABEL_COPY_ONE);
        JMenu copyAllImagesMenu = new JMenu(MENU_LABEL_COPY_ALL);
        JMenu copyDirMenu = new JMenu(MENU_LABEL_COPY_DIR);
        JMenu linkImageMenu = new JMenu(MENU_LABEL_LINK_ONE);
        JMenu linkAllImagesMenu = new JMenu(MENU_LABEL_LINK_ALL);
        JMenu linkDirMenu = new JMenu(MENU_LABEL_LINK_DIR);

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
                buildImageOperationMenuRecursive(rootNode, rootNode, moveImageMenu, ImageOperation.moveSingleImage());
                buildImageOperationMenuRecursive(rootNode, rootNode, moveAllImagesMenu, ImageOperation.moveAllImages());
                buildImageOperationMenuRecursive(rootNode, rootNode, moveDirMenu, ImageOperation.moveDirectory());
            }

            if (AppConfig.getInstance().isQuickCopyEnabled()) {
                buildImageOperationMenuRecursive(rootNode, rootNode, copyImageMenu, ImageOperation.copySingleImage());
                buildImageOperationMenuRecursive(rootNode, rootNode, copyAllImagesMenu, ImageOperation.copyAllImages());
                buildImageOperationMenuRecursive(rootNode, rootNode, copyDirMenu, ImageOperation.copyDirectory());
            }

            if (AppConfig.getInstance().isQuickLinkEnabled()) {
                buildImageOperationMenuRecursive(rootNode, rootNode, linkImageMenu, ImageOperation.linkSingleImage());
                buildImageOperationMenuRecursive(rootNode, rootNode, linkAllImagesMenu, ImageOperation.linkAllImages());
                buildImageOperationMenuRecursive(rootNode, rootNode, linkDirMenu, ImageOperation.linkDirectory());
            }
        }

        return menuList;
    }

    /**
     * Interrogates the current list of ImageSets to build out an "add to favorites" menu.
     */
    public static JMenu buildFavoritesMenu() {
        JMenu menu = new JMenu(MENU_LABEL_FAVORITE);
        List<ImageSet> topLevelNodes = MainWindow.getInstance().getImageSetPanel().getFavorites();
        for (ImageSet topLevelNode : topLevelNodes) {
            buildFavoriteMenuRecursive(topLevelNode, topLevelNode, menu);
        }
        return menu;
    }

    /**
     * Builds menu items related to image deletion and returns them in a list that can be
     * inserted into a JMenu or JPopupMenu.
     *
     * @return A List of menu components related to image deletion.
     */
    public static List<Component> buildDeleteMenuItems() {
        List<Component> menuList = new ArrayList<>();
        menuList.add(new JMenuItem(new ImageOperationAction("Delete this image", ImageOperation.deleteSingleImage())));
        menuList.add(new JMenuItem(
                new ImageOperationAction("Delete all images in this directory", ImageOperation.deleteAllImages())));
        menuList.add(
                new JMenuItem(new ImageOperationAction("Delete this directory", ImageOperation.deleteDirectory())));
        return menuList;
    }

    /**
     * Invoked internally to recurse through the given tree node and generate menu items as
     * appropriate for the given ImageOperation into the given JMenu.
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

    private static void buildFavoriteMenuRecursive(ImageSet rootNode, ImageSet node, JMenu menu) {
        if (node != null && node.getChildCount() > 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                ImageSet childSet = (ImageSet)node.getChildAt(i);
                if (childSet.getChildCount() > 0) {
                    JMenu subMenu = new JMenu(childSet.getName());
                    buildFavoriteMenuRecursive(rootNode, childSet, subMenu);
                    menu.add(subMenu);
                }
                else {
                    menu.add(new JMenuItem(childSet.getName())); // TODO this should be an action
                }
            }
        }

        // If we're at the root level, also add an option to create a new list:
        if (node == rootNode) {
            menu.add(new JMenuItem("Create new list...")); // TODO this should be an action
        }
    }
}
