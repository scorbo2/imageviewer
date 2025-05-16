package ca.corbett.imageviewer.ui.dialogs;

import ca.corbett.extras.MessageUtil;
import ca.corbett.extras.image.ImagePanel;
import ca.corbett.extras.image.ImagePanelConfig;
import ca.corbett.extras.image.ImageUtil;
import ca.corbett.imageviewer.ui.MainWindow;
import org.apache.commons.io.FileUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A dialog to handle name conflicts during move operations.
 * The user is presented with information about the source and destination files,
 * and given the choice to overwrite, cancel the move, or rename the source file
 * to some non-conflicting name.
 *
 * @author scorbo2
 * @since 2017-11-26
 */
public final class NameConflictDialog extends JDialog {

    private MessageUtil messageUtil;
    private static NameConflictDialog instance;
    private boolean batchMode;

    private File sourceFile;
    private File destDir;
    private File destFile;
    private Result result = Result.CANCEL;

    private ImagePanel sourceImage;
    private ImagePanel destImage;
    private ImagePanelConfig imageProps;

    private JLabel sourceNameLabel;
    private JLabel sourceSizeLabel;
    private JLabel sourceDateLabel;

    private JLabel destNameLabel;
    private JLabel destSizeLabel;
    private JLabel destDateLabel;

    private JButton cancelOneBtn;
    private JButton cancelAllBtn;

    /**
     * Used to represent the result of this dialog to the caller.
     */
    public enum Result {
        SUCCESS, RENAME, CANCEL, CANCEL_ALL
    }

    ;

    /**
     * Constructor is private to enforce singleton access.
     */
    private NameConflictDialog() {
        super(MainWindow.getInstance(), "Name conflict", true);
        setSize(440, 440);
        setMinimumSize(new Dimension(420, 420));
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        batchMode = false;
        initComponents();
    }

    /**
     * Singleton accessor.
     *
     * @return The single instance of this dialog.
     */
    public static NameConflictDialog getInstance() {
        if (instance == null) {
            instance = new NameConflictDialog();
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
     * Returns the result of this dialog (CANCEL, CANCEL_ALL, OVERWRITE, or RENAME).
     * In the case of RENAME you can invoke getDestFile to get the new destination file.
     * If batchMode is true, an additional cancel result, CANCEL_ALL, may be returned.
     * CANCEL means to skip the one file in questions, while CANCEL_ALL means to stop the
     * batch operation entirely.
     *
     * @return One of Result.CANCEL, Result.CANCEL_ALL, Result.SUCCESS, or Result.RENAME.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Returns the destination file, which will be named according to user preference
     * in the case of Result.RENAME.
     *
     * @return The destination file.
     */
    public File getDestFile() {
        return destFile;
    }

    /**
     * Configures this dialog to show the conflict between the given source file
     * and the destination directory.
     *
     * @param srcFile   The file to be moved.
     * @param destDir   The destination directory.
     * @param batchMode If true, a "cancel all" button will be made available.
     */
    public void setConflict(File srcFile, File destDir, boolean batchMode) {
        this.sourceFile = srcFile;
        this.destDir = destDir;
        this.batchMode = batchMode;
        destFile = new File(destDir, srcFile.getName());

        BufferedImage srcI = null;
        BufferedImage dstI = null;
        try {
            srcI = ImageUtil.loadImage(srcFile);
        }
        catch (ArrayIndexOutOfBoundsException aioobe) {
            Logger.getLogger(NameConflictDialog.class.getName())
                  .log(Level.WARNING, "Malformed GIF file: {0}", srcFile.getAbsolutePath());
        }
        catch (IOException ex) {
            Logger.getLogger(NameConflictDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            dstI = ImageUtil.loadImage(destFile);
        }
        catch (ArrayIndexOutOfBoundsException aioobe) {
            Logger.getLogger(NameConflictDialog.class.getName())
                  .log(Level.WARNING, "Malformed GIF file: {0}", destFile.getAbsolutePath());
        }
        catch (IOException ex) {
            Logger.getLogger(NameConflictDialog.class.getName()).log(Level.SEVERE, null, ex);
        }

        sourceImage.setImage(srcI);
        destImage.setImage(dstI);

        String srcDim = "Unknown";
        String dstDim = "Unknown";
        if (srcI != null) {
            srcDim = srcI.getWidth() + "x" + srcI.getHeight();
        }
        if (dstI != null) {
            dstDim = dstI.getWidth() + "x" + dstI.getHeight();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd h:mm:ssa");
        sourceNameLabel.setText(trimFilename(srcFile.getName()));
        sourceSizeLabel.setText(FileUtils.byteCountToDisplaySize(srcFile.length()) + ", " + srcDim);
        sourceDateLabel.setText(dateFormat.format(new Date(srcFile.lastModified())));

        if (destFile.exists()) {
            destNameLabel.setText(trimFilename(destFile.getName()));
            destSizeLabel.setText(FileUtils.byteCountToDisplaySize(destFile.length()) + ", " + dstDim);
            destDateLabel.setText(dateFormat.format(new Date(destFile.lastModified())));
        }
        else {
            destNameLabel.setText("(file not found)");
            destSizeLabel.setText("N/A");
            destDateLabel.setText("N/A");
        }

        // Set cancel options depending on how we're being invoked:
        if (batchMode) {
            cancelOneBtn.setText("Skip");
            cancelAllBtn.setVisible(true);
        }
        else {
            cancelOneBtn.setText("Cancel");
            cancelAllBtn.setVisible(false);
        }
    }

    /**
     * Invoked internally to set up basic UI layout.
     */
    private void initComponents() {
        imageProps = ImagePanelConfig.createDefaultProperties();
        imageProps.setEnableZoomOnMouseClick(false);
        imageProps.setEnableZoomOnMouseWheel(false);
        imageProps.setDisplayMode(ImagePanelConfig.DisplayMode.BEST_FIT);
        sourceImage = new ImagePanel(imageProps);
        destImage = new ImagePanel(imageProps);

        setLayout(new BorderLayout());
        add(buildContentPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Builds and returns the main content panel for this dialog.
     *
     * @return The content panel.
     */
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridheight = 4;
        constraints.insets = new Insets(8, 8, 8, 8);
        constraints.weightx = 1;
        constraints.weighty = 0.5;
        panel.add(sourceImage, constraints);

        constraints.gridy = 4;
        panel.add(destImage, constraints);

        Font titleFont = new Font("SansSerif", Font.BOLD, 14);
        Font headerFont = new Font("SansSerif", Font.BOLD, 11);
        Font normalFont = new Font("SansSerif", 0, 11);

        // SOURCE IMAGE PROPERTIES
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(8, 2, 2, 2);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.WEST;
        JLabel label = new JLabel("Source image");
        label.setFont(titleFont);
        panel.add(label, constraints);

        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        label = new JLabel("Name:");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 2;
        sourceNameLabel = new JLabel("");
        sourceNameLabel.setFont(normalFont);
        panel.add(sourceNameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 2;
        label = new JLabel("Date");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 2;
        sourceDateLabel = new JLabel("");
        sourceDateLabel.setFont(normalFont);
        panel.add(sourceDateLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        label = new JLabel("Size:");
        label.setFont(headerFont);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        panel.add(label, constraints);

        constraints.gridx = 2;
        sourceSizeLabel = new JLabel("");
        sourceSizeLabel.setFont(normalFont);
        panel.add(sourceSizeLabel, constraints);

        // DEST IMAGE PROPERTIES
        constraints.gridx = 1;
        constraints.gridy = 4;
        constraints.insets = new Insets(8, 2, 2, 2);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = 2;
        label = new JLabel("Destination image");
        label.setFont(titleFont);
        panel.add(label, constraints);

        constraints.insets = new Insets(2, 2, 2, 2);
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        label = new JLabel("Name:");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 2;
        destNameLabel = new JLabel("");
        destNameLabel.setFont(normalFont);
        panel.add(destNameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 6;
        label = new JLabel("Date");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 2;
        destDateLabel = new JLabel("");
        destDateLabel.setFont(normalFont);
        panel.add(destDateLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 7;
        label = new JLabel("Size:");
        label.setFont(headerFont);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        panel.add(label, constraints);

        constraints.gridx = 2;
        destSizeLabel = new JLabel("");
        destSizeLabel.setFont(normalFont);
        panel.add(destSizeLabel, constraints);

        return panel;
    }

    /**
     * Builds and returns a button panel for the bottom of the dialog.
     *
     * @return A button panel.
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JButton renameBtn = new JButton("Rename...");
        renameBtn.setPreferredSize(new Dimension(110, 28));
        final NameConflictDialog _this = this;
        renameBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog(_this, "Enter new name:", suggestNewName());
                if (newName != null) {
                    destFile = new File(destDir, newName);
                    if (destFile.exists()) {
                        getMessageUtil().info("That name is not unique in the destination directory.");
                        return;
                    }
                    result = Result.RENAME;
                    setVisible(false);
                }
            }

        });
        panel.add(renameBtn);

        JButton replaceBtn = new JButton("Overwrite");
        replaceBtn.setPreferredSize(new Dimension(110, 28));
        replaceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.SUCCESS;
                setVisible(false);
            }

        });
        panel.add(replaceBtn);

        cancelOneBtn = new JButton("Cancel");
        cancelOneBtn.setPreferredSize(new Dimension(110, 28));
        cancelOneBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.CANCEL;
                setVisible(false);
            }

        });
        panel.add(cancelOneBtn);

        cancelAllBtn = new JButton("Stop moving");
        cancelAllBtn.setPreferredSize(new Dimension(110, 28));
        cancelAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.CANCEL_ALL;
                setVisible(false);
            }

        });
        panel.add(cancelAllBtn);

        return panel;
    }

    /**
     * Internal utility function to suggest a new name for the current conflict.
     * Will try appending an underscore and a number to the existing name, and will keep trying
     * ascending numbers until it finds one that isn't taken in the destination directory.
     *
     * @return A best suggestion for a name in the destination directory that is available.
     */
    private String suggestNewName() {
        int numberSuffix = 1;
        boolean nameAvailable;
        String newName;

        do {
            String[] nameComponents = destFile.getName().split("\\.");
            if (nameComponents.length > 1) {
                nameComponents[nameComponents.length - 2] += "_" + numberSuffix;
            }
            else {
                nameComponents[0] += "_" + numberSuffix;
            }
            newName = "";
            for (String component : nameComponents) {
                newName += component + ".";
            }
            newName = newName.substring(0, newName.length() - 1); // Lose the last .

            nameAvailable = !new File(destDir, newName).exists();
            numberSuffix++;

        } while (!nameAvailable);

        return newName;
    }

    /**
     * Unreasonably long filenames stretch the dialog horizontally (IMGVIEW-109).
     * We could either wrap everything in a scroll panel, or just trim the filename
     * if it's too long, and add "..." to the end of it.
     *
     * @param filename The filename in question
     * @return The trimmed filename for display purposes.
     */
    private String trimFilename(String filename) {
        String trimmedName = filename;
        if (trimmedName.length() > 25) { // arbitrary length limit
            trimmedName = trimmedName.substring(0, 25) + "...";
        }
        return trimmedName;
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(this, Logger.getLogger(NameConflictDialog.class.getName()));
        }
        return messageUtil;
    }

}
