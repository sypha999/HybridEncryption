
package org.sypha.encryptor.encryption;

public class RSAKeyHolder {
	private RSAKeyHolder() {
	}

	private static final ThreadLocal<String> rsaKeyThreadLocal = new ThreadLocal<>();

	public static void setKey(String key) {
		rsaKeyThreadLocal.set(key);
	}

	public static String getKey() {
		return rsaKeyThreadLocal.get();
	}

	public static void clear() {
		rsaKeyThreadLocal.remove();
	}
}
