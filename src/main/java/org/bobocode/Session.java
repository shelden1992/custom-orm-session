package org.bobocode;

import org.apache.log4j.Logger;
import org.bobocode.actions.ActionQuery;
import org.bobocode.actions.DeleteAction;
import org.bobocode.actions.InsertAction;
import org.bobocode.actions.UpdateAction;
import org.bobocode.dao.EntityDao;
import org.bobocode.dao.EntityDaoImpl;
import org.bobocode.exeptions.OrmException;
import org.bobocode.keyType.KeyTypeId;
import org.bobocode.utils.Utils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.bobocode.utils.ReflectionUtils.getIdValue;
import static org.bobocode.utils.ReflectionUtils.getValue;

/**
 * Created by Shelupets Denys on 05.12.2021.
 */

public class Session {
    private final static Logger LOG = Logger.getLogger(Session.class);
    private final EntityDao entityDao;
    private final Map<KeyTypeId<?>, Object> cache = new ConcurrentHashMap<>();
    private final Map<KeyTypeId<?>, Object[]> snapshotCopy = new ConcurrentHashMap<>();
    private final ActionQuery queryAction;
    private boolean isClosed = false;

    public Session(DataSource dataSource) {
        this.entityDao = new EntityDaoImpl(dataSource);
        this.queryAction = new ActionQuery();
    }

    public Map<KeyTypeId<?>, Object> getCache() {
        return cache;
    }

    public EntityDao getEntityDao() {
        return entityDao;
    }

    public <T> T find(Class<T> entityType, Object id) {
        checkSession();
        KeyTypeId<T> key = new KeyTypeId<>(entityType, id);

        Optional<T> entity = Optional.ofNullable(cache.get(key)).map(entityType::cast);

        if (entity.isPresent()) {
            return entity.get();
        }

        T findEntity = entityDao.selectEntity(key);

        Optional.ofNullable(findEntity)
                .ifPresent(findEnt -> {
                    cache.put(key, findEnt);
                    addSnapshot(key, findEnt);
                });

        return findEntity;
    }

    public <T> void persist(T entity) {
        checkSession();
        Objects.requireNonNull(entity);

        if (getKeyTypeId(entity) != null) return;

        KeyTypeId<?> keyTypeId = new KeyTypeId<>(entity.getClass(), getIdValue(entity));
        cache.put(keyTypeId, entity);
        addSnapshot(keyTypeId, entity);

        addInsertAction(entity, keyTypeId);
    }

    public <T> void remove(T entity) {
        checkSession();
        Objects.requireNonNull(entity);
        KeyTypeId<?> keyTypeId = Optional.ofNullable(getKeyTypeId(entity)).orElseThrow(() -> new OrmException("Entity not persist."));
        addDeleteAction(entity, keyTypeId);
    }

    public void close() {
        checkSession();
        checkToChangeAndAddToUpdateAction();
        flush();
        isClosed = true;
        queryAction.clear();
    }

    public void flush() {
        checkSession();
        queryAction.execute();
    }

    public <T> void update(T entity) {
        checkSession();
        Objects.requireNonNull(entity);

        KeyTypeId<?> keyTypeId = getKeyTypeId(entity);

        if (keyTypeId == null) {
            persist(entity);
        } else {
            addUpdateAction(entity, keyTypeId);
        }
    }

    public <T> void removeSnapshot(KeyTypeId<?> key) {
        snapshotCopy.remove(key);
    }

    private void checkSession() {
        if (isClosed) throw new OrmException("Session is closed");
    }

    private void updateSnapshotValues(Map.Entry<KeyTypeId<?>, Object> cacheKeyEntities) {
        checkSession();

        Objects.requireNonNull(cacheKeyEntities.getValue());

        Object idValue = getIdValue(cacheKeyEntities.getValue());

        if (!Objects.equals(idValue, cacheKeyEntities.getKey().id())) {
            persist(cacheKeyEntities.getValue());
        } else {
            addUpdateAction(cacheKeyEntities.getValue(), cacheKeyEntities.getKey());
        }
    }

    private boolean hasChanged(Map.Entry<KeyTypeId<?>, Object> keyTypeIdObjectEntry) {
        return Utils.hasChanged(keyTypeIdObjectEntry, snapshotCopy);
    }

    public <T> void addSnapshot(KeyTypeId<?> key, T entity) {
        Object[] fields = Stream.of(key.getType().getDeclaredFields()).sorted(comparing(Field::getName)).map(field -> getValue(field, entity)).toArray();
        snapshotCopy.put(key, fields);
    }

    private <T> void addDeleteAction(T entity, KeyTypeId<?> keyTypeId) {
        queryAction.addDeleteAction(new DeleteAction(entity, this, keyTypeId));
    }

    private <T> void addInsertAction(T entity, KeyTypeId<?> keyTypeId) {
        queryAction.addInsertAction(new InsertAction(entity, this, keyTypeId));
    }

    private <T> void addUpdateAction(T entity, KeyTypeId<?> keyTypeId) {
        queryAction.addUpdateAction(new UpdateAction(entity, this, keyTypeId));
    }

    private <T> KeyTypeId<?> getKeyTypeId(T entity) {
        return cache.entrySet().stream().filter(entry -> entry.getValue().equals(entity)).map(Map.Entry::getKey).findFirst().orElse(null);
    }

    private void checkToChangeAndAddToUpdateAction() {
        cache.entrySet()
                .stream()
                .filter(this::hasChanged)
                .forEach(this::updateSnapshotValues);
    }

}
