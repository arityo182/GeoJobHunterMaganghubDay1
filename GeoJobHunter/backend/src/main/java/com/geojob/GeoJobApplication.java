package com.geojob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GeoJobApplication {

    public static void main(String[] args) {
        // ─────────────────────────────────────────────────────────────────
        // Railway auto-injects DATABASE_PUBLIC_URL with format:
        //   postgresql://user:pass@host:port/db
        //
        // Spring Boot / HikariCP requires JDBC format:
        //   jdbc:postgresql://user:pass@host:port/db
        //
        // We auto-convert it here BEFORE Spring Boot creates the DataSource.
        // Priority: DATABASE_PUBLIC_URL > DATABASE_URL > JDBC_DATABASE_URL
        // ─────────────────────────────────────────────────────────────────
        for (String var : new String[]{"DATABASE_PUBLIC_URL", "DATABASE_URL", "JDBC_DATABASE_URL"}) {
            String raw = System.getenv(var);
            if (raw != null && !raw.isBlank()) {
                if (!raw.startsWith("jdbc:")) {
                    raw = "jdbc:" + raw;
                }
                System.setProperty("JDBC_DATABASE_URL", raw);
                System.out.println("[GeoJob] ✅ Auto-converted " + var + " → JDBC_DATABASE_URL");
                break;
            }
        }

        SpringApplication.run(GeoJobApplication.class, args);
    }
}
