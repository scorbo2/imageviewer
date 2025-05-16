package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.extras.MessageUtil;
import ca.corbett.imageviewer.ui.ImageInstance;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * Launches the ImageInformation dialog for the currently selected image, if there is one.
 *
 * @author scorbo2
 */
class ImageInfoAction extends AbstractAction {

    private static final Logger logger = Logger.getLogger(ImageInfoAction.class.getName());
    private MessageUtil messageUtil;

    public ImageInfoAction() {
        super("Image information");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageInstance image = MainWindow.getInstance().getSelectedImage();
        if (image.isEmpty()) {
            getMessageUtil().info("Image information", "No image selected.");
        }
        new ImageInfoDialog(MainWindow.getInstance(), image).setVisible(true);
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), logger);
        }
        return messageUtil;
    }

}
