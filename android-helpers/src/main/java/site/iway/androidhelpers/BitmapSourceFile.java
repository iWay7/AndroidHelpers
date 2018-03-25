package site.iway.androidhelpers;

public class BitmapSourceFile extends BitmapSource {

    String filePath;

    public BitmapSourceFile(String path, BitmapFilter filter) {
        super(filter);
        filePath = path;
    }

    public BitmapSourceFile(String path) {
        super(null);
        filePath = path;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean isValid() {
        return filePath != null && filePath.length() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BitmapSourceFile) {
            BitmapSourceFile other = (BitmapSourceFile) o;
            return compareValidString(filePath, other.filePath) && compareFilters(filter, other.filter);
        }
        return false;
    }

}
