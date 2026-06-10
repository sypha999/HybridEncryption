
package org.sypha.encryptor.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESUtil {

	private AESUtil() {
	}

	private static final IvParameterSpec IV_SPEC = new IvParameterSpec(
			"0000000000000000".getBytes(StandardCharsets.UTF_8));

	public static String decrypt(String base64Cipher, String base64Key)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] cipherBytes = Base64.getDecoder().decode(base64Cipher);
		byte[] keyBytes = Base64.getDecoder().decode(base64Key);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec, IV_SPEC);

		byte[] decrypted = cipher.doFinal(cipherBytes);
		return new String(decrypted, StandardCharsets.UTF_8);
	}

	public static String encrypt(String plainText, String base64Key)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] keyBytes = Base64.getDecoder().decode(base64Key);
		SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, IV_SPEC);

		byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(encrypted);
	}
}
