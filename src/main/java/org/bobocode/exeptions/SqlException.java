package org.bobocode.exeptions;

/**
 * Created by Shelupets Denys on 07.12.2021.
 */
public class SqlException extends RuntimeException {

    public SqlException(String message) {
        super(message);
    }

    public SqlException(String message, Throwable cause) {
        super(message, cause);
    }
}
