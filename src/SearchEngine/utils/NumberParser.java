package SearchEngine.utils;

/**
 * Created by sebastian on 13.11.2015.
 */

/**
 *
 */
public class NumberParser {
    /**
     * Tries to parse a string into a positive integer.
     * @param s
     * @return Integer value of s or -1 if no conversion is possible.
     */
    public static int parseDecimalInt(String s) {
        return -1;
    }

    public static long parseHexadecimalLong(String s) {
        long result = 0;
        char character;
        int length = s.length();
        int i = 0;

        while (i < length) {
            character = s.charAt(i++);
            result <<= 4;

            if (character >= '0' && character <= '9') {
                result += 0xf & (character - '0');
            } else if (character >= 'a' && character <= 'f') {
                // 'W' equals 'a' - 10
                result += 0xf & (character - 'W');
            } else {
                return -1;
            }
        }

        return result;
    }

    public static long parseDecimalLong(String s) {
        return -1;
    }

    public static int parseDecimalInt(Byte[] bytes) {
        int result = 0;

        for (int i = 0; i < bytes.length; ++i) {
            result *= 10;
            result += bytes[i] - '0';
        }

        return result;
    }

    public static long parseDecimalLong(Byte[] bytes) {
        long result = 0;

        for (int i = 0; i < bytes.length; ++i) {
            result *= 10;
            result += bytes[i] - '0';
        }

        return result;
    }
}
