package ca.corbett.imageviewer.ui;

import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.ui.actions.ReloadUIAction;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * A status panel that can be placed at the bottom of a window to display information
 * in two labels: left and right.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class StatusPanel extends JPanel implements UIReloadable {

    private final JLabel leftLabel;
    private final JPanel leftWrapperPanel;
    private final JLabel rightLabel;
    private final JPanel rightWrapperPanel;

    public StatusPanel() {
        setLayout(new BorderLayout());
        leftLabel = new JLabel("");
        rightLabel = new JLabel("");

        leftWrapperPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftWrapperPanel.add(leftLabel);
        add(leftWrapperPanel, BorderLayout.WEST);

        rightWrapperPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightWrapperPanel.add(rightLabel);
        add(rightWrapperPanel, BorderLayout.EAST);

        reloadUI();
        ReloadUIAction.getInstance().registerReloadable(this);
    }

    public String getLeftText() {
        return leftLabel.getText();
    }

    public void setLeftText(String text) {
        leftLabel.setText(text);
    }

    public String getRightText() {
        return rightLabel.getText();
    }

    public void setRightText(String text) {
        rightLabel.setText(text);
    }

    @Override
    public void reloadUI() {
        setBackground(AppConfig.getInstance().getDefaultBackground());
        leftWrapperPanel.setBackground(AppConfig.getInstance().getDefaultBackground());
        rightWrapperPanel.setBackground(AppConfig.getInstance().getDefaultBackground());
        leftLabel.setForeground(AppConfig.getInstance().getDefaultForeground());
        rightLabel.setForeground(AppConfig.getInstance().getDefaultForeground());
        leftLabel.setFont(AppConfig.getInstance().getStatusPanelFont());
        rightLabel.setFont(AppConfig.getInstance().getStatusPanelFont());

        if (AppConfig.getInstance().isStatusPanelBorderEnabled()) {
            setBorder(BorderFactory.createLoweredBevelBorder());
        }
        else {
            setBorder(BorderFactory.createEmptyBorder());
        }
    }
}
