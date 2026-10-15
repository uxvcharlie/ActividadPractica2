package com.example.actividadpractica2.controller;

import com.example.actividadpractica2.database.ConexionBD;
import com.example.actividadpractica2.model.Empleado;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmpleadosController {


    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtCargo;

    @FXML
    private TextField txtSalario;

    //Busqueda
    @FXML
    private ComboBox<String> cmbColumna1;
    @FXML
    private ComboBox<String> cmbColumna2;

    @FXML
    private ComboBox<String> cmbColumna3;

    @FXML
    private TextField txtValor1;
    @FXML
    private TextField txtValor2;
    @FXML
    private TextField txtValor3;

    //Tabla
    @FXML
    private TableView<Empleado> tblEmpleados;
    @FXML
    private TableColumn<Empleado, String> colCodigo;
    @FXML
    private TableColumn<Empleado, String> colNombre;
    @FXML
    private TableColumn<Empleado, String> colCorreo;
    @FXML
    private TableColumn<Empleado, String> colCargo;
    @FXML
    private TableColumn<Empleado, BigDecimal> colSalario;
    @FXML
    private TableColumn<Empleado, String> colFechaRegistro;
    @FXML
    private TableColumn<Empleado, String> colEstado;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObservableList<Empleado> empleados = FXCollections.observableArrayList();

    /*
     * Empleado elegido en la tabla. Si es null no hay nada que actualizar ni eliminar
     * */
    private Empleado empleadoSeleccionado;

    /*
     * Lo que ve el usuario en el ComboBox -> nombre real de la columna.
     * Los nombres de columna no se pueden mandar con "?", por eso solo
     * se aceptan los que estan en esta lista
     * */
    private static final Map<String, String> COLUMNAS_FILTRO = new LinkedHashMap<>();

    static {
        COLUMNAS_FILTRO.put("Código", "codigo");
        COLUMNAS_FILTRO.put("Nombre", "nombre");
        COLUMNAS_FILTRO.put("Correo", "correo");
        COLUMNAS_FILTRO.put("Cargo", "cargo");
        COLUMNAS_FILTRO.put("Salario", "salario");
    }

    private static final String SELECT_ACTIVOS = """
            SELECT id_empleado, codigo, nombre, correo, cargo, salario, fecha_registro, activo
            FROM catalogo.empleados
            WHERE activo = TRUE
            """;

    /*
     * JavaFX lo llama automaticamente al cargar el FXML
     * */
    @FXML
    private void initialize() {
        configurarTabla();
        configurarSeleccionTabla();
        configurarFiltros();
        cargarEmpleados();
    }

    private void configurarTabla() {
        colCodigo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCodigo()));
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreo()));
        colCargo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCargo()));
        colSalario.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSalario()));
        colFechaRegistro.setCellValueFactory(data ->
                new SimpleStringProperty(FORMATO_FECHA.format(data.getValue().getFechaRegistro())));
        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Inactivo"));

        tblEmpleados.setItems(empleados);
    }

    /*
     * Al hacer clic en una fila, sus datos pasan al formulario
     * */
    private void configurarSeleccionTabla() {
        tblEmpleados.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            empleadoSeleccionado = newValue;
            if (newValue != null) {
                txtCodigo.setText(newValue.getCodigo());
                txtNombre.setText(newValue.getNombre());
                txtCorreo.setText(newValue.getCorreo());
                txtCargo.setText(newValue.getCargo());
                txtSalario.setText(newValue.getSalario().toPlainString());
            }
        });
    }

    private void configurarFiltros() {
        for (ComboBox<String> cmb : List.of(cmbColumna1, cmbColumna2, cmbColumna3)) {
            cmb.setItems(FXCollections.observableArrayList(COLUMNAS_FILTRO.keySet()));
        }
    }

    /*
     * Consulta: solo empleados activos
     * */
    private void cargarEmpleados() {
        ejecutarConsulta(SELECT_ACTIVOS + " ORDER BY codigo", new ArrayList<>());
    }

    /*
     * Ejecuta un SELECT con sus parametros y llena el TableView
     * */
    private void ejecutarConsulta(String sql, List<Object> parametros) {
        empleados.clear();

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    empleados.add(new Empleado(
                            rs.getInt("id_empleado"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("correo"),
                            rs.getString("cargo"),
                            rs.getBigDecimal("salario"),
                            rs.getObject("fecha_registro", LocalDateTime.class),
                            rs.getBoolean("activo")
                    ));
                }
            }
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudieron cargar los empleados.", e);
        }
    }
    @FXML
    private void guardar() {
        if (!validarFormulario()) {
            return;
        }

        /*
         * fecha_registro y activo no se mandan: los pone el DEFAULT de la base
         * */
        String sql = """
                INSERT INTO catalogo.empleados (codigo, nombre, correo, cargo, salario)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            asignarDatosFormulario(ps);
            ps.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Empleado guardado",
                    "El empleado se registró correctamente.");
            limpiar();
            cargarEmpleados();
        } catch (SQLException e) {
            manejarErrorSQL("No se pudo guardar el empleado.", e);
        }
    }

    @FXML
    private void actualizar() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección",
                    "Selecciona un empleado de la tabla.");
            return;
        }

        if (!validarFormulario()) {
            return;
        }

        String sql = """
                UPDATE catalogo.empleados
                SET codigo = ?, nombre = ?, correo = ?, cargo = ?, salario = ?
                WHERE id_empleado = ? AND activo = TRUE
                """;

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            asignarDatosFormulario(ps);
            ps.setInt(6, empleadoSeleccionado.getIdEmpleado());
            ps.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Empleado actualizado",
                    "Los cambios se guardaron correctamente.");
            limpiar();
            cargarEmpleados();
        } catch (SQLException e) {
            manejarErrorSQL("No se pudo actualizar el empleado.", e);
        }
    }

    /*
     * Eliminacion logica: no se borra la fila, solo se marca activo = FALSE
     * */
    @FXML
    private void eliminar() {
        if (empleadoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin selección",
                    "Selecciona un empleado de la tabla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Dar de baja");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Dar de baja a " + empleadoSeleccionado.getNombre()
                + " (" + empleadoSeleccionado.getCodigo() + ")?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        String sql = "UPDATE catalogo.empleados SET activo = FALSE WHERE id_empleado = ?";

        try (
                Connection conexion = ConexionBD.getConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setInt(1, empleadoSeleccionado.getIdEmpleado());
            ps.executeUpdate();

            mostrarAlerta(Alert.AlertType.INFORMATION, "Empleado dado de baja",
                    "El empleado ya no aparece en la lista, pero sigue guardado en la base.");
            limpiar();
            cargarEmpleados();
        } catch (SQLException e) {
            mostrarErrorBaseDatos("No se pudo dar de baja al empleado.", e);
        }
    }

    @FXML
    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtCorreo.clear();
        txtCargo.clear();
        txtSalario.clear();
        tblEmpleados.getSelectionModel().clearSelection();
        empleadoSeleccionado = null;
        txtCodigo.requestFocus();
    }

    /*
     * Busqueda parametrizada: cada filtro con valor agrega un "AND columna ... ?"
     * */
    @FXML
    private void buscar() {
        StringBuilder sql = new StringBuilder(SELECT_ACTIVOS);
        List<Object> parametros = new ArrayList<>();

        if (!agregarFiltro(cmbColumna1, txtValor1, sql, parametros)
                || !agregarFiltro(cmbColumna2, txtValor2, sql, parametros)
                || !agregarFiltro(cmbColumna3, txtValor3, sql, parametros)) {
            return;
        }

        sql.append(" ORDER BY codigo");
        ejecutarConsulta(sql.toString(), parametros);

        if (empleados.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sin resultados",
                    "No hay empleados activos que cumplan los filtros.");
        }
    }

    /*
     * Devuelve false si el filtro esta mal llenado. Si no tiene valor se ignora
     * */
    private boolean agregarFiltro(ComboBox<String> cmbColumna, TextField txtValor,
                                  StringBuilder sql, List<Object> parametros) {
        String valor = txtValor.getText().trim();
        if (valor.isEmpty()) {
            return true;
        }

        String columna = COLUMNAS_FILTRO.get(cmbColumna.getValue());
        if (columna == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Falta la columna",
                    "Elige en qué columna buscar \"" + valor + "\".");
            cmbColumna.requestFocus();
            return false;
        }

        if (columna.equals("salario")) {
            try {
                parametros.add(new BigDecimal(valor));
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "Salario inválido",
                        "Para buscar por salario escribe un número, por ejemplo 45000.50");
                txtValor.requestFocus();
                return false;
            }
            sql.append(" AND salario = ?");
        } else {
            sql.append(" AND ").append(columna).append(" ILIKE ?");
            parametros.add("%" + valor + "%");
        }
        return true;
    }

    @FXML
    private void quitarFiltros() {
        for (ComboBox<String> cmb : List.of(cmbColumna1, cmbColumna2, cmbColumna3)) {
            cmb.getSelectionModel().clearSelection();
        }
        txtValor1.clear();
        txtValor2.clear();
        txtValor3.clear();
        cargarEmpleados();
    }

    /*
     * Solo revisa que no haya campos vacios y que el salario sea numero.
     * Lo demas (UNIQUE, CHECK salario > 0) lo valida PostgreSQL
     * */
    private boolean validarFormulario() {
        if (txtCodigo.getText().isBlank() || txtNombre.getText().isBlank()
                || txtCorreo.getText().isBlank() || txtCargo.getText().isBlank()
                || txtSalario.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos",
                    "Completa código, nombre, correo, cargo y salario.");
            return false;
        }

        try {
            new BigDecimal(txtSalario.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Salario inválido",
                    "El salario debe ser un número, por ejemplo 45000.50");
            txtSalario.requestFocus();
            return false;
        }
        return true;
    }

    private void asignarDatosFormulario(PreparedStatement ps) throws SQLException {
        ps.setString(1, txtCodigo.getText().trim());
        ps.setString(2, txtNombre.getText().trim());
        ps.setString(3, txtCorreo.getText().trim());
        ps.setString(4, txtCargo.getText().trim());
        ps.setBigDecimal(5, new BigDecimal(txtSalario.getText().trim()));
    }

    /*
     * Traduce los errores de los constraints de PostgreSQL.
     * Cada error trae un SQLState que dice que regla se rompio
     * */
    private void manejarErrorSQL(String mensaje, SQLException e) {
        String detalle = String.valueOf(e.getMessage());

        switch (String.valueOf(e.getSQLState())) {
            case "23505" -> { // UNIQUE
                if (detalle.contains("codigo")) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Código repetido",
                            "Ya existe un empleado con ese código.");
                } else if (detalle.contains("correo")) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Correo repetido",
                            "Ya existe un empleado con ese correo.");
                } else {
                    mostrarErrorBaseDatos(mensaje, e);
                }
            }
            case "23514" -> mostrarAlerta(Alert.AlertType.WARNING, "Salario inválido",
                    "El salario debe ser mayor que cero."); // CHECK
            case "23502" -> mostrarAlerta(Alert.AlertType.WARNING, "Campo obligatorio",
                    "Hay un campo obligatorio vacío."); // NOT NULL
            case "22001" -> mostrarAlerta(Alert.AlertType.WARNING, "Texto muy largo",
                    "Código máx. 10 caracteres, nombre y correo máx. 100, cargo máx. 60.");
            case "22003" -> mostrarAlerta(Alert.AlertType.WARNING, "Salario muy grande",
                    "El salario máximo es 99999999.99");
            default -> mostrarErrorBaseDatos(mensaje, e);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarErrorBaseDatos(String mensaje, SQLException e) {
        e.printStackTrace();

        mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos",
                mensaje + "\n\nDetalle: " + e.getMessage());
    }
}
