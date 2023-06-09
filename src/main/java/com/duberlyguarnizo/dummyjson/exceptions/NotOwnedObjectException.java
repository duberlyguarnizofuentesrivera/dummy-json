package com.duberlyguarnizo.dummyjson.exceptions;

/**
 * Thrown when trying to access a JSON content that is not owned by the user.
 */
public class NotOwnedObjectException extends RuntimeException {
    public NotOwnedObjectException(String message) {
        super(message);
    }
}
