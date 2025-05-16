package ca.corbett.imageviewer.ui.actions;

import ca.corbett.extras.logging.LogConsole;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class LogConsoleAction extends AbstractAction {

    public LogConsoleAction() {
        super("Log console");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LogConsole.getInstance().setVisible(true);
    }

}
