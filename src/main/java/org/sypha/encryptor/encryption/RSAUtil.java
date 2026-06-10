
package org.sypha.encryptor.encryption;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource.PSpecified;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.Base64;

@Component
@Slf4j
public class RSAUtil {

	@Value("${private.key}")
	private String keyContent;

	private static String CONTENT;

	private RSAUtil() {
	}

	@PostConstruct
	public void init() {
		CONTENT = keyContent;
	}

	public static String decrypt(String encryptedBase64AESKey, PrivateKey privateKey)
			throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException,
			InvalidKeyException, InvalidAlgorithmParameterException {

		Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");

		OAEPParameterSpec oaepParameterSpec = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"),
				PSpecified.DEFAULT);

		cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParameterSpec);
		byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64AESKey));
		return new String(decrypted);
	}

	public static PrivateKey loadPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] decodedBytes = Base64.getDecoder().decode(CONTENT);

		Path keyPath = Paths.get("key.pem");
		File file = new File(keyPath.toString());

		if (Files.notExists(keyPath) || file.length() == 0) {
			Files.write(keyPath, decodedBytes);
		}

		try (PemReader pemReader = new PemReader(new FileReader("key.pem"))) {
			byte[] content = pemReader.readPemObject().getContent();
			RSAPrivateKey rsa = RSAPrivateKey.getInstance(ASN1Sequence.getInstance(content));
			RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(rsa.getModulus(), rsa.getPublicExponent(),
					rsa.getPrivateExponent(), rsa.getPrime1(), rsa.getPrime2(), rsa.getExponent1(), rsa.getExponent2(),
					rsa.getCoefficient());

			PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
			PKCS8EncodedKeySpec pkcs8Spec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
			return KeyFactory.getInstance("RSA").generatePrivate(pkcs8Spec);
		}
	}
}
