package org.bobocode.actions;

import org.bobocode.Session;
import org.bobocode.keyType.KeyTypeId;

import static org.bobocode.utils.ReflectionUtils.getIdValue;

/**
 * Created by Shelupets Denys on 28.12.2021.
 */
public class DeleteAction extends AbstractAction {

    public DeleteAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        super(entity, session, keyTypeId);
    }


    void execute() {
        session.getEntityDao().deleteEntity(entity);
        session.getCache().remove(keyTypeId);
        session.removeSnapshot(keyTypeId);
    }
}
