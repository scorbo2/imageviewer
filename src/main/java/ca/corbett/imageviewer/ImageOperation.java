package ca.corbett.imageviewer;

import java.io.File;

public class ImageOperation {

    public enum Type {

        MOVE("Move"),
        COPY("Copy"),
        SYMLINK("Symlink"),
        DELETE("Delete");

        private final String label;

        Type(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static Type fromString(String input) {
            for (Type op : values()) {
                if (op.label.equals(input)) {
                    return op;
                }
            }
            return null;
        }

    }

    public enum Payload {
        SINGLE_IMAGE("Single image"),
        ALL_IMAGES("All images"),
        DIRECTORY("Directory");

        private final String label;

        Payload(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }

        public static Payload fromString(String input) {
            for (Payload op : values()) {
                if (op.label.equals(input)) {
                    return op;
                }
            }
            return null;
        }

    }

    protected final Type operation;
    protected final Payload payload;
    protected File destination;

    public ImageOperation(ImageOperation other) {
        this.operation = other.operation;
        this.payload = other.payload;
        this.destination = other.destination;
    }

    public ImageOperation(Type op, Payload payload) {
        this(op, payload, null);
    }

    public ImageOperation(Type op, Payload payload, File destination) {
        this.operation = op;
        this.payload = payload;
        this.destination = destination;

        if (op == null || payload == null) {
            throw new RuntimeException("Attempted to create a null ImageOperation.");
        }
    }

    public ImageOperation copy() {
        return new ImageOperation(this);
    }

    public boolean isUndoable() {
        return operation != Type.DELETE;
    }

    public static ImageOperation moveSingleImage() {
        return new ImageOperation(Type.MOVE, Payload.SINGLE_IMAGE);
    }

    public static ImageOperation moveAllImages() {
        return new ImageOperation(Type.MOVE, Payload.ALL_IMAGES);
    }

    public static ImageOperation moveDirectory() {
        return new ImageOperation(Type.MOVE, Payload.DIRECTORY);
    }

    public static ImageOperation copySingleImage() {
        return new ImageOperation(Type.COPY, Payload.SINGLE_IMAGE);
    }

    public static ImageOperation copyAllImages() {
        return new ImageOperation(Type.COPY, Payload.ALL_IMAGES);
    }

    public static ImageOperation copyDirectory() {
        return new ImageOperation(Type.COPY, Payload.DIRECTORY);
    }

    public static ImageOperation linkSingleImage() {
        return new ImageOperation(Type.SYMLINK, Payload.SINGLE_IMAGE);
    }

    public static ImageOperation linkAllImages() {
        return new ImageOperation(Type.SYMLINK, Payload.ALL_IMAGES);
    }

    public static ImageOperation linkDirectory() {
        return new ImageOperation(Type.SYMLINK, Payload.DIRECTORY);
    }

    public static ImageOperation deleteSingleImage() {
        return new ImageOperation(Type.DELETE, Payload.SINGLE_IMAGE);
    }

    public static ImageOperation deleteAllImages() {
        return new ImageOperation(Type.DELETE, Payload.ALL_IMAGES);
    }

    public static ImageOperation deleteDirectory() {
        return new ImageOperation(Type.DELETE, Payload.DIRECTORY);
    }

    public Type getType() {
        return operation;
    }

    public Payload getPayload() {
        return payload;
    }

    public File getDestination() {
        return destination;
    }

    public void setDestination(File dest) {
        destination = dest;
    }

    public boolean isDeleteOperation() {
        return operation == Type.DELETE;
    }

    /**
     * Gets a very short descriptive name for this operation, used for logging purposes.
     *
     * @return A very short descriptive name for this operation.
     */
    public String getShortName() {
        StringBuilder sb = new StringBuilder();
        switch (operation) {
            case COPY:
                sb.append("copy");
                break;
            case MOVE:
                sb.append("move");
                break;
            case DELETE:
                sb.append("delete");
                break;
            case SYMLINK:
                sb.append("link");
                break;
        }
        switch (payload) {
            case SINGLE_IMAGE:
                sb.append("SingleImage");
                break;
            case ALL_IMAGES:
                sb.append("AllImages");
                break;
            case DIRECTORY:
                sb.append("Directory");
                break;
        }
        return sb.toString();
    }

}
