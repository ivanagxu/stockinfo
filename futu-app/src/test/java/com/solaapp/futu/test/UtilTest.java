package com.solaapp.futu.test;

import static org.junit.jupiter.api.Assertions.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

class MD5Util {
    static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static String bytes2Hex(byte[] arr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : arr) {
            sb.append(hexChars[(b >>> 4) & 0xF]);
            sb.append(hexChars[b & 0xF]);
        }
        return sb.toString();
    }

    static String calcMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(str.getBytes());
            return bytes2Hex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
class UtilTest {

	

	
	@Test
	void test() {
		fail("Not yet implemented");
	}
	
	@Test
	void testMD5() {
		System.out.println(MD5Util.calcMD5("29eb0c5e3fbca6fe836ac64e961ca267"));
	}

}
