package com.ersalgado.idempotentizer.sql;

import com.ersalgado.idempotentizer.core.IdempotentizerException;
import com.ersalgado.idempotentizer.core.ObjectSerde;
import com.ersalgado.idempotentizer.core.Repository;
import com.ersalgado.idempotentizer.core.RequestInfo;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;

public class SQLRepository implements Repository {

    static final String QUERY =
            "SELECT idempotency_key, consumer_id, created_at, returned_value FROM processed_messages WHERE idempotency_key=? and consumer_id=?";
    static final String INSERT =
            "INSERT INTO processed_messages (idempotency_key, consumer_id, returned_value) VALUES (?, ?, ?)";

    private final DataSource datasource;
    private final ObjectSerde objectSerde;

    public SQLRepository(DataSource datasource, ObjectSerde objectSerde) {
        this.datasource = datasource;
        this.objectSerde = objectSerde;
    }

    @Override
    public RequestInfo findRequestInfo(UUID idempotencyKey, String consumerId) {
        try (Connection conn = datasource.getConnection();
                var pstmt = conn.prepareStatement(QUERY)) {
            pstmt.setObject(1, idempotencyKey);
            pstmt.setString(2, consumerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UUID key = (UUID) rs.getObject("idempotency_key");
                    String consumer = rs.getString("consumer_id");
                    Instant processedAt = rs.getTimestamp("created_at").toInstant();
                    byte[] returnedValue = rs.getBytes("returned_value");
                    return new RequestInfo(key, consumer, processedAt, objectSerde.deserialize(returnedValue));
                } else {
                    return new RequestInfo();
                }
            } catch (ClassNotFoundException | IOException e) {
                throw new IdempotentizerException(e.getMessage());
            }

        } catch (SQLException e) {
            throw new IdempotentizerException(e.getMessage());
        }
    }

    @Override
    public void persistRequestInfo(UUID idempotencyKey, String consumerId, Object returnedValue) {
        try (Connection conn = datasource.getConnection();
                var pstmt = conn.prepareStatement(INSERT)) {
            pstmt.setObject(1, idempotencyKey);
            pstmt.setString(2, consumerId);
            pstmt.setBytes(3, objectSerde.serialize(returnedValue));
            pstmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new IdempotentizerException(e.getMessage());
        }
    }
}
