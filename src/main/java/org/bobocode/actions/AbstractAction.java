package org.bobocode.actions;

import org.bobocode.Session;
import org.bobocode.keyType.KeyTypeId;

/**
 * Created by Shelupets Denys on 29.12.2021.
 */
public abstract class AbstractAction {
    protected final Object entity;
    protected final Session session;
    protected final KeyTypeId<?> keyTypeId;

    public AbstractAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        this.entity = entity;
        this.session = session;
        this.keyTypeId = keyTypeId;
    }

    abstract void execute();

}
