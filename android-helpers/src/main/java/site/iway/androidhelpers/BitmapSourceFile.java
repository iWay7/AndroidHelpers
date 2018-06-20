package site.iway.androidhelpers;

public class BitmapSourceFile extends BitmapSource {

    public BitmapSourceFile(String file, BitmapFilter filter) {
        super(TYPE_FILE, file, filter);
    }

    public BitmapSourceFile(String file) {
        this(file, null);
    }

}
