package ca.corbett.imageviewer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fallback handler for any exception that goes uncaught in any thread.
 * Basically we just want to make sure that the stack trace shows up
 * in the log file. A recent incident (issue #117) had an uncaught
 * runtime exception effectively kill the app with nothing in the log.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 3.1
 */
public class FallbackExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger log = Logger.getLogger(FallbackExceptionHandler.class.getName());

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.log(Level.SEVERE, "Uncaught exception in thread " + t.getName() + ": " + e.getMessage(), e);
    }
}
