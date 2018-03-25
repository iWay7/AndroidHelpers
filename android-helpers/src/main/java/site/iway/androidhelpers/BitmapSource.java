package site.iway.androidhelpers;

public abstract class BitmapSource {

    BitmapFilter filter;

    BitmapSource(BitmapFilter filter) {
        this.filter = filter;
    }

    protected boolean compareValidString(String a, String b) {
        if (a == b)
            return true;
        int aLen = a.length();
        int bLen = b.length();
        if (aLen != bLen)
            return false;
        for (int i = aLen - 1; i >= 0; i--)
            if (a.charAt(i) != b.charAt(i))
                return false;
        return true;
    }

    public abstract boolean isValid();

    @Override
    public abstract boolean equals(Object o);

}
