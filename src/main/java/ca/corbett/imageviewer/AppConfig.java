package ca.corbett.imageviewer;

import ca.corbett.extensions.AppProperties;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.BooleanProperty;
import ca.corbett.extras.properties.DecimalProperty;
import ca.corbett.extras.properties.DirectoryProperty;
import ca.corbett.extras.properties.EnumProperty;
import ca.corbett.extras.properties.IntegerProperty;
import ca.corbett.extras.properties.LookAndFeelProperty;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import com.formdev.flatlaf.FlatDarkLaf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AppConfig extends AppProperties<ImageViewerExtension> {

    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    protected static AppConfig instance;
    private MessageUtil messageUtil;

    private IntegerProperty sideSplitPanePositionProp;
    private IntegerProperty mainSplitPanePositionProp;
    private IntegerProperty mainWindowWidthProp;
    private IntegerProperty mainWindowHeightProp;

    private DirectoryProperty lockDirectoryProp;
    private DirectoryProperty startupDirectoryProp;

    private LookAndFeelProperty lookAndFeelProp;

    private BooleanProperty imagePanelAutoBestFitProp;
    private DecimalProperty imagePanelZoomIncrementProp;

    private BooleanProperty enableQuickMoveProp;
    private BooleanProperty enableQuickCopyProp;
    private BooleanProperty enableQuickLinkProp;
    private BooleanProperty preserveDateTimeProp;

    private EnumProperty<ThumbSize> thumbSizeProp;
    private EnumProperty<ThumbPageSize> thumbPageSizeProp;


    protected AppConfig() {
        super(Version.APPLICATION_NAME + " " + Version.VERSION,
              Version.APP_CONFIG_FILE,
              ImageViewerExtensionManager.getInstance());
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }

        return instance;
    }

    public int getSideSplitPanePosition() {
        return sideSplitPanePositionProp.getValue();
    }

    public void setSideSplitPanePosition(int value) {
        sideSplitPanePositionProp.setValue(value);
    }

    public int getMainSplitPanePosition() {
        return mainSplitPanePositionProp.getValue();
    }

    public void setMainSplitPanePosition(int value) {
        mainSplitPanePositionProp.setValue(value);
    }

    public int getMainWindowWidth() {
        return mainWindowWidthProp.getValue();
    }

    public void setMainWindowWidth(int value) {
        mainWindowWidthProp.setValue(value);
    }

    public int getMainWindowHeight() {
        return mainWindowHeightProp.getValue();
    }

    public void setMainWindowHeight(int value) {
        mainWindowHeightProp.setValue(value);
    }

    public File getLockDirectory() {
        return lockDirectoryProp.getDirectory();
    }

    public void setLockDirectory(File dir) {
        lockDirectoryProp.setDirectory(dir);
    }

    public File getStartupDirectory() {
        return startupDirectoryProp.getDirectory();
    }

    public void setStartupDirectory(File dir) {
        startupDirectoryProp.setDirectory(dir);
    }

    public boolean getImagePanelAutoBestFit() {
        return imagePanelAutoBestFitProp.getValue();
    }

    public double getImagePanelZoomIncrement() {
        return imagePanelZoomIncrementProp.getValue();
    }

    public String getLookAndFeelClassname() {
        return lookAndFeelProp.getSelectedLafClass();
    }

    public int getThumbnailSize() {
        return thumbSizeProp.getSelectedItem().getDimensions();
    }

    public int getThumbnailPageSize() {
        return thumbPageSizeProp.getSelectedItem().getSize();
    }

    public boolean isQuickMoveEnabled() {
        return enableQuickMoveProp.getValue();
    }

    public boolean isQuickCopyEnabled() {
        return enableQuickCopyProp.getValue();
    }

    public boolean isQuickLinkEnabled() {
        return enableQuickLinkProp.getValue();
    }

    public boolean isPreserveDateTimeEnabled() {
        logger.info("preserveDateTime: " + preserveDateTimeProp.getValue());
        return preserveDateTimeProp.getValue();
    }

    @Override
    protected List<AbstractProperty> createInternalProperties() {
        List<AbstractProperty> list = new ArrayList<>();

        sideSplitPanePositionProp = new IntegerProperty("UI.Main Window.sideSplitPanePosition", "sideSplitPanePosition",
                                                        400, 1, 9999, 1);
        sideSplitPanePositionProp.setExposed(false);
        list.add(sideSplitPanePositionProp);

        mainSplitPanePositionProp = new IntegerProperty("UI.Main Window.mainSplitPanePosition", "mainSplitPanePosition",
                                                        180, 1, 9999, 1);
        mainSplitPanePositionProp.setExposed(false);
        list.add(mainSplitPanePositionProp);

        mainWindowWidthProp = new IntegerProperty("UI.Main Window.mainWindowWidth", "mainWindowWidth",
                                                  MainWindow.MIN_WIDTH, MainWindow.MIN_WIDTH, 9999, 1);
        mainWindowWidthProp.setExposed(false);
        list.add(mainWindowWidthProp);

        mainWindowHeightProp = new IntegerProperty("UI.Main Window.mainWindowHeight", "mainWindowHeight",
                                                   MainWindow.MIN_HEIGHT, MainWindow.MIN_HEIGHT, 9999, 1);
        mainWindowHeightProp.setExposed(false);
        list.add(mainWindowHeightProp);

        lockDirectoryProp = new DirectoryProperty("UI.Main Window.lockDirectory", "Lock directory", true, null);
        lockDirectoryProp.setExposed(false);
        list.add(lockDirectoryProp);

        startupDirectoryProp = new DirectoryProperty("UI.Main Window.startupDirectory", "Startup directory", true,
                                                     null);
        startupDirectoryProp.setExposed(false);
        list.add(startupDirectoryProp);

        imagePanelAutoBestFitProp = new BooleanProperty("UI.ImagePanel.autoBestFit", "Auto best fit image", true);
        list.add(imagePanelAutoBestFitProp);

        imagePanelZoomIncrementProp = new DecimalProperty("UI.ImagePanel.zoomIncrement", "Zoom increment", 0.02, 0.01,
                                                          0.99, 0.01);
        list.add(imagePanelZoomIncrementProp);

        lookAndFeelProp = new LookAndFeelProperty("UI.Look and Feel.Look and Feel", "Look and Feel:",
                                                  FlatDarkLaf.class.getName());
        list.add(lookAndFeelProp);

        thumbSizeProp = new EnumProperty<>("Thumbnails.General.thumbSize", "Thumb size", ThumbSize.Normal);
        list.add(thumbSizeProp);

        thumbPageSizeProp = new EnumProperty<>("Thumbnails.General.pageSize", "Page size", ThumbPageSize.Normal);
        list.add(thumbPageSizeProp);

        enableQuickMoveProp = new BooleanProperty("Quick Move.enableQuickMove", "Enable image move operations", true);
        list.add(enableQuickMoveProp);
        enableQuickCopyProp = new BooleanProperty("Quick Move.enableQuickCopy", "Enable image copy operations", true);
        list.add(enableQuickCopyProp);
        enableQuickLinkProp = new BooleanProperty("Quick Move.enableQuickLink", "Enable image link operations", true);
        list.add(enableQuickLinkProp);
        preserveDateTimeProp = new BooleanProperty("Quick Move.File time preservation.preserveFileTime",
                                                   "Preserve date/time on file when moving/copying", true);
        list.add(preserveDateTimeProp);

        return list;
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), logger);
        }
        return messageUtil;
    }

    public enum ThumbSize {
        VerySmall("32x32", 32),
        Small("64x64", 64),
        Normal("80x80", 80),
        Large("100x100", 100),
        VeryLarge("150x150", 150),
        FriggingHuge("200x200", 200);

        private final String label;
        private final int dimensions;

        ThumbSize(String label, int dimensions) {
            this.label = label;
            this.dimensions = dimensions;
        }

        public int getDimensions() {
            return dimensions;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum ThumbPageSize {
        Unlimited(0),
        VerySmall(5),
        Small(10),
        Normal(20),
        Large(50),
        VeryLarge(100),
        FriggingHuge(200);

        private final int size;

        ThumbPageSize(int size) {
            this.size = size;
        }

        public int getSize() {
            return size;
        }

        @Override
        public String toString() {
            return Integer.toString(size);
        }
    }
}
