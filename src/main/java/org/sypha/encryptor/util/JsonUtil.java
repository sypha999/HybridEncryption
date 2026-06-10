
package org.sypha.encryptor.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.sypha.encryptor.exception.EncryptorException;

import java.io.IOException;

@Slf4j
public class JsonUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
	}

	private JsonUtil() {
	}

	public static <T> T convertJsonBodyToObject(String jsonBody, Class<T> clazz) throws RuntimeException {
		if (StringUtils.isEmpty(jsonBody)) {
			throw new EncryptorException("Received empty response body");
		}
		try {
			return OBJECT_MAPPER.readValue(jsonBody, clazz);
		} catch (IOException e) {
			log.error("Error while de-serializing json body", e);
			throw new EncryptorException("Could not read response body because of " + e.getMessage());
		}
	}

	public static String objToJsonStringMapper(final Object obj) {
		try {
			return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).findAndRegisterModules()
					.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new EncryptorException(e.getMessage());
		}
	}
}
