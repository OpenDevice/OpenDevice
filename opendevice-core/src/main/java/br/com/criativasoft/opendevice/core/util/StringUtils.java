package br.com.criativasoft.opendevice.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtils {

    private static final Map<Character, String> urlDirt = new HashMap<Character, String>();
    private static final Map<Character, String> alphabetics = new HashMap<Character, String>();
    private static final Map<Character, String> specials = new HashMap<Character, String>();

    static {
        String empty = "";
        specials.put('-', empty);
        specials.put('@', empty);
        specials.put('º', empty);
        specials.put('!', empty);
        specials.put('?', empty);
        specials.put('.', empty);
        specials.put(',', empty);
        specials.put(':', empty);
        specials.put(';', empty);
        specials.put('(', empty);
        specials.put(')', empty);
        specials.put('\'', empty);
        specials.put('"', empty);
        specials.put('\\', empty);
        specials.put('/', empty);
        specials.put('<', empty);
        specials.put('>', empty);
        specials.put('\t', empty);
        specials.put('\n', empty);
        specials.put('\r', empty);
        specials.put('%', empty);
        specials.put('Ø', empty);
        specials.put('ø', empty);
        specials.put('Ð', empty);
        specials.put('ð', empty);
        specials.put('Æ', empty);
        specials.put('æ', empty);

        alphabetics.put('á', "a");
        alphabetics.put('â', "a");
        alphabetics.put('à', "a");
        alphabetics.put('å', "a");
        alphabetics.put('ã', "a");
        alphabetics.put('ä', "a");
        alphabetics.put('é', "e");
        alphabetics.put('ê', "e");
        alphabetics.put('è', "e");
        alphabetics.put('&', "e");
        alphabetics.put('ë', "e");
        alphabetics.put('í', "i");
        alphabetics.put('î', "i");
        alphabetics.put('ì', "i");
        alphabetics.put('ï', "i");
        alphabetics.put('ó', "o");
        alphabetics.put('ô', "o");
        alphabetics.put('ò', "o");
        alphabetics.put('õ', "o");
        alphabetics.put('ö', "o");
        alphabetics.put('ú', "u");
        alphabetics.put('û', "u");
        alphabetics.put('ù', "u");
        alphabetics.put('ü', "u");
        alphabetics.put('Ç', "c");
        alphabetics.put('ç', "c");
        alphabetics.put('ñ', "n");

        urlDirt.putAll(specials);
        urlDirt.putAll(alphabetics);
        // urlDirt.put('-',"_");
        urlDirt.put(' ', "-");
        urlDirt.put('@', "_");

    }

    public static String join(List<String> array, String sep) {
        return join(array.toArray(new String[0]), sep.charAt(0));
    }

    public static String join(String[] array, String sep) {
        return join(array, sep.charAt(0));
    }

    public static String join(String[] array, char sep) {
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

    /**
     * Remove os caracteres especiais da string. Por padrão tambem remove os espaços em branco.
     *
     * @see #removeSpecialChars(String, boolean)
     */
    public static String removeSpecialChars(String s) {
        return removeSpecialChars(s, true);
    }


    /**
     * Remove Caracteres especiais como !,@,#,$,%,^,& e substitue os Acentos.
     */
    public static String removeSpecialChars(String s, boolean removeBlank) {
        if (removeBlank) s = s.replaceAll(" ", "");
        s = StringUtils.replace(s, specials);
        s = StringUtils.replace(s, alphabetics);
        return s;
    }


    public static String replace(String strIn, Map<Character, String> dirt) {
        if (isEmpty(strIn)) return "";

        StringBuilder outBuffer = new StringBuilder();
        String clean;
        for (Character charDirty : strIn.toCharArray()) {

            if (dirt.containsKey(charDirty)) {
                clean = dirt.get(charDirty);
            } else {
                clean = charDirty.toString();
            }
            outBuffer.append(clean);
        }
        return outBuffer.toString();
    }

}
