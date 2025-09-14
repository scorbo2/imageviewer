package ca.corbett.imageviewer;

import ca.corbett.extras.image.ImageUtil;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.AboutAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelActualSizeAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelBestFitAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelZoomInAction;
import ca.corbett.imageviewer.ui.actions.ImagePanelZoomOutAction;
import ca.corbett.imageviewer.ui.actions.ManageExtensionsAction;
import ca.corbett.imageviewer.ui.actions.NextImageAction;
import ca.corbett.imageviewer.ui.actions.PreferencesAction;
import ca.corbett.imageviewer.ui.actions.PreviousImageAction;
import ca.corbett.imageviewer.ui.actions.ReloadAction;

import javax.swing.AbstractAction;
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains static helper methods for building up a JMenuBar for the main window.
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public final class ToolBarManager {

    private static final Logger logger = Logger.getLogger(ToolBarManager.class.getName());

    public static final int iconSize = 26;

    private static JPopupMenu quickMovePopupMenu;
    private static JPopupMenu deletePopupMenu;

    private ToolBarManager() {

    }

    public static JButton buildButton(BufferedImage iconImage, String toolTip, AbstractAction action) {
        return buildButton(iconImage, toolTip, action, 30);
    }

    /**
     * Builds and returns a toolbar button of the correct size with the specified
     * icon and action. Whatever icon is loaded from the given URL will be scaled
     * to match the size of our tool bar (currently this is not configurable).
     *
     * @param iconImage The image to use as the button icon.
     * @param toolTip   The text description of this button.
     * @param action    An optional Action to attach to the button.
     * @return A JButton that can be added to a toolbar.
     */
    public static JButton buildButton(BufferedImage iconImage, String toolTip, AbstractAction action, int btnSize) {
        JButton button = new JButton(action);
        button.setText("");
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(btnSize, btnSize));
        ImageIcon icon = new ImageIcon(iconImage, toolTip);
        button.setIcon(icon);
        button.setToolTipText(toolTip);

        return button;
    }

    /**
     * Builds a JToolBar with all built-in buttons as well as any buttons
     * supplied by enabled extensions.
     *
     * @return A JToolBar instance.
     */
    public static JToolBar buildToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JPanel wrapper = new JPanel();
        wrapper.setBackground(UIManager.getDefaults().getColor("Button.background"));
        wrapper.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        wrapper.setPreferredSize(new Dimension(500, 38));

        wrapper.add(new JLabel(" "));

        try {
            wrapper.add(
                    buildButton(loadIconImage("icon-go-previous.png"), "Previous image", new PreviousImageAction()));
            wrapper.add(buildButton(loadIconImage("icon-go-next.png"), "Next image", new NextImageAction()));

            wrapper.add(new JLabel(" "));

            wrapper.add(buildButton(loadIconImage("icon-zoom-in2.png"), "Zoom in", new ImagePanelZoomInAction()));
            wrapper.add(buildButton(loadIconImage("icon-zoom-out2.png"), "Zoom out", new ImagePanelZoomOutAction()));
            wrapper.add(buildButton(loadIconImage("icon-best-fit2.png"), "Best fit", new ImagePanelBestFitAction()));
            wrapper.add(buildButton(loadIconImage("icon-actual-size2.png"), "Actual size",
                                    new ImagePanelActualSizeAction()));

            wrapper.add(new JLabel(" "));

            wrapper.add(buildButton(loadIconImage("icon-reload.png"), "Reload", new ReloadAction()));

            wrapper.add(new JLabel(" "));

            final JButton moveButton = buildButton(loadIconImage("icon-document-upload.png"), "Quick Move...", null);
            wrapper.add(moveButton);
            final JButton deleteButton = buildButton(loadIconImage("icon-x.png"), "Delete...", null);
            wrapper.add(deleteButton);

            // Create a popup menu for the quick move button and attach it:
            quickMovePopupMenu = new JPopupMenu();
            for (Component c : MainWindow.getInstance().getMenuManager().buildImageMovementMenuItems()) {
                quickMovePopupMenu.add(c);
            }
            moveButton.addActionListener(e -> quickMovePopupMenu.show(moveButton, 0, moveButton.getHeight()));

            // Create a popup menu for the delete button and attach it:
            deletePopupMenu = new JPopupMenu();
            for (Component c : MainWindow.getInstance().getMenuManager().buildImageRemovalMenuItems()) {
                deletePopupMenu.add(c);
            }
            deleteButton.addActionListener(e -> deletePopupMenu.show(deleteButton, 0, deleteButton.getHeight()));

            // Insert any buttons supplied by extensions, if any:
            for (JButton extensionBtn : ImageViewerExtensionManager.getInstance().getToolBarButtons()) {
                wrapper.add(extensionBtn);
            }

            wrapper.add(new JLabel(" "));

            wrapper.add(buildButton(loadIconImage("icon-settings.png"), "Preferences", new PreferencesAction()));
            wrapper.add(buildButton(loadIconImage("icon-image-information.png"), "Extensions",
                                    new ManageExtensionsAction()));

            wrapper.add(new JLabel(" "));

            wrapper.add(buildButton(loadIconImage("icon-help2.png"), "About", new AboutAction()));

            toolBar.add(wrapper);
        }
        catch (IOException ioe) {
            logger.log(Level.SEVERE, "Error loading icon image: " + ioe.getMessage(), ioe);
        }

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

    /**
     * Invoked internally to load an icon image from resources, scale it if needed, and return it.
     */
    private static BufferedImage loadIconImage(String resourceName) throws IOException {
        return ImageUtil.loadFromResource(MainWindow.class, "/ca/corbett/imageviewer/images/" + resourceName, iconSize,
                                          iconSize);
    }
}
