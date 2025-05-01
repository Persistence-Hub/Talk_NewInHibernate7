package com.thorben.janssen.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(String street, String city, String state, String zip) {
}
