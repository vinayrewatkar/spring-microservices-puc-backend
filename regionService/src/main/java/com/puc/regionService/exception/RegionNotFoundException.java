package com.puc.regionService.exception;

public class RegionNotFoundException extends RuntimeException {
    public RegionNotFoundException(String message) {
        super(message);
    }
}
