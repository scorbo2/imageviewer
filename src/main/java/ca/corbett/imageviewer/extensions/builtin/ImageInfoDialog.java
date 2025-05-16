package ca.corbett.imageviewer.extensions.builtin;

import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.imageviewer.ui.ImageInstance;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;

/**
 * Shows basic information for the given image (file size, image dimensions, date/time).
 *
 * @author scorbett
 */
class ImageInfoDialog extends JDialog {

    private final Frame ownerFrame;
    private final ImageInstance image;
    private final int imgWidth;
    private final int imgHeight;

    public ImageInfoDialog(Frame owner, ImageInstance image) {
        super(owner, "Image information", true);
        this.ownerFrame = owner;
        this.image = image;
        imgWidth = image.getImageWidth();
        imgHeight = image.getImageHeight();
        initComponents();
    }

    private void initComponents() {
        setSize(300, 200);
        setResizable(false);
        setLocationRelativeTo(ownerFrame);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildContentPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildContentPanel() {
        String fileName = image.getImageFileName(28);
        String fileSize = image.getFileSizePrintable();
        String imgSize = imgWidth + "x" + imgHeight;
        String imgDate = image.getFileDatePrintable(new SimpleDateFormat("yyyy-MM-dd h:mm:ssa"));

        FormPanel formPanel = new FormPanel(FormPanel.Alignment.TOP_CENTER);
        formPanel.addFormField(createLabelField("Name:", fileName, 24));
        formPanel.addFormField(createLabelField("Size:", fileSize + ", " + imgSize));
        formPanel.addFormField(createLabelField("Date:", imgDate));

        formPanel.render();
        return formPanel;
    }

    private LabelField createLabelField(String label, String text) {
        return createLabelField(label, text, 2);
    }

    private LabelField createLabelField(String label, String text, int topMargin) {
        LabelField theLabel = new LabelField(label, text);
        Font headerFont = theLabel.getFieldLabelFont().deriveFont(Font.BOLD, 12);
        theLabel.setFieldLabelFont(headerFont);
        theLabel.setMargins(topMargin, 32, 2, 10, 4);
        return theLabel;
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        JButton okBtn = new JButton("OK");
        okBtn.setPreferredSize(new Dimension(90, 28));
        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });
        okBtn.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {

                    case KeyEvent.VK_ESCAPE:
                    case KeyEvent.VK_ENTER:
                        dispose();
                        break;
                }
            }

        });
        panel.add(okBtn);

        return panel;
    }

}
