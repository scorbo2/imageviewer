package ca.corbett.imageviewer;

import ca.corbett.extras.about.AboutInfo;

import java.io.File;

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

    public static final AboutInfo aboutInfo;

    /** The major version. **/
    public static final int VERSION_MAJOR = 2;

    /** The minor (patch) version. **/
    public static final int VERSION_MINOR = 2;

    /** A user-friendly version string in the form "MAJOR.MINOR" (example: "1.0"). **/
    public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR;

    /** The user-friendly name of this application. **/
    public static final String APPLICATION_NAME = "ImageViewer";

    public static final String NAME = APPLICATION_NAME + " " + VERSION;
    public static String COPYRIGHT = "Copyright © 2017-2025 Steve Corbett";

    /**
     * The directory where ImageViewer was installed -
     * caution, this might be null! We can't guess a
     * value for this property, it has to be supplied
     * by the launcher script, but the launcher script
     * might have been modified by the user, or the user
     * might have started the app without using the launcher.
     * <p>
     * The installer script for linux defaults this
     * to /opt/ImageViewer, but the user can override that.
     * </p>
     */
    public static final File INSTALL_DIR;

    /**
     * The directory where application configuration and
     * log files can go. If not given to us explicitly by
     * the launcher script, we default it a directory named
     * .ImageViewer in the user's home directory.
     */
    public static final File SETTINGS_DIR;

    /**
     * The directory to scan for extension jars at startup.
     * If not given to us explicitly by the launcher script,
     * we default it to a directory called "extensions"
     * inside of SETTINGS_DIR.
     */
    public static final File EXTENSIONS_DIR;

    /** The file containing our saved application config. **/
    public static final File APP_CONFIG_FILE;

    /** The project Url. **/
    public static String PROJECT_URL = "https://github.com/scorbo2/imageviewer";

    /** The project license. **/
    public static String LICENSE = "https://opensource.org/license/mit";

    static {
        aboutInfo = new AboutInfo();
        aboutInfo.applicationName = APPLICATION_NAME;
        aboutInfo.applicationVersion = VERSION;
        aboutInfo.copyright = COPYRIGHT;
        aboutInfo.license = LICENSE;
        aboutInfo.projectUrl = PROJECT_URL;
        aboutInfo.showLogConsole = true;
        aboutInfo.releaseNotesLocation = "/ca/corbett/imageviewer/ReleaseNotes.txt";
        aboutInfo.logoImageLocation = "/ca/corbett/imageviewer/images/logo_wide.jpg";
        aboutInfo.shortDescription = "Fast and extensible image viewer and sorter.";

        String installDir = System.getProperty("INSTALL_DIR", null);
        INSTALL_DIR = installDir == null ? null : new File(installDir);

        String appDir = System.getProperty("SETTINGS_DIR",
                                           new File(System.getProperty("user.home"), "." + NAME).getAbsolutePath());
        SETTINGS_DIR = new File(appDir);
        if (!SETTINGS_DIR.exists()) {
            SETTINGS_DIR.mkdirs();
        }

        String extDir = System.getProperty("EXTENSIONS_DIR", new File(SETTINGS_DIR, "extensions").getAbsolutePath());
        EXTENSIONS_DIR = new File(extDir);
        if (!EXTENSIONS_DIR.exists()) {
            EXTENSIONS_DIR.mkdirs();
        }

        APP_CONFIG_FILE = new File(SETTINGS_DIR, APPLICATION_NAME + ".prefs");
    }
}
