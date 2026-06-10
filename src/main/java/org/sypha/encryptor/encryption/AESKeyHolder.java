
package org.sypha.encryptor.encryption;

public class AESKeyHolder {

	private AESKeyHolder() {
	}

	private static final ThreadLocal<String> aesKeyThreadLocal = new ThreadLocal<>();

	public static void setKey(String key) {
		aesKeyThreadLocal.set(key);
	}

	public static String getKey() {
		return aesKeyThreadLocal.get();
	}

	public static void clear() {
		aesKeyThreadLocal.remove();
	}
}
