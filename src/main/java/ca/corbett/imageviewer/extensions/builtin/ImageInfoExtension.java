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
import java.util.List;

/**
 * This built-in extension to ImageViewer provides a basic image information dialog that
 * can be summoned for any selected image.
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
                                            + "currently selected image.")
                .setReleaseNotes("1.0 - extracted from ImageViewer 1.3")
                .addCustomField("Keyboard shortcut", "Ctrl+I")
                .build();
    }

    @Override
    public AppExtensionInfo getInfo() {
        return extInfo;
    }

    @Override
    protected List<AbstractProperty> createConfigProperties() {
        return List.of();
    }

    @Override
    public List<JMenuItem> getMenuItems(String menu, MainWindow.BrowseMode browseMode) {
        return "View".equals(menu) ? List.of(createMenuItem()) : null;
    }

    @Override
    public List<JMenuItem> getPopupMenuItems(MainWindow.BrowseMode browseMode) {
        return List.of(createMenuItem());
    }

    private JMenuItem createMenuItem() {
        JMenuItem menuItem = new JMenuItem(new ImageInfoAction());
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
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
