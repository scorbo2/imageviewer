package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.ComboField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.forms.fields.ShortTextField;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Optional;

/**
 * Provides a popup dialog for choosing a new ImageSet within the tree.
 * <p>
 *     The user can opt to enter a fully qualified path manually.
 *     This is probably the easiest way for advanced users to
 *     select the desired path. Use the delimiter character (default "/")
 *     to separate path nodes. Example: "Folder1/My Image Set"
 * </p>
 * <p>
 *     The user can also opt to choose the parent node visually, then
 *     enter the ImageSet name manually. Delimiter characters are not
 *     allowed in the ImageSet name.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetChooserDialog extends JDialog {

    private boolean wasOkayed;
    private String selectedPath;
    private final boolean isEnableOverwriteCheck;

    private ComboField<String> typeChooser;
    private ImageSetTree imageSetTree;
    private PanelField parentTreeField;
    private ShortTextField nameField;
    private LabelField manualEntryLabel;
    private ShortTextField manualEntryField;

    public ImageSetChooserDialog(String title) {
        this(title, true);
    }

    public ImageSetChooserDialog(String title, boolean enableOverwriteCheck) {
        super(MainWindow.getInstance(), title, true);
        this.isEnableOverwriteCheck = enableOverwriteCheck;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(new Dimension(525, 400));
        setLocationRelativeTo(MainWindow.getInstance());
        wasOkayed = false;
        selectedPath = null;
        setResizable(false);
        initComponents();
    }

    public boolean wasOkayed() {
        return wasOkayed;
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    private String computeSelectedPath() {
        String path;
        if (typeChooser.getSelectedIndex() == 0) {
            path = imageSetTree.getSelectedPath(); // already validated by isFormValid
        }
        else if (typeChooser.getSelectedIndex() == 1) {
            String parentPath = imageSetTree.getSelectedPath();
            if (parentPath == null) {
                parentPath = String.valueOf(ImageSetManager.PATH_DELIMITER);
            }
            path = parentPath + ImageSetManager.PATH_DELIMITER + nameField.getText();
        }
        else {
            path = manualEntryField.getText();
        }

        return ImageSetManager.parseFullyQualifiedName(path);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        FormPanel formPanel = new FormPanel(Alignment.TOP_CENTER);
        formPanel.setBorderMargin(16);

        List<String> options = List.of("Choose destination node",
                                       "Choose a parent node and enter name",
                                       "Manual path entry");
        typeChooser = new ComboField<>("Choose:", options, 0);
        typeChooser.addValueChangedListener(field -> {
            if (typeChooser.getSelectedIndex() == 0) {
                manualEntryLabel.setVisible(false);
                manualEntryField.setVisible(false);
                parentTreeField.setVisible(true);
                nameField.setVisible(false);
            }
            else if (typeChooser.getSelectedIndex() == 1) {
                manualEntryLabel.setVisible(false);
                manualEntryField.setVisible(false);
                parentTreeField.setVisible(true);
                nameField.setVisible(true);
            }
            else {
                manualEntryLabel.setVisible(true);
                manualEntryField.setVisible(true);
                parentTreeField.setVisible(false);
                nameField.setVisible(false);
            }
        });
        formPanel.add(typeChooser);

        parentTreeField = new PanelField(new BorderLayout());
        parentTreeField.getPanel().setPreferredSize(new Dimension(325, 180));
        imageSetTree = new ImageSetTree();
        JScrollPane scrollPane = new JScrollPane(imageSetTree.getTree());
        parentTreeField.getPanel().add(scrollPane, BorderLayout.CENTER);
        formPanel.add(parentTreeField);

        nameField = new ShortTextField("Name:", 20);
        nameField.setVisible(false);
        formPanel.add(nameField);

        manualEntryLabel = new LabelField("<html>You can use the / character to create a folder structure.<br>"
                                                  + "Example: folder1/folder2/new image set");
        manualEntryLabel.setVisible(false);
        formPanel.add(manualEntryLabel);

        manualEntryField = new ShortTextField("Path:", 20);
        manualEntryField.setVisible(false);
        formPanel.add(manualEntryField);

        add(formPanel, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private boolean isFormValid() {
        String errorMsg = null;
        if (typeChooser.getSelectedIndex() == 0) {
            if (imageSetTree.getSelectedImageSet().isEmpty()) {
                errorMsg = "Select a destination node.";
            }
        }
        else if (typeChooser.getSelectedIndex() == 1) {
            if (imageSetTree.getSelectedImageSet().isEmpty()) {
                errorMsg = "You must select the parent node.";
            }
            String entry = nameField.getText();
            if (entry.contains(String.valueOf(ImageSetManager.PATH_DELIMITER))) {
                errorMsg = "Name field can't contain the / character.";
            }
            if (entry.isBlank()) {
                errorMsg = "Name cannot be blank.";
            }
        }
        else {
            String path = ImageSetManager.parseFullyQualifiedName(manualEntryField.getText());
            if (path.length() <= 1) {
                errorMsg = "Path cannot be empty.";
            }
        }

        if (isEnableOverwriteCheck && typeChooser.getSelectedIndex() != 0) {
            String fullPath = computeSelectedPath();
            ImageSetManager manager = MainWindow.getInstance().getImageSetManager();
            Optional<ImageSet> existingSet = manager.findImageSet(fullPath);
            if (existingSet.isPresent()) {
                errorMsg = "An image set already exists with that path. Will not overwrite.";
            }
        }

        if (errorMsg != null) {
            MainWindow.getInstance().showMessageDialog("Error", errorMsg);
            return false;
        }

        return true;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(actionEvent -> {
            if (!isFormValid()) {
                return;
            }
            wasOkayed = true;
            selectedPath = computeSelectedPath();
            dispose();
        });
        panel.add(button);

        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 23));
        button.addActionListener(actionEvent -> {
            wasOkayed = false;
            selectedPath = null;
            dispose();
        });
        panel.add(button);

        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        return panel;
    }
}
