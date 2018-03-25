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

    protected boolean compareFilters(BitmapFilter a, BitmapFilter b) {
        if (a == null) {
            if (b == null) {
                return true;
            } else {
                return false;
            }
        } else {
            if (b == null) {
                return false;
            } else {
                String aName = a.toString();
                String bName = b.toString();
                if (aName == null) {
                    if (bName == null) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    if (bName == null) {
                        return false;
                    } else {
                        return compareValidString(aName, bName);
                    }
                }
            }
        }
    }

    public abstract boolean isValid();

    @Override
    public abstract boolean equals(Object o);

}
