package org.bobocode;

import java.util.List;

import static org.bobocode.utils.ReflectionUtils.getTableName;

/**
 * Created by Shelupets Denys on 07.12.2021.
 */
public class QueryBuilder {

    public static <T> String selectEntityQuery(Class<T> type) {
        return String.format("SELECT * FROM %s WHERE id = ?", getTableName(type));
    }

    public static <T> String updateEntityQuery(T entity, List<FieldNameValue> columnNames) {
        String tableName = getTableName(entity.getClass());
        StringBuilder stringBuilder = new StringBuilder();
        columnNames.stream().map(FieldNameValue::name).forEach(name -> stringBuilder.append(name).append("= ?").append(","));
        StringBuilder arguments = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return String.format("UPDATE %s SET %s WHERE id = ?", tableName, arguments);
    }


}
