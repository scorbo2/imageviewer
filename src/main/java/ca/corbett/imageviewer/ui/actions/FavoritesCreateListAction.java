package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

public class FavoritesCreateListAction extends AbstractAction {

    public FavoritesCreateListAction() {
        super("Create new list...");
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ImageInstance currentImage = MainWindow.getInstance().getSelectedImage();
        if (currentImage.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Rename", "Nothing selected.");
            return;
        }
        File file = currentImage.getImageFile();
        String name = JOptionPane.showInputDialog(MainWindow.getInstance(), "Enter name for new list:");
        if (name != null) {
            Optional<ImageSet> imageSetOptional = MainWindow.getInstance().getImageSetPanel()
                                                            .findOrCreateFavoritesSet(name);
            if (imageSetOptional.isPresent()) {
                if (imageSetOptional.get().addImageFile(file)) {
                    ReloadUIAction.getInstance().actionPerformed(actionEvent); // TODO overkill
                }
            }
        }
    }
}
