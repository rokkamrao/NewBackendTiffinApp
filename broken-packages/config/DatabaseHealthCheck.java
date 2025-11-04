package com.tiffin.api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

@Configuration
public class DatabaseHealthCheck {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Environment env;

    @PostConstruct
    public void checkDatabaseConnection() {
        try (Connection conn = dataSource.getConnection()) {
            String dbUrl = env.getProperty("spring.datasource.url");
            System.out.println("✅ Database connection successful!");
            System.out.println("Connected to: " + dbUrl);
            System.out.println("Database product: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Database version: " + conn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure PostgreSQL is running and the database 'tiffindb' exists.");
            System.err.println("You can create the database using:");
            System.err.println("  createdb tiffindb");
            System.err.println("or");
            System.err.println("  CREATE DATABASE tiffindb;");
            // Don't throw exception to allow application to start without DB for development
        }
    }
}