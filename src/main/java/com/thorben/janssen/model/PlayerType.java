package com.thorben.janssen.model;

import jakarta.persistence.EnumeratedValue;

public enum PlayerType {
    Hobby("H"), Professional("P");

    @EnumeratedValue
    final String type;

    PlayerType(String type) {
        this.type = type;
    }
}
