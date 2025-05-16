package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ImagePanelActualSizeAction extends AbstractAction {

    public ImagePanelActualSizeAction() {
        super("Actual size");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().zoomActualSize();
    }

}
