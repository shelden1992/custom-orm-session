package org.bobocode.dao;

import org.bobocode.keyType.KeyTypeId;

/**
 * Created by Shelupets Denys on 12.12.2021.
 */
public interface EntityDao {

    <T> void updateEntity(T entity, Object id);

    <T> T selectEntity(KeyTypeId<T> key);

    <T> void saveEntity(T entity);

    <T> void deleteEntity(T entity);
}
