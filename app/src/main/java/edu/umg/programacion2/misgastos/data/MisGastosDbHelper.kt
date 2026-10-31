package edu.umg.programacion2.misgastos.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Clase encargada de crear y administrar
// la base de datos SQLite de la aplicación.
class MisGastosDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Se ejecuta cuando la base de datos se crea por primera vez.
    override fun onCreate(db: SQLiteDatabase) {

        // Crea la tabla "movimientos".
        // Aquí se almacenan los ingresos y gastos del usuario.
        db.execSQL(
            """
            CREATE TABLE $TABLE_MOVIMIENTOS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIPO TEXT NOT NULL,
                $COLUMN_MONTO REAL NOT NULL,
                $COLUMN_DESCRIPCION TEXT NOT NULL,
                $COLUMN_CATEGORIA TEXT NOT NULL,
                $COLUMN_FECHA TEXT NOT NULL
            )
            """.trimIndent()
        )

        // Crea también la tabla donde se almacenan
        // los presupuestos mensuales.
        crearTablaPresupuestos(db)
    }

    // Método encargado específicamente de crear
    // la tabla de presupuestos.
    private fun crearTablaPresupuestos(db: SQLiteDatabase) {

        // IF NOT EXISTS evita intentar crear nuevamente
        // la tabla si ya existe.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_PRESUPUESTOS (
                $COLUMN_MES TEXT PRIMARY KEY,
                $COLUMN_PRESUPUESTO REAL NOT NULL
                    CHECK ($COLUMN_PRESUPUESTO >= 0)
            )
            """.trimIndent()
        )

        // La tabla presupuestos contiene:
        // - mes: identifica el mes del presupuesto.
        // - presupuesto: cantidad de dinero presupuestada.
        //
        // CHECK evita guardar presupuestos negativos.
    }

    // Se ejecuta cuando cambia la versión de la base de datos.
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        // Si la base de datos instalada es anterior
        // a la versión 2, se agrega la tabla de presupuestos.
        if (oldVersion < 2) {
            crearTablaPresupuestos(db)
        }
    }

    // Contiene las constantes utilizadas para identificar
    // la base de datos, sus tablas y sus columnas.
    companion object {

        // Nombre de la base de datos SQLite.
        const val DATABASE_NAME = "misgastos.db"

        // Versión actual de la base de datos.
        const val DATABASE_VERSION = 2

        // -------------------------
        // TABLA DE MOVIMIENTOS
        // -------------------------

        const val TABLE_MOVIMIENTOS = "movimientos"

        // Identificador único del movimiento.
        const val COLUMN_ID = "id"

        // Tipo de movimiento: INGRESO o GASTO.
        const val COLUMN_TIPO = "tipo"

        // Cantidad de dinero del movimiento.
        const val COLUMN_MONTO = "monto"

        // Descripción del movimiento.
        const val COLUMN_DESCRIPCION = "descripcion"

        // Categoría del movimiento.
        const val COLUMN_CATEGORIA = "categoria"

        // Fecha en la que se realizó el movimiento.
        const val COLUMN_FECHA = "fecha"

        // -------------------------
        // TABLA DE PRESUPUESTOS
        // -------------------------

        const val TABLE_PRESUPUESTOS = "presupuestos"

        // Mes al que pertenece el presupuesto.
        const val COLUMN_MES = "mes"

        // Cantidad de dinero presupuestada para ese mes.
        const val COLUMN_PRESUPUESTO = "presupuesto"
    }
}