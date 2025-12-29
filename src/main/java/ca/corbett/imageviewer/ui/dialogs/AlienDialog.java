package ca.corbett.imageviewer.ui.dialogs;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.PopupTextDialog;
import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.io.TextFileDetector;
import ca.corbett.imageviewer.ImageOperation;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.ThumbContainerPanel;
import org.apache.commons.io.FileUtils;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A simple dialog for viewing and optionally deleting "alien" files in a given directory.
 * An alien file is any file that is not recognized (either by the application code itself
 * or by any enabled extension) as an image file or a companion file.
 *
 * @author scorbo2
 * @since 2017-11-24
 */
public final class AlienDialog extends JDialog {

    private static final Logger logger = Logger.getLogger(AlienDialog.class.getName());
    public static final String DARWIN_METADATA_FILENAME = ".00darwin-metadata"; // TODO wtf is this doing here

    private static final long VIEW_AS_TEXT_MAX_FILE_SIZE = 256 * 1024; // 256KB arbitrary default

    private MessageUtil messageUtil;
    private static AlienDialog instance;
    private JList alienList;
    private DefaultListModel listModel;
    private File directory;
    private List<File> files;
    private long viewAsTextMaxFileSize = VIEW_AS_TEXT_MAX_FILE_SIZE;

    private AlienDialog() {
        super(MainWindow.getInstance(), "Aliens detected", true);
        setSize(400, 360);
        setMinimumSize(new Dimension(400, 320));
        initComponents();
    }

    /**
     * Singleton accessor.
     *
     * @return The single instance.
     */
    public static AlienDialog getInstance() {
        if (instance == null) {
            instance = new AlienDialog();
            instance.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        }

        return instance;
    }

    /**
     * Overridden to update our position if the main window moves.
     *
     * @param visible Whether to show or hide the form.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(MainWindow.getInstance());
        }
        super.setVisible(visible);
    }

    /**
     * Returns the maximum file size (in bytes) that can be viewed as text.
     */
    public long getViewAsTextMaxFileSize() {
        return viewAsTextMaxFileSize;
    }

    /**
     * Sets the maximum file size (in bytes) that can be viewed as text.
     * The default is 256KB.
     */
    public void setViewAsTextMaxFileSize(long viewAsTextMaxFileSize) {
        this.viewAsTextMaxFileSize = viewAsTextMaxFileSize;
    }

    /**
     * Sets the directory to scan for alien files.
     *
     * @param dir The directory in question.
     */
    public void setDirectory(File dir) {
        this.directory = dir;
        rescanDir();
    }

    /**
     * Sets the list of alien files to display. Can be empty.
     *
     * @param list the list of File objects representing aliens.
     */
    private void setAlienList(List<File> list) {
        files = new ArrayList<>();
        for (File f : list) {
            if (!f.getName().equals(DARWIN_METADATA_FILENAME)) {
                files.add(f);
            }
        }
        listModel.clear();
        for (File file : files) {
            String prettySize = FileUtils.byteCountToDisplaySize(file.length());
            listModel.addElement(file.getName() + " (" + prettySize + ")");
        }

        // As a convenience, if we have at least one file in the list, select the first one:
        if (files.size() > 0) {
            alienList.setSelectedIndex(0);
        }
    }

    /**
     * Invoked internally to delete the selected file(s). Does nothing if there is no selection.
     */
    private void deleteSelected() {
        int[] selectedIndeces = alienList.getSelectedIndices();
        boolean okay = true;
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            for (int i : selectedIndeces) {
                File file = files.get(i);
                ImageViewerExtensionManager
                        .getInstance()
                        .preImageOperation(ImageOperation.Type.DELETE, file, null);
                okay = okay && file.delete();
                if (okay) {
                    ImageViewerExtensionManager
                            .getInstance()
                            .postImageOperation(ImageOperation.Type.DELETE, file, null);
                }
            }
            rescanDir();
            if (!okay) {
                getMessageUtil().error("Deletion error", "One or more files could not be deleted.");
            }
        }
        finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Invoked internally to rename the selected file. Does nothing if there is no selection.
     */
    private void renameSelected() {
        int[] selectedIndices = alienList.getSelectedIndices();
        for (int i : selectedIndices) {
            File file = files.get(i);
            String prefix = "";
            String newName;
            boolean isOkay;
            do {
                newName = JOptionPane.showInputDialog(prefix + "Enter new name for " + file.getName(), file.getName());
                if (newName != null && !newName.isEmpty()) {
                    File testFile = new File(file.getParentFile(), newName);
                    isOkay = !testFile.exists();
                    if (!isOkay) {
                        prefix = "That name is in use.\n";
                    }
                }
                else {
                    return; // if user hits "cancel" on any of them, abort the whole batch rename.
                }
            } while (!isOkay);
            try {
                // Note: we don't update extensions here, because this is an alien file,
                //       not an image file or a companion file. So, extensions don't care.
                FileUtils.moveFile(file, new File(file.getParentFile(), newName));

                // Also update the list with this new name:
                // (we do this surgically instead of rescanDir() because we're in the middle of a batch operation,
                //  so we don't want to rescan after each rename, but also because rescanDir() will reset the selection,
                //  which is annoying for the user)
                for (int listIndex = 0; listIndex < alienList.getModel().getSize(); listIndex++) {
                    if (alienList.getModel().getElementAt(listIndex).toString().startsWith(file.getName() + " ")) {
                        String prettySize = FileUtils.byteCountToDisplaySize(file.length());
                        listModel.set(listIndex, newName + " (" + prettySize + ")");
                        break;
                    }
                }
            }
            catch (IOException ioe) {
                getMessageUtil().error("Rename error", "Problem renaming file: " + ioe.getMessage(), ioe);
            }
        }
    }

    /**
     * Invoked internally to delete the entire file list.
     */
    private void deleteAll() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            boolean okay = true;
            for (File file : files) {
                ImageViewerExtensionManager
                        .getInstance()
                        .preImageOperation(ImageOperation.Type.DELETE, file, null);
                okay = okay && file.delete();
                if (okay) {
                    ImageViewerExtensionManager
                            .getInstance()
                            .postImageOperation(ImageOperation.Type.DELETE, file, null);
                }
            }
            rescanDir();
            if (!okay) {
                getMessageUtil().error("Deletion error", "One or more files could not be deleted.");
            }
        }
        finally {
            this.setCursor(Cursor.getDefaultCursor());
            setVisible(false);
        }
    }

    /**
     * If the selected file is smaller than a certain threshold and appears to be a text
     * file, we'll attempt to load and show it in a simple text viewer.
     */
    private void viewAsText() {
        int[] selectedIndices = alienList.getSelectedIndices();

        // Must select something:
        if (selectedIndices.length == 0) {
            getMessageUtil().info("View as text", "Please select a file to view.");
            return;
        }

        // One at a time, please:
        if (selectedIndices.length > 1) {
            getMessageUtil().info("View as text", "Please select only one file to view at a time.");
            return;
        }

        // First make sure the file isn't unreasonably large:
        File file = files.get(selectedIndices[0]);
        if (file.length() > viewAsTextMaxFileSize) {
            getMessageUtil().info("View as text",
                                  "The selected file is too large to view as text.\n"
                                          + "Maximum size is "
                                          + FileSystemUtil.getPrintableSize(viewAsTextMaxFileSize)
                                          + ", this file is "
                                          + FileSystemUtil.getPrintableSize(file.length())
                                          + ".");
            return;
        }

        try {
            // Is it a text file? We can use the new TextFileDetector in swing-extras 2.6 for this:
            if (TextFileDetector.isTextFile(file, 8192, 0.05)) {
                // Load and show it:
                String content = FileUtils.readFileToString(file, "UTF-8");
                PopupTextDialog dialog = new PopupTextDialog(this, "Viewing: " + file.getName(),
                                                             content, false);
                dialog.setReadOnly(true); // should we allow saving edits? Eh, we're just viewing the file...
                dialog.setLocationRelativeTo(MainWindow.getInstance());
                dialog.setVisible(true);
            }
            else {
                getMessageUtil().info("View as text", "The selected file does not appear to be a text file.");
            }
        }
        catch (IOException ioe) {
            getMessageUtil().error("View as text", "Problem reading file: " + ioe.getMessage(), ioe);
        }
    }

    /**
     * Programmatically selects all items in the alien list.
     */
    private void selectAll() {
        int size = listModel.getSize();
        if (size > 0) {
            alienList.setSelectionInterval(0, size - 1);
        }
    }

    /**
     * Rescans the current directory looking for alien files.
     */
    private void rescanDir() {
        setAlienList(ThumbContainerPanel.findAlienFiles(directory));
    }

    /**
     * Sets up the dialog and creates all child components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(buildContentPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Builds and returns the main content panel.
     *
     * @return The main content panel.
     */
    public JPanel buildContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        listModel = new DefaultListModel();
        alienList = new JList(listModel);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridheight = 7;
        JScrollPane scrollPane = new JScrollPane(alienList);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        panel.add(scrollPane, constraints);

        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.gridx = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridheight = 1;
        panel.add(buildActionButton("Delete", e -> deleteSelected()), constraints);

        constraints.gridy = 1;
        panel.add(buildActionButton("Delete all", e -> deleteAll()), constraints);

        constraints.gridy = 2;
        panel.add(buildActionButton("Rename", e -> renameSelected()), constraints);

        constraints.gridy = 3;
        panel.add(buildActionButton("View as text", e -> viewAsText()), constraints);

        constraints.gridy = 4;
        panel.add(buildActionButton("Select all", e -> selectAll()), constraints);

        constraints.gridy = 5;
        panel.add(buildActionButton("Rescan", e -> rescanDir()), constraints);

        // Add a filler label to take up remaining space:
        JLabel dummy = new JLabel("");
        constraints.gridy = 6;
        constraints.weighty = 1;
        panel.add(dummy, constraints);

        return panel;
    }

    private JButton buildActionButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(130, 25));
        button.addActionListener(action);
        return button;
    }

    /**
     * Builds and returns the button panel for the bottom of the form.
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        final AlienDialog dialog = this;
        okBtn.addActionListener(e -> dialog.setVisible(false));
        okBtn.setPreferredSize(new Dimension(90, 25));
        panel.add(okBtn);

        return panel;
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(this, logger);
        }
        return messageUtil;
    }

}
