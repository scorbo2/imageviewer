package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ImagePanelZoomOutAction extends AbstractAction {

    public ImagePanelZoomOutAction() {
        super("Zoom out");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().zoomOut();
    }

}
