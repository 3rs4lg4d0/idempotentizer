package com.ersalgado.idempotentizer.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import com.ersalgado.idempotentizer.core.IdempotentizerException;
import com.ersalgado.idempotentizer.core.Repository;

public class PostgresSQLRepositoryTest {

    private static final String DB_NAME = "test";
    private static final String DB_USER = "test";
    private static final String DB_PASSWORD = "test";

    @ClassRule
    public static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER).withPassword(DB_PASSWORD)
            .withCopyFileToContainer(MountableFile.forClasspathResource("/create_table_postgres.sql"),
                    "/docker-entrypoint-initdb.d/")
            .waitingFor(Wait.defaultWaitStrategy());

    private static Repository repository;

    @BeforeClass
    public static void setUp() throws Exception {
        var ds = new PGSimpleDataSource();
        ds.setServerNames(new String[] { "localhost" });
        ds.setDatabaseName(DB_NAME);
        ds.setUser(DB_USER);
        ds.setPassword(DB_PASSWORD);
        ds.setPortNumbers(new int[] { database.getFirstMappedPort() });
        repository = new SQLRepository(ds);
    }

    @Test
    public void givenUUIDAndConsumerId_whenPersistRequestInfo_thenPersisted() {
        var uuid = UUID.randomUUID();
        var consumerId = "consumer1";

        repository.persistRequestInfo(uuid, consumerId);
        var requestInfo = repository.findRequestInfo(uuid, consumerId);

        assertNotNull(requestInfo);
        assertTrue(requestInfo.getProcessed());
        assertEquals(requestInfo.getIdempotencyKey(), uuid);
        assertEquals(requestInfo.getConsumerId(), consumerId);
        assertNotNull(requestInfo.getProcessedAt());
    }

    @Test(expected = IdempotentizerException.class)
    public void givenWrongInput_whenPersistRequestInfo_thenIdempotentizerException() {
        var uuid = UUID.randomUUID();
        String consumerId = null;

        repository.persistRequestInfo(uuid, consumerId);
    }

    @Test
    public void givenNotProcessedRequestInfo_whenFindRequestInfo_thenEmptyRequestInfo() {
        var uuid = UUID.randomUUID();
        var consumerId = "consumer2";

        var requestInfo = repository.findRequestInfo(uuid, consumerId);

        assertNotNull(requestInfo);
        assertFalse(requestInfo.getProcessed());
        assertNull(requestInfo.getIdempotencyKey());
        assertNull(requestInfo.getConsumerId());
        assertNull(requestInfo.getProcessedAt());
    }

    @Test(expected = IdempotentizerException.class)
    public void givenRequestInfoAndUnstableDatasource_whenFindRequestInfo_thenIdempotentizerException() {
        var uuid = UUID.randomUUID();
        String consumerId = "consumer1";

        var ds = new PGSimpleDataSource();
        ds.setServerNames(new String[] { "localhost" });
        ds.setDatabaseName(DB_NAME);
        ds.setUser(DB_USER);
        ds.setPassword("wrongpass");
        ds.setPortNumbers(new int[] { database.getFirstMappedPort() });
        var unstableRepository = new SQLRepository(ds);

        unstableRepository.findRequestInfo(uuid, consumerId);
    }
}
