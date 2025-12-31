package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This built-in extension to ImageViewer provides a basic image information dialog that
 * can be summoned for any selected image, and a basic directory information dialog that
 * can be summoned for any selected directory (with optional recursion).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ImageInfoExtension extends ImageViewerExtension {

    private final AppExtensionInfo extInfo;

    public ImageInfoExtension() {
        extInfo = new AppExtensionInfo.Builder("Image information")
                .setAuthor("steve@corbett.ca")
                .setVersion(Version.VERSION)
                .setTargetAppName(Version.APPLICATION_NAME)
                .setTargetAppVersion(Version.VERSION)
                .setShortDescription("Shows information for the current image.")
                .setLongDescription("Displays an information dialog for the "
                                            + "currently selected image or directory.")
                .setReleaseNotes("1.0 - extracted from ImageViewer 1.3")
                .addCustomField("Keyboard shortcut", "Ctrl+I")
                .build();
    }

    @Override
    public AppExtensionInfo getInfo() {
        return extInfo;
    }

    @Override
    public void loadJarResources() {
    }

    @Override
    protected List<AbstractProperty> createConfigProperties() {
        return List.of();
    }

    @Override
    public List<JMenuItem> getMenuItems(String menu, MainWindow.BrowseMode browseMode) {
        if (!"View".equals(menu)) {
            return null;
        }

        List<JMenuItem> items = new ArrayList<>();
        items.add(createImageInfoMenuItem());
        if (browseMode == MainWindow.BrowseMode.FILE_SYSTEM) {
            items.add(createDirectoryInfoMenuItem());
        }
        return items;
    }

    @Override
    public List<JMenuItem> getPopupMenuItems(MainWindow.BrowseMode browseMode) {
        List<JMenuItem> items = new ArrayList<>();
        items.add(createImageInfoMenuItem());
        if (browseMode == MainWindow.BrowseMode.FILE_SYSTEM) {
            items.add(createDirectoryInfoMenuItem());
        }
        return items;
    }

    private JMenuItem createImageInfoMenuItem() {
        JMenuItem menuItem = new JMenuItem(new ImageInfoAction());
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        return menuItem;
    }

    private JMenuItem createDirectoryInfoMenuItem() {
        JMenuItem menuItem = new JMenuItem(new DirectoryInfoAction());
        menuItem.setMnemonic(KeyEvent.VK_D);
        return menuItem;
    }

    @Override
    public boolean handleKeyboardShortcut(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_I) {
            if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) > 0) {
                new ImageInfoAction().actionPerformed(null);
                return true;
            }
        }
        return false;
    }
}
