package org.bobocode.actions;

import org.bobocode.Session;
import org.bobocode.keyType.KeyTypeId;

/**
 * Created by Shelupets Denys on 28.12.2021.
 */
public class InsertAction extends AbstractAction {

    public InsertAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        super(entity, session, keyTypeId);
    }

    @Override
    void execute() {
        session.getEntityDao().saveEntity(entity);
    }
}
