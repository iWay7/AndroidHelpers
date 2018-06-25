package site.iway.androidhelpers;

public class BitmapSource {

    public static final int TYPE_INVALID = -1;
    public static final int TYPE_ASSET = 3;
    public static final int TYPE_FILE = 1;
    public static final int TYPE_RESOURCE = 2;
    public static final int TYPE_URL = 0;

    final int type;
    final String content;
    final BitmapFilter filter;
    final String id;
    final int hashCode;

    BitmapSource(int type, String content, BitmapFilter filter) {
        if (type < TYPE_URL || type > TYPE_ASSET)
            type = TYPE_INVALID;
        this.type = type;
        this.content = content;
        this.filter = filter;
        if (filter == null)
            id = type + "|" + content;
        else
            id = type + "|" + content + "|" + filter.id();
        hashCode = id.hashCode();
    }

    public BitmapSource(int type, String content) {
        this(type, content, null);
    }

    public int type() {
        return type;
    }

    public String content() {
        return content;
    }

    public String id() {
        return id;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof BitmapSource) {
            BitmapSource other = (BitmapSource) o;
            return id.equals(other.id);
        }
        return false;
    }

}
