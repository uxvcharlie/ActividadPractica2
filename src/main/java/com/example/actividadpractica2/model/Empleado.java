package com.example.actividadpractica2.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * Representa una fila de la tabla catalogo.empleados
 * */
public class Empleado {

    private int idEmpleado;
    private String codigo;
    private String nombre;
    private String correo;
    private String cargo;
    private BigDecimal salario;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public Empleado(int idEmpleado, String codigo, String nombre, String correo,
                    String cargo, BigDecimal salario, LocalDateTime fechaRegistro, boolean activo) {
        this.idEmpleado = idEmpleado;
        this.codigo = codigo;
        this.nombre = nombre;
        this.correo = correo;
        this.cargo = cargo;
        this.salario = salario;
        this.fechaRegistro = fechaRegistro;
        this.activo = activo;
    }

    public int getIdEmpleado() {
        return idEmpleado;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getCargo() {
        return cargo;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }
}
