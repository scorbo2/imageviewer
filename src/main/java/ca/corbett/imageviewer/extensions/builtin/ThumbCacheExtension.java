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
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * @author scorbo2
 */
public class ThumbCacheExtension extends ImageViewerExtension {

    private static final Logger logger = Logger.getLogger(ThumbCacheExtension.class.getName());
    private static final File CACHE_DIR = new File(Version.APPLICATION_HOME, "thumbnails");

    private final AppExtensionInfo extInfo;
    private MessageUtil messageUtil;

    public ThumbCacheExtension() {
        extInfo = new AppExtensionInfo.Builder("Thumbnail caching")
                .setAuthor("steve@corbett.ca")
                .setVersion("1.0")
                .setTargetAppName(Version.APPLICATION_NAME)
                .setTargetAppVersion(Version.VERSION)
                .setShortDescription("Basic caching of thumbnails")
                .setLongDescription("Stores all generated thumbnails to "
                                            + "reduce load time in directories that "
                                            + "have already been visited.\n\n"
                                            + "Note: no upper limit on cache size. Thumbnails will be stored in the "
                                            + Version.APPLICATION_HOME.getAbsolutePath() + "/thumbnails directory.")
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
    public List<JButton> getToolBarButtons() {
        List<JButton> list = new ArrayList<>();
        try {
            list.add(ToolBarManager.buildButton(
                    ImageUtil.loadFromResource(MainWindow.class, "/ca/corbett/imageviewer/images/icon-pregenerate.png",
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
    public List<JMenuItem> getMenuItems(String topLevelMenu) {
        if ("View".equals(topLevelMenu)) {
            List<JMenuItem> list = new ArrayList<>();
            JMenuItem item = new JMenuItem("View thumbnail cache stats");
            item.setMnemonic(KeyEvent.VK_V);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    getMessageUtil().info("Thumbnail cache stats", ThumbCacheManager.gatherCacheStats().toString());
                }

            });
            list.add(item);

            item = new JMenuItem("Clear thumbnail cache");
            item.setMnemonic(KeyEvent.VK_C);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                                      "Are you sure you wish to clear cache? This can't be undone.",
                                                      "Confirm",
                                                      JOptionPane.YES_NO_OPTION,
                                                      JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        ThumbCacheManager.clear();
                        getMessageUtil().info("Cache cleared!");
                    }

                }

            });
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
