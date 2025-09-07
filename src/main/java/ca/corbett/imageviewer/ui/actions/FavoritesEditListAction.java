package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetEditDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class FavoritesEditListAction extends AbstractAction {

    public FavoritesEditListAction() {
        this("Edit favorites list");
    }

    public FavoritesEditListAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageSet selectedImageSet = MainWindow.getInstance().getImageSetPanel().getSelectedImageSet();
        if (selectedImageSet == null) {
            MainWindow.getInstance().showMessageDialog("Edit image set", "Nothing selected.");
            return;
        }

        ImageSetEditDialog dialog = new ImageSetEditDialog(selectedImageSet);
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            MainWindow.getInstance().setImageSet(selectedImageSet);
        }
    }
}
