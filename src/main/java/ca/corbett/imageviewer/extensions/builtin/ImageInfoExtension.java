package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.KeyStrokeProperty;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.KeyStroke;
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

    private static final String HOTKEY_PROP = AppConfig.KEYSTROKE_PREFIX + "General.ImageInfo";
    private static final ImageInfoAction imageInfoAction = new ImageInfoAction();
    private static final DirectoryInfoAction directoryInfoAction = new DirectoryInfoAction();

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
        KeyStrokeProperty prop = new KeyStrokeProperty(HOTKEY_PROP,
                                                       "Image information:",
                                                       KeyStrokeManager.parseKeyStroke("Ctrl+I"),
                                                       imageInfoAction);
        prop.setAllowBlank(true);
        prop.setReservedKeyStrokes(AppConfig.RESERVED_KEYSTROKES);
        return List.of(prop);
    }

    @Override
    public List<EnhancedAction> getMenuActions(String menu, MainWindow.BrowseMode browseMode) {
        if (!"View".equals(menu)) {
            return null;
        }

        List<EnhancedAction> actions = new ArrayList<>();
        imageInfoAction.setAcceleratorKey(getConfiguredHotkey());
        actions.add(imageInfoAction);
        if (browseMode == MainWindow.BrowseMode.FILE_SYSTEM) {
            actions.add(directoryInfoAction);
        }
        return actions;
    }

    @Override
    public List<EnhancedAction> getPopupMenuActions(MainWindow.BrowseMode browseMode) {
        List<EnhancedAction> items = new ArrayList<>();
        imageInfoAction.setAcceleratorKey(getConfiguredHotkey());
        items.add(imageInfoAction);
        if (browseMode == MainWindow.BrowseMode.FILE_SYSTEM) {
            items.add(directoryInfoAction);
        }
        return items;
    }

    private KeyStroke getConfiguredHotkey() {
        AbstractProperty prop = AppConfig.getInstance().getPropertiesManager().getProperty(HOTKEY_PROP);
        if (prop instanceof KeyStrokeProperty ksp) {
            return ksp.getKeyStroke();
        }
        return null;
    }
}
