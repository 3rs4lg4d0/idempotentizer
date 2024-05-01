package com.ersalgado.idempotentizer.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.ersalgado.idempotentizer.core.IdempotentizerException;
import com.ersalgado.idempotentizer.core.JacksonObjectSerde;
import com.ersalgado.idempotentizer.core.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public class PostgresSQLRepositoryTest {

    private static final String DB_NAME = "test";
    private static final String DB_USER = "test";
    private static final String DB_PASSWORD = "test";

    @ClassRule
    public static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASSWORD)
            .withCopyFileToContainer(
                    MountableFile.forClasspathResource("/create_table_postgres.sql"), "/docker-entrypoint-initdb.d/")
            .waitingFor(Wait.defaultWaitStrategy());

    private static DataSource datasource;
    private static Repository repository;

    @BeforeClass
    public static void setUp() throws Exception {
        var ds = new PGSimpleDataSource();
        ds.setServerNames(new String[] {"localhost"});
        ds.setDatabaseName(DB_NAME);
        ds.setUser(DB_USER);
        ds.setPassword(DB_PASSWORD);
        ds.setPortNumbers(new int[] {database.getFirstMappedPort()});

        repository = new SQLRepository(ds, new JacksonObjectSerde());
        datasource = ds;
    }

    @Test
    public void givenValidInputData_whenPersistRequestInfo_thenPersisted() {
        var uuid = UUID.randomUUID();
        var consumerId = "consumer1";
        var returnedValue = new UserDto(7, "fakeUser", Instant.now());

        repository.persistRequestInfo(uuid, consumerId, returnedValue);
        var requestInfo = repository.findRequestInfo(uuid, consumerId);

        assertNotNull(requestInfo);
        assertTrue(requestInfo.getProcessed());
        assertEquals(requestInfo.getIdempotencyKey(), uuid);
        assertEquals(requestInfo.getConsumerId(), consumerId);
        assertNotNull(requestInfo.getProcessedAt());
        assertEquals(returnedValue, requestInfo.getReturnedValue());
    }

    @Test
    public void givenValidInputDataWithoutReturnedValue_whenPersistRequestInfo_thenPersisted() {
        var uuid = UUID.randomUUID();
        var consumerId = "consumer1";
        Object returnedValue = null;

        repository.persistRequestInfo(uuid, consumerId, returnedValue);
        var requestInfo = repository.findRequestInfo(uuid, consumerId);

        assertNotNull(requestInfo);
        assertTrue(requestInfo.getProcessed());
        assertEquals(requestInfo.getIdempotencyKey(), uuid);
        assertEquals(requestInfo.getConsumerId(), consumerId);
        assertNotNull(requestInfo.getProcessedAt());
        assertNull(requestInfo.getReturnedValue());
    }

    @Test(expected = IdempotentizerException.class)
    public void givenWrongInput_whenPersistRequestInfo_thenIdempotentizerException() {
        var uuid = UUID.randomUUID();
        String consumerId = null; // cannot be null at database level
        Object returnedValue = null;

        repository.persistRequestInfo(uuid, consumerId, returnedValue);
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
        ds.setServerNames(new String[] {"localhost"});
        ds.setDatabaseName(DB_NAME);
        ds.setUser(DB_USER);
        ds.setPassword("wrongpass");
        ds.setPortNumbers(new int[] {database.getFirstMappedPort()});
        var unstableRepository = new SQLRepository(ds, new JacksonObjectSerde());

        unstableRepository.findRequestInfo(uuid, consumerId);
    }

    @Test(expected = IdempotentizerException.class)
    public void givenPersistedRequestInfoForWrongReturnedValue_whenFindRequestInfo_thenIdempotentizerException() {
        var uuid = UUID.randomUUID();
        String consumerId = "consumer1";
        byte[] serializedReturnedValue = "wrong.data".getBytes();
        persistRequestInfo(uuid, consumerId, serializedReturnedValue);

        repository.findRequestInfo(uuid, consumerId);
    }

    @Test(expected = IdempotentizerException.class)
    public void givenPersistedRequestInfoForUnknownClassName_whenFindRequestInfo_thenIdempotentizerException()
            throws IOException {
        var uuid = UUID.randomUUID();
        String consumerId = "consumer1";
        var returnedValue = new UserDto(7, "fakeUser", Instant.now());
        persistRequestInfo(uuid, consumerId, serialize("BadClassName", returnedValue));

        repository.findRequestInfo(uuid, consumerId);
    }

    private void persistRequestInfo(UUID idempotencyKey, String consumerId, byte[] serializedReturnedValue) {
        try (Connection conn = datasource.getConnection();
                var pstmt = conn.prepareStatement(SQLRepository.INSERT)) {
            pstmt.setObject(1, idempotencyKey);
            pstmt.setString(2, consumerId);
            pstmt.setBytes(3, serializedReturnedValue);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private byte[] serialize(String className, Object object) throws IOException {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        if (object == null) return new byte[0];

        byte[] objectBytes = objectMapper.writeValueAsBytes(object);

        SerializedObjectWrapperForTesting wrapper = new SerializedObjectWrapperForTesting(className, objectBytes);
        return objectMapper.writeValueAsBytes(wrapper);
    }
}
