package ca.corbett.imageviewer.ui;

/**
 * UI classes can implement this interface if they are capable
 * of reloading themselves when user preferences change. For example,
 * if your UI component is affected by a change in the current theme,
 * or any other user-configurable property, then you can register
 * with the global ReloadUIAction to receive notification when
 * these properties have changed. You can respond to that message
 * by redrawing whatever UI elements may have been changed.
 *
 * @author scorbo2
 */
public interface UIReloadable {

    /**
     * Invoked by ReloadUIAction when it's time to reload the UI.
     * AppConfig should be queried for the latest state of all
     * user-configurable application settings.
     */
    void reloadUI();
}