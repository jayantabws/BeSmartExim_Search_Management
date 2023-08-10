package com.besmartexim.dto.request;

import java.io.Serializable;

public enum SearchType implements Serializable{
    TRADE,
    IN_DEPTH,
    MACRO;
    public String getValue() {
        return this.name();
    }
}
