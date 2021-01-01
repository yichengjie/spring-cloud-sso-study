package com.yicj.study.zuul.common;

import org.apache.commons.codec.binary.Base64;
import java.nio.charset.StandardCharsets;

public class CommonUtils {

    public static String base64Encode(String content){
        Base64 base64 = new Base64();
        byte[] textByte = content.getBytes(StandardCharsets.UTF_8);
        return base64.encodeToString(textByte);
    }
}
