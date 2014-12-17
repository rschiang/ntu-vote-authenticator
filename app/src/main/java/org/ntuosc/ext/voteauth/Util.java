package org.ntuosc.ext.voteauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.CharBuffer;
import java.util.Map;

public class Util {

    private Util() {
        // This class is static.
    }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String toHexString(byte[] bytes) {
        char[] result = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            result[i * 2] = HEX_CHARS[value >> 4];
            result[i * 2 + 1] = HEX_CHARS[value & 0x0f];
        }
        return new String(result);
    }

    public static String toQueryString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (String key : params.keySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, AppConfig.DEFAULT_ENCODING));
            result.append("=");
            result.append(URLEncoder.encode(params.get(key), AppConfig.DEFAULT_ENCODING));
        }

        return result.toString();
    }

    public static String readToEnd(InputStream stream) throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(stream, AppConfig.DEFAULT_ENCODING));
        String response = Util.readToEnd(reader);
        reader.close();
        stream.close();
        return response;
    }

    public static String readToEnd(Reader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        CharBuffer buffer = CharBuffer.allocate(1024);
        int read;

        while ((read = reader.read(buffer)) != -1) {
            buffer.flip();
            result.append(buffer);
        }

        return result.toString();
    }
}
