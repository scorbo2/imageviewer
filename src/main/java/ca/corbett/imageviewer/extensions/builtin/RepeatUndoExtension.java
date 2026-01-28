package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.KeyStrokeProperty;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * This built-in extension to ImageViewer allows you (within reason) to repeat or to undo
 * the last image operation, whether it was a move, a copy, or a symlink (note that delete
 * operations can't be undone).
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class RepeatUndoExtension extends ImageViewerExtension {

    private static final String REPEAT_PROP = AppConfig.KEYSTROKE_PREFIX + "Repeat / Undo.RepeatHotkey";
    private static final String UNDO_PROP = AppConfig.KEYSTROKE_PREFIX + "Repeat / Undo.UndoHotkey";

    private static final RepeatAction repeatAction = new RepeatAction();
    private static final UndoAction undoAction = new UndoAction();

    private final AppExtensionInfo extInfo;

    public RepeatUndoExtension() {
        extInfo = new AppExtensionInfo.Builder("Repeat and undo actions")
                .setAuthor("steve@corbett.ca")
                .setTargetAppName(Version.APPLICATION_NAME)
                .setTargetAppVersion(Version.VERSION)
                .setVersion(Version.VERSION)
                .setShortDescription("Repeat and undo move, copy, or symlink actions.")
                .setLongDescription("Allows you to repeat the last move, copy, or symlink action "
                                            + "by pressing Ctrl+R. Delete operations can't be repeated.\n\n"
                                            + "Allows you to undo the last move, copy, or symlink action "
                                            + "by pressing Ctrl+Z. Delete operations can't be undone.")
                .addCustomField("Repeat shortcut", "Ctrl+R")
                .addCustomField("Undo shortcut", "Ctrl+Z")
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
        List<AbstractProperty> props = new ArrayList<>();

        KeyStrokeProperty prop = new KeyStrokeProperty(REPEAT_PROP,
                                                       "Repeat last action:",
                                                       KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK),
                                                       repeatAction);
        prop.setAllowBlank(true);
        prop.setReservedKeyStrokes(AppConfig.RESERVED_KEYSTROKES);
        props.add(prop);

        prop = new KeyStrokeProperty(UNDO_PROP,
                                     "Undo last action:",
                                     KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK),
                                     undoAction);
        prop.setAllowBlank(true);
        prop.setReservedKeyStrokes(AppConfig.RESERVED_KEYSTROKES);
        props.add(prop);

        return props;
    }

    @Override
    public List<EnhancedAction> getMenuActions(String topLevelMenu, MainWindow.BrowseMode browseMode) {
        return "Edit".equals(topLevelMenu) ?
                List.of(repeatAction, undoAction)
                : null;
    }

    @Override
    public List<EnhancedAction> getPopupMenuActions(MainWindow.BrowseMode browseMode) {
        return List.of(repeatAction, undoAction);
    }
}
