package com.crimescope.desktop.jdbc;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Educational Direct JDBC Runner for Desktop Client.
 * Demonstrates manual database connectivity (Connection, PreparedStatement, ResultSet)
 * without hardcoding credentials into source code.
 */
public class DirectJdbcDemoRunner {

    public Map<String, Object> runDirectQuery(String jdbcUrl, String username, String password,
                                              String districtCode, Integer year) {
        long start = System.currentTimeMillis();
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT d.code AS dist_code, d.name AS dist_name, c.name AS cat_name,
                   r.reporting_year, r.case_count, r.arrest_count, r.convicted_count
            FROM crime_records r
            JOIN districts d ON r.district_id = d.id
            JOIN crime_categories c ON r.category_id = c.id
            WHERE (? IS NULL OR UPPER(d.code) = UPPER(?))
              AND (? IS NULL OR r.reporting_year = ?)
            ORDER BY r.reporting_year DESC, r.case_count DESC
            LIMIT 30
        """;

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            result.put("databaseProduct", meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            result.put("driverName", meta.getDriverName());
            result.put("driverVersion", meta.getDriverVersion());
            result.put("autoCommit", conn.getAutoCommit());

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, districtCode);
                stmt.setString(2, districtCode);
                if (year != null) {
                    stmt.setInt(3, year);
                    stmt.setInt(4, year);
                } else {
                    stmt.setNull(3, Types.INTEGER);
                    stmt.setNull(4, Types.INTEGER);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("districtCode", rs.getString("dist_code"));
                        row.put("districtName", rs.getString("dist_name"));
                        row.put("categoryName", rs.getString("cat_name"));
                        row.put("year", rs.getInt("reporting_year"));
                        row.put("cases", rs.getInt("case_count"));
                        row.put("arrests", rs.getInt("arrest_count"));
                        row.put("convictions", rs.getInt("convicted_count"));
                        rows.add(row);
                    }
                }
            }

            result.put("status", "SUCCESS");
            result.put("rows", rows);
            result.put("rowCount", rows.size());
            result.put("executionTimeMs", System.currentTimeMillis() - start);

        } catch (SQLException e) {
            result.put("status", "ERROR");
            result.put("errorMessage", e.getMessage());
            result.put("errorCode", e.getErrorCode());
            result.put("sqlState", e.getSQLState());
        }

        return result;
    }
}
