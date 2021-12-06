package org.bobocode.dao;

import org.apache.log4j.Logger;
import org.bobocode.FieldNameValue;
import org.bobocode.KeyTypeId;
import org.bobocode.exeptions.ReflectionException;
import org.bobocode.exeptions.SqlException;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bobocode.QueryBuilder.selectEntityQuery;
import static org.bobocode.QueryBuilder.updateEntityQuery;
import static org.bobocode.utils.ReflectionUtils.getColumnName;
import static org.bobocode.utils.ReflectionUtils.initialEntity;

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

        try (Connection connection = dataSource.getConnection()) {
            String sql = selectEntityQuery(key.getType());
            LOG.debug("Method selectEntity. Sql query = " + sql);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setObject(1, key.getId());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    Stream.of(key.getType().getDeclaredFields())
                            .forEach(field -> {
                                field.setAccessible(true);
                                setValue(entity, resultSet, field);
                            });
                }

                return entity;

            } catch (SQLException e) {
                LOG.error(String.format("Error preparedStatement. Method selectEntity failed. Entity id = %s", key.getId()));
                throw new SqlException(String.format("Error preparedStatement. Method selectEntity failed. Entity id = %s", key.getId()), e);
            }
        } catch (SQLException e) {
            LOG.error("Error getConnection. Method selectEntity failed.");
            throw new SqlException("Error getConnection. Method selectEntity failed.", e);
        }

    }

    private <T> void setValue(T entity, ResultSet resultSet, Field field) {
        try {
            field.set(entity, resultSet.getObject(getColumnName(field)));
        } catch (SQLException e) {
            LOG.error("Error getObject from ResultSet. Fail field name = " + field.getName());
            throw new SqlException("Error getObject from ResultSet. Fail field name = " + field.getName(), e);
        } catch (IllegalAccessException e) {
            LOG.error("Error setField to instance. Fail field name = " + field.getName());
            throw new ReflectionException("Error setField to instance. Fail field name = " + field.getName(), e);
        }
    }


    @Override
    public <T> void updateEntity(T entity, Object id) {
        //
//        //todo: if ID change compare of snapshotCopy copy do persist;
        try (Connection connection = dataSource.getConnection()) {
            List<FieldNameValue> fieldNameValues = Stream.of(entity.getClass().getDeclaredFields())
                    .peek(field -> field.setAccessible(true))
                    .filter(field -> !getColumnName(field).equalsIgnoreCase("id"))
                    .map(field -> {
                        try {
                            return new FieldNameValue(getColumnName(field), field.get(entity));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                            throw new ReflectionException("Error get value of the field = " + field, e);
                        }
                    }).collect(Collectors.toList());


            String sql = updateEntityQuery(entity, fieldNameValues);
            LOG.debug("Method updateEntity. Sql query =  = " + sql);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                int i = 1;
                for (FieldNameValue fieldNameValue : fieldNameValues) {
                    preparedStatement.setObject(i++, fieldNameValue.value());
                }
                preparedStatement.setObject(i, id);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                LOG.error(String.format("Error preparedStatement. Method updateEntity failed. Entity id = %s", id));
                throw new SqlException(String.format("Error preparedStatement. Method updateEntity failed. Entity id = %s", id));
            }
        } catch (SQLException e) {
            LOG.error("Error getConnection. Method updateEntity failed.");
            throw new SqlException("Error getConnection. Method updateEntity failed.", e);
        }
    }


}
