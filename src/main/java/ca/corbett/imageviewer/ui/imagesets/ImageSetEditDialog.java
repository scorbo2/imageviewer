package ca.corbett.imageviewer.ui.imagesets;

import ca.corbett.forms.Alignment;
import ca.corbett.forms.FormPanel;
import ca.corbett.forms.fields.CheckBoxField;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.LabelField;
import ca.corbett.forms.fields.PanelField;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Provides a way to view/edit the image list within an ImageSet.
 * This is mostly used for re-ordering the list as desired, or to
 * remove unwanted entries. Note that removing entries from the ImageSet
 * can also trivially be done from the MainWindow by hitting the
 * delete key. In IMAGE_SET browse mode, this simply removes the selected
 * image from the selected ImageSet.
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 * @since ImageViewer 2.2
 */
public class ImageSetEditDialog extends JDialog {

    private final ImageSet imageSet;
    private boolean wasOkayed = false;
    private DefaultListModel<String> listModel;
    private CheckBoxField transientField;
    private CheckBoxField lockedField;

    public ImageSetEditDialog(ImageSet imageSet) {
        super(MainWindow.getInstance(), "Edit image set: "+imageSet.getName(), true);
        this.imageSet = imageSet;
        setSize(new Dimension(640, 460));
        setResizable(false);
        setLocationRelativeTo(MainWindow.getInstance());
        setLayout(new BorderLayout());
        add(buildFormPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    private FormPanel buildFormPanel() {
        FormPanel formPanel = new FormPanel(Alignment.TOP_LEFT);
        formPanel.setBorderMargin(12);
        formPanel.add(LabelField.createBoldHeaderLabel("Edit image set"));
        formPanel.add(new LabelField("Full name:", imageSet.getFullyQualifiedName()));
        formPanel.add(LabelField.createPlainHeaderLabel("Drag+drop or ctrl+up/ctrl+down to reorder, DEL to remove"));
        formPanel.add(buildListField());
        transientField = new CheckBoxField("Save this image set on shutdown", !imageSet.isTransient());
        formPanel.add(transientField);
        lockedField = new CheckBoxField("Lock this image set to prevent deletion", imageSet.isLocked());
        formPanel.add(lockedField);

        return formPanel;
    }

    public boolean wasOkayed() {
        return wasOkayed;
    }

    private FormField buildListField() {
        listModel = new DefaultListModel<>();
        for (String path : imageSet.getImageFilePaths()) {
            listModel.addElement(path);
        }
        JList<String> list = new JList<>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new ListReorderTransferHandler());
        setupKeyboardShortcuts(list);

        PanelField panelField = new PanelField(new BorderLayout());
        panelField.setShouldExpand(true);
        panelField.getPanel().add(new JScrollPane(list), BorderLayout.CENTER);
        return panelField;
    }

    /**
     * Sets up keyboard shortcuts for moving items up and down in the list.
     */
    private void setupKeyboardShortcuts(JList<String> list) {
        // Input maps for key bindings
        InputMap inputMap = list.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = list.getActionMap();

        // Move up shortcut (Ctrl+Up)
        inputMap.put(KeyStroke.getKeyStroke("ctrl UP"), "moveUp");
        actionMap.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                moveSelectedItem(list, -1);
            }
        });

        // Move down shortcut (Ctrl+Down)
        inputMap.put(KeyStroke.getKeyStroke("ctrl DOWN"), "moveDown");
        actionMap.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                moveSelectedItem(list, 1);
            }
        });

        // Remove (delete)
        inputMap.put(KeyStroke.getKeyStroke("DELETE"), "remove");
        actionMap.put("remove", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                removeSelectedItem(list);
            }
        });
    }

    /**
     * Moves the selected item in the list by the specified offset.
     * @param list The JList to modify
     * @param offset Direction to move (-1 for up, 1 for down)
     */
    private void moveSelectedItem(JList<String> list, int offset) {
        listModel = (DefaultListModel<String>) list.getModel();
        int selectedIndex = list.getSelectedIndex();

        if (selectedIndex == -1) {
            return; // No selection
        }

        int newIndex = selectedIndex + offset;

        // Check bounds
        if (newIndex < 0 || newIndex >= listModel.getSize()) {
            return; // Can't move beyond bounds
        }

        // Move the item
        String item = listModel.getElementAt(selectedIndex);
        listModel.removeElementAt(selectedIndex);
        listModel.insertElementAt(item, newIndex);

        // Maintain selection on moved item
        list.setSelectedIndex(newIndex);
        list.ensureIndexIsVisible(newIndex);
    }

    private void removeSelectedItem(JList<String> list) {
        listModel = (DefaultListModel<String>)list.getModel();
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex == -1) {
            return;
        }

        listModel.removeElementAt(selectedIndex);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton button = new JButton("OK");
        button.setPreferredSize(new Dimension(90,23));
        button.addActionListener(actionEvent -> close(true));
        panel.add(button);

        button = new JButton("Cancel");
        button.setPreferredSize(new Dimension(90,23));
        button.addActionListener(actionEvent -> close(false));
        panel.add(button);

        return panel;
    }

    private void close(boolean okay) {
        wasOkayed = okay;
        if (wasOkayed) {
            imageSet.clearImages();
            for (int i = 0; i < listModel.getSize(); i++) {
                imageSet.addImageFilePath(listModel.getElementAt(i));
            }
            imageSet.setTransient(!transientField.isChecked());
            imageSet.setLocked(lockedField.isChecked());
        }
        dispose();
    }

    /**
     * Custom TransferHandler that handles dragging and dropping list items
     * to reorder them within the same list.
     */
    private static class ListReorderTransferHandler extends TransferHandler {
        private static final DataFlavor LOCAL_OBJECT_FLAVOR =
                new DataFlavor(Integer.class, "application/x-java-Integer");

        private static final DataFlavor[] SUPPORTED_FLAVORS = {LOCAL_OBJECT_FLAVOR};

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            JList<?> list = (JList<?>) c;
            int selectedIndex = list.getSelectedIndex();
            if (selectedIndex < 0) {
                return null;
            }
            return new IntegerTransferable(selectedIndex);
        }

        @Override
        protected void exportDone(JComponent c, Transferable t, int action) {
            // Clean up is handled in importData
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                return false;
            }
            return support.isDataFlavorSupported(LOCAL_OBJECT_FLAVOR);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            JList.DropLocation dropLocation = (JList.DropLocation) support.getDropLocation();
            int dropIndex = dropLocation.getIndex();

            JList<Object> list = (JList<Object>) support.getComponent();
            DefaultListModel<Object> model = (DefaultListModel<Object>) list.getModel();

            try {
                Integer sourceIndex = (Integer) support.getTransferable()
                                                       .getTransferData(LOCAL_OBJECT_FLAVOR);

                if (sourceIndex == null || sourceIndex == dropIndex) {
                    return false;
                }

                // Remove the item from its original position
                Object item = model.getElementAt(sourceIndex);
                model.removeElementAt(sourceIndex);

                // Adjust drop index if we removed an item before the drop position
                if (sourceIndex < dropIndex) {
                    dropIndex--;
                }

                // Insert the item at its new position
                model.insertElementAt(item, dropIndex);

                // Select the moved item
                list.setSelectedIndex(dropIndex);

                return true;

            } catch (UnsupportedFlavorException |
                     IOException e) {
                e.printStackTrace(); // todo log this? show a dialog? ignore it?
                return false;
            }
        }
    }

    /**
     * Simple Transferable implementation for transferring integer indices.
     */
    private static class IntegerTransferable implements Transferable {
        private final Integer value;

        public IntegerTransferable(Integer value) {
            this.value = value;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{ListReorderTransferHandler.LOCAL_OBJECT_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ListReorderTransferHandler.LOCAL_OBJECT_FLAVOR.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return value;
        }
    }
}
