package ca.corbett.imageviewer.ui;

import java.awt.Color;

/**
 * Cheesy built-in color schemes for users who don't want
 * to rely on the Look and Feel colors.
 * <p>
 * This could be externalized, to avoid hard-coding, but eh... it's
 * fine for a neat little extra feature that might rarely actually get used.
 * Relying on the Look and Feel for this stuff is a better option in general,
 * and ImageViewer ships with all the extra Look and Feels that come
 * for free out of the box with swing-extras.
 * </p>
 * <p>
 * Note: unfortunately, we can't change the colors of the DirTree component,
 * because it's part of the swing-extras library, and doesn't expose its
 * underlying JTree directly. That seems to be the only major UI component that
 * we can't skin with these color schemes. It's distracting, but not the end
 * of the world.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 3.0
 */
public enum ColorScheme {

    Matrix(
        new Color(0, 0, 0),  // Image panel background
        new Color(0, 255, 0),// Thumb selected background
        new Color(0, 50, 0), // Thumb unselected background
        new Color(0, 0, 0),  // Thumb selected font color
        new Color(0, 200, 0),// Thumb unselected font color
        new Color(0, 0, 0),  // Thumb container background
        new Color(0, 50, 0), // Status panel background
        new Color(0, 255, 0) // Status panel font color
    ),
    Dark(
        new Color(45, 45, 45),     // Image panel background
        new Color(70, 130, 180),   // Thumb selected background
        new Color(60, 60, 60),     // Thumb unselected background
        new Color(255, 255, 255),  // Thumb selected font color
        new Color(200, 200, 200),  // Thumb unselected font color
        new Color(50, 50, 50),     // Thumb container background
        new Color(40, 40, 40),     // Status panel background
        new Color(220, 220, 220)   // Status panel font color
    ),
    VeryDark(
        new Color(25, 25, 25),   // Image panel background
        new Color(40, 100, 150), // Thumb selected background
        new Color(40, 40, 40),   // Thumb unselected background
        new Color(205, 205, 205),// Thumb selected font color
        new Color(180, 180, 180),// Thumb unselected font color
        new Color(25, 25, 25),   // Thumb container background
        new Color(25, 25, 25),   // Status panel background
        new Color(205, 205, 205) // Status panel font color
    ),
    ExtremelyDark(
        new Color(0, 0, 0),      // Image panel background
        new Color(40, 100, 150), // Thumb selected background
        new Color(20, 20, 20),   // Thumb unselected background
        new Color(205, 205, 205),// Thumb selected font color
        new Color(180, 180, 180),// Thumb unselected font color
        new Color(0, 0, 0),      // Thumb container background
        new Color(0, 0, 0),      // Status panel background
        new Color(205, 205, 205) // Status panel font color
    ),
    ShadesOfGrey(
        new Color(45, 45, 45),   // Image panel background
        new Color(130, 130, 130),// Thumb selected background
        new Color(75, 75, 75),   // Thumb unselected background
        new Color(255, 255, 255),// Thumb selected font color
        new Color(200, 200, 200),// Thumb unselected font color
        new Color(45, 45, 45),   // Thumb container background
        new Color(45, 45, 45),   // Status panel background
        new Color(200, 200, 200) // Status panel font color
    ),
    GotTheBlues(
        new Color(25, 25, 65),    // Image panel background
        new Color(30, 30, 150),   // Thumb selected background
        new Color(45, 45, 105),   // Thumb unselected background
        new Color(205, 205, 255), // Thumb selected font color
        new Color(100, 100, 200), // Thumb unselected font color
        new Color(25, 25, 65),    // Thumb container background
        new Color(25, 25, 65),    // Status panel background
        new Color(100, 100, 200)  // Status panel font color
    ),
    HotDogStand( // Just a joke! :D
        new Color(155, 155, 25),  // Image panel background
        new Color(155, 40, 50),   // Thumb selected background
        new Color(165, 75, 5),    // Thumb unselected background
        new Color(205, 205, 255), // Thumb selected font color
        new Color(225, 75, 75),   // Thumb unselected font color
        new Color(155, 155, 25),  // Thumb container background
        new Color(155, 155, 25),  // Status panel background
        new Color(200, 200, 200)  // Status panel font color
    );

    private final Color imagePanelBgColor;
    private final Color thumbSelectedBgColor;
    private final Color thumbUnselectedBgColor;
    private final Color thumbSelectedFontColor;
    private final Color thumbUnselectedFontColor;
    private final Color thumbContainerBgColor;
    private final Color statusPanelBgColor;
    private final Color statusPanelFontColor;

    ColorScheme(Color imagePanelBg, Color thumbSelectedBg, Color thumbUnselectedBg,
                Color thumbSelectedFont, Color thumbUnselectedFont,
                Color thumbContainerBg, Color statusPanelBg,
                Color statusPanelFont) {
        imagePanelBgColor = imagePanelBg;
        thumbSelectedBgColor = thumbSelectedBg;
        thumbUnselectedBgColor = thumbUnselectedBg;
        thumbSelectedFontColor = thumbSelectedFont;
        thumbUnselectedFontColor = thumbUnselectedFont;
        thumbContainerBgColor = thumbContainerBg;
        statusPanelBgColor = statusPanelBg;
        statusPanelFontColor = statusPanelFont;
    }

    public Color getImagePanelBgColor() {
        return imagePanelBgColor;
    }

    public Color getThumbSelectedBgColor() {
        return thumbSelectedBgColor;
    }

    public Color getThumbUnselectedBgColor() {
        return thumbUnselectedBgColor;
    }

    public Color getThumbSelectedFontColor() {
        return thumbSelectedFontColor;
    }

    public Color getThumbUnselectedFontColor() {
        return thumbUnselectedFontColor;
    }

    public Color getThumbContainerBgColor() {
        return thumbContainerBgColor;
    }

    public Color getStatusPanelBgColor() {
        return statusPanelBgColor;
    }

    public Color getStatusPanelFontColor() {
        return statusPanelFontColor;
    }
}
