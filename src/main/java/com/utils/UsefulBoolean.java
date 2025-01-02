package com.utils;

import lombok.Getter;
import lombok.Setter;

public class UsefulBoolean {
    @Getter @Setter
    private boolean ok;

    @Getter @Setter
    private String message;

    public UsefulBoolean(final boolean ok, final String message) {
        this.ok = ok;
        this.message = message;
    }

    public UsefulBoolean() {
        this.ok = true;
    }

}
