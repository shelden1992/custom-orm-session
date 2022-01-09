package org.bobocode;

import java.util.List;
import java.util.Set;

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

    public static <T> String saveEntityQuery(T entity, Set<String> columnNames) {
        String tableName = getTableName(entity.getClass());

        StringBuilder names = new StringBuilder("(");
        columnNames.forEach(columnName -> names.append(columnName).append(","));
        names.deleteCharAt(names.length() - 1);
        names.append(")");

        StringBuilder values = new StringBuilder("(");
        columnNames.forEach(columnName -> values.append("?").append(","));
        values.deleteCharAt(values.length() - 1);
        values.append(")");

        return String.format("INSERT INTO %s %s VALUES %s", tableName, names, values);

    }

    public static <T> String deleteEntityQuery(T entity) {
        String tableName = getTableName(entity.getClass());
        return String.format("DELETE FROM %s WHERE id = ?", tableName);
    }


}
