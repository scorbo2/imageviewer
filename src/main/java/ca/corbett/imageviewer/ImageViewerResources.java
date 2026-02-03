package ca.corbett.imageviewer;

import ca.corbett.extras.ResourceLoader;
import ca.corbett.extras.image.ImageUtil;

import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class ImageViewerResources extends ResourceLoader {

    private static final Logger log = Logger.getLogger(ImageViewerResources.class.getName());

    private static final String PREFIX = "ca/corbett/imageviewer/images/";

    // Icons used in core application:
    private static final String ICON_PREVIOUS = "icon-go-previous.png";
    private static final String ICON_NEXT = "icon-go-next.png";
    private static final String ICON_ZOOM_IN = "icon-zoom-in2.png";
    private static final String ICON_ZOOM_OUT = "icon-zoom-out2.png";
    private static final String ICON_BEST_FIT = "icon-best-fit2.png";
    private static final String ICON_ACTUAL_SIZE = "icon-actual-size2.png";
    private static final String ICON_RELOAD = "icon-reload.png";
    private static final String ICON_MOVE_ITEM = "icon-document-upload.png";
    private static final String ICON_DELETE = "icon-x.png"; // regular "X"
    private static final String ICON_NUKE = "icon-delete.png"; // scary red "X"
    private static final String ICON_SETTINGS = "icon-settings.png";
    private static final String ICON_EXT_MANAGER = "icon-image-information.png";
    private static final String ICON_ABOUT = "icon-help2.png";
    private static final String ICON_THUMBNAILS = "icon-thumbnails.png";
    private static final String ICON_SET_EDIT = "icon-document-edit.png";
    private static final String ICON_SET_RELOAD = "icon-reboot.png";
    private static final String ICON_SAVE = "icon-save.png";
    private static final String ICON_RENAME = "icon-rename.png";

    // Not used in core application, but can be used by extensions:
    private static final String ICON_PLUS = "icon-plus.png";
    private static final String ICON_MINUS = "icon-minus.png";
    private static final String ICON_CHECK = "icon-check.png";
    private static final String ICON_QUESTION = "icon-help.png";
    private static final String ICON_EXTENSIONS = "icon-extensions.png";
    private static final String ICON_EYE = "icon-eye.png";
    private static final String ICON_FULLSCREEN = "icon-fullscreen.png";
    private static final String ICON_HEART = "icon-heart.png";
    private static final String ICON_ARROW_LEFT = "icon-previous.png";
    private static final String ICON_ARROW_RIGHT = "icon-next.png";

    private static final int NO_RESIZE = 0;
    public static final int NATIVE_ICON_SIZE = 48;
    static final int MAX_ICON_SIZE = 256;

    public static BufferedImage getIconPrevious(int size) {
        return internalLoad(ICON_PREVIOUS, size);
    }

    public static BufferedImage getIconNext(int size) {
        return internalLoad(ICON_NEXT, size);
    }

    public static BufferedImage getIconZoomIn(int size) {
        return internalLoad(ICON_ZOOM_IN, size);
    }

    public static BufferedImage getIconZoomOut(int size) {
        return internalLoad(ICON_ZOOM_OUT, size);
    }

    public static BufferedImage getIconBestFit(int size) {
        return internalLoad(ICON_BEST_FIT, size);
    }

    public static BufferedImage getIconReload(int size) {
        return internalLoad(ICON_RELOAD, size);
    }

    public static BufferedImage getIconMoveItem(int size) {
        return internalLoad(ICON_MOVE_ITEM, size);
    }

    public static BufferedImage getIconDelete(int size) {
        return internalLoad(ICON_DELETE, size);
    }

    public static BufferedImage getIconNuke(int size) {
        return internalLoad(ICON_NUKE, size);
    }

    public static BufferedImage getIconActualSize(int size) {
        return internalLoad(ICON_ACTUAL_SIZE, size);
    }

    public static BufferedImage getIconSettings(int size) {
        return internalLoad(ICON_SETTINGS, size);
    }

    public static BufferedImage getIconExtManager(int size) {
        return internalLoad(ICON_EXT_MANAGER, size);
    }

    public static BufferedImage getIconAbout(int size) {
        return internalLoad(ICON_ABOUT, size);
    }

    public static BufferedImage getIconThumbnails(int size) {
        return internalLoad(ICON_THUMBNAILS, size);
    }

    public static BufferedImage getIconImageSetEdit(int size) {
        return internalLoad(ICON_SET_EDIT, size);
    }

    public static BufferedImage getIconImageSetReload(int size) {
        return internalLoad(ICON_SET_RELOAD, size);
    }

    public static BufferedImage getIconSave(int size) {
        return internalLoad(ICON_SAVE, size);
    }

    public static BufferedImage getIconRename(int size) {
        return internalLoad(ICON_RENAME, size);
    }

    public static BufferedImage getIconPlus(int size) {
        return internalLoad(ICON_PLUS, size);
    }

    public static BufferedImage getIconMinus(int size) {
        return internalLoad(ICON_MINUS, size);
    }

    public static BufferedImage getIconCheck(int size) {
        return internalLoad(ICON_CHECK, size);
    }

    public static BufferedImage getIconQuestion(int size) {
        return internalLoad(ICON_QUESTION, size);
    }

    public static BufferedImage getIconExtensions(int size) {
        return internalLoad(ICON_EXTENSIONS, size);
    }

    public static BufferedImage getIconEye(int size) {
        return internalLoad(ICON_EYE, size);
    }

    public static BufferedImage getIconFullscreen(int size) {
        return internalLoad(ICON_FULLSCREEN, size);
    }

    public static BufferedImage getIconHeart(int size) {
        return internalLoad(ICON_HEART, size);
    }

    public static BufferedImage getIconArrowLeft(int size) {
        return internalLoad(ICON_ARROW_LEFT, size);
    }

    public static BufferedImage getIconArrowRight(int size) {
        return internalLoad(ICON_ARROW_RIGHT, size);
    }

    /**
     * Returns a scaled version of the input icon, if it is not already at the
     * given size (assuming square icons). If the input icon is null, null is returned.
     *
     * @param imageIcon Any ImageIcon instance.
     * @param size      The requested size (assuming square icons).
     * @return A scaled ImageIcon instance, or null if the input icon was null.
     */
    public static ImageIcon scaleIcon(ImageIcon imageIcon, int size) {
        BufferedImage image = null;
        if (imageIcon != null) {
            image = (BufferedImage)imageIcon.getImage();
            if (image.getHeight() != size || image.getWidth() != size) {
                // Resize the image to match the specified size (assuming square icons):
                image = ImageUtil.generateThumbnailWithTransparency(image, size, size);
            }
        }
        return image == null ? null : new ImageIcon(image);
    }

    /**
     * All icons are all stored at 48x48 internally, but can be requested at any size.
     *
     * @param resourceName Any of the icon name constants.
     * @param size         The requested size. Use NO_RESIZE or a negative value to get the native size of 48x48.
     * @return A BufferedImage instance.
     */
    static BufferedImage internalLoad(String resourceName, int size) {
        if (resourceName == null) {
            return null;
        }

        BufferedImage icon = getImage(PREFIX + resourceName); // Load at native size (48x48)

        // If it couldn't be loaded, we have a problem:
        // (this *should* never happen, but perhaps our jar was packaged incorrectly)
        if (icon == null) {
            log.severe("ImageViewerResources: Could not load icon resource: " + resourceName);
            return null;
        }

        // Resize if needed:
        if (size > 0 && size != NATIVE_ICON_SIZE) {
            // Put some kind of cap on it to avoid stupid issues:
            if (size > MAX_ICON_SIZE) {
                // *should* never happen, but let's be safe:
                log.warning("ImageViewerResources: Requested icon size too large (" + size + "), capping at 256");
                size = MAX_ICON_SIZE;
            }

            // Now scale it:
            icon = ImageUtil.generateThumbnailWithTransparency(icon, size, size);
        }

        return icon;
    }
}
