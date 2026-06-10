
package org.sypha.encryptor.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.sypha.encryptor.exception.EncryptorException;

import java.util.*;

@ControllerAdvice
@Slf4j
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(EncryptorException.class)
	public final ResponseEntity<ExceptionResponse> handleAllExceptions(EncryptorException ex, WebRequest request) {

		log.error("Exception occurred while making a call to {} with reason: \n {}", request.getDescription(false),
				ex.getMessage());
		ExceptionResponse exceptionResponse = new ExceptionResponse(new Date(),
				ex.getMessage(),
				request.getDescription(false));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}


	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleResponseStatusException(Exception ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("message", ex.getMessage());
		return new ResponseEntity<>(body,HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
