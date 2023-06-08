package com.duberlyguarnizo.dummyjson.exceptions;

import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class CustomAppException extends RuntimeException {
    public CustomAppException(String origin, Throwable cause) {
        super("Origin: " + origin, cause);
        String errorMessage = "There was an error trying to configure a part of the system: "
                + origin
                + ". Detail: "
                + cause.getMessage();
        log.log(Level.WARNING, errorMessage);
    }
}
