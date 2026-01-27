package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.EnhancedAction;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.imageviewer.AppConfig;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

/**
 * An Action for launching a popup menu from a toolbar button.
 * If no button is supplied, the action does nothing.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ButtonPopupMenuAction extends EnhancedAction {

    private final JPopupMenu popupMenu;
    private JButton sourceButton;
    private final BufferedImage sourceImage;

    public ButtonPopupMenuAction(String name, JPopupMenu popupMenu, BufferedImage iconImage) {
        super(name);
        setTooltip(name);
        this.popupMenu = popupMenu;
        this.sourceImage = iconImage;
        scaleIcon();
    }

    public void setButton(JButton button) {
        this.sourceButton = button;
    }

    /**
     * Scale our source image if needed to fit the currently configured toolbar icon size.
     */
    private void scaleIcon() {
        int iconSize = AppConfig.getInstance().getToolbarIconSize();
        BufferedImage iconImage = sourceImage;
        if (iconImage.getWidth() != iconSize || iconImage.getHeight() != iconSize) {
            iconImage = ImageUtil.generateThumbnailWithTransparency(iconImage, iconSize, iconSize);
        }

        setIcon(new ImageIcon(iconImage));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (sourceButton == null) {
            return;
        }

        // Show the popup menu just below the button:
        // (this makes it look like the menu "dropped down" from the button)
        popupMenu.show(sourceButton, 0, sourceButton.getHeight());
    }
}
