package ca.corbett.imageviewer;

import ca.corbett.extras.FallbackExceptionHandler;
import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.JFrame;
import java.awt.SplashScreen;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    /**
     * Main entry point. Usage:
     * <blockquote>
     * ImageViewer [-v] [startupDir]
     * </blockquote>
     * If "-v" is specified, application name and version will be output and the app will exit.
     * <p>
     * if "startupDir" is specified, main window will scroll to that directory, temporarily
     * ignoring the saved preferences for startup directory and lock dir.
     *
     * @param args Command line args.
     */
    public static void main(String[] args) {

        // Before we do anything else...
        initializeLogging();
        Logger log = Logger.getLogger(Main.class.getName());

        // Check for -v command line arg:
        for (String arg : args) {
            if (arg.trim().equals("-v")) {
                System.out.println(Version.APPLICATION_NAME + " " + Version.VERSION);
                return;
            }
        }

        // For those pesky unchecked exceptions (this ensures they get logged):
        FallbackExceptionHandler.register();

        // You can specify a directory on the command line to open at startup.
        // You can also specify a fully-qualified image file, in which case we'll open
        // its containing directory. Extra arguments beyond the first are ignored.
        // If the given directory doesn't exist, it is logged and then ignored.
        File overrideDir = null;
        if (args.length > 0) {
            File file = new File(args[0]);
            if (file.exists()) {
                overrideDir = file.isDirectory() ? file : file.getParentFile();
            }
            else {
                log.severe("Startup directory does not exist: " + file.getAbsolutePath());
            }
        }

        LookAndFeelManager.installExtraLafs();

        // Get the splash screen if there is one:
        final SplashScreen splashScreen = SplashScreen.getSplashScreen();

        // Load saved application config:
        log.info(Version.APPLICATION_NAME + " " + Version.VERSION + " initializing...");
        ImageViewerExtensionManager.getInstance().loadAll();
        AppConfig.getInstance().load();
        LookAndFeelManager.switchLaf(AppConfig.getInstance().getLookAndFeelClassname());

        // Load and show main window:
        MainWindow window = MainWindow.getInstance();
        window.setStartupDir(overrideDir);
        if (splashScreen != null) {
            try {
                // Wait a second or so, so it doesn't just flash up and disappear immediately.
                Thread.sleep(744);
            }
            catch (InterruptedException ignored) {
                // ignored
            }
            splashScreen.close();
        }

        // Create and display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setVisible(true);
            }
        });
    }

    private static void initializeLogging() {
        // log file can be supplied as a system property:
        if (System.getProperties().containsKey("java.util.logging.config.file")) {
            // Do nothing. It will be used automatically.
        }

        // If it is not set, we'll assume it's in APPLICATION_HOME:
        else {
            File logProperties = new File(Version.SETTINGS_DIR, "logging.properties");
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
    }
}
