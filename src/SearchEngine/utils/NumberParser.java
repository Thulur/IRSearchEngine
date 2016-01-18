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

    public static long parseDecimalLong(String s) {
        long result = 0;

        for (int i = 0; i < s.length(); ++i) {
            result *= 10;
            result += s.charAt(i) - '0';
        }

        return result;
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

    public static long parseHexadecimalLong(Byte[] bytes) {
        return parseHexadecimalLong(bytes, 0, bytes.length);
    }

    public static long parseHexadecimalLong(Byte[] bytes, int start, int length) {
        long result = 0;

        for (int i = start; i < length; ++i) {
            result <<= 4;

            if (bytes[i] >= '0' && bytes[i] <= '9') {
                result += 0xf & (bytes[i] - '0');
            } else if (bytes[i] >= 'a' && bytes[i] <= 'f') {
                // 'W' equals 'a' - 10
                result += 0xf & (bytes[i] - 'W');
            } else {
                return -1;
            }
        }

        return result;
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

    public static long parseDecimalLong(byte[] bytes, int start, int length) {
        long result = 0;

        for (int i = start; i < length; ++i) {
            result *= 10;

            if (bytes[i] - '0' < 0 || bytes[i] - '0' > 9) return -1;

            result += bytes[i] - '0';
        }

        return result;
    }

    public static long parseHexadecimalLong(byte[] bytes) {
        return parseHexadecimalLong(bytes, 0, bytes.length);
    }

    public static long parseHexadecimalLong(byte[] bytes, int start, int length) {
        long result = 0;

        for (int i = start; i < length; ++i) {
            result <<= 4;

            if (bytes[i] >= '0' && bytes[i] <= '9') {
                result += 0xf & (bytes[i] - '0');
            } else if (bytes[i] >= 'a' && bytes[i] <= 'f') {
                // 'W' equals 'a' - 10
                result += 0xf & (bytes[i] - 'W');
            } else {
                return -1;
            }
        }

        return result;
    }
}
