package ca.corbett.imageviewer;

import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.actions.RenameAction;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

/**
 * Utility class for handling global application keyboard shortcuts.
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public final class KeyboardManager {

    private KeyboardManager() {

    }

    /**
     * Sets up the key listener for the given MainWindow instance - note that we DON'T use
     * getInstance() from MainWindow here because this method may be called from there.
     *
     * @param instance Callers must supply the MainWindow instance to use.
     */
    public static void addGlobalKeyListener(final MainWindow instance) {
        //Hijack the keyboard manager
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {

                if (!instance.isActive()) {
                    return false; // don't capture keystrokes if a popup dialog is showing.
                }

                if (e.getID() == KeyEvent.KEY_PRESSED) {

                    switch (e.getKeyCode()) {

                        // Left or Up arrow for "previous image":
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_UP:
                            instance.selectPreviousImage();
                            break;

                        // Right or down arrow for "next image":
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_DOWN:
                            instance.selectNextImage();
                            break;

                        // Delete key to delete current image:
                        case KeyEvent.VK_DELETE:
                            ImageOperationHandler.deleteImage();
                            break;

                        // F2 to rename current image:
                        case KeyEvent.VK_F2:
                            new RenameAction().actionPerformed(null);
                            break;

                        default:
                            break;
                    }

                    // Give extensions a chance to handle this shortcut:
                    ImageViewerExtensionManager.getInstance().handleKeyboardShortcut(e);
                }

                //Allow the event to be redispatched
                return false;
            }

        });
    }

}
