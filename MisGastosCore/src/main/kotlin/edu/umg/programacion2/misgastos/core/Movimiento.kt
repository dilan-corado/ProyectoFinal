package edu.umg.programacion2.misgastos.core

// Data class que representa un movimiento financiero
// dentro de la aplicación.
//
// Un movimiento puede ser un INGRESO o un GASTO
// y contiene toda la información necesaria
// para identificarlo y mostrarlo.
data class Movimiento(

    // Identificador único del movimiento.
    //
    // Por defecto es 0 porque cuando se crea
    // un movimiento nuevo todavía no tiene
    // el ID generado por SQLite.
    val id: Long = 0,

    // Indica si el movimiento es:
    // INGRESO o GASTO.
    val tipo: TipoMovimiento,

    // Cantidad de dinero del movimiento.
    // Ejemplo: 500.00
    val monto: Double,

    // Descripción escrita por el usuario.
    // Ejemplo: "Compra de supermercado".
    val descripcion: String,

    // Categoría asociada al movimiento.
    // Ejemplo:
    // COMIDA, TRANSPORTE, SALARIO, etc.
    val categoria: Categoria,

    // Fecha del movimiento.
    //
    // En tu aplicación se maneja como String.
    // Ejemplo: "31/10/2026".
    val fecha: String
)


// Enum que define los únicos dos tipos
// de movimientos permitidos en la aplicación.
enum class TipoMovimiento {

    // Representa dinero que entra.
    INGRESO,

    // Representa dinero que sale.
    GASTO
}