package com.maciejwalkowiak.paseq;

/**
 * Exception thrown when task is in invalid state.
 *
 * @author Maciej Walkowiak
 */
public class InvalidTaskDefinitionException extends RuntimeException {
    public InvalidTaskDefinitionException(String s) {
        super(s);
    }
}
