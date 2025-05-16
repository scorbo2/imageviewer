package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

class ThumbCachePregenerateAction extends AbstractAction {

    public ThumbCachePregenerateAction() {
        super("Pre-generate thumbnails...");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                                          "Thumbnails will be pregenerated in this directory and all "
                                                  + "subdirectories. This may take some time to complete. Proceed?",
                                          "Confirm thumbnail generation",
                                          JOptionPane.YES_NO_OPTION,
                                          JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            new Thread(new ThumbCachePregenerateThread(MainWindow.getInstance().getCurrentDirectory())).start();
        }
    }

}
