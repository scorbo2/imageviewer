package ca.corbett.imageviewer.extensions.builtin;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Launches the StatisticsDialog.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ViewStatisticsAction extends AbstractAction {

    private final StatisticsExtension owner;

    public ViewStatisticsAction(String name, StatisticsExtension instance) {
        super(name);
        this.owner = instance;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        new StatisticsDialog(owner).setVisible(true);
    }
}
