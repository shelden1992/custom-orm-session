package org.bobocode;

import javax.sql.DataSource;

/**
 * Created by Shelupets Denys on 05.12.2021.
 */
public class SessionFactory {
    private final DataSource dataSource;

    public SessionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Session createSession() {
        return new Session(dataSource);
    }
}
