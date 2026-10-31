package edu.umg.programacion2.misgastos.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import edu.umg.programacion2.misgastos.core.Categoria
import edu.umg.programacion2.misgastos.core.Movimiento
import edu.umg.programacion2.misgastos.core.TipoMovimiento

// Repositorio encargado de comunicar la aplicación
// con la base de datos para manejar los movimientos.
class MovimientoRepository(context: Context) {

    // Permite acceder a la base de datos SQLite.
    private val dbHelper = MisGastosDbHelper(context)

    // Guarda un nuevo movimiento en la tabla "movimientos".
    fun guardar(movimiento: Movimiento): Long {
        return dbHelper.writableDatabase.insert(
            MisGastosDbHelper.TABLE_MOVIMIENTOS,
            null,
            crearValores(movimiento)
        )
    }

    // Obtiene todos los movimientos almacenados en SQLite.
    fun obtenerTodos(): List<Movimiento> {

        // Lista donde se guardarán los movimientos encontrados.
        val movimientos = mutableListOf<Movimiento>()

        // Consulta la tabla de movimientos.
        // Se ordenan del ID más reciente al más antiguo.
        val cursor = dbHelper.readableDatabase.query(
            MisGastosDbHelper.TABLE_MOVIMIENTOS,
            null,
            null,
            null,
            null,
            null,
            "${MisGastosDbHelper.COLUMN_ID} DESC"
        )

        // Recorre los resultados obtenidos de la base de datos.
        cursor.use {
            while (it.moveToNext()) {

                // Convierte cada fila de SQLite en un objeto Movimiento.
                movimientos.add(crearMovimiento(it))
            }
        }

        return movimientos
    }

    // Actualiza un movimiento existente utilizando su ID.
    fun actualizar(movimiento: Movimiento): Boolean {

        val filas = dbHelper.writableDatabase.update(
            MisGastosDbHelper.TABLE_MOVIMIENTOS,
            crearValores(movimiento),

            // Solo actualiza el movimiento que tenga este ID.
            "${MisGastosDbHelper.COLUMN_ID} = ?",
            arrayOf(movimiento.id.toString())
        )

        // Si se modificó al menos una fila, la actualización fue exitosa.
        return filas > 0
    }

    // Elimina un movimiento utilizando su ID.
    fun eliminar(id: Long): Boolean {


        val cursor = dbHelper.readableDatabase.query(
            MisGastosDbHelper.TABLE_MOVIMIENTOS,
            arrayOf(
                MisGastosDbHelper.COLUMN_TIPO,
                MisGastosDbHelper.COLUMN_MONTO
            ),
            "${MisGastosDbHelper.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {

            if (it.moveToFirst()) {

                // Obtenemos el tipo del movimiento.
                val tipo = it.getString(
                    it.getColumnIndexOrThrow(
                        MisGastosDbHelper.COLUMN_TIPO
                    )
                )

                // Obtenemos el monto del movimiento.
                val monto = it.getDouble(
                    it.getColumnIndexOrThrow(
                        MisGastosDbHelper.COLUMN_MONTO
                    )
                )


                if (
                    tipo == TipoMovimiento.GASTO.name &&
                    monto > 5000.0
                ) {

                    Log.d(
                        "MovimientoRepository",
                        "DELETE bloqueado: gasto de Q$monto supera los Q5,000"
                    )

                    return false
                }
            }
        }


        // DELETE

        val filas = dbHelper.writableDatabase.delete(
            MisGastosDbHelper.TABLE_MOVIMIENTOS,
            "${MisGastosDbHelper.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )

        // Si se eliminó al menos una fila, devuelve true.
        return filas > 0
    }

    // Convierte un objeto Movimiento en ContentValues.
    // ContentValues es el formato utilizado para guardar
    // los datos en SQLite.
    private fun crearValores(movimiento: Movimiento) =
        ContentValues().apply {

            put(
                MisGastosDbHelper.COLUMN_TIPO,
                movimiento.tipo.name
            )

            put(
                MisGastosDbHelper.COLUMN_MONTO,
                movimiento.monto
            )

            put(
                MisGastosDbHelper.COLUMN_DESCRIPCION,
                movimiento.descripcion
            )

            put(
                MisGastosDbHelper.COLUMN_CATEGORIA,
                movimiento.categoria.name
            )

            put(
                MisGastosDbHelper.COLUMN_FECHA,
                movimiento.fecha
            )
        }

    // Hace el proceso contrario a crearValores().
    // Toma una fila obtenida de SQLite mediante un Cursor
    // y la convierte nuevamente en un objeto Movimiento.
    private fun crearMovimiento(cursor: Cursor) =
        Movimiento(

            // Obtiene el ID.
            id = cursor.getLong(
                cursor.getColumnIndexOrThrow(
                    MisGastosDbHelper.COLUMN_ID
                )
            ),

            // Obtiene el tipo guardado como texto y
            // lo convierte nuevamente al enum TipoMovimiento.
            tipo = TipoMovimiento.valueOf(
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        MisGastosDbHelper.COLUMN_TIPO
                    )
                )
            ),

            // Obtiene el monto.
            monto = cursor.getDouble(
                cursor.getColumnIndexOrThrow(
                    MisGastosDbHelper.COLUMN_MONTO
                )
            ),

            // Obtiene la descripción.
            descripcion = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    MisGastosDbHelper.COLUMN_DESCRIPCION
                )
            ),

            // Obtiene la categoría guardada como texto y
            // la convierte nuevamente al enum Categoria.
            categoria = Categoria.valueOf(
                cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        MisGastosDbHelper.COLUMN_CATEGORIA
                    )
                )
            ),

            // Obtiene la fecha.
            fecha = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    MisGastosDbHelper.COLUMN_FECHA
                )
            )
        )
}