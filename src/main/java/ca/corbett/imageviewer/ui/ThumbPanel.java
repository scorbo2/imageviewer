package ca.corbett.imageviewer.ui;

import ca.corbett.extras.LookAndFeelManager;
import ca.corbett.extras.RedispatchingMouseAdapter;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A Panel to display a single Image's thumbnail. This panel wraps an ImagePanel instance,
 * as well as a title label. It can also display itself as "selected" or not (by changing
 * bgcolor), but it does not itself listen for mouse events. This is because the client code
 * likely wants to display a list of these panels to handle multiple selection events
 * (ctrl+click or shift+click, or from a menu event), and that is beyond the scope of
 * this component to handle.
 *
 * @author scorbo2
 * @since 2017-11-11 (based on ThumbPanel from ice)
 */
public class ThumbPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(ThumbPanel.class.getName());
    private static BufferedImage invalidImage = null; // to represent images that can't be loaded

    private File srcFile;
    private final BufferedImage thumbImage;
    private boolean isSelected;
    private JLabel imageLabel;
    private ImagePanel imagePanel;
    private String title;
    private int width;
    private int height;
    private static final ImagePanelConfig PROPS;
    private final Map<String, Object> extraProperties;

    // Set up image panel props once to save a bit of memory.
    static {
        PROPS = ImagePanelConfig.createSimpleReadOnlyProperties();
        PROPS.setRenderingQuality(ImagePanelConfig.Quality.QUICK_AND_DIRTY);
        PROPS.setDisplayMode(ImagePanelConfig.DisplayMode.CENTER);
        PROPS.setBgColor(LookAndFeelManager.getLafColor("Panel.background", Color.LIGHT_GRAY));
    }

    /**
     * To construct, supply the File handle on the image in question, an optional title
     * to display as a caption under the image, and a desired panel width and height.
     * Note that the thumbnail dimensions will be taken from preferences.
     *
     * @param file       The source file from which this image was loaded.
     * @param thumbImage The thumbnail that has been generated for the given File.
     * @param title      The title to write into the label under the image. Can be null or empty.
     * @param width      The desired width of this panel.
     * @param height     The desired height of this panel.
     */
    public ThumbPanel(File file, BufferedImage thumbImage, String title, int width, int height) {
        this.srcFile = file;
        this.thumbImage = thumbImage;
        this.title = (title == null) ? "" : title;
        this.width = width;
        this.height = height;
        isSelected = false;
        extraProperties = new HashMap<>();

        initComponents();
    }

    /**
     * Sets the preferred size of this panel. Overridden here as we need to scale our
     * thumbnail up or down as needed.
     *
     * @param size
     */
    @Override
    public void setPreferredSize(Dimension size) {
        width = size.width;
        height = size.height;
        super.setPreferredSize(size);
    }

    /**
     * Allows setting some arbitrary property to some arbitrary value.
     *
     * @param name  The name of the property
     * @param value Any Object
     */
    public void setExtraProperty(String name, Object value) {
        extraProperties.put(name, value);
    }

    /**
     * Returns the extra property by the given name, if there is one.
     *
     * @param name The name of the property.
     * @return An Object value, or null if not found.
     */
    public Object getExtraProperty(String name) {
        return extraProperties.get(name);
    }

    /**
     * Internal method to initialize all subcomponents and set up the layout.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        setBackground(LookAndFeelManager.getLafColor("Panel.background", Color.LIGHT_GRAY));
        setPreferredSize(new Dimension(width, height));

        // If invalidImage has not yet been generated, do it now.
        // We only generate one of these statically to save memory usage.
        if (invalidImage == null) {
            int thumbSize = AppConfig.getInstance().getThumbnailSize();
            invalidImage = new BufferedImage(thumbSize - 20, thumbSize - 20, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = invalidImage.createGraphics();
            g.setColor(LookAndFeelManager.getLafColor("Panel.background", Color.LIGHT_GRAY));
            g.fillRect(0, 0, thumbSize, thumbSize);
            g.setColor(LookAndFeelManager.getLafColor("textHighlightText", Color.WHITE));
            g.setFont(new Font("SansSerif", 0, 64));
            int textWidth = (int)g.getFontMetrics().stringWidth("?");
            int textHeight = (int)g.getFontMetrics().getLineMetrics("?", g).getAscent();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.drawString("?", (thumbSize - textWidth) / 2 - 10, (thumbSize / 2) + (int)(textHeight / 2.4) - 10);
            g.dispose();
        }

        imagePanel = new ImagePanel(thumbImage == null ? invalidImage : thumbImage, PROPS);
        add(imagePanel, BorderLayout.CENTER);

        // Note the ImagePanel above doesn't need these redispatching adapters because
        // it's smart enough to do this itself. The JLabel below is not so smart.
        imageLabel = new JLabel(title, JLabel.CENTER);
        imageLabel.addMouseListener(new RedispatchingMouseAdapter());
        imageLabel.addMouseWheelListener(new RedispatchingMouseAdapter());
        imageLabel.addMouseMotionListener(new RedispatchingMouseAdapter());
        add(imageLabel, BorderLayout.SOUTH);
    }

    /**
     * Sets the "selected" value of this panel. This changes the background colour to indicate such.
     *
     * @param selected Whether to select this panel or not.
     */
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        // TODO is this still needed? seems to work okay without it
        //if (!MainWindow.getInstance().isFullscreenActive()) {
        //  this.requestFocus();
        // }
        if (isSelected) {
            Color selectedBg = LookAndFeelManager.getLafColor("textHighlight", Color.BLUE);
            setBackground(selectedBg);
            imagePanel.setBackground(selectedBg);
            imageLabel.setForeground(LookAndFeelManager.getLafColor("textHighlightText", Color.LIGHT_GRAY));
        }
        else {
            Color bg = LookAndFeelManager.getLafColor("Panel.background", Color.LIGHT_GRAY);
            setBackground(bg);
            imagePanel.setBackground(bg);
            imageLabel.setForeground(LookAndFeelManager.getLafColor("textHighlightText", Color.BLACK));
        }
        ImageViewerExtensionManager.getInstance().thumbPanelSelectionChanged(this, isSelected);
        repaint();
    }

    /**
     * Returns whether this panel is showing as "selected" or not.
     *
     * @return The current selected value.
     */
    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Updates the label showing underneath the thumb image. Can be null or empty string.
     *
     * @param title The new label text.
     */
    public void setTitle(String title) {
        this.title = (title == null) ? "" : title;
        imageLabel.setText(title);
    }

    /**
     * Returns the label for this ThumbPanel.
     *
     * @return The current label for this ThumbPanel.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the BufferedImage being displayed in this panel.
     *
     * @return A BufferedImage.
     */
    public BufferedImage getThumbImage() {
        return thumbImage == null ? invalidImage : thumbImage;
    }

    /**
     * Returns the source file from which this image was loaded.
     *
     * @return The source file for this image.
     */
    public File getFile() {
        return srcFile;
    }

    /**
     * Modifies the source file and the title of this ThumbPanel.
     * Invoked only on a rename.
     *
     * @param newFile the new File object. Not validated here.
     */
    public void renameFile(File newFile) {
        srcFile = newFile;
        ImageViewerExtensionManager.getInstance().thumbPanelRenamed(this, newFile);
        if (!title.isEmpty()) {
            setTitle(newFile.getName());
        }
    }

}
