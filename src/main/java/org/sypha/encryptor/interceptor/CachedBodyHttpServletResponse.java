
package org.sypha.encryptor.interceptor;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	private final ServletOutputStream servletOutputStream;
	private final PrintWriter printWriter;

	public CachedBodyHttpServletResponse(HttpServletResponse response) {
		super(response);
		this.servletOutputStream = new ServletOutputStream() {
			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener listener) {
			}

			@Override
			public void write(int b) throws IOException {
				outputStream.write(b);
			}
		};

		this.printWriter = new PrintWriter(outputStream);
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return servletOutputStream;
	}

	@Override
	public PrintWriter getWriter() {
		return printWriter;
	}

	public byte[] getCapturedResponseBody() {
		printWriter.flush();
		return outputStream.toByteArray();
	}
}
