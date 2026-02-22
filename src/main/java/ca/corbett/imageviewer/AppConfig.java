package ca.corbett.imageviewer;

import ca.corbett.extensions.AppProperties;
import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.actionpanel.ActionPanel;
import ca.corbett.extras.actionpanel.ColorTheme;
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
import ca.corbett.extras.properties.dialog.ActionPanelPropertiesDialog;
import ca.corbett.extras.properties.dialog.PropertiesDialog;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.ColorField;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.imageviewer.extensions.ImageViewerExtension;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.ColorScheme;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ThumbCacheManager;
import ca.corbett.imageviewer.ui.actions.AboutAction;
import ca.corbett.imageviewer.ui.actions.DeleteCurrentAction;
import ca.corbett.imageviewer.ui.actions.ExitAction;
import ca.corbett.imageviewer.ui.actions.ImageSetDeleteSourceImageAction;
import ca.corbett.imageviewer.ui.actions.NextImageAction;
import ca.corbett.imageviewer.ui.actions.PreviousImageAction;
import ca.corbett.imageviewer.ui.actions.ReloadAction;
import ca.corbett.imageviewer.ui.actions.RenameAction;
import ca.corbett.imageviewer.ui.actions.SetBrowseModeAction;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static ca.corbett.extras.io.KeyStrokeManager.parseKeyStroke;

/**
 * A custom AppProperties implementation for ImageViewer, defining all of the
 * application's built-in properties.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class AppConfig extends AppProperties<ImageViewerExtension> {

    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());
    protected static AppConfig instance;
    private MessageUtil messageUtil;

    /**
     * Extensions can use this prefix when defining their own keystroke properties,
     * so that they show up on the same properties dialog tab/action group as the other ones.
     * This is optional! Extensions can opt to keep all of their properties
     * on their own separate tab/action group if they prefer.
     * <p>
     * Suggested format: KEYSTROKE_PREFIX + ExtensionUserFriendlyName + "." + ActionName
     * </p>
     */
    public static final String KEYSTROKE_PREFIX = "Keystrokes.";

    /**
     * If an extension only has a single keystroke to publish, then consider putting it
     * in the "miscellaneous tools" section, to avoid cluttering up the main keystrokes tab/action group.
     * If there are several keystrokes, then consider using the KEYSTROKE_PREFIX with its
     * suggested format instead.
     */
    public static final String KEYSTROKE_MISC_PREFIX = KEYSTROKE_PREFIX + "Miscellaneous tools.";

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

    private static final String KEY_EXIT = "Keystrokes.Main window.exit";
    private static final String KEY_PREVIOUS_IMAGE1 = "Keystrokes.Main window.previousImage1";
    private static final String KEY_NEXT_IMAGE1 = "Keystrokes.Main window.nextImage1";
    private static final String KEY_PREVIOUS_IMAGE2 = "Keystrokes.Main window.previousImage2";
    private static final String KEY_NEXT_IMAGE2 = "Keystrokes.Main window.nextImage2";
    private static final String KEY_RENAME = "Keystrokes.Main window.rename";
    private static final String KEY_DELETE_CURRENT = "Keystrokes.Main window.deleteCurrent";
    private static final String KEY_DELETE_SOURCE = "Keystrokes.Main window.deleteSource";
    private static final String KEY_BROWSE_MODE_FILESYSTEM = "Keystrokes.Main window.setBrowseModeFileSystem";
    private static final String KEY_BROWSE_MODE_IMAGE_SET = "Keystrokes.Main window.setBrowseModeImageSet";
    private static final String KEY_REFRESH = "Keystrokes.Main window.refresh";
    private static final String KEY_ABOUT = "Keystrokes.Main window.about";

    private IntegerProperty fileSystemVerticalSplitPanePositionProp;
    private IntegerProperty imageSetVerticalSplitPanePositionProp;
    private IntegerProperty mainSplitPanePositionProp;
    private IntegerProperty mainWindowWidthProp;
    private IntegerProperty mainWindowHeightProp;
    private BooleanProperty showHiddenDirsProp;

    private DirectoryProperty lockDirectoryProp;
    private DirectoryProperty startupDirectoryProp;

    private LookAndFeelProperty lookAndFeelProp;

    private BooleanProperty useCustomColorsProp;
    private EnumProperty<ColorScheme> colorSchemeProp;
    private ColorProperty defaultBackgroundProp;
    private ColorProperty defaultForegroundProp;
    private ColorProperty selectedBackgroundProp;
    private ColorProperty selectedForegroundProp;
    private ColorProperty unselectedBackgroundProp;
    private ColorProperty unselectedForegroundProp;
    private EnumProperty<ColorTheme> actionPanelThemeProp;
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
        boolean isCustom = useCustomColorsProp.getValue();
        colorSchemeProp.setInitiallyEditable(isCustom);
        defaultBackgroundProp.setInitiallyEditable(isCustom);
        defaultForegroundProp.setInitiallyEditable(isCustom);
        selectedBackgroundProp.setInitiallyEditable(isCustom);
        selectedForegroundProp.setInitiallyEditable(isCustom);
        unselectedBackgroundProp.setInitiallyEditable(isCustom);
        unselectedForegroundProp.setInitiallyEditable(isCustom);
        actionPanelThemeProp.setInitiallyEditable(isCustom);
        return super.showPropertiesDialog(owner);
    }

    /**
     * Overridden here so we can customize the PropertiesDialog a little bit
     * before showing it.
     */
    @Override
    public void propertiesDialogCreated(PropertiesDialog dialog) {
        dialog.setSize(800, 600); // a bit bigger than the default size
        dialog.setMinimumSize(new Dimension(600, 500)); // ditto

        if (dialog instanceof ActionPanelPropertiesDialog actionPanelDialog) {
            ActionPanel actionPanel = actionPanelDialog.getActionPanel();
            actionPanel.getActionTrayMargins().setLeft(6).setRight(6); // bit of horizontal padding

            // There are a crazy amount of customization options for ActionPanel.
            // For example, we can change color options:
            if (useCustomColorsProp.getValue()) {
                actionPanel.getColorOptions().setFromTheme(actionPanelThemeProp.getSelectedItem());
            }
            else {
                actionPanel.getColorOptions().useSystemDefaults();
            }

            // We can also make expand/collapse a little more user-accessible:
            actionPanel.getExpandCollapseOptions().setAllowHeaderDoubleClick(true);

            // We could expose all of ActionPanel's options as user config here,
            // but it might quickly get overwhelming! So we'll go with defaults for most of it.
        }
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

    /**
     * Returns a Color suitable for general background use within the application.
     * This will either come from the current Look and Feel if system colors are in use,
     * or will be the user-selected custom value otherwise.
     */
    public Color getDefaultBackground() {
        Color c = useCustomColorsProp.getValue()
                ? defaultBackgroundProp.getSolidColor()
                : UIManager.getDefaults().getColor("Panel.background");
        if (c == null) {
            // Emergency fallback:
            c = Color.LIGHT_GRAY;
        }
        return c;
    }

    /**
     * Returns a Color suitable for general foreground use within the application.
     * This will either come from the current Look and Feel if system colors are in use,
     * or will be the user-selected custom value otherwise.
     */
    public Color getDefaultForeground() {
        Color c = useCustomColorsProp.getValue()
                ? defaultForegroundProp.getSolidColor()
                : UIManager.getDefaults().getColor("Panel.foreground");
        if (c == null) {
            // Emergency fallback:
            c = Color.BLACK;
        }
        return c;
    }

    /**
     * Returns a Color suitable for the background of selected items within the application.
     * This will either come from the current Look and Feel if system colors are in use,
     * or will be the user-selected custom value otherwise.
     */
    public Color getSelectedBackground() {
        Color c = useCustomColorsProp.getValue()
                ? selectedBackgroundProp.getSolidColor()
                : UIManager.getDefaults().getColor("TextArea.selectionBackground");
        if (c == null) {
            // Emergency fallback:
            c = Color.BLUE;
        }
        return c;
    }

    /**
     * Returns a Color suitable for the background of unselected items within the application.
     * This will either come from the current Look and Feel if system colors are in use,
     * or will be the user-selected custom value otherwise.
     */
    public Color getUnselectedBackground() {
        Color c = useCustomColorsProp.getValue()
                ? unselectedBackgroundProp.getSolidColor()
                : UIManager.getDefaults().getColor("Button.background");
        if (c == null) {
            // Emergency fallback:
            c = Color.LIGHT_GRAY;
        }
        return c;
    }

    /**
     * Returns a Color suitable for the foreground of selected items within the application.
     * This will either come from the current Look and Feel if system colors are in use,
     * or will be the user-selected custom value otherwise.
     */
    public Color getSelectedForeground() {
        Color c = useCustomColorsProp.getValue()
                ? selectedForegroundProp.getSolidColor()
                : UIManager.getDefaults().getColor("TextArea.selectionForeground");
        if (c == null) {
            // Emergency fallback:
            c = Color.WHITE;
        }
        return c;
    }

    /**
     * Returns a Color suitable for the foreground of unselected items within the application.
     * This will either come from the current Look and Feel if system colors are in use,
     * or will be the user-selected custom value otherwise.
     */
    public Color getUnselectedForeground() {
        Color c = useCustomColorsProp.getValue()
                ? unselectedForegroundProp.getSolidColor()
                : UIManager.getDefaults().getColor("Button.foreground");
        if (c == null) {
            // Emergency fallback:
            c = Color.BLACK;
        }
        return c;
    }

    /**
     * Returns the user-selected ColorTheme for the ActionPanel,
     * or null if system colors are in use.
     */
    public ColorTheme getActionPanelTheme() {
        return useCustomColorsProp.getValue() ? actionPanelThemeProp.getSelectedItem() : null;
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

    public KeyStroke getDeleteSourceKeyStroke() {
        KeyStrokeProperty prop = (KeyStrokeProperty)getPropertiesManager().getProperty(KEY_DELETE_SOURCE);
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
        list.add((KeyStrokeProperty)getPropertiesManager().getProperty(KEY_DELETE_SOURCE));
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

        list.addAll(buildHiddenProps());
        list.addAll(buildGeneralUIProps());
        list.addAll(buildLookAndFeelProps());
        list.addAll(buildFontProps());
        list.addAll(buildImageSetProps());
        list.addAll(buildKeyboardProps());
        list.addAll(buildThumbnailProps());
        list.addAll(buildQuickMoveProps());

        return list;
    }

    /**
     * Invoked internally to build all the properties that are not meant to be directly exposed
     * to the user, but rather managed by the application itself. Window size and such.
     */
    private List<AbstractProperty> buildHiddenProps() {
        List<AbstractProperty> list = new ArrayList<>();

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
        return list;
    }

    /**
     * Builds all the general UI-related options.
     */
    private List<AbstractProperty> buildGeneralUIProps() {
        List<AbstractProperty> list = new ArrayList<>();

        imagePanelAutoBestFitProp = new BooleanProperty("UI.General options.autoBestFit", "Auto best fit image", true);
        list.add(imagePanelAutoBestFitProp);

        imagePanelZoomIncrementProp = new DecimalProperty("UI.General options.zoomIncrement", "Zoom increment", 0.02,
                                                          0.01,
                                                          0.99, 0.01);
        list.add(imagePanelZoomIncrementProp);

        toolbarIconSizeProp = new IntegerProperty("UI.General options.iconSize", "Main toolbar icons:", 24, 16, 128, 2);
        toolbarIconSizeProp.setHelpText("Size of icons in the main toolbar.");
        list.add(toolbarIconSizeProp);

        toolbarIconMarginProp = new IntegerProperty("UI.General options.iconMargin", "Main toolbar margin:", 4, 2, 32,
                                                    2);
        toolbarIconMarginProp.setHelpText("Margin around icons in the main toolbar.");
        list.add(toolbarIconMarginProp);

        miniToolbarIconSizeProp = new IntegerProperty("UI.General options.miniIconSize", "Mini toolbar icons:", 16,
                                                      16, 64, 2);
        miniToolbarIconSizeProp.setHelpText("Size of icons in mini toolbars (e.g., image set toolbar).");
        list.add(miniToolbarIconSizeProp);

        miniToolbarIconMarginProp = new IntegerProperty("UI.General options.miniIconMargin", "Mini toolbar margin:", 4,
                                                        2, 16, 2);
        miniToolbarIconMarginProp.setHelpText("Margin around icons in mini toolbars (e.g., image set toolbar).");
        list.add(miniToolbarIconMarginProp);

        statusPanelBorderProp = new BooleanProperty("UI.General options.statusPanelBorder",
                                                    "Show border around status panel",
                                                    true);
        list.add(statusPanelBorderProp);

        return list;
    }

    /**
     * Creates all Look and Feel related properties used within the application.
     */
    private List<AbstractProperty> buildLookAndFeelProps() {
        final String PREFIX = "UI.Look and Feel.";
        List<AbstractProperty> props = new ArrayList<>();
        lookAndFeelProp = new LookAndFeelProperty(PREFIX + "Look and Feel", "Look and Feel:",
                                                  FlatDarkLaf.class.getName());
        props.add(lookAndFeelProp);
        useCustomColorsProp = new BooleanProperty(PREFIX + "useCustomColors",
                                                  "Override selected Look and Feel for some colors",
                                                  false);
        useCustomColorsProp.setHelpText("<html>If enabled, the selected look-and-feel's colors will be overridden"
                                                + "<br>for various UI elements. The custom colors specified below" +
                                                " will be used instead.</html>");
        colorSchemeProp = new EnumProperty<>(PREFIX + "colorScheme",
                                             "Set from scheme:",
                                             ColorScheme.MATRIX);
        colorSchemeProp.addFormFieldChangeListener(event -> setColorScheme(event));
        defaultBackgroundProp = createColorProp("defaultBackground",
                                                "Default bg:",
                                                "Panel.background");
        defaultForegroundProp = createColorProp("defaultForeground",
                                                "Default fg:",
                                                "Panel.foreground");
        selectedBackgroundProp = createColorProp("selectedBackground",
                                                 "Selected bg:",
                                                   "TextArea.selectionBackground");
        unselectedBackgroundProp = createColorProp("unselectedBackground",
                                                   "Unselected bg:",
                                                     "Button.background");
        selectedForegroundProp = createColorProp("selectedForeground",
                                                 "Selected fg:",
                                                     "TextArea.selectionForeground");
        unselectedForegroundProp = createColorProp("unselectedForeground",
                                                   "Unselected fg:",
                                                       "Button.foreground");
        actionPanelThemeProp = new EnumProperty<>(PREFIX + "actionPanelTheme",
                                                  "ActionPanel theme:",
                                                  ColorTheme.DEFAULT);

        // Set up a listener to ensure proper enabled/disabled state:
        useCustomColorsProp.addFormFieldChangeListener(new PropertyFormFieldChangeListener() {
            @Override
            public void valueChanged(PropertyFormFieldValueChangedEvent event) {
                boolean isCustom = ((CheckBoxField)event.formField()).isChecked();
                FormPanel fp = event.formPanel();
                fp.getFormField(colorSchemeProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(defaultBackgroundProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(defaultForegroundProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(unselectedBackgroundProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(unselectedForegroundProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(selectedBackgroundProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(selectedForegroundProp.getFullyQualifiedName()).setEnabled(isCustom);
                fp.getFormField(actionPanelThemeProp.getFullyQualifiedName()).setEnabled(isCustom);
            }
        });

        props.add(useCustomColorsProp);
        props.add(colorSchemeProp);
        props.add(defaultBackgroundProp);
        props.add(defaultForegroundProp);
        props.add(selectedBackgroundProp);
        props.add(selectedForegroundProp);
        props.add(unselectedBackgroundProp);
        props.add(unselectedForegroundProp);
        props.add(actionPanelThemeProp);

        return props;
    }

    private List<AbstractProperty> buildFontProps() {
        List<AbstractProperty> list = new ArrayList<>();
        statusPanelFontProp = new FontProperty("UI.Fonts.statusPanelFont",
                                               "Status panel font:",
                                               new Font("Dialog", Font.PLAIN, 12));
        list.add(statusPanelFontProp);

        thumbPanelFontProp = new FontProperty("UI.Fonts.thumbPanelFont",
                                              "Thumbnail panel font:",
                                              new Font("Dialog", Font.PLAIN, 11));
        list.add(thumbPanelFontProp);

        return list;
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
        ColorField defaultBgField = (ColorField)formPanel.getFormField(defaultBackgroundProp.getFullyQualifiedName());
        ColorField defaultFgField = (ColorField)formPanel.getFormField(defaultForegroundProp.getFullyQualifiedName());
        ColorField selectedBgField = (ColorField)formPanel.getFormField(selectedBackgroundProp.getFullyQualifiedName());
        ColorField selectedFgField = (ColorField)formPanel.getFormField(selectedForegroundProp.getFullyQualifiedName());
        ColorField unselectedBgField = (ColorField)formPanel.getFormField(
                unselectedBackgroundProp.getFullyQualifiedName());
        ColorField unselectedFgField = (ColorField)formPanel.getFormField(
                unselectedForegroundProp.getFullyQualifiedName());
        @SuppressWarnings("unchecked")
        ComboField<ColorTheme> actionPanelThemeField = (ComboField<ColorTheme>)formPanel.getFormField(
                actionPanelThemeProp.getFullyQualifiedName());

        // If any of them are null, something has gone off the rails:
        if (defaultBgField == null || defaultFgField == null ||
                selectedBgField == null || selectedFgField == null ||
                unselectedBgField == null || unselectedFgField == null ||
                actionPanelThemeField == null) {
            logger.warning("Unable to set color scheme: one or more form fields are null.");
            return;
        }

        // Set them all!
        ColorScheme scheme = (ColorScheme)((ComboField<?>)event.formField()).getSelectedItem();
        defaultBgField.setColor(scheme.getDefaultBackground());
        defaultFgField.setColor(scheme.getDefaultForeground());
        selectedBgField.setColor(scheme.getSelectedBackground());
        selectedFgField.setColor(scheme.getSelectedForeground());
        unselectedBgField.setColor(scheme.getUnselectedBackground());
        unselectedFgField.setColor(scheme.getUnselectedForeground());

        // We can suggest an ActionPanel theme that matches the above color scheme:
        ColorTheme suggestedTheme = switch (scheme) {
            case MATRIX -> ColorTheme.MATRIX;
            case DARK, VERY_DARK, EXTREMELY_DARK -> ColorTheme.DARK;
            case SHADES_OF_GREY -> ColorTheme.SHADES_OF_GRAY;
            case GOT_THE_BLUES -> ColorTheme.ICE;
            case HOT_DOG_STAND -> ColorTheme.HOT_DOG_STAND;
        };
        actionPanelThemeField.setSelectedItem(suggestedTheme); // User can override if they don't like our pick.
    }

    private ColorProperty createColorProp(String name, String label, String defaultKey) {
        final String PREFIX = "UI.Look and Feel.";
        ColorProperty prop = new ColorProperty(PREFIX + name, label, ColorSelectionType.SOLID);
        prop.setSolidColor(UIManager.getDefaults().getColor(defaultKey));
        return prop;
    }

    /**
     * Builds properties related to ImageSet handling.
     */
    private List<AbstractProperty> buildImageSetProps() {
        final String PREFIX = "Image sets.Image set persistence.";
        List<AbstractProperty> list = new ArrayList<>();
        imageSetSaveLocation = new ComboProperty<>(PREFIX + "imageSetSaveLocation", "Persistence:",
                                                   List.of("Use application settings directory",
                                                           "Choose a specific directory..."), 0, false);
        list.add(imageSetSaveLocation);
        imageSetSaveLocationOverride = new DirectoryProperty(PREFIX + "imageSetSaveDirectoryOverride",
                                                             "Persistence dir:", false, Version.SETTINGS_DIR);
        boolean initiallyVisible = false;
        String currentOption = peek(Version.APP_CONFIG_FILE, PREFIX + "imageSetSaveLocation");
        if (currentOption != null && currentOption.equals("Choose a specific directory...")) {
            initiallyVisible = true;
        }
        imageSetSaveLocationOverride.setInitiallyVisible(initiallyVisible);
        list.add(imageSetSaveLocationOverride);

        // Set up a listener to make the override visible/invisible as needed:
        imageSetSaveLocation.addFormFieldChangeListener(event -> {
            int index = ((ComboField)event.formField()).getSelectedIndex();
            FormField field = event.formPanel().getFormField(PREFIX + "imageSetSaveDirectoryOverride");
            field.setVisible(index == 1);
        });
        return list;
    }

    /**
     * Creates all properties related to keystrokes used within the application.
     */
    private List<AbstractProperty> buildKeyboardProps() {
        List<AbstractProperty> props = new ArrayList<>();

        // Non-configurable:
        final String notConfigurableText = "This keystroke cannot be changed.";
        props.add(new KeyStrokeProperty(KEY_EXIT,
                                        "Exit application:",
                                        parseKeyStroke("Ctrl+Q"),
                                        new ExitAction())
                          .setInitiallyEditable(false)
                          .setHelpText(notConfigurableText));
        props.add(new KeyStrokeProperty(KEY_PREVIOUS_IMAGE1,
                                        "Previous image:",
                                        parseKeyStroke("left"),
                                        new PreviousImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false)
                          .setHelpText(notConfigurableText));
        props.add(new KeyStrokeProperty(KEY_PREVIOUS_IMAGE2,
                                        "Previous image:",
                                        parseKeyStroke("up"),
                                        new PreviousImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false)
                          .setHelpText(notConfigurableText));
        props.add(new KeyStrokeProperty(KEY_NEXT_IMAGE1,
                                        "Next image:",
                                        parseKeyStroke("right"),
                                        new NextImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false)
                          .setHelpText(notConfigurableText));
        props.add(new KeyStrokeProperty(KEY_NEXT_IMAGE2,
                                        "Next image:",
                                        parseKeyStroke("down"),
                                        new NextImageAction(MenuManager.MENU_ICON_SIZE))
                          .setInitiallyEditable(false)
                          .setHelpText(notConfigurableText));
        props.add(new KeyStrokeProperty(KEY_DELETE_CURRENT,
                                        "Delete image:",
                                        parseKeyStroke("del"),
                                        new DeleteCurrentAction())
                          .setInitiallyEditable(false)
                          .setHelpText("<html>" + notConfigurableText + "<br>"
                                               + "<b>Note:</b> In Image Set mode, this will only remove the<br><br>" +
                                               "image from the set, but will not delete the source file.<br>" +
                                               "In file system mode, this will delete the actual image file.</html>"));

        // Configurable:
        props.add(new KeyStrokeProperty(KEY_REFRESH,
                                        "Refresh:",
                                        parseKeyStroke("F5"),
                                        new ReloadAction(MenuManager.MENU_ICON_SIZE))
                          .setAllowBlank(true)
                          .setReservedKeyStrokes(RESERVED_KEYSTROKES));
        props.add(new KeyStrokeProperty(KEY_RENAME,
                                        "Rename image:",
                                        parseKeyStroke("F2"),
                                        new RenameAction(MenuManager.MENU_ICON_SIZE))
                          .setAllowBlank(true)
                          .setReservedKeyStrokes(RESERVED_KEYSTROKES));
        props.add(new KeyStrokeProperty(KEY_BROWSE_MODE_FILESYSTEM,
                                        "Filesystem mode:",
                                        parseKeyStroke("alt+1"),
                                        new SetBrowseModeAction(MainWindow.BrowseMode.FILE_SYSTEM))
                          .setAllowBlank(true)
                          .setReservedKeyStrokes(RESERVED_KEYSTROKES));
        props.add(new KeyStrokeProperty(KEY_BROWSE_MODE_IMAGE_SET,
                                        "Image set mode:",
                                        parseKeyStroke("alt+2"),
                                        new SetBrowseModeAction(MainWindow.BrowseMode.IMAGE_SET))
                          .setAllowBlank(true)
                          .setReservedKeyStrokes(RESERVED_KEYSTROKES));
        props.add(new KeyStrokeProperty(KEY_DELETE_SOURCE,
                                        "Delete source image:",
                                        parseKeyStroke("Ctrl+Del"),
                                        new ImageSetDeleteSourceImageAction(MenuManager.MENU_ICON_SIZE))
                          .setAllowBlank(true)
                          .setReservedKeyStrokes(RESERVED_KEYSTROKES)
                          .setHelpText("<html>In ImageSet mode, this will remove the current image" +
                                               " and delete its source file.<br>" +
                                               "This does nothing in file system mode.</html>"));
        props.add(new KeyStrokeProperty(KEY_ABOUT,
                                        "About dialog:",
                                        parseKeyStroke("Ctrl+A"),
                                        new AboutAction(MenuManager.MENU_ICON_SIZE))
                          .setAllowBlank(true)
                          .setReservedKeyStrokes(RESERVED_KEYSTROKES));

        return props;
    }

    /**
     * Builds options related to Thumbnail handling.
     */
    private List<AbstractProperty> buildThumbnailProps() {
        List<AbstractProperty> list = new ArrayList<>();
        thumbSizeProp = new EnumProperty<>("Thumbnails.Thumbnail options.thumbSize", "Thumb size", ThumbSize.Normal);
        list.add(thumbSizeProp);

        thumbPageSizeProp = new EnumProperty<>("Thumbnails.Thumbnail options.pageSize", "Page size",
                                               ThumbPageSize.Normal);
        list.add(thumbPageSizeProp);

        thumbCacheEnabledProp = new BooleanProperty("Thumbnails.Thumbnail caching.enableThumbCache",
                                                    "Enable automatic caching of thumbnails",
                                                    true);
        thumbCacheEnabledProp.setHelpText("<html>Disabling caching will not remove existing cached thumbnails."
                                                  + "<br>Disabling caching only prevents new thumbnails from being cached."
                                                  + "<br>Any existing thumbnails will still be used when browsing images.</html>");
        list.add(thumbCacheEnabledProp);

        // Not currently configurable, but we can at least show the user where the cache is located:
        LabelProperty label = new LabelProperty("Thumbnails.Thumbnail caching.infoLabel",
                                                ThumbCacheManager.CACHE_DIR.getAbsolutePath());
        label.setFieldLabelText("Cache dir:");
        list.add(label);

        return list;
    }

    /**
     * Builds options related to the Quick Move feature.
     */
    private List<AbstractProperty> buildQuickMoveProps() {
        final String PREFIX = "Quick Move.Enabled operations.";
        List<AbstractProperty> list = new ArrayList<>();
        enableQuickMoveProp = new BooleanProperty(PREFIX + "enableQuickMove", "Enable image move operations", true);
        list.add(enableQuickMoveProp);
        enableQuickCopyProp = new BooleanProperty(PREFIX + "enableQuickCopy", "Enable image copy operations", true);
        list.add(enableQuickCopyProp);
        enableQuickLinkProp = new BooleanProperty(PREFIX + "enableQuickLink", "Enable image link operations", true);
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
