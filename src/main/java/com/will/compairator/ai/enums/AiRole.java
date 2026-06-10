package com.will.compairator.ai.enums;

import lombok.Getter;

@Getter
public enum AiRole {

    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String value;

    AiRole(String value) {
        this.value = value;
    }

}
