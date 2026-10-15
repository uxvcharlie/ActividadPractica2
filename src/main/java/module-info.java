module com.example.actividadpractica2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.example.actividadpractica2 to javafx.fxml;
    opens com.example.actividadpractica2.controller to javafx.fxml;
    exports com.example.actividadpractica2;
}
