package com.ersalgado.idempotentizer.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

import javax.sql.DataSource;

import com.ersalgado.idempotentizer.core.IdempotentizerException;
import com.ersalgado.idempotentizer.core.Repository;
import com.ersalgado.idempotentizer.core.RequestInfo;

public class SQLRepository implements Repository {

    private static final String QUERY = "SELECT idempotency_key, consumer_id, created_at FROM processed_messages WHERE idempotency_key=? and consumer_id=?";
    private static final String INSERT = "INSERT INTO processed_messages (idempotency_key, consumer_id) VALUES (?, ?)";

    private final DataSource datasource;

    public SQLRepository(DataSource datasource) {
        this.datasource = datasource;
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
                    return new RequestInfo(key, consumer, processedAt);
                } else {
                    return new RequestInfo();
                }
            }

        } catch (SQLException e) {
            throw new IdempotentizerException(e.getMessage());
        }
    }

    @Override
    public void persistRequestInfo(UUID idempotencyKey, String consumerId) {
        try (Connection conn = datasource.getConnection();
                var pstmt = conn.prepareStatement(INSERT)) {
            pstmt.setObject(1, idempotencyKey);
            pstmt.setString(2, consumerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new IdempotentizerException(e.getMessage());
        }
    }
}
