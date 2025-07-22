package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ui.UIReloadable;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * If your UI class depends on properties in AppConfig in order to render
 * itself (for example, for configurable theme colors or whatnot), you
 * can register yourself with this class to receive notice whenever it's
 * time to reload the UI. You can then respond by redrawing whatever
 * components might have been affected by property changes.
 *
 * @author scorbo2
 */
public class ReloadUIAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(ReloadUIAction.class.getName());

    private static ReloadUIAction instance;

    private final Set<UIReloadable> reloadables = new HashSet<>();

    private ReloadUIAction() {
    }

    public static ReloadUIAction getInstance() {
        if (instance == null) {
            instance = new ReloadUIAction();
        }

        return instance;
    }

    /**
     * Registers your class to receive messages whenever the UI is to reload.
     * Your class can redraw itself or do whatever it needs to reflect
     * the current settings. If AppConfig was modified by the user, it will
     * be in the new state before your class receives this message.
     *
     * @param reloadable Any class that implements UIReloadable.
     */
    public void registerReloadable(UIReloadable reloadable) {
        reloadables.add(reloadable);
    }

    public void unregisterReloadable(UIReloadable reloadable) {
        reloadables.remove(reloadable);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logger.info("Reloading UI");

        // Change the look and feel:
        LookAndFeelManager.switchLaf(AppConfig.getInstance().getLookAndFeelClassname());

        // Notify all listeners:
        for (UIReloadable reloadable : reloadables) {
            reloadable.reloadUI();
        }
    }
}