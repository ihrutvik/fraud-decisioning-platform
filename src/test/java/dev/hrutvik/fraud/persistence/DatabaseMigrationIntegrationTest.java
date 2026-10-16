package dev.hrutvik.fraud.persistence;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Testcontainers
class DatabaseMigrationIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fraud").withUsername("fraud").withPassword("fraud");

    @BeforeAll
    static void migrate() {
        Flyway.configure().dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword()).load().migrate();
    }

    @Test
    void appliesEveryMigrationAndSeedsTheDefaultRules() throws SQLException {
        try (var connection = connection()) {
            assertThat(scalar(connection, "select count(*) from flyway_schema_history where success")).isEqualTo(5);
            assertThat(scalar(connection, "select count(*) from fraud_rule_sets where tenant_id='_default' and status='ACTIVE'")).isEqualTo(1);
        }
    }

    @Test
    void databaseEnforcesTenantScopedIdempotency() throws SQLException {
        try (var connection = connection()) {
            insertDecision(connection, "00000000-0000-0000-0000-000000000101", "tenant-a", "checkout-1");
            assertThatExceptionOfType(SQLException.class)
                    .isThrownBy(() -> insertDecision(connection, "00000000-0000-0000-0000-000000000102", "tenant-a", "checkout-1"))
                    .satisfies(error -> assertThat(error.getSQLState()).isEqualTo("23505"));
            insertDecision(connection, "00000000-0000-0000-0000-000000000103", "tenant-b", "checkout-1");
        }
    }

    @Test
    void allowsOnlyOneActiveRuleSetPerTenant() throws SQLException {
        try (var connection = connection()) {
            insertRuleSet(connection, "00000000-0000-0000-0000-000000000201", "tenant-rules", 1);
            assertThatExceptionOfType(SQLException.class)
                    .isThrownBy(() -> insertRuleSet(connection, "00000000-0000-0000-0000-000000000202", "tenant-rules", 2))
                    .satisfies(error -> assertThat(error.getSQLState()).isEqualTo("23505"));
        }
    }

    @Test
    void rejectsResolvedReviewWithoutResolutionEvidence() throws SQLException {
        try (var connection = connection()) {
            insertDecision(connection, "00000000-0000-0000-0000-000000000301", "tenant-review", "review-1");
            assertThatExceptionOfType(SQLException.class).isThrownBy(() -> connection.createStatement().executeUpdate("""
                    insert into review_cases(id,tenant_id,decision_id,status,created_at)
                    values ('00000000-0000-0000-0000-000000000302','tenant-review','00000000-0000-0000-0000-000000000301','RESOLVED',now())
                    """)).satisfies(error -> assertThat(error.getSQLState()).isEqualTo("23514"));
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private static long scalar(Connection connection, String sql) throws SQLException {
        try (var result = connection.createStatement().executeQuery(sql)) { result.next(); return result.getLong(1); }
    }

    private static void insertDecision(Connection connection, String id, String tenant, String key) throws SQLException {
        try (var statement = connection.prepareStatement("""
                insert into transaction_decisions(id,tenant_id,idempotency_key,request_fingerprint,transaction_id,amount,currency,account_id,outcome,score,reason_codes,created_at,rule_set_version)
                values (?::uuid,?,?,repeat('a',64),?,10.00,'USD','account-1','APPROVE',0,'',now(),1)
                """)) {
            statement.setString(1, id); statement.setString(2, tenant); statement.setString(3, key); statement.setString(4, "tx-" + id);
            statement.executeUpdate();
        }
    }

    private static void insertRuleSet(Connection connection, String id, String tenant, long version) throws SQLException {
        try (var statement = connection.prepareStatement("""
                insert into fraud_rule_sets(id,tenant_id,rule_version,status,high_value_threshold,new_account_days,account_velocity_limit,ip_velocity_limit,device_velocity_limit,review_threshold,decline_threshold,created_at)
                values (?::uuid,?,?,'ACTIVE',2000,7,5,10,4,35,70,now())
                """)) {
            statement.setString(1, id); statement.setString(2, tenant); statement.setLong(3, version); statement.executeUpdate();
        }
    }
}
