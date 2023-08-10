package com.besmartexim.dto.request;

import java.io.Serializable;

public enum SearchBy implements Serializable{
    HS_CODE,
    PRODUCT,
    IMPORTER,
    EXPORTER;
    public String getValue() {
        return this.name();
    }
}
