package com.lostwilderness.rpgcore.infra.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Async helpers for DB access. All methods run on the given executor (e.g. async pool).
 */
public final class SqlExecutor {

    private final DatabaseProvider db;
    private final Executor executor;

    public SqlExecutor(DatabaseProvider db, Executor executor) {
        this.db = db;
        this.executor = executor;
    }

    public CompletableFuture<Integer> update(String datasourceName, String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            DataSource ds = db.getDataSource(datasourceName);
            if (ds == null) throw new IllegalStateException("Unknown datasource: " + datasourceName);
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    public <T> CompletableFuture<T> query(String datasourceName, String sql,
                                          ResultSetMapper<T> mapper, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            DataSource ds = db.getDataSource(datasourceName);
            if (ds == null) throw new IllegalStateException("Unknown datasource: " + datasourceName);
            try (Connection conn = ds.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    return mapper.map(rs);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
