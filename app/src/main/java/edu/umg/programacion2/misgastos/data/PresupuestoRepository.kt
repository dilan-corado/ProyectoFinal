package edu.umg.programacion2.misgastos.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase

// Repositorio encargado de manejar la persistencia
// de los presupuestos en la base de datos SQLite.
class PresupuestoRepository(context: Context) {

    // Permite acceder a la base de datos SQLite.
    private val dbHelper = MisGastosDbHelper(context)

    // Guarda un presupuesto para un mes determinado.
    fun guardar(mes: String, monto: Double): Boolean {

        // Valida que el mes no esté vacío,
        // que el monto no sea negativo
        // y que sea un número válido.
        if (mes.isBlank() || monto < 0 || !monto.isFinite()) {
            return false
        }

        // ContentValues prepara los datos que serán
        // almacenados en la tabla de presupuestos.
        val valores = ContentValues().apply {

            // Guarda el mes.
            put(MisGastosDbHelper.COLUMN_MES, mes)

            // Guarda el monto del presupuesto.
            put(MisGastosDbHelper.COLUMN_PRESUPUESTO, monto)
        }

        // Inserta el presupuesto en SQLite.
        val resultado = dbHelper.writableDatabase.insertWithOnConflict(
            MisGastosDbHelper.TABLE_PRESUPUESTOS,
            null,
            valores,

            // Si ya existe un presupuesto para ese mes,
            // reemplaza el presupuesto anterior por el nuevo.
            SQLiteDatabase.CONFLICT_REPLACE
        )

        // SQLite devuelve -1 si ocurrió un error.
        // Si el resultado es diferente de -1,
        // significa que se guardó correctamente.
        return resultado != -1L
    }

    // Busca y obtiene el presupuesto de un mes específico.
    fun obtener(mes: String): Double? {

        // Consulta la tabla de presupuestos buscando
        // únicamente el mes solicitado.
        dbHelper.readableDatabase.query(
            MisGastosDbHelper.TABLE_PRESUPUESTOS,

            // Solo necesitamos obtener la columna presupuesto.
            arrayOf(MisGastosDbHelper.COLUMN_PRESUPUESTO),

            // Condición de búsqueda: mes = ?
            "${MisGastosDbHelper.COLUMN_MES} = ?",

            // El ? anterior se reemplaza por este mes.
            arrayOf(mes),

            null,
            null,
            null

        ).use { cursor ->

            // Si se encontró el mes, devuelve
            // el monto del presupuesto.
            return if (cursor.moveToFirst()) {
                cursor.getDouble(0)
            } else {

                // Si no existe presupuesto para ese mes,
                // devuelve null.
                null
            }
        }
    }

    // Elimina el presupuesto correspondiente
    // al mes recibido.
    fun eliminar(mes: String): Boolean {

        return dbHelper.writableDatabase.delete(
            MisGastosDbHelper.TABLE_PRESUPUESTOS,

            // Busca el presupuesto por su mes.
            "${MisGastosDbHelper.COLUMN_MES} = ?",

            // Reemplaza el ? por el mes recibido.
            arrayOf(mes)

        ) > 0 // Si eliminó al menos una fila, devuelve true.
    }
}