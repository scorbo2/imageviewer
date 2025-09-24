package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extensions.AppExtensionInfo;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.ToolBarManager;
import ca.corbett.imageviewer.Version;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This built-in extension provides a basic thumbnail caching mechanism for ImageViewer,
 * to speed up subsequent visits to already-visited directories.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ThumbCacheExtension extends ImageViewerExtension {

    private static final Logger logger = Logger.getLogger(ThumbCacheExtension.class.getName());
    private static final File CACHE_DIR = new File(Version.SETTINGS_DIR, "thumbnails");

    private final AppExtensionInfo extInfo;
    private MessageUtil messageUtil;

    public ThumbCacheExtension() {
        extInfo = new AppExtensionInfo.Builder("Thumbnail caching")
                .setAuthor("steve@corbett.ca")
                .setVersion(Version.VERSION)
                .setTargetAppName(Version.APPLICATION_NAME)
                .setTargetAppVersion(Version.VERSION)
                .setShortDescription("Basic caching of thumbnails")
                .setLongDescription("Stores all generated thumbnails to "
                                            + "reduce load time in directories that "
                                            + "have already been visited.\n\n"
                                            + "Note: no upper limit on cache size. Thumbnails will be stored in the "
                                            + Version.SETTINGS_DIR.getAbsolutePath() + "/thumbnails directory.")
                .setReleaseNotes("1.0 - extracted from ImageViewer 1.3")
                .build();
    }

    @Override
    public AppExtensionInfo getInfo() {
        return extInfo;
    }

    @Override
    protected List<AbstractProperty> createConfigProperties() {
        return List.of(new LabelProperty("Thumbnails.Caching.label", "Thumbnail caching extension is active."));
    }

    @Override
    public void onActivate() {
        CACHE_DIR.mkdirs();
    }

    @Override
    public List<JButton> getMainToolBarButtons() {
        List<JButton> list = new ArrayList<>();
        try {
            list.add(ToolBarManager.buildButton(
                    ImageUtil.loadFromResource(MainWindow.class, "/ca/corbett/imageviewer/images/icon-thumbnails.png",
                                               ToolBarManager.iconSize, ToolBarManager.iconSize),
                    "Pre-generate thumbnails...",
                    new ThumbCachePregenerateAction()));
        }
        catch (IOException ioe) {
            logger.log(Level.SEVERE, "Unable to load image icon for ThumbCacheExtension!", ioe);
        }
        return list;
    }

    @Override
    public List<JMenuItem> getMenuItems(String topLevelMenu, MainWindow.BrowseMode browseMode) {
        if ("View".equals(topLevelMenu)) {
            List<JMenuItem> list = new ArrayList<>();
            JMenuItem item = new JMenuItem(new ThumbCacheViewStatsAction("View thumbnail cache stats"));
            item.setMnemonic(KeyEvent.VK_V);
            list.add(item);

            item = new JMenuItem(new ThumbCacheClearAction("Clear thumbnail cache"));
            item.setMnemonic(KeyEvent.VK_C);
            list.add(item);

            return list;
        }
        return null;
    }

    /**
     * Invoked when the application wants to generate a thumbnail for the given image
     * file at the given size. We will try to locate the desired thumbnail in our cache,
     * and generate it if not found.
     *
     * @param imageFile The file containing the image in question.
     * @param thumbSize The pixel width/height (square) of the desired thumbnail.
     * @return A BufferedImage containing the cached or newly-generated thumbnail.
     */
    @Override
    public BufferedImage getThumbnail(File imageFile, int thumbSize) {
        BufferedImage thumb = ThumbCacheManager.get(imageFile, thumbSize);
        if (thumb == null) {
            ThumbCacheManager.add(imageFile);
            thumb = ThumbCacheManager.get(imageFile, thumbSize);
        }
        return thumb;
    }

    @Override
    public void removeThumbnail(File imageFile) {
        ThumbCacheManager.remove(imageFile);
    }

    /**
     * We hook into this so that we can move, copy, symlink, and delete thumbnails as their
     * master image gets moved, copied, symlinked, or deleted.
     * Note this should be in postImageOperation but there's
     * an <a href="https://github.com/scorbo2/imageviewer/issues/42">issue</a> in the way.
     * TODO clean this up when issue 42 is resolved.
     */
    @Override
    public void preImageOperation(ImageOperation.Type opType, File srcFile, File destFile) {
        switch (opType) {
            case COPY:
            case SYMLINK:
                ThumbCacheManager.copy(srcFile, destFile);
                break;

            case MOVE:
                ThumbCacheManager.move(srcFile, destFile);
                break;

            case DELETE:
                ThumbCacheManager.remove(srcFile);
                break;
        }
    }

    @Override
    public void directoryWasCopied(File oldLocation, File newLocation) {
        ThumbCacheManager.copyDirectory(oldLocation, newLocation);
    }

    @Override
    public void directoryWasMoved(File oldLocation, File newLocation) {
        ThumbCacheManager.moveDirectory(oldLocation, newLocation);
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), logger);
        }
        return messageUtil;
    }

}
