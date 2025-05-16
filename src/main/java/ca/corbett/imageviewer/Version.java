package ca.corbett.imageviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

/**
 * Constants concerning the application name and version information,
 * along with properties describing the location of the application config dir.
 *
 * @author scorbo2
 * @since 2017-11-28
 */
public final class Version {

    private Version() {
    }

    /** The major version. **/
    public static final int VERSION_MAJOR = 2;

    /** The minor (patch) version. **/
    public static final int VERSION_MINOR = 1;

    /** A user-friendly version string in the form "MAJOR.MINOR" (example: "1.0"). **/
    public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR;

    /** The user-friendly name of this application. **/
    public static final String APPLICATION_NAME = "ImageViewer";

    /**
     * The fully qualified directory where user-specific settings and extensions are stored.
     * This is typically ${user.home}/.${APPLICATION_NAME}/ but can be overridden
     */
    public static final File APPLICATION_HOME;

    /** The directory where extension jars live. **/
    public static final File EXTENSIONS_DIR;

    /** The file containing our saved application config. **/
    public static final File APP_CONFIG_FILE;

    /** The project Url. **/
    public static String PROJECT_URL = "https://github.com/scorbo2/musicplayer";

    /** The project license. **/
    public static String LICENSE = "https://opensource.org/license/mit";

    static {
        File appHome;
        File extensionsDir;

        // APPLICATION_HOME can be supplied as a system property:
        if (System.getProperties().containsKey("ca.corbett.imageviewer.home")) {
            appHome = new File(System.getProperty("ca.corbett.imageviewer.home"));
        }

        // If it is not set, we'll default it to inside the user's home directory:
        else {
            appHome = new File(System.getProperty("user.home"), "." + APPLICATION_NAME);
        }

        // EXTENSION_DIR can be supplied as a system property:
        if (System.getProperties().containsKey("ca.corbett.imageviewer.extensions")) {
            extensionsDir = new File(System.getProperty("ca.corbett.imageviewer.extensions"));
        }

        // If it does not exist, stick it under APPLICATION_DIR:
        else {
            extensionsDir = new File(appHome, "extensions");
        }

        // LOG_PROPERTIES can be supplied as a system property:
        if (System.getProperties().containsKey("java.util.logging.config.file")) {
            // Do nothing. It will be used automatically.
        }

        // If it is not set, we'll assume it's in APPLICATION_HOME:
        else {
            File logProperties = new File(appHome, "logging.properties");
            if (logProperties.exists() && logProperties.canRead()) {
                try {
                    LogManager.getLogManager().readConfiguration(new FileInputStream(logProperties));
                }
                catch (IOException ioe) {
                    System.out.println("WARN: Unable to load log configuration from app dir: " + ioe.getMessage());
                }
            }

            // Otherwise, load our built-in default from jar resources:
            else {
                try {
                    LogManager.getLogManager().readConfiguration(
                            Main.class.getResourceAsStream("/ca/corbett/imageviewer/logging.properties"));
                }
                catch (IOException ioe) {
                    System.out.println("WARN: Unable to load log configuration from jar: " + ioe.getMessage());
                }
            }
        }

        // Create our directories if they do not exist:
        APPLICATION_HOME = appHome;
        EXTENSIONS_DIR = extensionsDir;
        if (!APPLICATION_HOME.exists()) {
            APPLICATION_HOME.mkdirs();
        }
        if (!EXTENSIONS_DIR.exists()) {
            EXTENSIONS_DIR.mkdirs();
        }
        APP_CONFIG_FILE = new File(APPLICATION_HOME, APPLICATION_NAME + ".prefs");
    }
}
