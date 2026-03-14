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
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 3.0
 */
public enum ColorScheme {

    MATRIX("Matrix",
           new Color(0, 0, 0),   // Default background
           new Color(0, 255, 0), // Default foreground
           new Color(0, 255, 0), // Selected background
           new Color(0, 0, 0),   // Selected foreground
           new Color(0, 50, 0),  // Unselected background
           new Color(0, 200, 0)  // Unselected foreground
    ),
    DARK("Dark",
         new Color(45, 45, 45),    // Default background
         new Color(220, 220, 220), // Default foreground
         new Color(70, 130, 180),  // Selected background
         new Color(255, 255, 255), // Selected foreground
         new Color(60, 60, 60),    // Unselected background
         new Color(200, 200, 200)  // Unselected foreground
    ),
    VERY_DARK("Very dark",
              new Color(25, 25, 25),    // Default background
              new Color(205, 205, 205), // Default foreground
              new Color(40, 100, 150),  // Selected background
              new Color(205, 205, 205), // Selected foreground
              new Color(40, 40, 40),    // Unselected background
              new Color(180, 180, 180)  // Unselected foreground
    ),
    EXTREMELY_DARK("Extremely dark",
                   new Color(0, 0, 0),       // Default background
                   new Color(205, 205, 205), // Default foreground
                   new Color(40, 100, 150),  // Selected background
                   new Color(205, 205, 205), // Selected foreground
                   new Color(20, 20, 20),    // Unselected background
                   new Color(180, 180, 180)  // Unselected foreground
    ),
    SHADES_OF_GREY("Shades of grey",
                   new Color(45, 45, 45),    // Default background
                   new Color(200, 200, 200), // Default foreground
                   new Color(130, 130, 130), // Selected background
                   new Color(255, 255, 255), // Selected foreground
                   new Color(75, 75, 75),    // Unselected background
                   new Color(200, 200, 200)  // Unselected foreground
    ),
    GOT_THE_BLUES("Got the blues",
                  new Color(25, 25, 65),    // Default background
                  new Color(100, 100, 200), // Default foreground
                  new Color(30, 30, 150),   // Selected background
                  new Color(205, 205, 255), // Selected foreground
                  new Color(45, 45, 105),   // Unselected background
                  new Color(100, 100, 200)  // Unselected foreground
    ),
    HOT_DOG_STAND("Hot dog stand", // Just a joke! :D
                  new Color(155, 155, 25),  // Default background
                  new Color(200, 200, 200), // Default foreground
                  new Color(155, 40, 50),   // Selected background
                  new Color(205, 205, 255), // Selected foreground
                  new Color(165, 75, 5),    // Unselected background
                  new Color(225, 75, 75)    // Unselected foreground
    );

    private final String label;
    private final Color defaultBackground;
    private final Color defaultForeground;
    private final Color selectedBackground;
    private final Color selectedForeground;
    private final Color unselectedBackground;
    private final Color unselectedForeground;

    ColorScheme(String label, Color defaultBackground, Color defaultForeground,
                Color selectedBackground, Color selectedForeground,
                Color unselectedBackground, Color unselectedForeground) {
        this.label = label;
        this.defaultBackground = defaultBackground;
        this.defaultForeground = defaultForeground;
        this.unselectedBackground = unselectedBackground;
        this.unselectedForeground = unselectedForeground;
        this.selectedBackground = selectedBackground;
        this.selectedForeground = selectedForeground;
    }

    @Override
    public String toString() {
        return label;
    }

    public Color getDefaultBackground() {
        return defaultBackground;
    }

    public Color getDefaultForeground() {
        return defaultForeground;
    }

    public Color getSelectedBackground() {
        return selectedBackground;
    }

    public Color getSelectedForeground() {
        return selectedForeground;
    }

    public Color getUnselectedBackground() {
        return unselectedBackground;
    }

    public Color getUnselectedForeground() {
        return unselectedForeground;
    }
}
