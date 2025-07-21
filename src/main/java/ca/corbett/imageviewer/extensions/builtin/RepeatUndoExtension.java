package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;

import javax.swing.JMenuItem;
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
 * @author scorbo2
 */
public class RepeatUndoExtension extends ImageViewerExtension {

    private final AppExtensionInfo extInfo;

    public RepeatUndoExtension() {
        extInfo = new AppExtensionInfo.Builder("Repeat and undo actions")
                .setAuthor("steve@corbett.ca")
                .setTargetAppName(Version.APPLICATION_NAME)
                .setTargetAppVersion(Version.VERSION)
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
    protected List<AbstractProperty> createConfigProperties() {
        return List.of();
    }

    @Override
    public List<JMenuItem> getMenuItems(String topLevelMenu) {
        if ("Edit".equals(topLevelMenu)) {
            List<JMenuItem> list = new ArrayList<>();
            list.add(buildRepeatMenuItem());
            list.add(buildUndoMenuItem());
            return list;
        }
        return null;
    }

    @Override
    public List<JMenuItem> getPopupMenuItems() {
        List<JMenuItem> list = new ArrayList<>();
        list.add(buildRepeatMenuItem());
        list.add(buildUndoMenuItem());
        return list;
    }

    private JMenuItem buildRepeatMenuItem() {
        JMenuItem menuItem = new JMenuItem(new RepeatAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        return menuItem;
    }

    private JMenuItem buildUndoMenuItem() {
        JMenuItem menuItem = new JMenuItem(new UndoAction());
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        return menuItem;
    }

}
