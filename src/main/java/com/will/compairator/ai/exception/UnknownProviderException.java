package com.will.compairator.ai.exception;

import org.apache.coyote.BadRequestException;

public class UnknownProviderException extends BadRequestException {

    public UnknownProviderException(String message) {
        super(message);
    }

}
