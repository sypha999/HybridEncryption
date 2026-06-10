
package org.sypha.encryptor.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.sypha.encryptor.encryption.AESKeyHolder;
import org.sypha.encryptor.encryption.AESUtil;
import org.sypha.encryptor.encryption.RSAKeyHolder;
import org.sypha.encryptor.encryption.RSAUtil;
import org.sypha.encryptor.exception.EncryptorException;
import org.sypha.encryptor.exception.handler.ExceptionResponse;
import org.sypha.encryptor.util.JsonUtil;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.time.LocalDate;


@Component
@ConditionalOnProperty(name = "use.encryption", havingValue = "true")
@Slf4j
public class RequestWrapperFilter implements Filter {

	private static final String[] AUTH_WHITELIST = {""};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (checkUrlIsWhiteListed(httpRequest)) {
			chain.doFilter(request, response);
			return;
		}

		String key = httpRequest.getHeader("key");
		RSAKeyHolder.setKey(key);


		String encryptedBody = new String(httpRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

		if (key == null) {
			handleException(httpResponse, new EncryptorException("Missing required header 'key'"));
			return;
		}
		if (encryptedBody.trim().isEmpty()) {
			saveAES(key);
			chain.doFilter(request, response);
			return;
		}
		String decryptedBody;

		try {
			decryptedBody = decrypt(encryptedBody, key, httpResponse);

		} catch (Exception e) {
			handleException(httpResponse, new EncryptorException("Could not decrypt request because " + e.getMessage()));
			return;
		}

		CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest, decryptedBody);
		chain.doFilter(wrappedRequest, response);
	}

	public void saveAES(String key) {
		try {
			String aesKey = RSAUtil.decrypt(key, RSAUtil.loadPrivateKey());
			AESKeyHolder.setKey(aesKey);
		} catch (Exception e) {
			throw new EncryptorException( "error processing your request");
		}
	}

	private String decrypt(String encryptedBody, String key, HttpServletResponse response)
			throws IOException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException,
			NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, ParseException {
		org.json.simple.JSONObject object = (JSONObject) new JSONParser().parse(encryptedBody);
		try {
			key = RSAUtil.decrypt(key, RSAUtil.loadPrivateKey());
		} catch (Exception e) {
			handleException(response, new EncryptorException("Key is of an invalid format or was signed with the wrong public key"));
		}

		String data = object.get("data").toString().trim();
		AESKeyHolder.setKey(key);
		return AESUtil.decrypt(data, key);
	}

	private boolean checkUrlIsWhiteListed(HttpServletRequest httpRequest) {
		String url = httpRequest.getRequestURI();
		for (String whiteList : AUTH_WHITELIST) {
			if (url.contains(whiteList)) {
				return true;
			}
		}
		return false;
	}

	private void handleException(HttpServletResponse response, Exception ex) throws IOException {
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.getWriter().write(JsonUtil.objToJsonStringMapper(new ExceptionResponse(Date.valueOf(LocalDate.now()),
				ex.getMessage(), "/**", "error.msg.invalid.request")));
		response.setContentType("application/json");
		response.getWriter().flush();
	}
}
