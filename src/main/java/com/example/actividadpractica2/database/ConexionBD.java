package com.example.actividadpractica2.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL =
            "jdbc:postgresql://localhost:5432/bdempleados";

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private ConexionBD() {
    }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

}
