package ca.corbett.imageviewer;

import ca.corbett.extras.logging.LogConsole;
import ca.corbett.extras.logging.LogConsoleStyle;
import ca.corbett.extras.logging.LogConsoleTheme;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;

import java.awt.Color;
import java.util.List;

/**
 * Utility class for setting up the LogConsole with a custom theme.
 *
 * @author scorbo2
 * @since ImageViewer 2.0
 */
public final class LogConsoleManager {

    private LogConsoleManager() {

    }

    /**
     * Creates and sets our custom LogConsole theme with a few built-in styles and whatever
     * extra styles we get from our enabled extensions. It's safe to call this method again
     * later, as extensions are enabled or disabled... the old theme will be replaced
     * with the new theme.
     */
    public static void setCustomTheme() {
        LogConsoleTheme theme = LogConsoleTheme.createMatrixStyledTheme();

        // Add all our built-in styles:
        theme.setStyle("moveSingleFile", createStyle("moveSingleFile", true, Color.CYAN, false));
        theme.setStyle("copySingleFile", createStyle("copySingleFile", true, Color.CYAN, false));
        theme.setStyle("linkSingleFile", createStyle("linkSingleFile", true, Color.CYAN, false));
        theme.setStyle("moveDirectory", createStyle("moveDirectory", true, Color.CYAN, false));
        theme.setStyle("renameImage", createStyle("renameImage", true, Color.CYAN, true));
        theme.setStyle("deleteImage", createStyle("deleteImage", true, Color.RED, true));
        theme.setStyle("deleteDirectory", createStyle("deleteDirectory", true, Color.RED, true));

        // Add any provided by extensions:
        List<LogConsoleStyle> extensionStyles = ImageViewerExtensionManager.getInstance().getLogConsoleStyles();
        int index = 1; // this is dumb, but we need a unique identifier for each style
        for (LogConsoleStyle extensionStyle : extensionStyles) {
            theme.setStyle("extensionStyle" + index, extensionStyle);
            index++;
        }

        // Register our custom theme.
        // This will overwrite any previous theme of this name, so it's safe to call this repeatedly.
        LogConsole.getInstance().registerTheme("ImageViewer", theme, true);
    }

    public static LogConsoleStyle createStyle(String token, boolean isCaseSensitive, Color fontColor, boolean isBold) {
        LogConsoleStyle style = new LogConsoleStyle();
        style.setLogToken(token, isCaseSensitive);
        style.setFontColor(fontColor);
        style.setIsBold(isBold);
        return style;
    }

}
