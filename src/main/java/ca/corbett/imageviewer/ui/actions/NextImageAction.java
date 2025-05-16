package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class NextImageAction extends AbstractAction {

    public NextImageAction() {
        super("Next image");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().selectNextImage();
    }

}
