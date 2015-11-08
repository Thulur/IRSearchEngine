package SearchEngine.utils;

import java.math.BigInteger;

/**
 * Created by sebastian on 06.11.2015.
 */
public class IndexEncoder {
    public static String convertToVByte(long input) {
        String binaryString = Long.toBinaryString(input);
        String vByteString = "";

        if (binaryString.length() >= 8) {
            int count = 0;

            for (int i = binaryString.length(); i > 0; i--) {
                ++count;

                if (count == 8) {
                    vByteString = '1' + vByteString;
                    ++i;
                } else if (count % 8 == 0) {
                    vByteString = '0' + vByteString;
                    ++i;
                } else {
                    vByteString = binaryString.charAt(i-1) + vByteString;
                }
            }
        } else {
            int offset = binaryString.length() % 8;

            vByteString = binaryString;

            for (int i = 0; i < 8 - offset - 1; i++) {
                vByteString = '0' + vByteString;
            }

            vByteString = '1' + vByteString;
        }

        String outputString = Long.toHexString(new BigInteger(vByteString, 2).longValue());

        if ((outputString.length() % 2) != 0) {
            outputString = '0' + outputString;
        }

        return outputString;
    }

    public static String refactoredConvertToVByte(long input) {
        long value = input;
        long result = 0;
        int i = 0;

        while (value > 0) {
            result += (value & 127) << (8 * i);
            value>>=7;
            ++i;
        }
        result += 128;

        String outputString = Long.toHexString(result);

        if ((outputString.length() % 2) != 0) {
            outputString = '0' + outputString;
        }

        return outputString;
    }
}
