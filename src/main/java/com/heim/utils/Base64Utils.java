package com.heim.utils;

import javax.xml.bind.DatatypeConverter;

public class Base64Utils {


    public static String decode(String encoded) {
        byte[] b = DatatypeConverter.parseBase64Binary(encoded);
        String nullTerm = String.valueOf('\0');
        String[] decoded = new String(b).split(nullTerm);


        System.out.println(nullTerm);
        System.out.println(decoded);
        StringBuilder sb = new StringBuilder();
        if (decoded.length > 2) {
            sb.append(decoded[1]).append(":").append(decoded[2]);
            String res = sb.toString();

            //validate user/password if validated return user
            System.out.println(res);
            return decoded[1];
        }
        return null;

    }

    public static void main(String[] args) {
        decode("AGNhbWVsX3Byb2R1Y2VyADEyMw==");
    }
}
