package ca.corbett.imageviewer;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.AboutAction;
import ca.corbett.imageviewer.ui.actions.ButtonPopupMenuAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelActualSizeAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelBestFitAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelZoomInAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelZoomOutAction;
import ca.corbett.imageviewer.ui.actions.ManageExtensionsAction;
import ca.corbett.imageviewer.ui.actions.NextImageAction;
import ca.corbett.imageviewer.ui.actions.PreferencesAction;
import ca.corbett.imageviewer.ui.actions.PreviousImageAction;
import ca.corbett.imageviewer.ui.actions.ReloadAction;
import ca.corbett.imageviewer.ui.actions.ThumbCachePregenerateAction;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import static ca.corbett.imageviewer.ImageViewerResources.getIconDelete;
import static ca.corbett.imageviewer.ImageViewerResources.getIconMoveItem;

/**
 * Contains static helper methods for building up a JMenuBar for the main window.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.0
 */
public final class ToolBarManager {

    private static final Logger logger = Logger.getLogger(ToolBarManager.class.getName());

    private static JPopupMenu quickMovePopupMenu;
    private static JPopupMenu deletePopupMenu;

    private ToolBarManager() {
    }

    /**
     * Creates a button for the main application toolbar. The button will be sized
     * according to current user preferences. If the given BufferedImage
     * is not of the appropriate size, it is resized automatically.
     */
    public static JButton buildToolbarButton(EnhancedAction action) {
        return buildButton(action,
                           AppConfig.getInstance().getToolbarIconSize(),
                           AppConfig.getInstance().getToolbarIconMargin());
    }

    /**
     * Creates a button for use in mini toolbars (such as the one in the image set panel).
     * The button will be sized according to current user preferences. If the given BufferedImage
     * is not of the appropriate size, it is resized automatically.
     */
    public static JButton buildMiniToolbarButton(EnhancedAction action) {
        return buildButton(action,
                           AppConfig.getInstance().getMiniToolbarIconSize(),
                           AppConfig.getInstance().getMiniToolbarIconMargin());
    }

    /**
     * Invoked internally to build a button of the specified size.
     */
    private static JButton buildButton(EnhancedAction action, int iconImageSize, int iconMarginSize) {
        int buttonSize = iconImageSize + iconMarginSize;

        // Scale if needed:
        ImageIcon imageIcon = (ImageIcon)action.getIcon();
        BufferedImage image = null;
        if (imageIcon != null) {
            image = (BufferedImage)imageIcon.getImage();
            if (image.getHeight() != iconImageSize || image.getWidth() != iconImageSize) {
                // Resize the image to match the preferred toolbar icon size:
                image = ImageUtil.generateThumbnailWithTransparency(image, iconImageSize, iconImageSize);
            }
        }

        // Create and return the button:
        JButton button = new JButton(action);
        button.setText(image == null ? action.getName() : "");
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(buttonSize, buttonSize));
        if (image != null) {
            button.setIcon(new ImageIcon(image));
        }
        button.setToolTipText(action.getTooltip());

        return button;
    }

    /**
     * Builds a JToolBar with all built-in buttons as well as any buttons
     * supplied by enabled extensions.
     *
     * @return A JToolBar instance.
     */
    public static JToolBar buildToolBar() {
        int toolbarSize = AppConfig.getInstance().getToolbarIconSize() + AppConfig.getInstance().getToolbarIconMargin();
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JPanel wrapper = new JPanel();
        wrapper.setBackground(UIManager.getDefaults().getColor("Button.background"));
        wrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        wrapper.setPreferredSize(new Dimension(500, toolbarSize + 8));

        wrapper.add(new JLabel(" "));
        int size = AppConfig.getInstance().getToolbarIconSize();

        wrapper.add(buildToolbarButton(new PreviousImageAction()));
        wrapper.add(buildToolbarButton(new NextImageAction()));
        wrapper.add(new JLabel(" "));
        wrapper.add(buildToolbarButton(new ImagePanelZoomInAction()));
        wrapper.add(buildToolbarButton(new ImagePanelZoomOutAction()));
        wrapper.add(buildToolbarButton(new ImagePanelBestFitAction()));
        wrapper.add(buildToolbarButton(new ImagePanelActualSizeAction()));
        wrapper.add(new JLabel(" "));
        wrapper.add(buildToolbarButton(new ReloadAction()));

        // Only show  thumbnail pregeneration button if thumb caching is enabled:
        if (AppConfig.instance.isThumbCacheEnabled()) {
            wrapper.add(buildToolbarButton(new ThumbCachePregenerateAction()));
        }
        wrapper.add(new JLabel(" "));

        // Insert any buttons supplied by extensions, if any:
        for (EnhancedAction extensionAction : ImageViewerExtensionManager.getInstance().getMainToolBarActions()) {
            wrapper.add(buildToolbarButton(extensionAction));
        }

        // Create a popup menu for the quick move button and attach it:
        quickMovePopupMenu = new JPopupMenu();
        for (Component c : MainWindow.getInstance().getMenuManager().buildImageMovementMenuItems()) {
            quickMovePopupMenu.add(c);
        }

        // Create a popup menu for the delete button and attach it:
        deletePopupMenu = new JPopupMenu();
        for (Component c : MainWindow.getInstance().getMenuManager().buildImageRemovalMenuItems()) {
            deletePopupMenu.add(c);
        }

        ButtonPopupMenuAction quickMoveAction = new ButtonPopupMenuAction("Quick Move...", quickMovePopupMenu,
                                                                          getIconMoveItem(size));
        final JButton moveButton = buildToolbarButton(quickMoveAction);
        quickMoveAction.setButton(moveButton);
        wrapper.add(moveButton);

        ButtonPopupMenuAction deleteAction = new ButtonPopupMenuAction("Delete...", ToolBarManager.deletePopupMenu,
                                                                       getIconDelete(size));
        final JButton deleteButton = buildToolbarButton(deleteAction);
        deleteAction.setButton(deleteButton);
        wrapper.add(deleteButton);

        wrapper.add(new JLabel(" "));
        wrapper.add(buildToolbarButton(new PreferencesAction()));
        wrapper.add(buildToolbarButton(new ManageExtensionsAction()));
        wrapper.add(new JLabel(" "));
        wrapper.add(buildToolbarButton(new AboutAction()));

        toolBar.add(wrapper);

        return toolBar;

    }

    /**
     * Invoked when the quick move tree changes at runtime - this method will regenerate
     * our quick move popup menu.
     */
    public static void rebuildMenus() {
        quickMovePopupMenu.removeAll();
        for (JMenuItem i : MainWindow.getInstance().getMenuManager().buildImageMovementMenuItems()) {
            quickMovePopupMenu.add(i);
        }

        deletePopupMenu.removeAll();
        for (JMenuItem i : MainWindow.getInstance().getMenuManager().buildImageRemovalMenuItems()) {
            deletePopupMenu.add(i);
        }
    }
}
