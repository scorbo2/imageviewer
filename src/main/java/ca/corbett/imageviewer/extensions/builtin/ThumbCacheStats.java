package ca.corbett.imageviewer.extensions.builtin;

/**
 * A very quick and dirty wrapper class to represent a count of thumbnails and their combined size.
 *
 * @author scorbo2
 */
class ThumbCacheStats {

    private final int thumbnailCount;
    private final int fileCount;
    private final long totalSize;

    public ThumbCacheStats(int thumbnailCount, int fileCount, long totalSize) {
        this.thumbnailCount = thumbnailCount;
        this.fileCount = fileCount;
        this.totalSize = totalSize;
    }

    @Override
    public String toString() {
        return thumbnailCount + " thumbnails in " + fileCount + " files, " + getPrintableSize(totalSize) + " total.";
    }

    public String getPrintableSize(long size) {
        String suffix = " bytes";
        if (size > 1024) {
            size /= 1024;
            suffix = "KB";

            if (size > 1024) {
                size /= 1024;
                suffix = "MB";

                if (size > 1024) {
                    size /= 1024;
                    suffix = "GB";

                    if (size > 1024) {
                        size /= 1024;
                        suffix = "TB";
                    }
                }
            }
        }
        return size + suffix;
    }

}
