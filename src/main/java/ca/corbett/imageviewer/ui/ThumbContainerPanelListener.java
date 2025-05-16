package ca.corbett.imageviewer.ui;

/**
 * Provides an interface that can be used to listen for thumbnail selection and
 * deselection events in a ThumbContainerPanel instance.
 *
 * @author scorbo2
 * @since 2017-11-14
 */
public interface ThumbContainerPanelListener {

    /**
     * Fired when a ThumbPanel has been selected in the container. Currently only single
     * selection is allowed, so all other ThumbPanels in the container can be assumed
     * to be unselected.
     *
     * @param source        The ThumbContainerPanel that is firing this event.
     * @param selectedPanel The ThumbPanel that was selected.
     */
    public void thumbnailSelected(ThumbContainerPanel source, ThumbPanel selectedPanel);

    /**
     * Fired when the selection within the container has been cleared.
     *
     * @param source The ThumbContainerPanel that is firing this event.
     */
    public void selectionCleared(ThumbContainerPanel source);

    /**
     * Fired when the thumb container panel is starting to load more images.
     *
     * @param source The ThumbContainerPanel that is firing this event.
     */
    public void loadStarting(ThumbContainerPanel source);

    /**
     * Fired when the thumb container panel has finished loading images.
     *
     * @param source The ThumbContainerPanel that is firing this event.
     */
    public void loadCompleted(ThumbContainerPanel source);

}
