package com.geojob.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DatasourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        // ─────────────────────────────────────────────────────────────────
        // DIAGNOSTIC: log all relevant env vars available at startup
        // ─────────────────────────────────────────────────────────────────
        System.out.println("[GeoJob] 🔍 DIAGNOSTIC: Checking database-related env vars...");
        String[] relevantVars = {
            "DATABASE_URL", "DATABASE_PUBLIC_URL", "JDBC_DATABASE_URL",
            "PGHOST", "PGPORT", "PGDATABASE", "PGUSER", "PGPASSWORD",
            "SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD",
            "RAILWAY_SERVICE_ID", "RAILWAY_ENVIRONMENT", "RAILWAY_PROJECT_ID",
            "RAILWAY_TCP_PROXY_DOMAIN", "RAILWAY_PRIVATE_DOMAIN",
            "POSTGRES_URL", "POSTGRES_USER", "POSTGRES_PASSWORD", "POSTGRES_DB"
        };
        for (String var : relevantVars) {
            String val = System.getenv(var);
            if (val != null && !val.isBlank()) {
                // Mask password values for safe logging
                String display = var.contains("PASSWORD") || var.contains("SECRET")
                    ? val.substring(0, Math.min(4, val.length())) + "****"
                    : val;
                System.out.println("[GeoJob]   ✅ " + var + " = " + display);
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // Railway auto-injects DATABASE_PUBLIC_URL / DATABASE_URL / etc.
        // in postgresql:// format. We convert to jdbc:postgresql:// here.
        // Priority: DATABASE_PUBLIC_URL > DATABASE_URL > JDBC_DATABASE_URL
        // ─────────────────────────────────────────────────────────────────
        String rawUrl = null;
        String sourceVar = null;

        for (String var : new String[]{"DATABASE_PUBLIC_URL", "DATABASE_URL", "JDBC_DATABASE_URL"}) {
            String val = System.getenv(var);
            if (val != null && !val.isBlank()) {
                rawUrl = val;
                sourceVar = var;
                break;
            }
        }

        // If we found a URL, convert to JDBC format and parse it
        if (rawUrl != null) {
            // Convert postgresql:// → jdbc:postgresql://
            if (!rawUrl.startsWith("jdbc:")) {
                rawUrl = "jdbc:" + rawUrl;
            }

            System.out.println("[GeoJob] 📦 DataSource from " + sourceVar);

            // Parse the JDBC URL to extract components safely
            try {
                // Strip "jdbc:" prefix → "postgresql://user:pass@host:port/db"
                String jdbcUrl = rawUrl;
                String pgUrl = jdbcUrl.startsWith("jdbc:") ? jdbcUrl.substring(5) : jdbcUrl;

                URI uri = new URI(pgUrl);
                String host = uri.getHost();
                int port = uri.getPort();
                String path = uri.getPath(); // e.g. "/railway"
                String database = path != null ? path.replaceFirst("^/", "") : "railway";
                String username = null;
                String password = null;

                String userInfo = uri.getUserInfo();
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    if (parts.length > 1) {
                        password = parts[1];
                    }
                }

                // Fallback if URI parsing fails
                if (host == null) {
                    System.out.println("[GeoJob] ⚠️  URI parsing returned null host, using raw URL");
                    return createRawDataSource(jdbcUrl);
                }

                // Safety fallbacks
                if (username == null || username.isEmpty()) username = System.getenv("PGUSER");
                if (password == null || password.isEmpty()) password = System.getenv("PGPASSWORD");

                // Build final JDBC URL with known-good format
                String finalUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

                System.out.println("[GeoJob] 🔗 JDBC URL: jdbc:postgresql://" + host + ":" + port + "/" + database);

                DataSource ds = DataSourceBuilder.create()
                        .url(finalUrl)
                        .username(username)
                        .password(password)
                        .driverClassName("org.postgresql.Driver")
                        .build();

                // Configure HikariCP pool
                if (ds instanceof HikariDataSource hikari) {
                    hikari.setMaximumPoolSize(10);
                    hikari.setConnectionTimeout(30000);
                }

                return ds;

            } catch (URISyntaxException e) {
                System.out.println("[GeoJob] ⚠️  URI parsing failed: " + e.getMessage() + " — using raw URL");
                return createRawDataSource(rawUrl);
            }
        }

        // Fallback: use individual PGHOST/PGPORT/PGDATABASE/PGUSER/PGPASSWORD
        String host = getEnv("PGHOST", "localhost");
        String port = getEnv("PGPORT", "5433");
        String db   = getEnv("PGDATABASE", "jobhun");
        String user = getEnv("PGUSER", "core");
        String pass = getEnv("PGPASSWORD", "arie12345");

        System.out.println("[GeoJob] ❌ No DATABASE_URL/PUBLIC_URL/JDBC_URL found — using PGHOST/PGPORT/PGDATABASE fallback");
        System.out.println("[GeoJob]   PGHOST     = " + host);
        System.out.println("[GeoJob]   PGPORT     = " + port);
        System.out.println("[GeoJob]   PGDATABASE = " + db);
        System.out.println("[GeoJob]   PGUSER     = " + user);
        System.out.println("[GeoJob] 🔗 JDBC URL: jdbc:postgresql://" + host + ":" + port + "/" + db);

        DataSource ds = DataSourceBuilder.create()
                .url("jdbc:postgresql://" + host + ":" + port + "/" + db)
                .username(user)
                .password(pass)
                .driverClassName("org.postgresql.Driver")
                .build();

        if (ds instanceof HikariDataSource hikari) {
            hikari.setMaximumPoolSize(10);
            hikari.setConnectionTimeout(30000);
        }

        return ds;
    }

    /**
     * Fallback: create DataSource with raw URL as-is.
     */
    private DataSource createRawDataSource(String url) {
        System.out.println("[GeoJob] 🔗 Raw JDBC URL used");

        DataSource ds = DataSourceBuilder.create()
                .url(url)
                .driverClassName("org.postgresql.Driver")
                .build();

        if (ds instanceof HikariDataSource hikari) {
            hikari.setMaximumPoolSize(10);
            hikari.setConnectionTimeout(30000);
        }

        return ds;
    }

    private String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : fallback;
    }
}
