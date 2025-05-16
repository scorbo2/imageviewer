package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ImagePanelZoomInAction extends AbstractAction {

    public ImagePanelZoomInAction() {
        super("Zoom in");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MainWindow.getInstance().zoomIn();
    }

}
