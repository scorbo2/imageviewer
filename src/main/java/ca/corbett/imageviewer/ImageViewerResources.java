package ca.corbett.imageviewer;

import ca.corbett.extras.ResourceLoader;
import ca.corbett.extras.image.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class ImageViewerResources extends ResourceLoader {

    private static final Logger log = Logger.getLogger(ImageViewerResources.class.getName());

    private static final String PREFIX = "ca/corbett/imageviewer/images/";

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

    private static final int NO_RESIZE = 0;
    private static final int NATIVE_SIZE = 48;
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
        if (size > 0 && size != NATIVE_SIZE) {
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
