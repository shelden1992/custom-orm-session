package org.bobocode.actions;

import org.bobocode.Session;
import org.bobocode.keyType.KeyTypeId;

import static org.bobocode.utils.ReflectionUtils.getIdValue;

/**
 * Created by Shelupets Denys on 28.12.2021.
 */
public class UpdateAction extends AbstractAction {
    public UpdateAction(Object entity, Session session, KeyTypeId<?> keyTypeId) {
        super(entity, session, keyTypeId);
    }

    @Override
    void execute() {
        session.getEntityDao().updateEntity(entity, keyTypeId.getId());
        session.getCache().put(keyTypeId, entity);
        session.addSnapshot(keyTypeId, entity);
    }
}
