package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.dialogs.QuickMoveDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class QuickMoveEditAction extends AbstractAction {

    public QuickMoveEditAction() {
        super("Configure Quick Move destinations...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new QuickMoveDialog().setVisible(true);
    }

}
