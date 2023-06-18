package com.duberlyguarnizo.dummyjson.exceptions;

/**
 * Thrown when trying to access a JSON content that is not owned by the user.
 */
public class ForbiddenActionException extends RuntimeException {
    public ForbiddenActionException(String message) {
        super(message);
    }
}
