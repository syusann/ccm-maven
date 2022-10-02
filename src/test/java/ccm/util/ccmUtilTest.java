package ccm.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.junit.Test;

public class ccmUtilTest {

	@Test
	public void bytesToHex() {
		System.out.println(ccmUtil.bytesToHex(new byte[] { (byte) 0xE0, (byte) 0xAD }));
		System.out.println(ccmUtil.bytesToHex(new byte[] { -32, -83 }));

		System.out.println(ccmUtil.bytesToHex(new byte[] { (byte) 0x58, (byte) 0x03 }));
		System.out.println(ccmUtil.bytesToHex(new byte[] { 88, 3 }));
		System.out.println(new String((new byte[] { (byte) 0x58, (byte) 0x03 }), StandardCharsets.UTF_16));
		System.out.println(new String((new byte[] { 88, 3 }), StandardCharsets.UTF_16));
		System.out.println(new String((new byte[] { (byte) 0xE5, (byte) 0xA0, (byte) 0x83 }), StandardCharsets.UTF_8));
		System.out.println(new String((new byte[] { -27, -96, -125 }), StandardCharsets.UTF_8));
		System.out.println(new String(new int[] { (int) 22531 }, 0, 1));
	}

	@Test
	public void hexToBytes() {
		byte[] bytes = ccmUtil.hexToBytes("E5A083");
		System.out.print("Byte Array : ");
		for (int i = 0; i < bytes.length; i++)
			System.out.print(bytes[i] + "\t");
	}

	@Test
	public void test() throws ParseException, UnsupportedEncodingException {
		display("\uE0AD"); // 堃(造字）
		display("\u5803"); // 堃
		display("\u4E2D"); // 中
		display("\uD865\uDDE9"); // 𩗩 (金風)
		display("\u56FD"); // 国
	}

	public void display(String unicodeString) throws ParseException, UnsupportedEncodingException {

		System.out.println("[" + unicodeString + "]" + unicodeString.codePointAt(0) + "---------------------");
		// convert Unicode to UTF8 format
		byte[] utf8Bytes = unicodeString.getBytes(Charset.forName("UTF-8"));
		System.out.println("UTF-8 :" + ccmUtil.bytesToHex(utf8Bytes));

		byte[] utf16Bytes = unicodeString.getBytes(Charset.forName("UTF-16"));
		System.out.println("UTF-16:" + ccmUtil.bytesToHex(utf16Bytes));

		byte[] big5Bytes = unicodeString.getBytes(Charset.forName("BIG5"));
		System.out.println("BIG5  :" + ccmUtil.bytesToHex(big5Bytes));

		byte[] big5HKBytes = unicodeString.getBytes(Charset.forName("BIG5-HKSCS"));
		System.out.println("BIG5HK:" + ccmUtil.bytesToHex(big5HKBytes));

		byte[] cp950Bytes = unicodeString.getBytes(Charset.forName("CP950"));
		System.out.println("CP950 :" + ccmUtil.bytesToHex(cp950Bytes));

		byte[] cp937Bytes = unicodeString.getBytes(Charset.forName("CP937"));
		System.out.println("CP937 :" + ccmUtil.bytesToHex(cp937Bytes));

		byte[] gbkBytes = unicodeString.getBytes(Charset.forName("GBK"));
		System.out.println("GBK   :" + ccmUtil.bytesToHex(gbkBytes));

		byte[] gb2312Bytes = unicodeString.getBytes(Charset.forName("GB2312"));
		System.out.println("GB2312 :" + ccmUtil.bytesToHex(gb2312Bytes));

		System.out.println();
	}
}