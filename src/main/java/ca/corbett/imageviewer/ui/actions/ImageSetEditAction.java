package ca.corbett.imageviewer.ui.actions;

import ca.corbett.imageviewer.ui.MainWindow;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.imagesets.ImageSetEditDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * An Action for editing the currently selected ImageSet, if any.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetEditAction extends AbstractAction {

    public ImageSetEditAction() {
        this("Edit favorites list");
    }

    public ImageSetEditAction(String name) {
        super(name);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Optional<ImageSet> selectedImageSet = MainWindow.getInstance().getImageSetPanel().getSelectedImageSet();
        if (selectedImageSet.isEmpty()) {
            MainWindow.getInstance().showMessageDialog("Edit image set", "Nothing selected.");
            return;
        }

        ImageSetEditDialog dialog = new ImageSetEditDialog(selectedImageSet.get());
        dialog.setVisible(true);
        if (dialog.wasOkayed()) {
            MainWindow.getInstance().setImageSet(selectedImageSet.get());
        }
    }
}
