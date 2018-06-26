package com.heim.utils;

import com.heim.models.UserCredentials;

import javax.xml.bind.DatatypeConverter;

public class Base64Utils {

    public static UserCredentials decode(String encoded) {
        byte[] b = DatatypeConverter.parseBase64Binary(encoded);
        String nullTerm = String.valueOf('\0');
        String[] decoded = new String(b).split(nullTerm);
        UserCredentials userCredentials = null;
        if (decoded.length > 2) {
            userCredentials = new UserCredentials();
            userCredentials.setUsername(decoded[1]);
            userCredentials.setPassword(decoded[2]);
        }
        return userCredentials;
    }

//   // public static void main(String[] args) {
//        decode("AGNhbWVsX3Byb2R1Y2VyADEyMw==");
//    }
}
