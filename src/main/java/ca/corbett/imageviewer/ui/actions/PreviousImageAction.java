package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class PreviousImageAction extends AbstractAction {

    public PreviousImageAction() {
        super("Previous image");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().selectPreviousImage();
    }

}
