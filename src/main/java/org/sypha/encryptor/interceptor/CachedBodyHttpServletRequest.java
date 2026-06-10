
package org.sypha.encryptor.interceptor;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

	private final byte[] cachedBody;

	public CachedBodyHttpServletRequest(HttpServletRequest request, String overrideBody) {
		super(request);
		this.cachedBody = overrideBody.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);

		return new ServletInputStream() {
			public int read() {
				return byteArrayInputStream.read();
			}

			public boolean isFinished() {
				return byteArrayInputStream.available() == 0;
			}

			public boolean isReady() {
				return true;
			}

			public void setReadListener(ReadListener readListener) {
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}
}
