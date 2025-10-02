package ca.corbett.imageviewer.ui;

import ca.corbett.extras.io.FileSystemUtil;
import ca.corbett.imageviewer.AppConfig;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.dialogs.AlienDialog;
import ca.corbett.imageviewer.ui.imagesets.ImageSet;
import ca.corbett.imageviewer.ui.layout.WrapLayout;
import ca.corbett.imageviewer.ui.threads.ThumbLoaderThread;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A container to show a list of ThumbPanel instances.
 *
 * @author scorbo2
 * @since 2017-11-12
 */
public final class ThumbContainerPanel extends JPanel {

    /**
     * A file will be considered a valid image if its filename ends with any
     * extension in this list.
     */
    public static final List<String> imageExtensions;

    /**
     * A file will be considered an "alien" file UNLESS its filename ends with
     * any extension in this list. Note this list might not be the same as
     * the imageExtensions list, because reasons.
     */
    private static final List<String> alienExclusionExtensions;

    private final MainWindow.BrowseMode browseMode;
    private final List<ThumbContainerPanelListener> listeners;
    private List<File> imageFileList;
    private List<File> alienFileList;
    private File currentDir;
    private final List<ThumbPanel> loadedThumbPanels;
    private int selectedPanelIndex;
    private int loadOffset;
    private int thumbWidth;
    private int thumbHeight;

    private JPanel loadMorePanel;
    private JButton loadMoreBtn;
    private JButton loadAllBtn;

    private JPanel alienPanel;
    private JLabel alienLabel;
    private JButton viewAliensBtn;

    private static final int PANEL_MARGIN = 20;
    private static final int INFO_PANEL_WIDTH = 120;
    private static final int INFO_PANEL_HEIGHT = 77;

    /*
     * Statically create the list of allowable image extensions
     * and alien exclusion extensions.
     */
    static {
        imageExtensions = new ArrayList<>();
        imageExtensions.add("gif");
        imageExtensions.add("jpg");
        imageExtensions.add("jpeg");
        imageExtensions.add("png");
        imageExtensions.add("tiff");
        imageExtensions.add("bmp");

        alienExclusionExtensions = new ArrayList<>();
        alienExclusionExtensions.addAll(imageExtensions);
        alienExclusionExtensions.add(AlienDialog.DARWIN_METADATA_FILENAME.replace(".", ""));
    }

    /**
     * Constructor is private to force factory method access.
     */
    private ThumbContainerPanel(MainWindow.BrowseMode browseMode) {
        alienFileList = new ArrayList<>();
        listeners = new ArrayList<>();
        loadedThumbPanels = new ArrayList<>();
        selectedPanelIndex = -1;
        thumbWidth = thumbHeight = AppConfig.getInstance().getThumbnailSize();
        this.browseMode = browseMode;
        initComponents();
    }

    /**
     * Creates and returns a default ThumbPanelContainer.
     *
     * @return The new instance.
     */
    public static ThumbContainerPanel createThumbContainer(MainWindow.BrowseMode browseMode) {
        return new ThumbContainerPanel(browseMode);
    }

    /**
     * Returns the BrowseMode that this thumb panel was created to serve.
     */
    public MainWindow.BrowseMode getBrowseMode() {
        return browseMode;
    }

    /**
     * Returns the list of allowable image extensions. Implementation note: a copy of the
     * list is returned to prevent client modification.
     *
     * @return A static list of allowable image extensions.
     */
    public static List<String> getImageExtensions() {
        List<String> copy = new ArrayList<>();
        copy.addAll(imageExtensions);
        return copy;
    }

    /**
     * Returns a copy of the list of image files in the current directory.
     *
     * @return A copy of the list of image files in the current directory.
     */
    public List<File> getImageFiles() {
        List<File> copy = new ArrayList<>();
        copy.addAll(imageFileList);
        return copy;
    }

    /**
     * Returns a copy of the list of alien files in the current directory.
     *
     * @return A copy of the list of alien files in the current directory.
     */
    public List<File> getAliens() {
        List<File> copy = new ArrayList<>();
        copy.addAll(alienFileList);
        return copy;
    }

    /**
     * Returns the current directory.
     *
     * @return The current directory.
     */
    public File getDirectory() {
        return currentDir;
    }

    /**
     * Internal method to initialize the layout and cosmetic properties of this container.
     */
    private void initComponents() {
        setLayout(new WrapLayout());

        // Create the "load more" panel:
        loadMorePanel = new JPanel();
        loadMorePanel.setLayout(new FlowLayout());
        loadMorePanel.setBackground(Color.black);
        loadMorePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        loadMorePanel.setPreferredSize(new Dimension(INFO_PANEL_WIDTH, INFO_PANEL_HEIGHT));
        loadMoreBtn = new JButton("Load more");
        loadMoreBtn.setFont(new Font("SansSerif", 0, 9));
        loadMoreBtn.setPreferredSize(new Dimension(INFO_PANEL_WIDTH - PANEL_MARGIN, 26));
        loadAllBtn = new JButton("Load all");
        loadAllBtn.setFont(new Font("SansSerif", 0, 9));
        loadAllBtn.setPreferredSize(new Dimension(INFO_PANEL_WIDTH - PANEL_MARGIN, 26));
        loadMorePanel.add(loadMoreBtn);
        loadMorePanel.add(loadAllBtn);

        loadMoreBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadMoreImages();
            }

        });

        loadAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAllImages();
            }

        });

        // Create the "alien" panel:
        alienPanel = new JPanel();
        alienPanel.setLayout(new FlowLayout());
        alienPanel.setBackground(new Color(64, 0, 0));
        alienPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        alienPanel.setPreferredSize(new Dimension(INFO_PANEL_WIDTH, INFO_PANEL_HEIGHT - 12));
        alienLabel = new JLabel("");
        alienLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        alienLabel.setForeground(Color.red);
        viewAliensBtn = new JButton("View");
        viewAliensBtn.setFont(new Font("SansSerif", 0, 9));
        viewAliensBtn.setPreferredSize(new Dimension(INFO_PANEL_WIDTH - PANEL_MARGIN, 26));
        alienPanel.add(alienLabel);

        viewAliensBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AlienDialog.getInstance().setDirectory(currentDir);
                AlienDialog.getInstance().setVisible(true);
                alienFileList = findAlienFiles(currentDir);
                addAlienControl(); // will hide if no longer needed.
            }

        });

        alienPanel.add(viewAliensBtn);
    }

    /**
     * Intended to be invoked when the thumbnail dimensions are changed in preferences.
     * If any thumbnails are currently showing, the list will be regenerated at the new
     * size. The new size will also be used going forward.
     */
    public void reloadThumbSizePreference() {
        thumbWidth = thumbHeight = AppConfig.getInstance().getThumbnailSize();
        if (loadMorePanel != null) {
            loadMorePanel.setPreferredSize(new Dimension(thumbWidth + PANEL_MARGIN, INFO_PANEL_HEIGHT));
            loadMoreBtn.setPreferredSize(new Dimension(thumbWidth, 28));
            loadAllBtn.setPreferredSize(new Dimension(thumbWidth, 28));
        }
        if (alienPanel != null) {
            alienPanel.setPreferredSize(new Dimension(thumbWidth + PANEL_MARGIN, INFO_PANEL_HEIGHT));
            viewAliensBtn.setPreferredSize(new Dimension(thumbWidth, 28));
        }
    }

    /**
     * Registers to receive notification when a thumbnail is selected or deselected.
     *
     * @param listener The ThumbContainerPanelListener to register.
     */
    public void addListener(ThumbContainerPanelListener listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters the specified listener from receiving any further notifications from us.
     *
     * @param listener The ThumbContainerPanelListener to unregister.
     */
    public void removeListener(ThumbContainerPanelListener listener) {
        listeners.remove(listener);
    }

    /**
     * Removes any currently displayed thumbnail panels and loads from the given list instead.
     * Will use the currently set page size to determine how many images to load at a time.
     *
     * @param fileList The new list of images to load, or null to clear this container.
     */
    private void setImageList(List<File> fileList) {
        // Remove all current ThumbPanels and release any resources they were using:
        removeAll();
        revalidate();
        repaint();
        for (ThumbPanel pn : loadedThumbPanels) {
            pn.getThumbImage().flush();
        }
        loadedThumbPanels.clear();
        selectedPanelIndex = -1;
        fireSelectionClearedEvent();

        imageFileList = fileList;
        if (imageFileList == null) {
            imageFileList = new ArrayList<>();
        }
        alienFileList.clear();
        loadOffset = 0;
        loadMoreImages();
    }

    /**
     * Points this ThumbContainerPanel to the given directory, and loads all thumbnails
     * found there. This will remove and release any previously displayed images.
     *
     * @param dir The new directory from which to load thumbs.
     */
    public void setDirectory(File dir) {
        currentDir = dir;
        if (dir == null) {
            clear();
            return;
        }
        setImageList(FileSystemUtil.findFiles(dir, false, imageExtensions));
        alienFileList = findAlienFiles(dir);
    }

    public void setImageSet(ImageSet imageSet) {
        if (imageSet == null) {
            clear();
            return;
        }
        List<String> imageFilePaths = imageSet.getImageFilePaths();
        List<File> imageFiles = new ArrayList<>(imageFilePaths.size());
        for (String path : imageFilePaths) {
            imageFiles.add(new File(path));
        }
        setImageList(imageFiles);
    }

    /**
     * Overridden so we can add mouse listeners as needed to any added ThumbPanel.
     *
     * @param component the component to be added
     * @return The component argument
     */
    @Override
    public Component add(Component component) {
        if (component instanceof ThumbPanel) {
            ThumbPanel pn = (ThumbPanel)component;
            loadedThumbPanels.add(pn);

            // add mouse listener to handle selection
            pn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSelectedThumb(pn);
                }

            });
        }

        return super.add(component);
    }

    /**
     * Convenience method for adding a new ThumbPanel. Instead of creating the ThumbPanel
     * in client code and passing it to add(), you can just hand the File to this method
     * and have the ThumbPanel create it automatically.
     *
     * @param file       The file from which the thumbnail was generated.
     * @param thumbImage the Thumbnail which has been generated for the given file.
     */
    public void addThumb(File file, BufferedImage thumbImage) {
        int panelWidth = thumbWidth + PANEL_MARGIN;
        int panelHeight = thumbHeight + PANEL_MARGIN;
        String name = file.getName();

        ThumbPanel pn = new ThumbPanel(file, thumbImage, name, panelWidth, panelHeight);
        ImageViewerExtensionManager.getInstance().thumbPanelCreated(pn);
        add(pn);
    }

    /**
     * Adds a "load more images" control if the number of images to load is greater than
     * our current page size. Intended to be invoked from ThumbLoaderThread, but can be
     * invoked by client code also. It is safe to invoke this regardless of the current
     * load offset and page size - the control will be shown or hidden automatically as needed.
     */
    public void addLoadMoreControl() {
        int pageSize = AppConfig.getInstance().getThumbnailPageSize();
        int yetToLoad = imageFileList.size() - loadOffset;
        loadMoreBtn.setText("Load " + pageSize + " more");
        loadAllBtn.setText("Load all " + yetToLoad);

        if (yetToLoad < pageSize) {
            loadMoreBtn.setVisible(false);
        }
        else {
            loadMoreBtn.setVisible(true);
        }

        if (yetToLoad > 0) {
            add(loadMorePanel);
        }
        else {
            remove(loadMorePanel);
        }
        revalidate();
        repaint();
    }

    /**
     * Adds a control for viewing "alien" files, if any exist in the current directory.
     * If there are no aliens present, the control will be hidden. Otherwise, a control is
     * shown indicating the number of aliens and giving a button to view and optionally
     * delete them.
     */
    public void addAlienControl() {
        alienLabel.setText(alienFileList.size() + " alien file");
        if (alienFileList.size() > 1) {
            alienLabel.setText(alienLabel.getText() + "s");
        }

        if (!alienFileList.isEmpty()) {
            add(alienPanel);
        }
        else {
            remove(alienPanel);
        }
        revalidate();
        repaint();
    }

    /**
     * Loads the next pageSize worth of images, if needed (does nothing if the current
     * load offset is already past the end of the file list).
     */
    public void loadMoreImages() {
        int pageSize = AppConfig.getInstance().getThumbnailPageSize();
        remove(loadMorePanel);
        revalidate();
        repaint();

        ThumbLoaderThread thread = new ThumbLoaderThread(imageFileList, this, loadOffset);

        // Special case for page size of 0, it means to load everything:
        if (pageSize == 0) {
            pageSize = imageFileList.size() - loadOffset;
            thread.setCustomPageSize(pageSize);
        }

        // Notify listeners that we're starting to load:
        fireLoadStartedEvent();

        new Thread(thread).start();
    }

    /**
     * Loads all remaining images that have not already been loaded.
     */
    public void loadAllImages() {
        remove(loadMorePanel);
        revalidate();
        repaint();

        int remaining = imageFileList.size() - loadOffset;

        ThumbLoaderThread thread = new ThumbLoaderThread(imageFileList, this, loadOffset);
        thread.setCustomPageSize(remaining);

        // Notify listeners that we're starting to load:
        fireLoadStartedEvent();

        new Thread(thread).start();
    }

    /**
     * Empties this container of all thumbnails and clears any current selection.
     */
    public void clear() {
        setImageList(null);
        alienFileList.clear();
    }

    /**
     * Selects the given ThumbPanel. Unselects all other ThumbPanels (multiple selection
     * not supported).
     *
     * @param pn The ThumbPanel to select.
     */
    private void setSelectedThumb(ThumbPanel pn) {
        selectedPanelIndex = -1;
        for (int i = 0; i < loadedThumbPanels.size(); i++) {
            ThumbPanel candidate = loadedThumbPanels.get(i);
            if (candidate.getTitle().equals(pn.getTitle())) {
                selectedPanelIndex = i;
                candidate.setSelected(true);
                fireThumbSelectedEvent(candidate);
                ThumbContainerPanel containerPanel = (ThumbContainerPanel)candidate.getParent();
                if (containerPanel != null) {
                    containerPanel.scrollRectToVisible(candidate.getBounds());
                }
            }
            else {
                candidate.setSelected(false);
            }
        }

        if (selectedPanelIndex == -1) {
            fireSelectionClearedEvent();
        }
    }

    /**
     * Selects the next image in the list. Selection does not change if the currently
     * selected image was the last one in the list. If the currently selected image is the
     * last image currently loaded, but there are more yet to load, this will trigger
     * loading of the next page of thumbnails.
     */
    public void selectNext() {
        if (selectedPanelIndex < imageFileList.size() - 1) {
            selectedPanelIndex++;
            if (selectedPanelIndex >= loadedThumbPanels.size()) {
                loadMoreImages();
            }
            else {
                setSelectedThumb(loadedThumbPanels.get(selectedPanelIndex));
            }
        }
    }

    /**
     * Selects the previous image in the list. Selection does not change if the currently selected
     * image was the first one in the list.
     */
    public void selectPrevious() {
        if (selectedPanelIndex > 0) {
            selectedPanelIndex--;
            setSelectedThumb(loadedThumbPanels.get(selectedPanelIndex));
        }
    }

    /**
     * Selects the ThumbPanel at the given index. If the index is less than zero, the first
     * panel will be selected. If the index is greater than the size of the list, the last
     * panel will be selected. If there are no thumbs in this panel, this does nothing.
     *
     * @param index The index of the panel to select.
     */
    public void selectAtIndex(int index) {
        if (loadedThumbPanels.isEmpty()) {
            selectedPanelIndex = -1;
            return;
        }

        // Keep the selection in bounds:
        if (index < 0) {
            index = 0;
        }
        if (index >= loadedThumbPanels.size()) {
            index = loadedThumbPanels.size() - 1;
        }

        // Scroll to make this panel visible if needed:
        selectedPanelIndex = index;
        ThumbPanel thumbPanel = loadedThumbPanels.get(selectedPanelIndex);
        ThumbContainerPanel containerPanel = (ThumbContainerPanel)thumbPanel.getParent();
        containerPanel.scrollRectToVisible(thumbPanel.getBounds());

        // Notify listeners that thumb selection has changed:
        fireThumbSelectedEvent(thumbPanel);
    }

    /**
     * Renames the currently selected ThumbPanel. Does nothing if there is no selection.
     *
     * @param newFile the new File object (not validated here, assuming caller has done that.)
     */
    public void renameSelected(File newFile) {
        if (loadedThumbPanels.isEmpty() || selectedPanelIndex < 0) {
            return;
        }

        // Get the guy that was selected:
        ThumbPanel toRename = loadedThumbPanels.get(selectedPanelIndex);
        toRename.renameFile(newFile);
    }

    /**
     * Removes the currently selected ThumbPanel. This does nothing if there is no selection.
     */
    public void removeSelected() {
        if (loadedThumbPanels.isEmpty() || selectedPanelIndex < 0) {
            return;
        }

        // Remove the guy that was selected:
        ThumbPanel toRemove = loadedThumbPanels.get(selectedPanelIndex);
        toRemove.getThumbImage().flush(); // TODO is this relevant here... may still be loaded in main panel
        loadOffset--; // assuming here that we're removing because the file was moved or deleted
        loadedThumbPanels.remove(selectedPanelIndex);
        imageFileList.remove(selectedPanelIndex);
        remove(toRemove);
        revalidate();
        repaint();

        // If the new selection is larger than the image count, decrement it:
        if (selectedPanelIndex >= imageFileList.size()) {
            selectedPanelIndex--;
        }

        // If the new selection is past the end of the loaded thumb list, load some more:
        if (selectedPanelIndex >= loadedThumbPanels.size()) {
            loadMoreImages();
        }

        // Otherwise, select the next guy in the list:
        else if (selectedPanelIndex >= 0) {
            setSelectedThumb(loadedThumbPanels.get(selectedPanelIndex));
        }

        // If the new selected index is less than zero, it means we just deleted the
        // very last one, so clear all selection:
        else {
            fireSelectionClearedEvent();
        }
    }

    /**
     * Tells this thumb container panel to re-fire the selection event for whatever is
     * currently selected (or a selection cleared event if nothing is selected).
     */
    public void reselectCurrent() {
        ThumbPanel thumbPanel = selectedPanelIndex >= 0 ? loadedThumbPanels.get(selectedPanelIndex) : null;
        if (thumbPanel == null) {
            fireSelectionClearedEvent();
        }
        else {
            fireThumbSelectedEvent(thumbPanel);
        }
    }

    /**
     * Returns a count of the thumbs in this panel. Note that this count may be theoretical:
     * it's the count of all the thumbs that *should* be in this panel, not necessarily the count
     * of how many have actually been loaded so far.
     *
     * @return The number of thumbnails this panel should contain.
     */
    public int getCount() {
        return imageFileList.size();
    }

    /**
     * Returns the current selection index, or -1 if nothing is selected.
     *
     * @return The current selection index, or -1 if nothing is selected.
     */
    public int getSelectionIndex() {
        return selectedPanelIndex;
    }

    /**
     * Invoked by the loader thread when loading has completed.
     */
    public void notifyLoadFinished() {
        // Notify listeners:
        fireLoadCompletedEvent();
    }

    /**
     * Invoked by ThumbLoaderThread after completing a batch load.
     *
     * @param offset The new load offset value.
     */
    public void setLoadOffset(int offset) {
        // If we got at least one new thumbnail, select it:
        if (loadedThumbPanels.size() > loadOffset) {
            setSelectedThumb(loadedThumbPanels.get(loadOffset));
        }

        this.loadOffset = offset;
    }

    /**
     * Internal method to notify listeners that a thumbnail was selected.
     *
     * @param pn The ThumbPanel in question.
     */
    private void fireThumbSelectedEvent(ThumbPanel pn) {
        for (ThumbContainerPanelListener listener : listeners) {
            listener.thumbnailSelected(this, pn);
        }
    }

    /**
     * Internal method to notify listeners that the selection was cleared.
     */
    private void fireSelectionClearedEvent() {
        for (ThumbContainerPanelListener listener : listeners) {
            listener.selectionCleared(this);
        }
    }

    /**
     * Internal method to notify listeners that loading has commenced.
     */
    private void fireLoadStartedEvent() {
        for (ThumbContainerPanelListener listener : listeners) {
            listener.loadStarting(this);
        }
    }

    /**
     * Internal method to notify listeners that loading has completed.
     */
    private void fireLoadCompletedEvent() {
        for (ThumbContainerPanelListener listener : listeners) {
            listener.loadCompleted(this);
        }
    }

    public static List<File> findAlienFiles(File dir) {
        List<File> aliens = FileSystemUtil.findFilesExcluding(dir, false, alienExclusionExtensions);
        List<File> listToReturn = new ArrayList<>();

        for (File f : aliens) {

            // Ask all our extensions if they recognize this file:
            boolean isCompanion = ImageViewerExtensionManager.getInstance().isCompanionFile(f);
            if (!isCompanion) {
                listToReturn.add(f);
            }
        }

        return listToReturn;
    }

}
