package ca.corbett.imageviewer;

import ca.corbett.extensions.AppProperties;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.gradient.ColorSelectionType;
import ca.corbett.extras.io.KeyStrokeManager;
import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.BooleanProperty;
import ca.corbett.extras.properties.ColorProperty;
import ca.corbett.extras.properties.ComboProperty;
import ca.corbett.extras.properties.DecimalProperty;
import ca.corbett.extras.properties.DirectoryProperty;
import ca.corbett.extras.properties.EnumProperty;
import ca.corbett.extras.properties.FontProperty;
import ca.corbett.extras.properties.IntegerProperty;
import ca.corbett.extras.properties.KeyStrokeProperty;
import ca.corbett.extras.properties.LabelProperty;
import ca.corbett.extras.properties.LookAndFeelProperty;
import ca.corbett.extras.properties.PropertyFormFieldChangeListener;
import ca.corbett.extras.properties.PropertyFormFieldValueChangedEvent;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.ColorScheme;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ReservedKeyStrokeWorkaround;
import ca.corbett.imageviewer.ui.ThumbCacheManager;
import ca.corbett.imageviewer.ui.actions.AboutAction;
import ca.corbett.imageviewer.ui.actions.DeleteCurrentAction;
import ca.corbett.imageviewer.ui.actions.ExitAction;
import ca.corbett.imageviewer.ui.actions.NextImageAction;
import ca.corbett.imageviewer.ui.actions.PreviousImageAction;
import ca.corbett.imageviewer.ui.actions.ReloadAction;
import ca.corbett.imageviewer.ui.actions.RenameAction;
import ca.corbett.imageviewer.ui.actions.SetBrowseModeAction;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static ca.corbett.extras.io.KeyStrokeManager.parseKeyStroke;

public class AppConfig extends AppProperties<ImageViewerExtension> {

    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    protected static AppConfig instance;
    private MessageUtil messageUtil;

    /**
     * Extensions can use this prefix when defining their own keystroke properties,
     * so that they show up on the same properties dialog tab as the other ones.
     * This is optional! Extensions can opt to keep all of their properties
     * on their own separate tab if they prefer.
     * <p>
     * Suggested format: KEYSTROKE_PREFIX + ExtensionUserFriendlyName + "." + ActionName
     * </p>
     */
    public static final String KEYSTROKE_PREFIX = "Keystrokes.";

    /**
     * The application reserves these keystrokes for its own use,
     * so extensions are encouraged to use this reserved list when
     * creating their own KeyStrokeProperties, to avoid conflicts.
     */
    public static final List<KeyStroke> RESERVED_KEYSTROKES = List.of(
            KeyStrokeManager.parseKeyStroke("up"),
            KeyStrokeManager.parseKeyStroke("left"),
            KeyStrokeManager.parseKeyStroke("right"),
            KeyStrokeManager.parseKeyStroke("down"),
            KeyStrokeManager.parseKeyStroke("Ctrl+Q"),
            KeyStrokeManager.parseKeyStroke("Del")
    );

    private static final String KEY_EXIT = "Keystrokes.General.exit";
    private static final String KEY_PREVIOUS_IMAGE1 = "Keystrokes.General.previousImage1";
    private static final String KEY_NEXT_IMAGE1 = "Keystrokes.General.nextImage1";
    private static final String KEY_PREVIOUS_IMAGE2 = "Keystrokes.General.previousImage2";
    private static final String KEY_NEXT_IMAGE2 = "Keystrokes.General.nextImage2";
    private static final String KEY_RENAME = "Keystrokes.General.rename";
    private static final String KEY_DELETE_CURRENT = "Keystrokes.General.deleteCurrent";
    private static final String KEY_BROWSE_MODE_FILESYSTEM = "Keystrokes.General.setBrowseModeFileSystem";
    private static final String KEY_BROWSE_MODE_IMAGE_SET = "Keystrokes.General.setBrowseModeImageSet";
    private static final String KEY_REFRESH = "Keystrokes.General.refresh";
    private static final String KEY_ABOUT = "Keystrokes.General.about";

    private IntegerProperty fileSystemVerticalSplitPanePositionProp;
    private IntegerProperty imageSetVerticalSplitPanePositionProp;
    private IntegerProperty mainSplitPanePositionProp;
    private IntegerProperty mainWindowWidthProp;
    private IntegerProperty mainWindowHeightProp;
    private BooleanProperty showHiddenDirsProp;

    private DirectoryProperty lockDirectoryProp;
    private DirectoryProperty startupDirectoryProp;

    private LookAndFeelProperty lookAndFeelProp;

    private BooleanProperty useSystemColorsProp;
    private EnumProperty<ColorScheme> colorSchemeProp;
    private ColorProperty imagePanelBgColorProp;
    private ColorProperty thumbSelectedBgColorProp;
    private ColorProperty thumbUnselectedBgColorProp;
    private ColorProperty thumbSelectedFontColorProp;
    private ColorProperty thumbUnselectedFontColorProp;
    private ColorProperty thumbContainerBgColorProp;
    private ColorProperty statusPanelBgColorProp;
    private ColorProperty statusPanelFontColorProp;
    private FontProperty thumbPanelFontProp;
    private FontProperty statusPanelFontProp;
    private BooleanProperty statusPanelBorderProp;

    private IntegerProperty toolbarIconSizeProp;
    private IntegerProperty toolbarIconMarginProp;
    private IntegerProperty miniToolbarIconSizeProp;
    private IntegerProperty miniToolbarIconMarginProp;

    private BooleanProperty imagePanelAutoBestFitProp;
    private DecimalProperty imagePanelZoomIncrementProp;

    private BooleanProperty enableQuickMoveProp;
    private BooleanProperty enableQuickCopyProp;
    private BooleanProperty enableQuickLinkProp;
    private BooleanProperty preserveDateTimeProp;

    private EnumProperty<ThumbSize> thumbSizeProp;
    private EnumProperty<ThumbPageSize> thumbPageSizeProp;
    private BooleanProperty thumbCacheEnabledProp;

    private ComboProperty<String> imageSetSaveLocation;
    private DirectoryProperty imageSetSaveLocationOverride;

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

    /**
     * Overridden so we can set the initial enabled/disabled state our properties.
     */
    @Override
    public boolean showPropertiesDialog(Frame owner) {
        boolean isCustom = !useSystemColorsProp.getValue();
        colorSchemeProp.setInitiallyEditable(isCustom);
        imagePanelBgColorProp.setInitiallyEditable(isCustom);
        thumbSelectedBgColorProp.setInitiallyEditable(isCustom);
        thumbUnselectedBgColorProp.setInitiallyEditable(isCustom);
        thumbSelectedFontColorProp.setInitiallyEditable(isCustom);
        thumbUnselectedFontColorProp.setInitiallyEditable(isCustom);
        thumbContainerBgColorProp.setInitiallyEditable(isCustom);
        statusPanelBgColorProp.setInitiallyEditable(isCustom);
        statusPanelFontColorProp.setInitiallyEditable(isCustom);
        return super.showPropertiesDialog(owner);
    }

    public int getFileSystemVerticalSplitPanePosition() {
        return fileSystemVerticalSplitPanePositionProp.getValue();
    }

    public int getImageSetVerticalSplitPanePosition() {
        return imageSetVerticalSplitPanePositionProp.getValue();
    }

    public void setFileSystemVerticalSplitPanePosition(int value) {
        fileSystemVerticalSplitPanePositionProp.setValue(value);
    }

    public void setImageSetVerticalSplitPanePosition(int value) {
        imageSetVerticalSplitPanePositionProp.setValue(value);
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

    public boolean getShowHiddenDirectories() {
        return showHiddenDirsProp.getValue();
    }

    public void setShowHiddenDirectories(boolean show) {
        showHiddenDirsProp.setValue(show);
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

    public Color getImagePanelBackgroundColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("Panel.background")
                : imagePanelBgColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.LIGHT_GRAY;
        }
        return c;
    }

    public Color getThumbPanelSelectedBackgroundColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("TextArea.selectionBackground")
                : thumbSelectedBgColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.BLUE;
        }
        return c;
    }

    public Color getThumbPanelUnselectedBackgroundColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("Button.background")
                : thumbUnselectedBgColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.LIGHT_GRAY;
        }
        return c;
    }

    public Color getThumbPanelSelectedFontColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("TextArea.selectionForeground")
                : thumbSelectedFontColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.WHITE;
        }
        return c;
    }

    public Color getThumbPanelUnselectedFontColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("Button.foreground")
                : thumbUnselectedFontColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.BLACK;
        }
        return c;
    }

    public Color getThumbContainerBackgroundColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("Panel.background")
                : thumbContainerBgColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.LIGHT_GRAY;
        }
        return c;
    }

    public Color getStatusPanelBackgroundColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("Panel.background")
                : statusPanelBgColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.LIGHT_GRAY;
        }
        return c;
    }

    public Color getStatusPanelFontColor() {
        Color c = useSystemColorsProp.getValue()
                ? UIManager.getDefaults().getColor("Label.foreground")
                : statusPanelFontColorProp.getSolidColor();
        if (c == null) {
            // Emergency fallback:
            c = Color.BLACK;
        }
        return c;
    }

    public boolean isStatusPanelBorderEnabled() {
        return statusPanelBorderProp.getValue();
    }

    public Font getThumbPanelFont() {
        return thumbPanelFontProp.getFont();
    }

    public Font getStatusPanelFont() {
        return statusPanelFontProp.getFont();
    }

    public int getToolbarIconSize() {
        return toolbarIconSizeProp.getValue();
    }

    public int getToolbarIconMargin() {
        return toolbarIconMarginProp.getValue();
    }

    public int getMiniToolbarIconSize() {
        return miniToolbarIconSizeProp.getValue();
    }

    public int getMiniToolbarIconMargin() {
        return miniToolbarIconMarginProp.getValue();
    }

    public int getThumbnailSize() {
        return thumbSizeProp.getSelectedItem().getDimensions();
    }

    public int getThumbnailPageSize() {
        return thumbPageSizeProp.getSelectedItem().getSize();
    }

    public boolean isThumbCacheEnabled() {
        return thumbCacheEnabledProp.getValue();
    }

    public File getImageSetSaveLocation() {
        //noinspection unchecked
        ComboProperty<String> prop = (ComboProperty<String>)getPropertiesManager().getProperty(
                imageSetSaveLocation.getFullyQualifiedName());
        if (prop.getSelectedIndex() == 0) {
            return Version.SETTINGS_DIR;
        }
        return imageSetSaveLocationOverride.getDirectory();
    }

    // for unit tests only
    public void setImageSetSaveLocation(File saveLocation) {
        imageSetSaveLocation.setSelectedIndex(1);
        imageSetSaveLocationOverride.setDirectory(saveLocation);
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

    public KeyStroke getRenameKeyStroke() {
        KeyStrokeProperty prop = (KeyStrokeProperty)getPropertiesManager().getProperty(KEY_RENAME);
        return prop.getKeyStroke();
    }

    public KeyStroke getExitKeyStroke() {
        KeyStrokeProperty prop = (KeyStrokeProperty)getPropertiesManager().getProperty(KEY_EXIT);
        return prop.getKeyStroke();
    }

    public KeyStroke getRefreshKeyStroke() {
        KeyStrokeProperty prop = (KeyStrokeProperty)getPropertiesManager().getProperty(KEY_REFRESH);
        return prop.getKeyStroke();
    }

    public KeyStroke getAboutKeyStroke() {
        KeyStrokeProperty prop = (KeyStrokeProperty)getPropertiesManager().getProperty(KEY_ABOUT);
        return prop.getKeyStroke();
    }

    public List<KeyStrokeProperty> getKeyStrokeProperties() {
        List<KeyStrokeProperty> list = new ArrayList<>();

        // Return our built-ins:
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_EXIT));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_PREVIOUS_IMAGE1));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_PREVIOUS_IMAGE2));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_NEXT_IMAGE1));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_NEXT_IMAGE2));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_RENAME));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_DELETE_CURRENT));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_BROWSE_MODE_FILESYSTEM));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_BROWSE_MODE_IMAGE_SET));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_REFRESH));
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_ABOUT));

        // Now ask our extension manager:
        list.addAll(ImageViewerExtensionManager.getInstance().getKeyStrokeProperties());

        return list;
    }

    @Override
    protected List<AbstractProperty> createInternalProperties() {
        List<AbstractProperty> list = new ArrayList<>();

        fileSystemVerticalSplitPanePositionProp = new IntegerProperty(
                "UI.Main Window.fileSystemVerticalSplitPanePosition",
                "sideSplitPanePosition", 400, 1, 9999, 1);
        fileSystemVerticalSplitPanePositionProp.setExposed(false);
        list.add(fileSystemVerticalSplitPanePositionProp);

        imageSetVerticalSplitPanePositionProp = new IntegerProperty(
                "UI.Main Window.imageSetVerticalSplitPanePosition",
                "sideSplitPanePosition", 400, 1, 9999, 1);
        imageSetVerticalSplitPanePositionProp.setExposed(false);
        list.add(imageSetVerticalSplitPanePositionProp);

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

        showHiddenDirsProp = new BooleanProperty("UI.Main Window.showHiddenDirectories", "Show hidden directories",
                                                 true);
        showHiddenDirsProp.setExposed(false); // exposed via dirtree's popup menu
        list.add(showHiddenDirsProp);

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

        toolbarIconSizeProp = new IntegerProperty("UI.Toolbars.iconSize", "Main toolbar icons:", 24, 16, 128, 2);
        toolbarIconSizeProp.setHelpText("Size of icons in the main toolbar.");
        list.add(toolbarIconSizeProp);

        toolbarIconMarginProp = new IntegerProperty("UI.Toolbars.iconMargin", "Main toolbar margin:", 4, 2, 32, 2);
        toolbarIconMarginProp.setHelpText("Margin around icons in the main toolbar.");
        list.add(toolbarIconMarginProp);

        miniToolbarIconSizeProp = new IntegerProperty("UI.Toolbars.miniIconSize", "Mini toolbar icons:", 16,
                                                      16, 64, 2);
        miniToolbarIconSizeProp.setHelpText("Size of icons in mini toolbars (e.g., image set toolbar).");
        list.add(miniToolbarIconSizeProp);

        miniToolbarIconMarginProp = new IntegerProperty("UI.Toolbars.miniIconMargin", "Mini toolbar margin:", 4,
                                                        2, 16, 2);
        miniToolbarIconMarginProp.setHelpText("Margin around icons in mini toolbars (e.g., image set toolbar).");
        list.add(miniToolbarIconMarginProp);

        list.addAll(createColorProperties());
        list.addAll(createFontProperties());

        imageSetSaveLocation = new ComboProperty<>("Image sets.General.imageSetSaveLocation", "Persistence:",
                                                   List.of("Use application settings directory",
                                                           "Choose a specific directory..."), 0, false);
        list.add(imageSetSaveLocation);
        imageSetSaveLocationOverride = new DirectoryProperty("Image sets.General.imageSetSaveDirectoryOverride",
                                                             "Persistence dir:", false, Version.SETTINGS_DIR);
        boolean initiallyVisible = false;
        String currentOption = peek(Version.APP_CONFIG_FILE, "Image sets.General.imageSetSaveLocation");
        if (currentOption != null && currentOption.equals("Choose a specific directory...")) {
            initiallyVisible = true;
        }
        imageSetSaveLocationOverride.setInitiallyVisible(initiallyVisible);
        list.add(imageSetSaveLocationOverride);

        // Set up a listener to make the override visible/invisible as needed:
        imageSetSaveLocation.addFormFieldChangeListener(event -> {
            int index = ((ComboField)event.formField()).getSelectedIndex();
            FormField field = event.formPanel().getFormField("Image sets.General.imageSetSaveDirectoryOverride");
            field.setVisible(index == 1);
        });

        list.addAll(createKeyboardProperties());

        thumbSizeProp = new EnumProperty<>("Thumbnails.General.thumbSize", "Thumb size", ThumbSize.Normal);
        list.add(thumbSizeProp);

        thumbPageSizeProp = new EnumProperty<>("Thumbnails.General.pageSize", "Page size", ThumbPageSize.Normal);
        list.add(thumbPageSizeProp);

        thumbCacheEnabledProp = new BooleanProperty("Thumbnails.Caching.enableThumbCache",
                                                    "Enable automatic caching of thumbnails",
                                                    true);
        thumbCacheEnabledProp.setHelpText("<html>Disabling caching will not remove existing cached thumbnails."
                                                  + "<br>Disabling caching only prevents new thumbnails from being cached."
                                                  + "<br>Any existing thumbnails will still be used when browsing images.</html>");
        list.add(thumbCacheEnabledProp);

        // Not currently configurable, but we can at least show the user where the cache is located:
        LabelProperty label = new LabelProperty("Thumbnails.Caching.infoLabel",
                                                ThumbCacheManager.CACHE_DIR.getAbsolutePath());
        label.setFieldLabelText("Cache dir:");
        list.add(label);

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

    /**
     * Creates all color-related properties used within the application.
     */
    private List<AbstractProperty> createColorProperties() {
        final String PREFIX = "UI.Colors.";
        List<AbstractProperty> props = new ArrayList<>();
        useSystemColorsProp = new BooleanProperty(PREFIX + "useSystemColors", "Use system colors", true);
        useSystemColorsProp.setHelpText("<html>If enabled, the application will use the selected look-and-feel's colors"
                                                + "<br>for various UI elements. If disabled, custom colors can be set"
                                                + "<br>for these elements.</html>");
        colorSchemeProp = new EnumProperty<>(PREFIX + "colorScheme",
                                             "Set from scheme:",
                                             ColorScheme.MATRIX);
        colorSchemeProp.addFormFieldChangeListener(event -> setColorScheme(event));
        imagePanelBgColorProp = createColorProp("imagePanelBackgroundColor",
                                                "Image panel bg:",
                                                "ColorPalette.primaryBackground");
        thumbSelectedBgColorProp = createColorProp("thumbPanelSelectedBackgroundColor",
                                                   "Thumb selected bg:",
                                                   "TextArea.selectionBackground");
        thumbUnselectedBgColorProp = createColorProp("thumbPanelUnselectedBackgroundColor",
                                                     "Thumb unselected bg:",
                                                     "Button.background");
        thumbSelectedFontColorProp = createColorProp("thumbPanelSelectedFontColor",
                                                     "Thumb selected text:",
                                                     "TextArea.selectionForeground");
        thumbContainerBgColorProp = createColorProp("thumbPanelContainerBackgroundColor",
                                                    "Thumb container bg:",
                                                    "Panel.background");
        thumbUnselectedFontColorProp = createColorProp("thumbPanelUnselectedFontColor",
                                                       "Thumb unselected text:",
                                                       "Button.foreground");
        statusPanelBgColorProp = createColorProp("statusPanelBackgroundColor",
                                                 "Status panel bg:",
                                                 "Panel.background");
        statusPanelFontColorProp = createColorProp("statusPanelFontColor",
                                                   "Status panel text:",
                                                   "Label.foreground");

        // Set up a listener to ensure proper enabled/disabled state:
        useSystemColorsProp.addFormFieldChangeListener(new PropertyFormFieldChangeListener() {
            @Override
            public void valueChanged(PropertyFormFieldValueChangedEvent event) {
                boolean isCustom = !((CheckBoxField)event.formField()).isChecked();
                FormPanel fp = event.formPanel();
                fp.getFormField(colorSchemeProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(imagePanelBgColorProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(thumbSelectedBgColorProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(thumbUnselectedBgColorProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(thumbSelectedFontColorProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(thumbUnselectedFontColorProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(thumbContainerBgColorProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(statusPanelBgColorProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(statusPanelFontColorProp.getFullyQualifiedName()).setEnabled(isCustom);
            }
        });

        props.add(useSystemColorsProp);
        props.add(colorSchemeProp);
        props.add(imagePanelBgColorProp);
        props.add(thumbSelectedBgColorProp);
        props.add(thumbUnselectedBgColorProp);
        props.add(thumbSelectedFontColorProp);
        props.add(thumbUnselectedFontColorProp);
        props.add(thumbContainerBgColorProp);
        props.add(statusPanelBgColorProp);
        props.add(statusPanelFontColorProp);

        statusPanelBorderProp = new BooleanProperty(PREFIX + "statusPanelBorder",
                                                    "Show border around status panel",
                                                    true);
        props.add(statusPanelBorderProp);

        return props;
    }

    /**
     * Invoked when the color scheme chooser is modified. Will look up all relevant
     * color fields and set all their values according to the selected scheme.
     */
    private void setColorScheme(PropertyFormFieldValueChangedEvent event) {
        FormPanel formPanel = event.formPanel();
        if (formPanel == null) {
            logger.warning("Unable to set color scheme: form panel is null.");
            return;
        }

        // Look up all our generated form fields:
        ColorField imagePanelBgColorField = (ColorField)formPanel.getFormField(
                imagePanelBgColorProp.getFullyQualifiedName());
        ColorField thumbSelectedBgColorField = (ColorField)formPanel.getFormField(
                thumbSelectedBgColorProp.getFullyQualifiedName());
        ColorField thumbUnselectedBgColorField = (ColorField)formPanel.getFormField(
                thumbUnselectedBgColorProp.getFullyQualifiedName());
        ColorField thumbSelectedFontColorField = (ColorField)formPanel.getFormField(
                thumbSelectedFontColorProp.getFullyQualifiedName());
        ColorField thumbUnselectedFontColorField = (ColorField)formPanel.getFormField(
                thumbUnselectedFontColorProp.getFullyQualifiedName());
        ColorField thumbContainerBgColorField = (ColorField)formPanel.getFormField(
                thumbContainerBgColorProp.getFullyQualifiedName());
        ColorField statusPanelBgColorField = (ColorField)formPanel.getFormField(
                statusPanelBgColorProp.getFullyQualifiedName());
        ColorField statusPanelFontColorField = (ColorField)formPanel.getFormField(
                statusPanelFontColorProp.getFullyQualifiedName());

        // If any of them are null, something has gone off the rails:
        if (imagePanelBgColorField == null || thumbSelectedBgColorField == null ||
                thumbUnselectedBgColorField == null || thumbSelectedFontColorField == null ||
                thumbUnselectedFontColorField == null || thumbContainerBgColorField == null ||
                statusPanelBgColorField == null || statusPanelFontColorField == null) {
            logger.warning("Unable to set color scheme: one or more form fields are null.");
            return;
        }

        // Set them all!
        ColorScheme scheme = (ColorScheme)((ComboField<?>)event.formField()).getSelectedItem();
        imagePanelBgColorField.setColor(scheme.getImagePanelBgColor());
        thumbSelectedBgColorField.setColor(scheme.getThumbSelectedBgColor());
        thumbUnselectedBgColorField.setColor(scheme.getThumbUnselectedBgColor());
        thumbSelectedFontColorField.setColor(scheme.getThumbSelectedFontColor());
        thumbUnselectedFontColorField.setColor(scheme.getThumbUnselectedFontColor());
        thumbContainerBgColorField.setColor(scheme.getThumbContainerBgColor());
        statusPanelBgColorField.setColor(scheme.getStatusPanelBgColor());
        statusPanelFontColorField.setColor(scheme.getStatusPanelFontColor());
    }

    private ColorProperty createColorProp(String name, String label, String defaultKey) {
        final String PREFIX = "UI.Colors.";
        ColorProperty prop = new ColorProperty(PREFIX + name, label, ColorSelectionType.SOLID);
        prop.setSolidColor(UIManager.getDefaults().getColor(defaultKey));
        return prop;
    }

    private List<AbstractProperty> createFontProperties() {
        final String PREFIX = "UI.Fonts.";
        List<AbstractProperty> props = new ArrayList<>();

        statusPanelFontProp = new FontProperty(PREFIX + "statusPanelFont",
                                               "Status panel font:",
                                               new Font("Dialog", Font.PLAIN, 12));
        thumbPanelFontProp = new FontProperty(PREFIX + "thumbPanelFont",
                                              "Thumbnail panel font:",
                                              new Font("Dialog", Font.PLAIN, 11));

        props.add(statusPanelFontProp);
        props.add(thumbPanelFontProp);

        return props;
    }

    /**
     * Creates all properties related to keystrokes used within the application.
     */
    private List<AbstractProperty> createKeyboardProperties() {
        List<AbstractProperty> props = new ArrayList<>();

        // Non-configurable:
        props.add(new KeyStrokeProperty(KEY_EXIT,
                                        "Exit application:",
                                        parseKeyStroke("Ctrl+Q"),
                                        new ExitAction())
                          .setInitiallyEditable(false));
        props.add(new KeyStrokeProperty(KEY_PREVIOUS_IMAGE1,
                                        "Previous image:",
                                        parseKeyStroke("left"),
                                        new PreviousImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false));
        props.add(new KeyStrokeProperty(KEY_PREVIOUS_IMAGE2,
                                        "Previous image:",
                                        parseKeyStroke("up"),
                                        new PreviousImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false));
        props.add(new KeyStrokeProperty(KEY_NEXT_IMAGE1,
                                        "Next image:",
                                        parseKeyStroke("right"),
                                        new NextImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false));
        props.add(new KeyStrokeProperty(KEY_NEXT_IMAGE2,
                                        "Next image:",
                                        parseKeyStroke("down"),
                                        new NextImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false));
        props.add(new KeyStrokeProperty(KEY_DELETE_CURRENT,
                                        "Delete image:",
                                        parseKeyStroke("del"),
                                        new DeleteCurrentAction())
                          .setInitiallyEditable(false));

        // Configurable:
        props.add(new KeyStrokeProperty(KEY_REFRESH,
                                        "Refresh:",
                                        parseKeyStroke("F5"),
                                        new ReloadAction(MenuManager.MENU_ICON_SIZE))
                          .setAllowBlank(true)
                          //.setReservedKeyStrokes(RESERVED_KEYSTROKES) // TODO does not work
                          .addFormFieldGenerationListener(new ReservedKeyStrokeWorkaround())); // workaround
        props.add(new KeyStrokeProperty(KEY_RENAME,
                                        "Rename image:",
                                        parseKeyStroke("F2"),
                                        new RenameAction(MenuManager.MENU_ICON_SIZE))
                          .setAllowBlank(true)
                          //.setReservedKeyStrokes(RESERVED_KEYSTROKES) // TODO does not work
                          .addFormFieldGenerationListener(new ReservedKeyStrokeWorkaround())); // workaround
        props.add(new KeyStrokeProperty(KEY_BROWSE_MODE_FILESYSTEM,
                                        "Filesystem mode:",
                                        parseKeyStroke("alt+1"),
                                        new SetBrowseModeAction(MainWindow.BrowseMode.FILE_SYSTEM))
                          .setAllowBlank(true)
                          //.setReservedKeyStrokes(RESERVED_KEYSTROKES) // TODO does not work
                          .addFormFieldGenerationListener(new ReservedKeyStrokeWorkaround())); // workaround
        props.add(new KeyStrokeProperty(KEY_BROWSE_MODE_IMAGE_SET,
                                        "Image set mode:",
                                        parseKeyStroke("alt+2"),
                                        new SetBrowseModeAction(MainWindow.BrowseMode.IMAGE_SET))
                          .setAllowBlank(true)
                          //.setReservedKeyStrokes(RESERVED_KEYSTROKES) // TODO does not work
                          .addFormFieldGenerationListener(new ReservedKeyStrokeWorkaround())); // workaround
        props.add(new KeyStrokeProperty(KEY_ABOUT,
                                        "About dialog:",
                                        parseKeyStroke("Ctrl+A"),
                                        new AboutAction(MenuManager.MENU_ICON_SIZE))
                          .setAllowBlank(true)
                          //.setReservedKeyStrokes(RESERVED_KEYSTROKES) // TODO does not work
                          .addFormFieldGenerationListener(new ReservedKeyStrokeWorkaround())); // workaround

        return props;
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
