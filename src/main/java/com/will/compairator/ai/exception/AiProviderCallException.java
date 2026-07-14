package com.will.compairator.ai.exception;

import org.springframework.web.client.RestClientException;

public class AiProviderCallException extends RuntimeException {
    // Throwable exception to keep the stack trace and in the
    // future it might become something else than a RestClientException
    public AiProviderCallException(String message, Throwable cause) {
        super(message, cause);
    }

}
