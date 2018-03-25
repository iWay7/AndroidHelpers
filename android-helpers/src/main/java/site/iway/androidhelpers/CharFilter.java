package site.iway.androidhelpers;

import android.text.InputFilter;
import android.text.Spanned;

import site.iway.javahelpers.ArrayHelper;

public class CharFilter implements InputFilter {

    private char[] mChars;

    public CharFilter(String chars) {
        mChars = chars.toCharArray();
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (ArrayHelper.contains(mChars, c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}
