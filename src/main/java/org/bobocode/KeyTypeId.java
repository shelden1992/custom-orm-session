package org.bobocode;

/**
 * Created by Shelupets Denys on 06.12.2021.
 */
public record KeyTypeId<T>(Class<T> type, Object id) {

    public Class<T> getType() {
        return type;
    }


    public Object getId() {
        return id;
    }
}
