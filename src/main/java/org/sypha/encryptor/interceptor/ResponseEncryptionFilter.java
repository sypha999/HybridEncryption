
package org.sypha.encryptor.interceptor;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.sypha.encryptor.encryption.AESKeyHolder;
import org.sypha.encryptor.encryption.AESUtil;
import org.sypha.encryptor.exception.EncryptorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Component
@ConditionalOnProperty(name = "use.encryption", havingValue = "true")
@Slf4j
public class ResponseEncryptionFilter implements Filter {

	private static final List<String> exemptedUri = List.of("");

	private static final List<String> patternMatch = List.of("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/api-docs/**");

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;

		String uri = httpRequest.getRequestURI();

		if (exemptedUri.contains(uri) || matches(uri)) {
			chain.doFilter(request, response);
			return;
		}

		CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(httpResponse);

		chain.doFilter(request, wrappedResponse);

		byte[] responseBodyBytes = wrappedResponse.getCapturedResponseBody();
		String plainBody = new String(responseBodyBytes, StandardCharsets.UTF_8);
		if (plainBody.isEmpty()) {
			return;
		}
		String encryptedData;
		try {
			encryptedData = AESUtil.encrypt(plainBody, AESKeyHolder.getKey());
		} catch (Exception e) {
			throw new EncryptorException("There was a problem encrypting the response string");
		}

		String finalResponseJson = String.format("""
				{
				    "data": "%s"
				}
				""", encryptedData);
		httpResponse.setContentLength(finalResponseJson.getBytes().length);
		httpResponse.getOutputStream().write(finalResponseJson.getBytes());
		httpResponse.setContentType("application/json");
		AESKeyHolder.clear();

	}

	private boolean matches(String pattern) {
		for (String s : ResponseEncryptionFilter.patternMatch) {
			if (pattern.contains(s.replaceAll("[*]", ""))) {
				return true;
			}
		}
		return false;
	}
}
