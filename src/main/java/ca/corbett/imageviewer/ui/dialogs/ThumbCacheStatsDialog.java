package ca.corbett.imageviewer.ui.dialogs;

import ca.corbett.extras.MessageUtil;
import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.imageviewer.ui.ThumbCacheManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.logging.Logger;

/**
 * Dialog to show thumbnail cache statistics, with options to clear the cache.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ThumbCacheStatsDialog extends JDialog {

    private static final Logger log = Logger.getLogger(ThumbCacheStatsDialog.class.getName());

    private MessageUtil messageUtil;
    private final Window owner;
    private final LabelField statsLabel;
    private boolean isScanInProgress = false;

    public ThumbCacheStatsDialog(Window owner) {
        super(owner, "Thumbnail Cache Statistics", ModalityType.APPLICATION_MODAL);
        this.owner = owner;

        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(16);
        statsLabel = new LabelField("Cache stats: ", "Calculating...");
        formPanel.add(statsLabel);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        setSize(500, 160);
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        rescan();
    }

    /**
     * Invoked to rescan the thumbnail cache and update statistics.
     */
    private void rescan() {
        if (isScanInProgress) {
            log.info("Ignoring rescan request; scan already in progress.");
            return;
        }

        statsLabel.setText("Calculating...");
        isScanInProgress = true;
        new Thread(() -> {
            String results = "Scan failed.";
            try {
                results = ThumbCacheManager.gatherCacheStats().toString();
            }
            finally {
                isScanInProgress = false;
                final String resultsStr = results;
                SwingUtilities.invokeLater(() -> statsLabel.setText(resultsStr));
            }
        }).start();
    }

    /**
     * Will prompt for confirmation, and then clear the thumbnail cache if confirmed.
     */
    private void clearCache() {
        int response = getMessageUtil().askYesNo(
                "Confirm Clear Cache",
                "Are you sure you want to clear the thumbnail cache? This action cannot be undone.");
        if (response != MessageUtil.YES) {
            return;
        }

        ThumbCacheManager.clear();
        getMessageUtil().info("Cache Cleared", "Thumbnail cache cleared successfully.");
        rescan();
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createRaisedBevelBorder());

        JButton button = new JButton("Rescan");
        button.setPreferredSize(new Dimension(100, 25));
        button.addActionListener(e -> rescan());
        buttonPanel.add(button);

        button = new JButton("Clear");
        button.setPreferredSize(new Dimension(100, 25));
        button.addActionListener(e -> clearCache());
        buttonPanel.add(button);

        button = new JButton("OK");
        button.setPreferredSize(new Dimension(100, 25));
        button.addActionListener(e -> dispose());
        buttonPanel.add(button);

        return buttonPanel;
    }


    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(owner, log);
        }
        return messageUtil;
    }
}
