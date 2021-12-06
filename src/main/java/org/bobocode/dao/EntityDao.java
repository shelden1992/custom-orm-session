package org.bobocode.dao;

import org.bobocode.KeyTypeId;

/**
 * Created by Shelupets Denys on 12.12.2021.
 */
public interface EntityDao {

    <T> void updateEntity(T entity, Object id);

    <T> T selectEntity(KeyTypeId<T> key);
}
