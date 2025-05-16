package ca.corbett.imageviewer.ui.dialogs;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.TextField;
import ca.corbett.forms.validators.FieldValidator;
import ca.corbett.forms.validators.ValidationResult;
import ca.corbett.imageviewer.ImageOperationHandler;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * Allows the user to rename a given image filename, checking to make sure that the new
 * name is valid and is not already in use.
 *
 * @author scorbo2
 */
public final class RenameDialog extends JDialog {

    private final File file;
    private FormPanel formPanel;
    private TextField textField;

    public RenameDialog(File file) {
        super(MainWindow.getInstance(), "Rename image");
        this.file = file;
        setSize(440, 160);
        setResizable(true);
        setLocationRelativeTo(MainWindow.getInstance());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        initComponents();
        textField.getFieldComponent().requestFocus();
    }

    private void doRename() {
        if (formPanel.isFormValid()) {
            ImageOperationHandler.renameImage(textField.getText());
            dispose();
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        formPanel = new FormPanel(FormPanel.Alignment.TOP_LEFT);

        LabelField label = new LabelField("Rename image:");
        label.setFont(label.getFieldLabelFont().deriveFont(Font.BOLD, 16f));
        formPanel.addFormField(label);

        textField = new TextField("New name:", 20, 1, false);
        textField.setText(file.getName());
        ((JTextField)textField.getFieldComponent()).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRename();
            }

        });
        textField.addFieldValidator(new FieldValidator(textField) {
            @Override
            public ValidationResult validate() {
                if (filenameContainsInvalidCharacters()) {
                    return new ValidationResult(false, "New name contains invalid characters.");
                }
                if (filenameInUse()) {
                    return new ValidationResult(false, "New name is already in use.");
                }
                if (extensionHasChanged()) {
                    return new ValidationResult(false, "New name must match old file extension.");
                }
                return new ValidationResult();
            }

        });
        formPanel.addFormField(textField);

        formPanel.render();
        add(formPanel, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JButton btn = new JButton("Rename");
        btn.setPreferredSize(new Dimension(100, 24));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doRename();
            }

        });
        panel.add(btn);

        btn = new JButton("Cancel");
        btn.setPreferredSize(new Dimension(100, 24));
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });
        panel.add(btn);

        return panel;
    }

    private boolean filenameInUse() {
        String filename = textField.getText();
        File parentDir = file.getParentFile();
        File testFile = new File(parentDir, filename);
        return testFile.exists();
    }

    private boolean extensionHasChanged() {
        String newName = textField.getText();
        if (!newName.contains(".")) {
            return true;
        }
        String oldExt = file.getName().substring(file.getName().lastIndexOf("."));
        String newExt = newName.substring(newName.lastIndexOf("."));
        return !oldExt.equals(newExt);
    }

    private boolean filenameContainsInvalidCharacters() {
        String filename = textField.getText();
        if (filename.isEmpty()) {
            return true;
        }
        if (filename.contains(File.separator)
                || filename.contains(File.pathSeparator)
                || filename.contains("*")
                || filename.contains("?")
                || filename.contains("!")) {
            return true;
        }
        return false;
    }

}
