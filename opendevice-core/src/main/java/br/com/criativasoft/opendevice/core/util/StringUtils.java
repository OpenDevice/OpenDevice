package br.com.criativasoft.opendevice.core.util;

import java.util.List;

public class StringUtils {

    public static String join(List<String> array , String sep ) {
        return join(array.toArray(new String[0]), sep.charAt(0));
    }

    public static String join( String[] array , String sep ) {
        return join(array, sep.charAt(0));
    }
    public static String join( String[] array , char sep ) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = array.length; i < il; i++) {
            if (i > 0) sbStr.append(sep);
            sbStr.append(array[i]);
        }
        return sbStr.toString();
    }

    public static boolean isEmpty(final String cs) {
        return cs == null || cs.length() == 0;
    }

}
