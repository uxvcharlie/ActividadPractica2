# ActividadPractica2


## Queries que se trabajaronen pdAdmin4
```sql
-- Crear base de datos 
CREATE DATABASE bdempleados;

-- Crear esquema 
CREATE SCHEMA catalogo; 

-- Crear tabla empleados 
CREATE TABLE catalogo.empleados(
id_empleado SERIAL PRIMARY KEY, 
codigo VARCHAR(10) NOT NULL UNIQUE, 
nombre VARCHAR(100) NOT NULL, 
correo VARCHAR(100) NOT NULL UNIQUE,
cargo VARCHAR(60) NOT NULL, 
salario NUMERIC(10, 2) NOT NULL,

CONSTRAINT chk_salario_mayorquecero
CHECK (salario > 0),

fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
activo BOOLEAN NOT NULL DEFAULT TRUE
)
-- Analizar que los valores automáticos de fecha_registro y activo se asignen correctamente.
INSERT INTO catalogo.empleados(codigo, nombre, correo, cargo, salario);
VALUES
('001', 'Charlie Brown', 'charlie1@gmail.com', 'analista', 60000)

SELECT * FROM catalogo.empleados
-- Insertar 10 empleados diferentes 
INSERT INTO catalogo.empleados(codigo, nombre, correo, cargo, salario)
VALUES
('002', 'Lesther Rugama', 'Lesther123@gmail.com', 'Desarrollador', 90000),
('003', 'Sergio Cabrera', 'sergio@gmail.com', 'Analista', 70000.50),
('004', 'Miguel Espinoza', 'mrayo@gmail.com', 'Tester QA', 70000.50),
('005', 'Chelsea Quintanilla', 'chelsea.quintanilla@gmail.com', 'Desarrolladora', 70000.50),
('006', 'Diego Martínez', 'diego.martinez@gmail.com', 'Administrador de BD', 65000.00),
('007', 'Valeria Hernández', 'valeria.hernandez@gmail.com', 'Diseñadora UX', 58000.75),
('008', 'José Ramírez', 'jose.ramirez@gmail.com', 'Soporte Técnico', 42000.00),
('009', 'Andrea López', 'andrea.lopez@gmail.com', 'Contadora', 55000.25),
('010', 'Marco Flores', 'marco.flores@gmail.com', 'Gerente de Proyectos', 85000.00);
```

