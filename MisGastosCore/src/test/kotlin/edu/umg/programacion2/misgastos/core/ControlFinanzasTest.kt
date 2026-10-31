package edu.umg.programacion2.misgastos.core

// Importamos las herramientas de JUnit 5
// necesarias para realizar las pruebas.
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


// Clase que contiene las pruebas unitarias
// de ControlFinanzas.
//
// Estas pruebas permiten comprobar automáticamente
// que la lógica de negocio funciona correctamente.
class ControlFinanzasTest {


    // --------------------------------
    // FUNCIÓN AUXILIAR
    // --------------------------------

    // Esta función crea movimientos de prueba
    // para evitar repetir el mismo código
    // en cada prueba.
    private fun movimiento(
        tipo: TipoMovimiento,
        monto: Double,
        descripcion: String = "Prueba",

        // Si es INGRESO utiliza SALARIO por defecto.
        // Si es GASTO utiliza COMIDA por defecto.
        categoria: Categoria =
            if (tipo == TipoMovimiento.INGRESO)
                Categoria.SALARIO
            else
                Categoria.COMIDA

    ) = Movimiento(
        tipo = tipo,
        monto = monto,
        descripcion = descripcion,
        categoria = categoria,
        fecha = "30/10/2026"
    )


    // --------------------------------
    // PRUEBA 1
    // REGISTRAR INGRESO
    // --------------------------------

    @Test
    fun `registrar ingreso correctamente`() {

        val control = ControlFinanzas()

        control.registrar(
            movimiento(
                TipoMovimiento.INGRESO,
                2000.0
            )
        )

        // Esperamos que exista exactamente
        // 1 movimiento registrado.
        assertEquals(
            1,
            control.obtenerMovimientos().size
        )
    }


    // --------------------------------
    // PRUEBA 2
    // REGISTRAR GASTO
    // --------------------------------

    @Test
    fun `registrar gasto correctamente`() {

        val control = ControlFinanzas()

        control.registrar(
            movimiento(
                TipoMovimiento.GASTO,
                500.0
            )
        )

        // Esperamos exactamente 1 gasto.
        assertEquals(
            1,
            control.obtenerGastos().size
        )
    }


    // --------------------------------
    // PRUEBA 3
    // TOTAL DE INGRESOS
    // --------------------------------

    @Test
    fun `calcular total de ingresos`() {

        val control = ControlFinanzas()

        // Registramos Q1,000.
        control.registrar(
            movimiento(
                TipoMovimiento.INGRESO,
                1000.0
            )
        )

        // Registramos Q500.
        control.registrar(
            movimiento(
                TipoMovimiento.INGRESO,
                500.0
            )
        )

        // 1000 + 500 = 1500
        assertEquals(
            1500.0,
            control.totalIngresos()
        )
    }


    // --------------------------------
    // PRUEBA 4
    // TOTAL DE GASTOS
    // --------------------------------

    @Test
    fun `calcular total de gastos`() {

        val control = ControlFinanzas()

        // Registramos Q300 de gasto.
        control.registrar(
            movimiento(
                TipoMovimiento.GASTO,
                300.0
            )
        )

        // Registramos Q200 de gasto.
        control.registrar(
            movimiento(
                TipoMovimiento.GASTO,
                200.0
            )
        )

        // 300 + 200 = 500
        assertEquals(
            500.0,
            control.totalGastos()
        )
    }


    // --------------------------------
    // PRUEBA 5
    // CALCULAR BALANCE
    // --------------------------------

    @Test
    fun `calcular balance`() {

        val control = ControlFinanzas()

        // Ingreso de Q2,000.
        control.registrar(
            movimiento(
                TipoMovimiento.INGRESO,
                2000.0
            )
        )

        // Gasto de Q500.
        control.registrar(
            movimiento(
                TipoMovimiento.GASTO,
                500.0
            )
        )

        // Balance:
        // 2000 - 500 = 1500
        assertEquals(
            1500.0,
            control.calcularBalance()
        )
    }


    // --------------------------------
    // PRUEBA 6
    // NO PERMITIR MONTO CERO
    // --------------------------------

    @Test
    fun `no permitir monto cero`() {

        val control = ControlFinanzas()

        // Esperamos que ControlFinanzas lance
        // una IllegalArgumentException.
        //
        // Esto comprueba la validación:
        // monto > 0
        assertThrows(
            IllegalArgumentException::class.java
        ) {

            control.registrar(
                movimiento(
                    TipoMovimiento.GASTO,
                    0.0
                )
            )
        }
    }


    // --------------------------------
    // PRUEBA 7
    // NO PERMITIR DESCRIPCIÓN VACÍA
    // --------------------------------

    @Test
    fun `no permitir descripcion vacia`() {

        val control = ControlFinanzas()

        // Una descripción vacía debe producir
        // una IllegalArgumentException.
        assertThrows(
            IllegalArgumentException::class.java
        ) {

            control.registrar(
                movimiento(
                    TipoMovimiento.GASTO,
                    100.0,
                    descripcion = ""
                )
            )
        }
    }


    // --------------------------------
    // PRUEBA 8
    // NO PERMITIR CATEGORÍA INCORRECTA
    // --------------------------------

    @Test
    fun `no permitir categoria incorrecta`() {

        val control = ControlFinanzas()

        // Estamos intentando crear:
        //
        // tipo = INGRESO
        // categoría = COMIDA
        //
        // Pero COMIDA pertenece a GASTO.
        //
        // Por eso esperamos una excepción.
        assertThrows(
            IllegalArgumentException::class.java
        ) {

            control.registrar(
                movimiento(
                    TipoMovimiento.INGRESO,
                    1000.0,
                    categoria = Categoria.COMIDA
                )
            )
        }
    }


    // --------------------------------
    // PRUEBA 9
    // ELIMINAR MOVIMIENTO
    // --------------------------------

    @Test
    fun `eliminar movimiento`() {

        val control = ControlFinanzas()


        // Creamos un movimiento con ID = 1.
        val movimiento = Movimiento(
            id = 1,
            tipo = TipoMovimiento.GASTO,
            monto = 100.0,
            descripcion = "Prueba",
            categoria = Categoria.COMIDA,
            fecha = "30/10/2026"
        )


        // Lo registramos.
        control.registrar(movimiento)


        // Esperamos que eliminar(1)
        // devuelva true.
        assertTrue(
            control.eliminar(1)
        )


        // Después de eliminarlo,
        // esperamos que la lista esté vacía.
        assertTrue(
            control.obtenerMovimientos()
                .isEmpty()
        )
    }
}