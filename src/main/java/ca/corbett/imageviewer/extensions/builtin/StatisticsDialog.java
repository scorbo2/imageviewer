package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A dialog for viewing or clearing statistics gathered by the StatisticsExtension.
 * Statistics can be cleared either via this dialog or by manually deleting the
 * stats.sqlite file in the user settings dir - it's not an error on startup if
 * this file does not exist. It will simply be recreated. To disable statistics
 * tracking entirely, you can disable the StatisticsExtension via the extension
 * manager dialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
final class StatisticsDialog extends JDialog {

    private static final String ALL_TYPES = "All types";
    private String extensionFilter = ALL_TYPES;

    private final StatisticsExtension owner;
    private JLabel todayLabel;
    private JLabel lastMonthLabel;
    private JLabel thisMonthLabel;
    private JLabel allTimeLabel;

    /**
     * Creates a new StatisticsDialog with the given StatisticsExtension owner.
     *
     * @param owner The StatisticsExtension which launched this dialog.
     */
    public StatisticsDialog(StatisticsExtension owner) {
        super(MainWindow.getInstance(), "Deletion statistics", true);
        this.owner = owner;
        setSize(280, 260);
        setResizable(false);
        setLocationRelativeTo(MainWindow.getInstance());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initComponents();
        updateStats();
    }

    /**
     * Invoked internally to create and lay out all dialog components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(buildContentPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

    }

    /**
     * Builds and returns the main content panel of this dialog.
     *
     * @return The main content panel for this dialog.
     */
    private JPanel buildContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        Font headerFont = new Font("SansSerif", Font.BOLD, 12);
        Font normalFont = new Font("SansSerif", 0, 12);

        JLabel label = new JLabel("Deletion statistics");
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(12, 36, 8, 18);
        panel.add(label, constraints);

        label = new JLabel("File type:");
        label.setFont(headerFont);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(4, 36, 2, 4);
        panel.add(label, constraints);

        constraints.gridx = 1;
        constraints.insets = new Insets(4, 4, 2, 18);
        constraints.weightx = 1;
        List<String> extensions = new ArrayList<>();
        extensions.add(extensionFilter);
        extensions.addAll(Arrays.asList(owner.getDeletedFileExtensions()));
        JComboBox extList = new JComboBox(extensions.toArray(new String[]{}));
        extList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extensionFilter = (String)((JComboBox)e.getSource()).getSelectedItem();
                updateStats();
            }

        });
        extList.setMinimumSize(new Dimension(110, 21));
        extList.setPreferredSize(new Dimension(110, 21));
        panel.add(extList, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.insets = new Insets(8, 36, 2, 2);
        label = new JLabel("Today:");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 1;
        todayLabel = new JLabel("n/a");
        todayLabel.setFont(normalFont);
        constraints.insets = new Insets(8, 2, 2, 18);
        panel.add(todayLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.insets = new Insets(8, 36, 2, 2);
        label = new JLabel("Last month:");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 1;
        lastMonthLabel = new JLabel("n/a");
        lastMonthLabel.setFont(normalFont);
        constraints.insets = new Insets(8, 2, 2, 18);
        panel.add(lastMonthLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.insets = new Insets(8, 36, 2, 2);
        label = new JLabel("This month:");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 1;
        thisMonthLabel = new JLabel("n/a");
        thisMonthLabel.setFont(normalFont);
        constraints.insets = new Insets(8, 2, 2, 18);
        panel.add(thisMonthLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.insets = new Insets(8, 36, 2, 2);
        label = new JLabel("All time:");
        label.setFont(headerFont);
        panel.add(label, constraints);

        constraints.gridx = 1;
        allTimeLabel = new JLabel("n/a");
        allTimeLabel.setFont(normalFont);
        constraints.insets = new Insets(8, 2, 2, 18);
        panel.add(allTimeLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 1;
        panel.add(new JLabel(""), constraints);

        return panel;
    }

    /**
     * Builds and returns the button panel for the bottom of the dialog.
     *
     * @return A button panel to show at the bottom of the dialog.
     */
    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton button = new JButton("Clear stats");
        button.setPreferredSize(new Dimension(95, 28));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearStats();
            }

        });
        panel.add(button);

        button = new JButton("Dismiss");
        button.setPreferredSize(new Dimension(95, 28));
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
     * Updates statistics based on current extensionFilter.
     */
    private void updateStats() {
        String ext = null;
        if (!extensionFilter.equals(ALL_TYPES)) {
            ext = extensionFilter;
        }

        long today = owner.getDeletionTotal(ext, StatisticsExtension.DateRange.TODAY);
        long lastMonth = owner.getDeletionTotal(ext, StatisticsExtension.DateRange.LAST_MONTH);
        long thisMonth = owner.getDeletionTotal(ext, StatisticsExtension.DateRange.THIS_MONTH);
        long allTime = owner.getDeletionTotal(ext, null);

        todayLabel.setText(byteCountToDisplaySize(today));
        lastMonthLabel.setText(byteCountToDisplaySize(lastMonth));
        thisMonthLabel.setText(byteCountToDisplaySize(thisMonth));
        allTimeLabel.setText(byteCountToDisplaySize(allTime));
    }

    /**
     * Apache's FileUtils contains an implementation of this method, but it oddly rounds down
     * to the nearest whole GB for large numbers, and that's not specific enough. So, here's
     * my implementation that shows two decimal places.
     *
     * @param input A count of bytes.
     * @return A human readable string along the lines of "2.41 GB"
     */
    private String byteCountToDisplaySize(long input) {
        final long GB = 1024 * 1024 * 1024;
        final long MB = 1024 * 1024;
        final long KB = 1024;
        double result = input;
        String suffix = " bytes";

        if (input > GB) {
            result = (double)input / GB;
            suffix = " GB";
        }
        else if (input > MB) {
            result = (double)input / MB;
            suffix = " MB";
        }
        else if (input > KB) {
            result = (double)input / KB;
            suffix = " KB";
        }
        else {
            return input + suffix;
        }

        return String.format("%.2f %s", result, suffix);
    }

    /**
     * Clears all statistics.
     */
    private void clearStats() {
        int input = JOptionPane.showConfirmDialog(this, "Really delete all statistics?",
                                                  "Confirm delete",
                                                  JOptionPane.YES_NO_OPTION);
        if (input == JOptionPane.NO_OPTION) {
            return;
        }

        owner.clearStats();
        updateStats();
    }
}
