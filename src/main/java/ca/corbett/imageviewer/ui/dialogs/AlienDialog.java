package ca.corbett.imageviewer.ui.dialogs;

import ca.corbett.extras.MessageUtil;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A simple dialog for viewing and optionally deleting "alien" files in a given directory.
 * An alien is any file that does not have a recognized extension.
 *
 * @author scorbo2
 * @since 2017-11-24
 */
public final class AlienDialog extends JDialog {

    private static final Logger logger = Logger.getLogger(AlienDialog.class.getName());
    public static final String DARWIN_METADATA_FILENAME = ".00darwin-metadata";

    private MessageUtil messageUtil;
    private static AlienDialog instance;
    private JList alienList;
    private DefaultListModel listModel;
    private File directory;
    private List<File> files;

    private AlienDialog() {
        super(MainWindow.getInstance(), "Aliens detected", true);
        setSize(320, 320);
        setMinimumSize(new Dimension(320, 320));
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
                ImageViewerExtensionManager.getInstance().preImageOperation(ImageOperation.Type.DELETE, file, null);
                okay = okay && file.delete();
                if (okay) {
                    ImageViewerExtensionManager.getInstance().postImageOperation(ImageOperation.Type.DELETE, file);
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
                    isOkay = true; // User canceled this one, move on
                }
            } while (!isOkay);
            try {
                FileUtils.moveFile(file, new File(file.getParentFile(), newName));
            }
            catch (IOException ioe) {
                getMessageUtil().error("Deletion error", "Problem renaming file: " + ioe.getMessage(), ioe);
            }
        }
        rescanDir();
    }

    /**
     * Invoked internally to delete the entire file list.
     */
    private void deleteAll() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            boolean okay = true;
            for (File file : files) {
                ImageViewerExtensionManager.getInstance().preImageOperation(ImageOperation.Type.DELETE, file, null);
                okay = okay && file.delete();
                if (okay) {
                    ImageViewerExtensionManager.getInstance().postImageOperation(ImageOperation.Type.DELETE, file);
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
     * Rescans the current directory looking for alien files.
     */
    private void rescanDir() {
        List<String> extensions = ThumbContainerPanel.getImageExtensions();
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
        constraints.gridheight = 5;
        JScrollPane scrollPane = new JScrollPane(alienList);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        panel.add(scrollPane, constraints);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setPreferredSize(new Dimension(110, 28));
        deleteBtn.setMinimumSize(new Dimension(110, 28));
        constraints.insets = new Insets(0, 4, 0, 4);
        constraints.gridx = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.gridheight = 1;
        deleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }

        });
        panel.add(deleteBtn, constraints);

        JButton deleteAllBtn = new JButton("Delete all");
        deleteAllBtn.setPreferredSize(new Dimension(110, 28));
        deleteAllBtn.setMinimumSize(new Dimension(110, 28));
        constraints.gridy = 1;
        deleteAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteAll();
                setVisible(false);
            }

        });
        panel.add(deleteAllBtn, constraints);

        JButton renameBtn = new JButton("Rename");
        renameBtn.setPreferredSize(new Dimension(110, 28));
        renameBtn.setMinimumSize(new Dimension(110, 28));
        constraints.gridy = 2;
        renameBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renameSelected();
            }

        });
        panel.add(renameBtn, constraints);

        JButton rescanBtn = new JButton("Rescan");
        rescanBtn.setPreferredSize(new Dimension(110, 28));
        rescanBtn.setMinimumSize(new Dimension(110, 28));
        constraints.gridy = 3;
        rescanBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rescanDir();
            }

        });
        panel.add(rescanBtn, constraints);

        JLabel dummy = new JLabel("");
        constraints.gridy = 4;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(dummy, constraints);

        return panel;
    }

    /**
     * Builds and returns the button panel for the bottom of the form.
     *
     * @return A panel with an Ok button for closing the form.
     */
    public JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        okBtn.setPreferredSize(new Dimension(90, 28));
        panel.add(okBtn);

        final AlienDialog dialog = this;
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }

        });

        return panel;
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(this, logger);
        }
        return messageUtil;
    }

}
