package site.iway.androidhelpers;

import java.util.regex.Pattern;

@Deprecated
public class RegexHelper {

    public static boolean isEmailAddress(String string) {
        String pattern = "(([\\w][\\.\\-]?)+[\\w])@([\\w\\-]+\\.)+[\\w]+";
        return Pattern.matches(pattern, string);
    }

    public static boolean isMobilePhoneNumber(String string) {
        String pattern = "^^[1][0-9]{10}$";
        return Pattern.matches(pattern, string);
    }

    public static boolean isQQNumber(String string) {
        String pattern = "^[1-9](\\d){4,11}$";
        return Pattern.matches(pattern, string);
    }

    public static boolean isIPv4Address(String string) {
        String pattern = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
        return Pattern.matches(pattern, string);
    }

    public static boolean isPortNumber(String string) {
        try {
            Integer integer = Integer.parseInt(string);
            return integer >= 0 && integer <= 65535;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isMacAddress(String string) {
        String pattern = "[0-9A-F][0-9A-F]-[0-9A-F][0-9A-F]-[0-9A-F][0-9A-F]-[0-9A-F][0-9A-F]-[0-9A-F][0-9A-F]-[0-9A-F][0-9A-F]";
        return Pattern.matches(pattern, string);
    }

}
