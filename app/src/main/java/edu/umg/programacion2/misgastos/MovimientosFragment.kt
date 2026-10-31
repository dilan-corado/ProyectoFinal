package edu.umg.programacion2.misgastos

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.umg.programacion2.misgastos.core.Movimiento
import edu.umg.programacion2.misgastos.core.TipoMovimiento
import edu.umg.programacion2.misgastos.data.MovimientoRepository

// Fragment encargado de mostrar el historial de movimientos.
// Permite:
// - Ver todos los movimientos.
// - Filtrar ingresos y gastos.
// - Editar movimientos.
// - Eliminar movimientos.
class MovimientosFragment : Fragment(R.layout.fragment_movimientos) {

    // Repositorio utilizado para obtener y eliminar
    // movimientos almacenados en SQLite.
    private lateinit var repository: MovimientoRepository

    // RecyclerView donde se muestra la lista.
    private lateinit var recyclerMovimientos: RecyclerView

    // Adapter que convierte los objetos Movimiento
    // en elementos visuales del RecyclerView.
    private lateinit var movimientoAdapter: MovimientoAdapter

    // Botones utilizados para filtrar la lista.
    private lateinit var btnTodos: Button
    private lateinit var btnIngresos: Button
    private lateinit var btnGastos: Button

    // Texto que aparece cuando no existen
    // movimientos para mostrar.
    private lateinit var tvSinMovimientos: TextView

    // El filtro inicial muestra todos los movimientos.
    private var filtroActual = FiltroMovimiento.TODOS


    // Enum que define los tres filtros disponibles.
    private enum class FiltroMovimiento {
        TODOS,
        INGRESOS,
        GASTOS
    }


    // Se ejecuta cuando la vista del Fragment está creada.
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el repositorio para acceder a SQLite.
        repository = MovimientoRepository(requireContext())

        // Configura los elementos de esta pantalla.
        iniciarVistas(view)
        configurarRecyclerView()
        configurarFiltros()

        // Obtiene y muestra los movimientos.
        actualizarLista()
    }


    // Se ejecuta cuando el Fragment vuelve
    // a estar visible.
    override fun onResume() {
        super.onResume()

        // Actualiza la lista para mostrar
        // cualquier cambio reciente.
        if (
            ::repository.isInitialized &&
            ::movimientoAdapter.isInitialized
        ) {
            actualizarLista()
        }
    }


    // --------------------------------
    // INICIALIZAR VISTAS
    // --------------------------------

    // Conecta las variables Kotlin con
    // fragment_movimientos.xml.
    private fun iniciarVistas(view: View) {

        recyclerMovimientos =
            view.findViewById(R.id.rvMovimientos)

        btnTodos =
            view.findViewById(R.id.btnTodos)

        btnIngresos =
            view.findViewById(R.id.btnIngresos)

        btnGastos =
            view.findViewById(R.id.btnGastos)

        tvSinMovimientos =
            view.findViewById(R.id.tvSinMovimientos)
    }


    // --------------------------------
    // RECYCLERVIEW
    // --------------------------------

    // Configura la lista donde se mostrarán
    // los movimientos.
    private fun configurarRecyclerView() {

        // Inicialmente el Adapter recibe una lista vacía.
        movimientoAdapter = MovimientoAdapter(
            emptyList(),

            // Acción que se ejecuta al presionar Editar.
            onEditar = { movimiento ->

                // MainActivity abre InicioFragment
                // enviando el movimiento seleccionado.
                (requireActivity() as MainActivity)
                    .editarMovimiento(movimiento)
            },

            // Acción que se ejecuta al presionar Eliminar.
            onEliminar = { movimiento ->

                // Primero muestra una confirmación.
                confirmarEliminacion(movimiento)
            }
        )


        recyclerMovimientos.apply {

            // Muestra los movimientos en una lista vertical.
            layoutManager =
                LinearLayoutManager(requireContext())

            // Conecta el RecyclerView con el Adapter.
            adapter = movimientoAdapter

            setHasFixedSize(false)
        }
    }


    // --------------------------------
    // FILTROS
    // --------------------------------

    // Configura los botones:
    // Todos, Ingresos y Gastos.
    private fun configurarFiltros() {

        btnTodos.setOnClickListener {
            cambiarFiltro(
                FiltroMovimiento.TODOS
            )
        }

        btnIngresos.setOnClickListener {
            cambiarFiltro(
                FiltroMovimiento.INGRESOS
            )
        }

        btnGastos.setOnClickListener {
            cambiarFiltro(
                FiltroMovimiento.GASTOS
            )
        }

        // Marca visualmente el filtro seleccionado.
        actualizarEstiloFiltros()
    }


    // Cambia el filtro actual.
    private fun cambiarFiltro(
        filtro: FiltroMovimiento
    ) {

        filtroActual = filtro

        // Actualiza el color de los botones.
        actualizarEstiloFiltros()

        // Actualiza los datos mostrados.
        actualizarLista()
    }


    // Cambia visualmente los botones dependiendo
    // del filtro que esté seleccionado.
    private fun actualizarEstiloFiltros() {

        aplicarEstilo(
            btnTodos,
            filtroActual == FiltroMovimiento.TODOS,
            R.color.navy
        )

        aplicarEstilo(
            btnIngresos,
            filtroActual == FiltroMovimiento.INGRESOS,
            R.color.teal
        )

        aplicarEstilo(
            btnGastos,
            filtroActual == FiltroMovimiento.GASTOS,
            R.color.expense_red
        )
    }


    // Aplica los colores correspondientes
    // a un botón dependiendo de si está activo.
    private fun aplicarEstilo(
        boton: Button,
        activo: Boolean,
        colorActivo: Int
    ) {

        // Si está seleccionado utiliza su color.
        // Si no, utiliza un color claro.
        val fondo =
            if (activo)
                colorActivo
            else
                R.color.border_light

        // El texto es blanco cuando está activo.
        val texto =
            if (activo)
                R.color.white
            else
                R.color.text_secondary


        boton.backgroundTintList =
            ContextCompat.getColorStateList(
                requireContext(),
                fondo
            )

        boton.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                texto
            )
        )
    }


    // --------------------------------
    // OBTENER Y MOSTRAR MOVIMIENTOS
    // --------------------------------

    private fun actualizarLista() {

        // Obtiene TODOS los movimientos
        // almacenados en SQLite.
        val movimientos =
            repository.obtenerTodos()


        // Filtra los movimientos dependiendo
        // del botón seleccionado.
        val filtrados =
            when (filtroActual) {

                // No aplica ningún filtro.
                FiltroMovimiento.TODOS ->
                    movimientos


                // Solo muestra ingresos.
                FiltroMovimiento.INGRESOS ->
                    movimientos.filter {
                        it.tipo ==
                                TipoMovimiento.INGRESO
                    }


                // Solo muestra gastos.
                FiltroMovimiento.GASTOS ->
                    movimientos.filter {
                        it.tipo ==
                                TipoMovimiento.GASTO
                    }
            }


        // Envía la lista filtrada al Adapter
        // para que RecyclerView la muestre.
        movimientoAdapter.actualizarLista(
            filtrados
        )


        // Comprueba si hay movimientos.
        val listaVacia =
            filtrados.isEmpty()


        // Si no existen movimientos,
        // muestra el mensaje correspondiente.
        tvSinMovimientos.visibility =
            if (listaVacia)
                View.VISIBLE
            else
                View.GONE


        // Si existen movimientos,
        // muestra el RecyclerView.
        recyclerMovimientos.visibility =
            if (listaVacia)
                View.GONE
            else
                View.VISIBLE


        if (listaVacia) {
            tvSinMovimientos.text =
                mensajeListaVacia()
        }
    }


    // Devuelve un mensaje diferente dependiendo
    // del filtro seleccionado.
    private fun mensajeListaVacia() =
        when (filtroActual) {

            FiltroMovimiento.TODOS ->
                "No hay movimientos registrados."

            FiltroMovimiento.INGRESOS ->
                "No hay ingresos registrados."

            FiltroMovimiento.GASTOS ->
                "No hay gastos registrados."
        }


    // --------------------------------
    // ELIMINAR MOVIMIENTO
    // --------------------------------

    // Muestra un cuadro de confirmación antes
    // de eliminar un movimiento.
    private fun confirmarEliminacion(
        movimiento: Movimiento
    ) {

        AlertDialog.Builder(requireContext())

            .setTitle(
                "Eliminar movimiento"
            )

            .setMessage(
                "¿Deseas eliminar \"${movimiento.descripcion}\"?"
            )

            // Si confirma, elimina el movimiento.
            .setPositiveButton(
                "Eliminar"
            ) { _, _ ->

                eliminarMovimiento(
                    movimiento
                )
            }

            // Si cancela, no hace nada.
            .setNegativeButton(
                "Cancelar",
                null
            )

            .show()
    }


    // Elimina realmente el movimiento de SQLite.
    private fun eliminarMovimiento(
        movimiento: Movimiento
    ) {

        // El Repository elimina el movimiento
        // utilizando su ID.
        if (
            repository.eliminar(
                movimiento.id
            )
        ) {

            mensaje(
                "Movimiento eliminado"
            )

            // Vuelve a consultar SQLite
            // y actualiza la lista.
            actualizarLista()

        } else {

            mensaje(
                "No se pudo eliminar"
            )
        }
    }


    // --------------------------------
    // MENSAJES
    // --------------------------------

    // Muestra un Toast en pantalla.
    private fun mensaje(texto: String) =
        Toast.makeText(
            requireContext(),
            texto,
            Toast.LENGTH_SHORT
        ).show()
}