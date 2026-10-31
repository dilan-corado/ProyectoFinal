package edu.umg.programacion2.misgastos.core

// Clase encargada de la lógica de negocio
// relacionada con los movimientos financieros.
//
// Permite:
// - Registrar movimientos.
// - Validarlos.
// - Obtener ingresos y gastos.
// - Buscar movimientos.
// - Calcular totales.
// - Calcular el balance.
// - Eliminar movimientos.
//
// Esta clase pertenece al Core y no depende de Android.
class ControlFinanzas {

    // Lista interna donde ControlFinanzas mantiene
    // los movimientos con los que está trabajando.
    //
    // mutableListOf permite agregar, eliminar
    // y modificar el contenido de la lista.
    private val movimientos =
        mutableListOf<Movimiento>()


    // --------------------------------
    // REGISTRAR MOVIMIENTO
    // --------------------------------

    // Recibe un movimiento, lo valida
    // y si es correcto lo agrega a la lista.
    fun registrar(movimiento: Movimiento) {

        // Primero verifica las reglas del movimiento.
        validarMovimiento(movimiento)

        // Si no hubo ningún error,
        // lo agrega a la lista.
        movimientos.add(movimiento)
    }


    // --------------------------------
    // CARGAR MOVIMIENTOS
    // --------------------------------

    // Reemplaza los movimientos actuales
    // por una lista recibida.
    //
    // En la aplicación se utiliza, por ejemplo,
    // para cargar en el Core los movimientos
    // recuperados desde SQLite.
    fun cargarMovimientos(
        lista: List<Movimiento>
    ) {

        // Valida cada movimiento antes de cargarlo.
        lista.forEach(::validarMovimiento)

        // Elimina los movimientos anteriores.
        movimientos.clear()

        // Agrega todos los movimientos recibidos.
        movimientos.addAll(lista)
    }


    // --------------------------------
    // OBTENER TODOS
    // --------------------------------

    // Devuelve una copia de la lista de movimientos.
    //
    // toList() evita entregar directamente
    // la lista mutable interna.
    fun obtenerMovimientos() =
        movimientos.toList()


    // --------------------------------
    // OBTENER INGRESOS
    // --------------------------------

    // Filtra la lista y devuelve únicamente
    // los movimientos de tipo INGRESO.
    fun obtenerIngresos() =
        movimientos.filter {
            it.tipo == TipoMovimiento.INGRESO
        }


    // --------------------------------
    // OBTENER GASTOS
    // --------------------------------

    // Filtra la lista y devuelve únicamente
    // los movimientos de tipo GASTO.
    fun obtenerGastos() =
        movimientos.filter {
            it.tipo == TipoMovimiento.GASTO
        }


    // --------------------------------
    // BUSCAR POR ID
    // --------------------------------

    // Busca un movimiento que tenga
    // el ID recibido.
    //
    // Si no lo encuentra, devuelve null.
    fun buscarPorId(id: Long) =
        movimientos.find {
            it.id == id
        }


    // --------------------------------
    // TOTAL DE INGRESOS
    // --------------------------------

    // Obtiene todos los ingresos
    // y suma sus montos.
    fun totalIngresos() =
        obtenerIngresos().sumOf {
            it.monto
        }


    // --------------------------------
    // TOTAL DE GASTOS
    // --------------------------------

    // Obtiene todos los gastos
    // y suma sus montos.
    fun totalGastos() =
        obtenerGastos().sumOf {
            it.monto
        }


    // --------------------------------
    // CALCULAR BALANCE
    // --------------------------------

    // Calcula cuánto dinero queda:
    //
    // BALANCE = INGRESOS - GASTOS
    fun calcularBalance() =
        totalIngresos() - totalGastos()


    // --------------------------------
    // ELIMINAR
    // --------------------------------

    // Elimina de la lista interna
    // el movimiento que tenga el ID recibido.
    //
    // Devuelve true si encontró y eliminó
    // algún movimiento.
    fun eliminar(id: Long) =
        movimientos.removeIf {
            it.id == id
        }


    // --------------------------------
    // VALIDACIÓN
    // --------------------------------

    // Método privado encargado de comprobar
    // que un movimiento cumpla las reglas
    // establecidas por el negocio.
    private fun validarMovimiento(
        movimiento: Movimiento
    ) {

        // El monto debe ser positivo.
        require(
            movimiento.monto > 0
        ) {
            "El monto debe ser mayor que cero"
        }


        // La descripción no puede estar vacía.
        require(
            movimiento.descripcion.isNotBlank()
        ) {
            "La descripción no puede estar vacía"
        }


        // La fecha no puede estar vacía.
        require(
            movimiento.fecha.isNotBlank()
        ) {
            "La fecha no puede estar vacía"
        }


        // Comprueba que la categoría sea compatible
        // con el tipo del movimiento.
        //
        // Ejemplo correcto:
        // GASTO + COMIDA
        //
        // Ejemplo incorrecto:
        // INGRESO + COMIDA
        require(
            movimiento.categoria.tipo ==
                    movimiento.tipo
        ) {
            "La categoría no corresponde al tipo de movimiento"
        }
    }
}