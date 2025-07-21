package ca.corbett.imageviewer;

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

        // Check for -v command line arg:
        for (String arg : args) {
            if (arg.trim().equals("-v")) {
                System.out.println(Version.APPLICATION_NAME + " " + Version.VERSION);
                return;
            }
        }

        // This is extremely stupid, but due to the way ProcessBuilder handles command
        // line arguments with spaces in the path or filename, we have to assume that if
        // we receive multiple args, that it's actually Java being stupid on the calling
        // end (i.e. Darwin) and that it is in fact a single path. So, join all arguments
        // together with spaces and try to parse it into a single path.
        File overrideDir = null;
        if (args.length > 0) {
            StringBuilder path = new StringBuilder();

            for (int i = 0; i < args.length; i++) {
                path.append(args[i]);
                if (i < args.length - 1) {
                    path.append(" ");
                }
            }

            File file = new File(path.toString());
            if (file.exists()) {

                // You can specify a directory name, in which case we'll take it as-is:
                if (file.isDirectory()) {
                    overrideDir = file;
                }

                // Of you can specify a specific file, in which case we'll take it's containing dir:
                else if (file.getParentFile().exists() && file.getParentFile().isDirectory()) {
                    overrideDir = file.getParentFile();
                }
            }
        }

        LookAndFeelManager.installExtraLafs();

        // Get the splash screen if there is one:
        final SplashScreen splashScreen = SplashScreen.getSplashScreen();

        // Load saved application config:
        Logger.getLogger(Main.class.getName())
              .info(Version.APPLICATION_NAME + " " + Version.VERSION + " initializing...");
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
