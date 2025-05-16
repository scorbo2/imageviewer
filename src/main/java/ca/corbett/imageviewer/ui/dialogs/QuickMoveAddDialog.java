package ca.corbett.imageviewer.ui.dialogs;

import ca.corbett.extras.MessageUtil;
import ca.corbett.imageviewer.QuickMoveManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Logger;

/**
 * Presents a dialog that the user can use to create a new Quick Move destination.
 * This is either a specific directory (and optionally the child directories it contains),
 * or a simple text label to be used to group other Quick Move destinations. Example:
 * <pre>
 * Quick Move destinations
 *   Category1
 *     /home/user/somedir
 *       /home/user/somechilddir1
 *       /home/user/somechilddir2
 *   Category2
 *     /home/user/someotherdir
 * </pre>
 * The "Category1" and "Category2" nodes are not actual destinations, just groupings with a
 * user-friendly name so that you can group actual destinations underneath them.
 *
 * @author scorbo2
 * @since 2017-11-15
 */
public final class QuickMoveAddDialog extends JDialog {

    private MessageUtil messageUtil;
    private QuickMoveManager.TreeNode resultNode;
    private QuickMoveManager.TreeNode editNode;
    private JTextField labelTextField;
    private JTextField dirTextField;
    private JCheckBox includeChildCheckBox;
    private static JFileChooser fileChooser;

    /**
     * Shows a dialog for creating a new Quick Move destination.
     *
     * @param owner The JDialog that owns this dialog. This dialog will be shown modal.
     */
    public QuickMoveAddDialog(JDialog owner) {
        this(owner, null);
    }

    /**
     * Shows a dialog for editing the given Quick Move destination.
     *
     * @param owner        The JDialog that owns this dialog. This dialog will be shown modal.
     * @param existingNode The existing destination to be shown and edited.
     */
    public QuickMoveAddDialog(JDialog owner, QuickMoveManager.TreeNode existingNode) {
        super(owner, "Edit Quick Move destination", true);
        setSize(340, 220);
        setMinimumSize(new Dimension(340, 220));
        setResizable(false);
        setLocationRelativeTo(owner);
        resultNode = null;
        editNode = existingNode;
        initComponents();
    }

    /**
     * Returns a new QuickMoveTreeNode instance, populated according to user input, or null
     * if the user canceled the dialog.
     *
     * @return A QuickMoveTreeNode instance, may be null if user hit cancel.
     */
    public QuickMoveManager.TreeNode getResult() {
        return resultNode;
    }

    /**
     * Invoked internally to build up the result node based upon current settings.
     */
    private void buildResultNode() {
        File dir = null;
        String dirName = dirTextField.getText();
        if (!dirName.trim().equals("")) {
            dir = new File(dirName);
        }

        QuickMoveManager qmInstance = QuickMoveManager.getInstance();
        resultNode = qmInstance.new TreeNode(dir, labelTextField.getText());

        if (dir != null && includeChildCheckBox.isSelected()) {
            File[] files = dir.listFiles();
            if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
                    }

                });
                for (File file : files) {
                    if (file.isDirectory()) {
                        resultNode.add(qmInstance.new TreeNode(file, file.getName()));
                    }
                }
            }
        }
    }

    /**
     * Used internally to lay out the dialog.
     */
    private void initComponents() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
        getContentPane().add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Builds and returns a JPanel with the main dialog contents.
     *
     * @return The main panel for this dialog.
     */
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        JLabel label = new JLabel("Label:");
        constraints.insets = new Insets(0, 10, 0, 2);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(label, constraints);

        labelTextField = new JTextField();
        constraints.insets = new Insets(0, 2, 0, 10);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        if (editNode != null) {
            labelTextField.setText(editNode.getLabel());
        }
        panel.add(labelTextField, constraints);

        label = new JLabel("Directory:");
        constraints.insets = new Insets(0, 10, 0, 2);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(label, constraints);

        dirTextField = new JTextField();
        constraints.insets = new Insets(0, 2, 0, 2);
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        if (editNode != null) {
            dirTextField.setText(editNode.getDirectory().getAbsolutePath());
        }
        panel.add(dirTextField, constraints);

        JButton btn = new JButton("Browse");
        constraints.insets = new Insets(0, 2, 0, 10);
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.weightx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileChooser();
            }

        });
        panel.add(btn, constraints);

        includeChildCheckBox = new JCheckBox("Include children");
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = 2;
        if (editNode != null) {
            includeChildCheckBox.setSelected(editNode.getChildCount() > 0);
        }
        panel.add(includeChildCheckBox, constraints);

        return panel;
    }

    /**
     * Validates the Quick Move form options.
     *
     * @return True if the form options are valid, false if some validation error was shown.
     */
    private boolean validateQuickMoveForm() {
        String labelText = labelTextField.getText().trim();
        String dirText = dirTextField.getText().trim();

        if (labelText.equals("") && dirText.equals("")) {
            getMessageUtil().info("Either label or directory must be filled out.");
            return false;
        }

        if (!dirText.equals("")) {
            File dir = new File(dirText);
            if (!dir.exists()) {
                getMessageUtil().info("Specified directory does not exist.");
                return false;
            }
            if (!dir.isDirectory()) {
                getMessageUtil().info("Specified file is not a directory.");
                return false;
            }
        }

        else if (includeChildCheckBox.isSelected()) {
            getMessageUtil().info("Cannot include children if no directory is selected.");
            return false;
        }

        return true;
    }

    /**
     * Builds and returns a button panel for the bottom of the dialog (ok/cancel).
     *
     * @return The button panel for the bottom of the dialog.
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90, 28));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateQuickMoveForm()) {
                    buildResultNode();
                    setVisible(false);
                }
            }

        });
        panel.add(button);

        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90, 28));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }

        });
        panel.add(button);

        return panel;
    }

    /**
     * Shows a file chooser for picking a Quick Move destination.
     * Stores the results in the Quick Move form.
     */
    private void showFileChooser() {
        int result = fileChooser.showDialog(this, "Choose");
        if (result == JFileChooser.APPROVE_OPTION) {
            dirTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            if (labelTextField.getText().trim().equals("")) {
                labelTextField.setText(fileChooser.getSelectedFile().getName());
            }
        }
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(this, Logger.getLogger(QuickMoveAddDialog.class.getName()));
        }
        return messageUtil;
    }

}
