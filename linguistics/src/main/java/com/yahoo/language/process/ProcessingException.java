// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.language.process;

/**
 * Exception class indicating that a fatal error occured during linguistic processing.
 *
 * @author Simon Thoresen Hult
 */
public class ProcessingException extends RuntimeException {

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
