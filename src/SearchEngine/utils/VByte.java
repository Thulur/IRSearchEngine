package SearchEngine.utils;

/**
 * Created by sebastian on 06.11.2015.
 */
public class VByte {
    public static String encode(long input) {
        long result = 0;
        int i = 0;

        while (input > 0) {
            result += (input & 127) << (8 * i);
            input>>=7;
            ++i;
        }
        result += 128;

        String outputString = Long.toHexString(result);

        if ((outputString.length() % 2) != 0) {
            outputString = '0' + outputString;
        }

        return outputString;
    }

    public static long decode(long vByteValue) {
        long result = 0;
        int i = 0;

        while (vByteValue > 0) {
            result += ((vByteValue & 127) << (7 * i));
            vByteValue >>= 8;
            ++i;
        }

        return result;
    }
}
