package org.bobocode;

import org.bobocode.entity.Person;
import org.bobocode.exeptions.SqlException;
import org.bobocode.util.FileReader;
import org.junit.jupiter.api.BeforeEach;
import org.postgresql.ds.PGSimpleDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionTest {
    Session session;

    @BeforeEach
    void init() {
        doInHibernateSession(em -> {
            String create = FileReader.readWholeFileFromResources("META-INF/sql_scripts/create.sql");
            String populate = FileReader.readWholeFileFromResources("META-INF/sql_scripts/populate.sql");
            em.createNativeQuery(create).executeUpdate();
            em.createNativeQuery(populate).executeUpdate();
        });

        session = initNewSession();
    }

    private void doInHibernateSession(Consumer<EntityManager> entityManagerConsumer) {
        EntityManagerFactory managerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = managerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        entityManagerConsumer.accept(entityManager);
        entityManager.getTransaction().commit();
    }

    private Session initNewSession() {
        PGSimpleDataSource pgSimpleDataSource = new PGSimpleDataSource();
        pgSimpleDataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        pgSimpleDataSource.setUser("postgres");
        pgSimpleDataSource.setPassword("postgres");
        return new Session(pgSimpleDataSource);
    }

    @org.junit.jupiter.api.Test
    void shouldSelectPersonFromDbWhenValidId() {
        Person person = session.find(Person.class, 1);
        assertThat(person, is(notNullValue()));
        assertThat(person.getId(), is(1));
    }

    @org.junit.jupiter.api.Test
    void shouldThrowSqlExceptionWhenIdNotValid() {
        assertThrows(SqlException.class, () -> session.find(Person.class, 2));
    }

    @org.junit.jupiter.api.Test
    void shouldUpdatePersonIntoDbWhenCloseSession() {
        Person person = session.find(Person.class, 1);
        person.setFirstName("Change First Name");
        person.setSecondName("Change Second Name");

        session.close();

        Session session = initNewSession();

        Person changePerson = session.find(Person.class, 1);

        assertNotNull(changePerson);
        assertThat(changePerson.getId(), is(1));
        assertThat(changePerson.getFirstName(), is(equalTo("Change First Name")));
        assertThat(changePerson.getSecondName(), is(equalTo("Change Second Name")));

    }


}