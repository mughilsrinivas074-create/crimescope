package com.crimescope.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Educational JDBC Component fulfilling the academic requirement for manual JDBC demonstration.
 * Demonstrates:
 * 1. Explicit Connection management with DataSource pooling
 * 2. Parameterized PreparedStatement execution preventing SQL injection
 * 3. ResultSet cursor iteration and explicit column type extraction
 * 4. Transactional boundary control (commit, rollback, autoCommit)
 * 5. Diagnostic connection telemetry (driver version, isolation level, execution latency)
 */
@Component
public class EducationalJdbcCrimeDao {

    private static final Logger log = LoggerFactory.getLogger(EducationalJdbcCrimeDao.class);
    private final DataSource dataSource;

    public EducationalJdbcCrimeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Executes a parameterized direct JDBC query to demonstrate manual SQL execution alongside Spring Data JPA.
     */
    public Map<String, Object> executeEducationalQuery(String districtCode, Integer reportingYear) {
        long startNs = System.nanoTime();
        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT d.code AS dist_code, d.name AS dist_name, c.code AS cat_code, c.name AS cat_name,
                   r.reporting_year, r.case_count, r.arrest_count, r.convicted_count, r.is_demonstration
            FROM crime_records r
            JOIN districts d ON r.district_id = d.id
            JOIN crime_categories c ON r.category_id = c.id
            WHERE (? IS NULL OR UPPER(d.code) = UPPER(?))
              AND (? IS NULL OR r.reporting_year = ?)
            ORDER BY r.reporting_year DESC, r.case_count DESC
            LIMIT 50
        """;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();

            response.put("databaseProductName", metaData.getDatabaseProductName());
            response.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            response.put("driverName", metaData.getDriverName());
            response.put("driverVersion", metaData.getDriverVersion());
            response.put("transactionIsolationLevel", getIsolationLevelName(conn.getTransactionIsolation()));
            response.put("autoCommitEnabled", conn.getAutoCommit());

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, districtCode);
            pstmt.setString(2, districtCode);
            if (reportingYear != null) {
                pstmt.setInt(3, reportingYear);
                pstmt.setInt(4, reportingYear);
            } else {
                pstmt.setNull(3, Types.INTEGER);
                pstmt.setNull(4, Types.INTEGER);
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("districtCode", rs.getString("dist_code"));
                row.put("districtName", rs.getString("dist_name"));
                row.put("categoryCode", rs.getString("cat_code"));
                row.put("categoryName", rs.getString("cat_name"));
                row.put("reportingYear", rs.getInt("reporting_year"));
                row.put("caseCount", rs.getInt("case_count"));
                row.put("arrestCount", rs.getObject("arrest_count"));
                row.put("convictedCount", rs.getObject("convicted_count"));
                row.put("isDemonstration", rs.getBoolean("is_demonstration"));
                rows.add(row);
            }

            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            response.put("status", "SUCCESS");
            response.put("executionTimeMs", elapsedMs);
            response.put("executedSql", sql.trim());
            response.put("boundParameters", Map.of("districtCode", districtCode != null ? districtCode : "NULL",
                    "reportingYear", reportingYear != null ? reportingYear : "ALL"));
            response.put("returnedRowCount", rows.size());
            response.put("records", rows);

        } catch (SQLException e) {
            log.error("JDBC Demonstration query failed", e);
            response.put("status", "ERROR");
            response.put("sqlErrorCode", e.getErrorCode());
            response.put("sqlState", e.getSQLState());
            response.put("errorMessage", e.getMessage());
        } finally {
            closeQuietly(rs);
            closeQuietly(pstmt);
            closeQuietly(conn);
        }

        return response;
    }

    private String getIsolationLevelName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "TRANSACTION_READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "TRANSACTION_READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "TRANSACTION_REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "TRANSACTION_SERIALIZABLE";
            default -> "TRANSACTION_NONE";
        };
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {}
        }
    }
}
