package com.vadelic.exmo.market;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Komyshenets on 10.01.2018.
 */
public class ExmoRestApiException extends Exception {
    private int code;

    public ExmoRestApiException(String error) {
        super("[EXMO] " + error);
        initCode(error);

    }

    private void initCode(String error) {
        Pattern p = Pattern.compile("\\A\\w+ (\\d+): ");
        Matcher m = p.matcher(error);

        if (m.find()) {
            code = Integer.parseInt(m.group(1));
        }
    }

    public int getCode() {
        return code;
    }
}
