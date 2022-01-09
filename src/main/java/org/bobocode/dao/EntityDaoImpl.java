package org.bobocode.dao;

import org.apache.log4j.Logger;
import org.bobocode.FieldNameValue;
import org.bobocode.exeptions.SqlException;
import org.bobocode.keyType.KeyTypeId;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bobocode.QueryBuilder.*;
import static org.bobocode.utils.ReflectionUtils.*;

/**
 * Created by Shelupets Denys on 12.12.2021.
 */
public class EntityDaoImpl implements EntityDao {
    private final static Logger LOG = Logger.getLogger(EntityDaoImpl.class);
    private final DataSource dataSource;

    public EntityDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public <T> T selectEntity(KeyTypeId<T> key) {
        T entity = initialEntity(key.getType());
        String sql = selectEntityQuery(key.getType());
        LOG.debug("Method selectEntity. Sql query = " + sql);

        T finalEntity = entity;
        entity = executeQueryPs(preparedStatement -> {
            try {
                preparedStatement.setObject(1, key.getId());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Stream.of(key.getType().getDeclaredFields())
                            .forEach(field -> {
                                Object resultSetValue = getResultSetValue(resultSet, field);
                                setValue(field, resultSetValue, finalEntity);
                            });
                    return finalEntity;
                }
                return null;
            } catch (SQLException e) {
                LOG.error(String.format("Error preparedStatement. Method selectEntity failed. Entity id = %s", key.getId()));
                throw new SqlException(String.format("Error preparedStatement. Method selectEntity failed. Entity id = %s", key.getId()), e);
            }
        }, sql);

        return entity;
    }

    @Override
    public <T> void saveEntity(T entity) {
        Map<String, Object> columnNameValues = getColumnNameValues(entity);

        String sql = saveEntityQuery(entity, columnNameValues.keySet());
        LOG.debug("Method saveEntity. Sql query = " + sql);

        executeUpdatePs(preparedStatement -> {
            AtomicInteger atomicInteger = new AtomicInteger(1);
            columnNameValues.values().forEach(value -> {
                try {
                    preparedStatement.setObject(atomicInteger.getAndIncrement(), value);
                } catch (SQLException e) {
                    LOG.error(String.format("Error setValue %s to preparedStatement ", value));
                    throw new SqlException(String.format("Error setValue to preparedStatement %s", entity), e);
                }
            });

            try {
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                LOG.error("Error saveEntity. Failed executeUpdate preparedStatement.");
                throw new SqlException("Error saveEntity. Failed executeUpdate preparedStatement.", e);
            }
        }, sql);

    }

    @Override
    public <T> void deleteEntity(T entity) {
        String sql = deleteEntityQuery(entity);
        LOG.debug("Method deleteEntity. Sql query = " + sql);

        executeUpdatePs(preparedStatement ->
        {
            try {
                preparedStatement.setObject(1, getIdValue(entity));
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                LOG.error("Error deleteEntity");
                throw new SqlException("Error deleteEntity", e);
            }
        }, sql);

    }

    private Object getResultSetValue(final ResultSet resultSet, final Field field) {
        try {
            return resultSet.getObject(getColumnName(field));
        } catch (SQLException e) {
            LOG.error("Error getObject from ResultSet. Fail field name = " + field.getName());
            throw new SqlException("Error getObject from ResultSet. Fail field name = " + field.getName(), e);
        }
    }


    @Override
    public <T> void updateEntity(T entity, Object id) {
        List<FieldNameValue> fieldNameValues = Stream.of(entity.getClass().getDeclaredFields())
                .peek(field -> field.setAccessible(true))
                .filter(field -> !getColumnName(field).equalsIgnoreCase("id"))
                .map(field -> new FieldNameValue(getColumnName(field), getValue(field, entity))).collect(Collectors.toList());

        String sql = updateEntityQuery(entity, fieldNameValues);
        LOG.debug("Method updateEntity. Sql query =  = " + sql);

        executeUpdatePs(preparedStatement -> {
            int i = 1;
            try {
                for (FieldNameValue fieldNameValue : fieldNameValues) {
                    preparedStatement.setObject(i++, fieldNameValue.value());
                }
                preparedStatement.setObject(i, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, sql);

    }

    private void executeUpdatePs(Consumer<PreparedStatement> consumer, String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                consumer.accept(preparedStatement);
            } catch (SQLException e) {
                LOG.error("Error executeUpdatePs.");
                throw new SqlException("Error executeUpdatePs.", e);
            }

        } catch (SQLException e) {
            LOG.error("Error getConnection. Method executeUpdatePs failed.");
            throw new SqlException("Error getConnection. Method executeUpdatePs failed.", e);
        }
    }

    private <T> T executeQueryPs(Function<PreparedStatement, T> function, String sqlQuery) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
                return function.apply(preparedStatement);
            } catch (SQLException e) {
                LOG.error("Error preparedStatement. Method executeQueryPs failed.");
                throw new SqlException("Error preparedStatement. Method executeQueryPs failed.", e);
            }

        } catch (SQLException e) {
            LOG.error("Error getConnection. Method executeQueryPs failed.");
            throw new SqlException("Error getConnection. Method executeQueryPs failed.", e);
        }
    }

}
