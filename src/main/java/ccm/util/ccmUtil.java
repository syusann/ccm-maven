package ccm.util;

public class ccmUtil {

	public static String bytesToHex(byte[] bytes) {
		String hex = "";
		for (byte i : bytes) {
			hex += String.format("%02x", i);
		}
		return hex;
	}

	public static byte[] hexToBytes(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			int index = i * 2;
			int j = Integer.parseInt(hex.substring(index, index + 2), 16);
			bytes[i] = (byte) j;
		}
		return bytes;
	}

}
