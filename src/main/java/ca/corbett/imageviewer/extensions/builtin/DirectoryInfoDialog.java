package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.extras.progress.MultiProgressDialog;
import ca.corbett.extras.progress.SimpleProgressWorker;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.ThumbContainerPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

/**
 * Shows information about the currently selected directory, with optional recursion.
 * The total number of files (images, companions, aliens) is shown, along with file sizes and totals.
 * <p>
 * <b>Note:</b> the directory information dialog is only available in file system browse mode.
 * The menu item will not show up in image set browse mode.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.4
 */
public class DirectoryInfoDialog extends JDialog {

    private final static Logger log = Logger.getLogger(DirectoryInfoDialog.class.getName());

    private final File directory;
    private final CheckBoxField recursiveField;
    private final LabelField imageFilesField;
    private final LabelField companionFilesField;
    private final LabelField alienFilesField;
    private final LabelField totalFilesField;

    public DirectoryInfoDialog(Frame owner, File dir) {
        super(owner, "Directory information", true);
        this.directory = dir;

        recursiveField = new CheckBoxField("Include subdirectories", true);
        recursiveField.addValueChangedListener(e -> rescan());
        imageFilesField = new LabelField("Image files: ", "0");
        companionFilesField = new LabelField("Companion files: ", "0");
        alienFilesField = new LabelField("Alien files: ", "0");
        totalFilesField = new LabelField("Total files: ", "0");
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(16);
        formPanel.add(List.of(
                recursiveField,
                imageFilesField,
                companionFilesField,
                alienFilesField,
                totalFilesField
        ));

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        setSize(400, 300);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        rescan();
    }

    private void rescan() {
        if (directory == null || ! directory.isDirectory()) {
            log.warning("No directory selected for DirectoryInfoDialog.");
            return;
        }

        // Fire off a worker thread as the scan may take some time:
        boolean isRecursive = recursiveField.isChecked();
        MultiProgressDialog progressDialog = new MultiProgressDialog(this, "Scanning directory...");
        progressDialog.setInitialShowDelayMS(500);
        progressDialog.runWorker(new ScanWorker(directory, isRecursive), true);
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        JButton button = new JButton("Rescan");
        button.setPreferredSize(new Dimension(100, 25));
        button.addActionListener(e -> rescan());
        buttonPanel.add(button);

        button = new JButton("OK");
        button.setPreferredSize(new Dimension(100, 25));
        button.addActionListener(e -> dispose());
        buttonPanel.add(button);

        return buttonPanel;
    }

    /**
     * An internal worker class to scan the directory in a background thread.
     */
    private class ScanWorker extends SimpleProgressWorker {

        private final File dir;
        private final boolean recursive;
        private int imageCount;
        private int companionCount;
        private int alienCount;
        private long imageSizeTotal;
        private long companionSizeTotal;
        private long alienSizeTotal;

        public ScanWorker(File dir, boolean recursive) {
            this.dir = dir;
            this.recursive = recursive;
        }

        @Override
        public void run() {
            imageCount = 0;
            companionCount = 0;
            alienCount = 0;
            imageSizeTotal = 0;
            companionSizeTotal = 0;
            alienSizeTotal = 0;

            ImageViewerExtensionManager extManager = ImageViewerExtensionManager.getInstance();
            List<File> allFiles = FileSystemUtil.findFiles(dir, recursive);
            fireProgressBegins(allFiles.size());

            try {
                for (int i = 0; i < allFiles.size(); i++) {
                    File candidate = allFiles.get(i);

                    // Figure out what it is and how big it is:
                    if (ThumbContainerPanel.isImageFile(candidate)) {
                        imageCount++;
                        imageSizeTotal += candidate.length();
                    } else if (extManager.isCompanionFile(candidate)) {
                        companionCount++;
                        companionSizeTotal += candidate.length();
                    } else {

                        // We have a ridiculous special case for "alien excluded files", which
                        // basically just get ignored as though they don't exist.
                        // One day I will remove that entire concept from this code...
                        // But until then, count all remaining files that AREN'T excluded as aliens:
                        if (!ThumbContainerPanel.isAlienExcludedFile(candidate)) {
                            alienCount++;
                            alienSizeTotal += candidate.length();
                        }
                    }

                    // Update progress
                    fireProgressUpdate(i, "Processing...");
                }

                // Update UI fields with final counts, but do it on the Swing EDT thread:
                SwingUtilities.invokeLater(() -> {
                    imageFilesField.setText(formatResult(imageCount, imageSizeTotal));
                    companionFilesField.setText(formatResult(companionCount, companionSizeTotal));
                    alienFilesField.setText(formatResult(alienCount, alienSizeTotal));
                    totalFilesField.setText(formatResult(imageCount + companionCount + alienCount,
                                                         imageSizeTotal + companionSizeTotal + alienSizeTotal));
                });
            }
            finally {
                // Don't forget to fire complete event!
                // Otherwise, the progress dialog hangs around.
                fireProgressComplete();
            }
        }

        private String formatResult(int fileCount, long fileSizeTotal) {
            return String.format("%d files (%s total)", fileCount, FileSystemUtil.getPrintableSize(fileSizeTotal));
        }
    }
}
