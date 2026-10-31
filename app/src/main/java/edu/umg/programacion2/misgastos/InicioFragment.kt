package edu.umg.programacion2.misgastos

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment

// Clases que vienen de MisGastosCore.
// Contienen el modelo y la lógica de negocio.
import edu.umg.programacion2.misgastos.core.Categoria
import edu.umg.programacion2.misgastos.core.ControlFinanzas
import edu.umg.programacion2.misgastos.core.Movimiento
import edu.umg.programacion2.misgastos.core.TipoMovimiento

// Repositorio utilizado para guardar y obtener
// movimientos de la base de datos SQLite.
import edu.umg.programacion2.misgastos.data.MovimientoRepository

import java.util.Calendar
import java.util.Locale


// Fragment que representa la pantalla principal (Inicio).
// Aquí el usuario puede registrar o editar movimientos
// y también visualizar ingresos, gastos y balance.
class InicioFragment : Fragment(R.layout.fragment_inicio) {

    // Repositorio que comunica esta pantalla con SQLite.
    private lateinit var repository: MovimientoRepository

    // Clase del Core que contiene la lógica de negocio
    // para trabajar con los movimientos y calcular totales.
    private val controlFinanzas = ControlFinanzas()


    // -----------------------------
    // COMPONENTES DE LA INTERFAZ
    // -----------------------------

    // Permite seleccionar si es ingreso o gasto.
    private lateinit var spinnerTipo: Spinner

    // Permite seleccionar la categoría.
    private lateinit var spinnerCategoria: Spinner

    // Campo donde el usuario escribe el monto.
    private lateinit var etMonto: EditText

    // Campo donde escribe la descripción.
    private lateinit var etDescripcion: EditText

    // Campo donde se muestra/selecciona la fecha.
    private lateinit var etFecha: EditText

    // Botón para guardar o actualizar.
    private lateinit var btnGuardar: Button

    // TextViews utilizados para mostrar el resumen financiero.
    private lateinit var tvBalance: TextView
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView


    // Lista de categorías disponibles dependiendo
    // de si el usuario selecciona ingreso o gasto.
    private var categoriasActuales = emptyList<Categoria>()

    // Guarda temporalmente el movimiento que se está editando.
    // Si es null significa que estamos creando uno nuevo.
    private var movimientoEnEdicion: Movimiento? = null


    // Se ejecuta cuando la vista del Fragment ya fue creada.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el repositorio para poder acceder a SQLite.
        repository = MovimientoRepository(requireContext())

        // Configura los componentes de la pantalla.
        iniciarVistas(view)
        configurarSpinnerTipo()
        configurarFecha()

        // Cuando el usuario presiona el botón Guardar,
        // se intenta guardar o actualizar el movimiento.
        btnGuardar.setOnClickListener {
            guardarOActualizarMovimiento()
        }

        // Muestra los ingresos, gastos y balance actuales.
        actualizarResumen()

        // Comprueba si llegamos a esta pantalla
        // para editar un movimiento existente.
        cargarMovimientoRecibido()
    }


    // Se ejecuta cuando el Fragment vuelve a estar visible.
    override fun onResume() {
        super.onResume()

        // Actualiza nuevamente los totales por si hubo cambios.
        if (::repository.isInitialized) {
            actualizarResumen()
        }
    }


    // -----------------------------
    // INICIALIZAR VISTAS
    // -----------------------------

    // Conecta las variables Kotlin con los componentes
    // definidos en fragment_inicio.xml.
    private fun iniciarVistas(view: View) {

        spinnerTipo = view.findViewById(R.id.spinnerTipo)
        spinnerCategoria = view.findViewById(R.id.spinnerCategoria)

        etMonto = view.findViewById(R.id.etMonto)
        etDescripcion = view.findViewById(R.id.etDescripcion)
        etFecha = view.findViewById(R.id.etFecha)

        btnGuardar = view.findViewById(R.id.btnGuardar)

        tvBalance = view.findViewById(R.id.tvBalance)
        tvIngresos = view.findViewById(R.id.tvIngresos)
        tvGastos = view.findViewById(R.id.tvGastos)
    }


    // -----------------------------
    // TIPO DE MOVIMIENTO
    // -----------------------------

    // Configura el Spinner de tipo.
    private fun configurarSpinnerTipo() {

        // Opciones que verá el usuario.
        spinnerTipo.adapter = crearAdapter(
            listOf(
                "Selecciona un tipo",
                "Ingreso",
                "Gasto"
            )
        )

        // Inicialmente no se muestran categorías.
        mostrarCategoriaInicial()

        // Detecta cuando el usuario selecciona
        // una opción del Spinner.
        spinnerTipo.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    // Dependiendo de la opción seleccionada,
                    // se muestran las categorías correspondientes.
                    when (position) {

                        // Ingreso
                        1 -> actualizarCategorias(
                            TipoMovimiento.INGRESO
                        )

                        // Gasto
                        2 -> actualizarCategorias(
                            TipoMovimiento.GASTO
                        )

                        // Ninguna opción seleccionada.
                        else -> mostrarCategoriaInicial()
                    }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) = Unit
            }
    }


    // Crea un Adapter para mostrar opciones
    // dentro de los Spinners.
    private fun crearAdapter(opciones: List<String>) =
        ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            opciones
        ).apply {

            setDropDownViewResource(
                R.layout.spinner_item
            )
        }


    // Muestra el mensaje inicial del Spinner de categorías.
    private fun mostrarCategoriaInicial() {

        categoriasActuales = emptyList()

        spinnerCategoria.adapter =
            crearAdapter(
                listOf("Selecciona una categoría")
            )
    }


    // Obtiene las categorías dependiendo
    // del tipo de movimiento seleccionado.
    private fun actualizarCategorias(
        tipo: TipoMovimiento
    ) {

        // Esta función viene del Core.
        categoriasActuales =
            Categoria.obtenerPorTipo(tipo)

        // Crea la lista que se mostrará en el Spinner.
        val opciones =
            mutableListOf(
                "Selecciona una categoría"
            ).apply {

                addAll(
                    categoriasActuales.map {
                        it.nombre
                    }
                )
            }

        spinnerCategoria.adapter =
            crearAdapter(opciones)
    }


    // -----------------------------
    // FECHA
    // -----------------------------

    // Configura el campo de fecha.
    private fun configurarFecha() {

        // Coloca automáticamente la fecha actual.
        colocarFechaActual()

        etFecha.apply {

            // Evita escribir manualmente en el campo.
            isFocusable = false

            // Permite hacer clic.
            isClickable = true

            // Al hacer clic muestra el calendario.
            setOnClickListener {
                mostrarCalendario()
            }
        }
    }


    // Obtiene la fecha actual del dispositivo
    // y la coloca en el campo de fecha.
    private fun colocarFechaActual() {

        val calendario =
            Calendar.getInstance()

        etFecha.setText(
            formatoFecha(
                calendario.get(
                    Calendar.DAY_OF_MONTH
                ),
                calendario.get(
                    Calendar.MONTH
                ) + 1,
                calendario.get(
                    Calendar.YEAR
                )
            )
        )
    }


    // Abre un DatePickerDialog para que
    // el usuario pueda seleccionar una fecha.
    private fun mostrarCalendario() {

        val calendario =
            Calendar.getInstance()

        // Divide la fecha actual usando "/".
        val partes =
            etFecha.text.toString().split("/")

        var dia =
            calendario.get(Calendar.DAY_OF_MONTH)

        var mes =
            calendario.get(Calendar.MONTH)

        var anio =
            calendario.get(Calendar.YEAR)

        // Si ya existe una fecha válida,
        // utiliza esa fecha como punto inicial.
        if (partes.size == 3) {

            dia =
                partes[0].toIntOrNull() ?: dia

            mes =
                (partes[1].toIntOrNull()
                    ?: (mes + 1)) - 1

            anio =
                partes[2].toIntOrNull() ?: anio
        }

        // Muestra el calendario.
        DatePickerDialog(
            requireContext(),

            { _, anioElegido,
              mesElegido,
              diaElegido ->

                // Coloca la fecha seleccionada
                // dentro del EditText.
                etFecha.setText(
                    formatoFecha(
                        diaElegido,
                        mesElegido + 1,
                        anioElegido
                    )
                )
            },

            anio,
            mes,
            dia

        ).show()
    }


    // -----------------------------
    // GUARDAR / ACTUALIZAR
    // -----------------------------

    // Función principal para registrar
    // o modificar un movimiento.
    private fun guardarOActualizarMovimiento() {

        // Obtiene los datos ingresados por el usuario.
        val montoTexto =
            etMonto.text.toString().trim()

        val descripcion =
            etDescripcion.text.toString().trim()

        val fecha =
            etFecha.text.toString().trim()


        // Verifica que los campos no estén vacíos.
        if (
            montoTexto.isEmpty() ||
            descripcion.isEmpty() ||
            fecha.isEmpty()
        ) {

            mensaje("Completa todos los campos")
            return
        }


        // Convierte el monto escrito a Double.
        val monto =
            montoTexto
                .replace(',', '.')
                .toDoubleOrNull()


        // Verifica que el monto sea válido y mayor a cero.
        if (
            monto == null ||
            monto <= 0 ||
            !monto.isFinite()
        ) {

            mensaje("Ingresa un monto válido")
            return
        }


        // Obtiene el tipo seleccionado.
        val tipo =
            when (
                spinnerTipo.selectedItemPosition
            ) {

                1 -> TipoMovimiento.INGRESO

                2 -> TipoMovimiento.GASTO

                else -> {
                    mensaje(
                        "Selecciona un tipo de movimiento"
                    )
                    return
                }
            }


        // Obtiene la categoría seleccionada.
        val posicionCategoria =
            spinnerCategoria.selectedItemPosition


        // Comprueba que se haya seleccionado
        // una categoría válida.
        if (
            posicionCategoria <= 0 ||
            categoriasActuales.isEmpty()
        ) {

            mensaje(
                "Selecciona una categoría"
            )
            return
        }


        // Comprueba si estamos editando
        // un movimiento existente.
        val movimientoAnterior =
            movimientoEnEdicion


        // Crea el objeto Movimiento.
        val movimiento = Movimiento(

            // Si estamos editando conserva el ID.
            // Si es nuevo utiliza 0.
            id = movimientoAnterior?.id ?: 0,

            tipo = tipo,
            monto = monto,
            descripcion = descripcion,

            categoria =
                categoriasActuales[
                    posicionCategoria - 1
                ],

            fecha = fecha
        )


        try {

            // Utiliza ControlFinanzas del Core
            // para validar el movimiento.
            ControlFinanzas()
                .registrar(movimiento)


            // Si no existe movimiento anterior,
            // guarda uno nuevo.
            //
            // Si existe, actualiza el movimiento.
            val guardado =
                if (movimientoAnterior == null) {

                    repository
                        .guardar(movimiento) != -1L

                } else {

                    repository
                        .actualizar(movimiento)
                }


            // Comprueba si SQLite realizó
            // correctamente la operación.
            if (guardado) {

                mensaje(
                    if (movimientoAnterior == null)
                        "Movimiento guardado"
                    else
                        "Movimiento actualizado"
                )

                // Limpia los campos.
                limpiarFormulario()

                // Recalcula ingresos, gastos y balance.
                actualizarResumen()

            } else {

                mensaje(
                    if (movimientoAnterior == null)
                        "Error al guardar"
                    else
                        "No se pudo actualizar"
                )
            }

        } catch (e: IllegalArgumentException) {

            // Muestra el error si el Core
            // considera que el movimiento no es válido.
            mensaje(
                e.message ?: "Movimiento inválido"
            )
        }
    }


    // -----------------------------
    // EDITAR MOVIMIENTO
    // -----------------------------

    // Comprueba si otro Fragment envió
    // información de un movimiento para editar.
    private fun cargarMovimientoRecibido() {

        val args = arguments ?: return

        val id =
            args.getLong(ARG_ID, -1L)

        // Si el ID no es válido, no continúa.
        if (id <= 0) return


        // Recupera el tipo.
        val tipo =
            enumOrNull<TipoMovimiento>(
                args.getString(ARG_TIPO)
            ) ?: return


        // Recupera la categoría.
        val categoria =
            enumOrNull<Categoria>(
                args.getString(ARG_CATEGORIA)
            ) ?: return


        val descripcion =
            args.getString(ARG_DESCRIPCION)
                ?: return

        val fecha =
            args.getString(ARG_FECHA)
                ?: return


        // Reconstruye el Movimiento
        // con los datos recibidos.
        cargarMovimientoParaEditar(
            Movimiento(
                id = id,
                tipo = tipo,

                monto =
                    args.getDouble(ARG_MONTO),

                descripcion = descripcion,
                categoria = categoria,
                fecha = fecha
            )
        )
    }


    // Coloca en el formulario los datos
    // del movimiento que se quiere editar.
    private fun cargarMovimientoParaEditar(
        movimiento: Movimiento
    ) {

        // Indica que estamos en modo edición.
        movimientoEnEdicion = movimiento


        // Selecciona Ingreso o Gasto.
        spinnerTipo.setSelection(
            if (
                movimiento.tipo ==
                TipoMovimiento.INGRESO
            ) 1 else 2
        )


        // Carga las categorías correspondientes.
        actualizarCategorias(
            movimiento.tipo
        )


        // Busca la posición de la categoría actual.
        val posicionCategoria =
            categoriasActuales.indexOf(
                movimiento.categoria
            )


        if (posicionCategoria >= 0) {

            spinnerCategoria.setSelection(
                posicionCategoria + 1
            )
        }


        // Coloca el monto.
        etMonto.setText(
            String.format(
                Locale.US,
                "%.2f",
                movimiento.monto
            )
        )


        // Coloca descripción y fecha.
        etDescripcion.setText(
            movimiento.descripcion
        )

        etFecha.setText(
            movimiento.fecha
        )


        // Cambia el texto del botón porque
        // ahora estamos editando.
        btnGuardar.text =
            "Guardar cambios"


        mensaje(
            "Editando ${movimiento.descripcion}"
        )
    }


    // -----------------------------
    // RESUMEN FINANCIERO
    // -----------------------------

    // Actualiza los ingresos, gastos y balance
    // que aparecen en la pantalla principal.
    private fun actualizarResumen() {

        // Repository obtiene los movimientos desde SQLite.
        // Luego se cargan en ControlFinanzas.
        controlFinanzas.cargarMovimientos(
            repository.obtenerTodos()
        )


        // El Core calcula los ingresos.
        tvIngresos.text =
            formatoQuetzales(
                controlFinanzas.totalIngresos()
            )


        // El Core calcula los gastos.
        tvGastos.text =
            formatoQuetzales(
                controlFinanzas.totalGastos()
            )


        // El Core calcula:
        // balance = ingresos - gastos.
        tvBalance.text =
            formatoQuetzales(
                controlFinanzas.calcularBalance()
            )
    }


    // -----------------------------
    // LIMPIAR FORMULARIO
    // -----------------------------

    // Deja el formulario listo para
    // registrar otro movimiento.
    private fun limpiarFormulario() {

        // Ya no estamos editando.
        movimientoEnEdicion = null

        // Limpia los campos.
        etMonto.text.clear()
        etDescripcion.text.clear()

        // Reinicia los Spinners.
        spinnerTipo.setSelection(0)
        mostrarCategoriaInicial()

        // Coloca nuevamente la fecha actual.
        colocarFechaActual()

        // Regresa el botón a su texto original.
        btnGuardar.text =
            "Guardar movimiento"
    }


    // -----------------------------
    // FUNCIONES AUXILIARES
    // -----------------------------

    // Da formato a una fecha:
    // día/mes/año.
    private fun formatoFecha(
        dia: Int,
        mes: Int,
        anio: Int
    ) =
        String.format(
            Locale.US,
            "%02d/%02d/%04d",
            dia,
            mes,
            anio
        )


    // Convierte un número al formato de quetzales.
    // Ejemplo: 1500 -> Q1,500.00
    private fun formatoQuetzales(
        monto: Double
    ) =
        "Q${
            String.format(
                Locale.US,
                "%,.2f",
                monto
            )
        }"


    // Muestra mensajes pequeños (Toast)
    // en la pantalla.
    private fun mensaje(texto: String) =
        Toast.makeText(
            requireContext(),
            texto,
            Toast.LENGTH_SHORT
        ).show()


    // Convierte un String nuevamente en un Enum.
    // Si el valor no existe, devuelve null
    // en lugar de provocar un error.
    private inline fun <
            reified T : Enum<T>
            > enumOrNull(
        valor: String?
    ): T? =

        valor?.let {

            try {

                enumValueOf<T>(it)

            } catch (
                _: IllegalArgumentException
            ) {

                null
            }
        }


    // -----------------------------
    // DATOS PARA EDITAR MOVIMIENTOS
    // -----------------------------

    companion object {

        // Claves utilizadas para enviar información
        // de un Fragment a otro.
        private const val ARG_ID =
            "movimiento_id"

        private const val ARG_TIPO =
            "movimiento_tipo"

        private const val ARG_MONTO =
            "movimiento_monto"

        private const val ARG_DESCRIPCION =
            "movimiento_descripcion"

        private const val ARG_CATEGORIA =
            "movimiento_categoria"

        private const val ARG_FECHA =
            "movimiento_fecha"


        // Crea un InicioFragment preparado
        // para editar un movimiento existente.
        fun nuevaInstanciaParaEditar(
            movimiento: Movimiento
        ) = InicioFragment().apply {

            // Bundle permite enviar los datos
            // al nuevo Fragment.
            arguments = Bundle().apply {

                putLong(
                    ARG_ID,
                    movimiento.id
                )

                putString(
                    ARG_TIPO,
                    movimiento.tipo.name
                )

                putDouble(
                    ARG_MONTO,
                    movimiento.monto
                )

                putString(
                    ARG_DESCRIPCION,
                    movimiento.descripcion
                )

                putString(
                    ARG_CATEGORIA,
                    movimiento.categoria.name
                )

                putString(
                    ARG_FECHA,
                    movimiento.fecha
                )
            }
        }
    }
}