package edu.umg.programacion2.misgastos

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import edu.umg.programacion2.misgastos.core.TipoMovimiento
import edu.umg.programacion2.misgastos.data.MovimientoRepository
import edu.umg.programacion2.misgastos.data.PresupuestoRepository
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

// Fragment encargado de la pantalla de presupuesto.
// Permite:
// - Seleccionar un mes.
// - Guardar un presupuesto mensual.
// - Consultar cuánto se ha gastado.
// - Calcular cuánto dinero queda disponible.
// - Mostrar el porcentaje utilizado.
// - Eliminar un presupuesto.
class PresupuestoFragment : Fragment(R.layout.fragment_presupuesto) {

    // Repositorio utilizado para obtener los movimientos
    // almacenados en SQLite.
    private lateinit var movimientoRepo: MovimientoRepository

    // Repositorio utilizado para guardar, consultar
    // y eliminar presupuestos en SQLite.
    private lateinit var presupuestoRepo: PresupuestoRepository


    // -----------------------------
    // COMPONENTES DE LA INTERFAZ
    // -----------------------------

    // Permite seleccionar el mes.
    private lateinit var spinnerMes: Spinner

    // Muestra el presupuesto establecido.
    private lateinit var tvPresupuesto: TextView

    // Muestra cuánto se ha gastado durante el mes.
    private lateinit var tvGastado: TextView

    // Muestra cuánto dinero queda disponible.
    private lateinit var tvDisponible: TextView

    // Muestra el porcentaje del presupuesto utilizado.
    private lateinit var tvPorcentaje: TextView

    // Muestra la relación entre gasto y presupuesto.
    private lateinit var tvRelacion: TextView

    // Muestra el estado del presupuesto.
    private lateinit var tvEstado: TextView

    // Barra visual que representa el porcentaje utilizado.
    private lateinit var progress: ProgressBar

    // Campo donde el usuario escribe el presupuesto.
    private lateinit var etPresupuesto: EditText


    // Guarda el número del mes seleccionado.
    // 0 significa que todavía no se ha seleccionado un mes.
    private var mes = 0


    // Obtiene automáticamente el año actual.
    private val anioActual: Int
        get() = Calendar.getInstance().get(Calendar.YEAR)


    // Se ejecuta cuando la vista del Fragment está creada.
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa los repositorios.
        movimientoRepo =
            MovimientoRepository(requireContext())

        presupuestoRepo =
            PresupuestoRepository(requireContext())

        // Conecta los componentes de la interfaz.
        iniciarVistas(view)

        // Configura el selector de meses.
        configurarMeses()


        // Botón para guardar el presupuesto.
        view.findViewById<Button>(
            R.id.btnGuardarPresupuesto
        ).setOnClickListener {
            guardarPresupuesto()
        }


        // Botón para eliminar el presupuesto.
        view.findViewById<Button>(
            R.id.btnEliminarPresupuesto
        ).setOnClickListener {
            confirmarEliminacion()
        }
    }


    // Cuando el Fragment vuelve a mostrarse,
    // actualiza la información.
    override fun onResume() {
        super.onResume()

        if (::movimientoRepo.isInitialized) {
            actualizarResumen()
        }
    }


    // -----------------------------
    // INICIALIZAR VISTAS
    // -----------------------------

    // Conecta las variables Kotlin con los componentes
    // definidos en fragment_presupuesto.xml.
    private fun iniciarVistas(view: View) {

        spinnerMes =
            view.findViewById(R.id.spinnerMes)

        tvPresupuesto =
            view.findViewById(R.id.tvPresupuesto)

        tvGastado =
            view.findViewById(R.id.tvGastadoMes)

        tvDisponible =
            view.findViewById(R.id.tvDisponiblePresupuesto)

        tvPorcentaje =
            view.findViewById(R.id.tvPorcentajePresupuesto)

        tvRelacion =
            view.findViewById(R.id.tvRelacionPresupuesto)

        tvEstado =
            view.findViewById(R.id.tvEstadoPresupuesto)

        progress =
            view.findViewById(R.id.progressPresupuesto)

        etPresupuesto =
            view.findViewById(R.id.etPresupuesto)
    }


    // -----------------------------
    // SELECCIÓN DEL MES
    // -----------------------------

    // Configura el Spinner con los 12 meses.
    private fun configurarMeses() {

        val meses = listOf(
            "Selecciona un mes",
            "Enero",
            "Febrero",
            "Marzo",
            "Abril",
            "Mayo",
            "Junio",
            "Julio",
            "Agosto",
            "Septiembre",
            "Octubre",
            "Noviembre",
            "Diciembre"
        )


        // Coloca los meses dentro del Spinner.
        spinnerMes.adapter =
            ArrayAdapter(
                requireContext(),
                R.layout.spinner_item,
                meses
            ).apply {

                setDropDownViewResource(
                    R.layout.spinner_item
                )
            }


        // Selecciona automáticamente el mes actual.
        // Calendar.MONTH empieza en 0,
        // por eso se suma 1.
        spinnerMes.setSelection(
            Calendar.getInstance()
                .get(Calendar.MONTH) + 1
        )


        // Detecta cuando el usuario cambia de mes.
        spinnerMes.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    // Guarda el mes seleccionado.
                    mes = position

                    // Actualiza los datos del presupuesto.
                    actualizarResumen()
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) = Unit
            }
    }


    // -----------------------------
    // GUARDAR PRESUPUESTO
    // -----------------------------

    private fun guardarPresupuesto() {

        // Comprueba que se haya seleccionado un mes.
        if (mes == 0) {
            mensaje(
                "Selecciona un mes para guardar el presupuesto"
            )
            return
        }


        // Obtiene el monto escrito por el usuario.
        val texto =
            etPresupuesto.text
                .toString()
                .trim()


        // Comprueba que el campo no esté vacío.
        if (texto.isEmpty()) {
            mensaje(
                "Escribe el monto del presupuesto"
            )
            return
        }


        // Convierte el texto a Double.
        val monto =
            texto
                .replace(',', '.')
                .toDoubleOrNull()


        // Valida que sea un número mayor que cero.
        if (
            monto == null ||
            monto <= 0 ||
            !monto.isFinite()
        ) {
            mensaje(
                "Ingresa un presupuesto mayor que cero"
            )
            return
        }


        // Utiliza PresupuestoRepository para guardar
        // el presupuesto en SQLite.
        if (
            presupuestoRepo.guardar(
                claveMes(),
                monto
            )
        ) {

            mensaje(
                "Presupuesto guardado para ${nombreMes()}"
            )

            // Actualiza la información mostrada.
            actualizarResumen()

        } else {

            mensaje(
                "No se pudo guardar el presupuesto"
            )
        }
    }


    // -----------------------------
    // ELIMINAR PRESUPUESTO
    // -----------------------------

    // Comprueba que exista un presupuesto
    // y pide confirmación antes de eliminarlo.
    private fun confirmarEliminacion() {

        if (mes == 0) {
            mensaje("Selecciona un mes")
            return
        }


        // Busca el presupuesto en SQLite.
        if (
            presupuestoRepo.obtener(
                claveMes()
            ) == null
        ) {

            mensaje(
                "No hay un presupuesto para ${nombreMes()}"
            )
            return
        }


        // Muestra cuadro de confirmación.
        AlertDialog.Builder(requireContext())

            .setTitle(
                "Eliminar presupuesto"
            )

            .setMessage(
                "¿Deseas eliminar el presupuesto de ${nombreMes()}?"
            )

            .setNegativeButton(
                "Cancelar",
                null
            )

            .setPositiveButton(
                "Eliminar"
            ) { _, _ ->

                eliminarPresupuesto()
            }

            .show()
    }


    // Elimina el presupuesto de SQLite.
    private fun eliminarPresupuesto() {

        if (
            presupuestoRepo.eliminar(
                claveMes()
            )
        ) {

            mensaje(
                "Presupuesto de ${nombreMes()} eliminado"
            )

            etPresupuesto.text.clear()

            actualizarResumen()

        } else {

            mensaje(
                "No se pudo eliminar el presupuesto"
            )
        }
    }


    // -----------------------------
    // ACTUALIZAR RESUMEN
    // -----------------------------

    // Calcula y muestra toda la información
    // correspondiente al mes seleccionado.
    private fun actualizarResumen() {

        // Si no existe un mes seleccionado,
        // muestra la pantalla vacía.
        if (mes == 0) {
            mostrarSinMes()
            return
        }


        // Obtiene todos los movimientos de SQLite.
        //
        // Después:
        // 1. Se queda solamente con los GASTOS.
        // 2. Se queda solamente con los del mes y año seleccionados.
        // 3. Suma sus montos.
        val gastos =
            movimientoRepo.obtenerTodos()

                .filter {
                    it.tipo ==
                            TipoMovimiento.GASTO &&
                            fechaCorresponde(it.fecha)
                }

                .sumOf {
                    it.monto
                }


        // Muestra el total gastado.
        tvGastado.text =
            quetzales(gastos)


        // Busca el presupuesto correspondiente
        // al mes seleccionado.
        val presupuesto =
            presupuestoRepo.obtener(
                claveMes()
            )


        // Si no existe presupuesto, muestra un mensaje.
        if (presupuesto == null) {

            mostrarSinPresupuesto(gastos)

        } else {

            // Si existe, realiza los cálculos.
            mostrarPresupuesto(
                presupuesto,
                gastos
            )
        }
    }


    // -----------------------------
    // SIN MES SELECCIONADO
    // -----------------------------

    private fun mostrarSinMes() {

        tvPresupuesto.text =
            "Selecciona un mes"

        tvGastado.text = "—"
        tvDisponible.text = "—"

        tvPorcentaje.text =
            "0% utilizado"

        tvRelacion.text = "—"

        tvEstado.text =
            "Selecciona un mes para consultar tu presupuesto."

        progress.progress = 0

        aplicarColor(R.color.teal)

        etPresupuesto.text.clear()
    }


    // -----------------------------
    // SIN PRESUPUESTO
    // -----------------------------

    // Se utiliza cuando existen gastos,
    // pero todavía no se ha establecido presupuesto.
    private fun mostrarSinPresupuesto(
        gastos: Double
    ) {

        tvPresupuesto.text =
            "No establecido"

        tvDisponible.text = "—"

        tvPorcentaje.text =
            "0% utilizado"

        tvRelacion.text =
            "${quetzales(gastos)} sin límite definido"

        tvEstado.text =
            "Establece un presupuesto para comenzar a controlar tus gastos."

        progress.progress = 0

        tvPorcentaje.setTextColor(
            color(R.color.teal)
        )

        tvEstado.setTextColor(
            color(R.color.text_secondary)
        )

        progress.progressTintList =
            ColorStateList.valueOf(
                color(R.color.teal)
            )

        etPresupuesto.text.clear()
    }


    // -----------------------------
    // MOSTRAR PRESUPUESTO
    // -----------------------------

    // Realiza los cálculos relacionados
    // con el presupuesto.
    private fun mostrarPresupuesto(
        presupuesto: Double,
        gastos: Double
    ) {

        // Dinero disponible:
        // presupuesto - gastos.
        val disponible =
            presupuesto - gastos


        // Calcula qué porcentaje
        // del presupuesto se ha utilizado.
        val porcentajeReal =
            gastos / presupuesto * 100


        // Redondea el porcentaje.
        val porcentaje =
            porcentajeReal.roundToInt()


        // Muestra presupuesto y gastos.
        tvPresupuesto.text =
            quetzales(presupuesto)

        tvGastado.text =
            quetzales(gastos)


        // Si queda dinero muestra Q...
        // Si se excedió muestra un valor negativo.
        tvDisponible.text =
            if (disponible >= 0)
                quetzales(disponible)
            else
                "-${quetzales(-disponible)}"


        // Ejemplo:
        // Q800.00 de Q1,000.00
        tvRelacion.text =
            "${quetzales(gastos)} de ${quetzales(presupuesto)}"


        // Ejemplo:
        // 80% utilizado
        tvPorcentaje.text =
            "$porcentaje% utilizado"


        // La barra visual solo puede estar
        // entre 0 y 100.
        progress.progress =
            porcentaje.coerceIn(
                0,
                100
            )


        // Coloca el presupuesto actual
        // dentro del EditText.
        etPresupuesto.setText(
            String.format(
                Locale.US,
                "%.2f",
                presupuesto
            )
        )


        // Cambia el estado dependiendo
        // del porcentaje utilizado.
        when {

            // 100% o más:
            // presupuesto alcanzado o excedido.
            porcentajeReal >= 100 -> {

                aplicarColor(
                    R.color.expense_red
                )

                tvEstado.text =
                    if (disponible < 0)

                        "Presupuesto excedido por ${quetzales(-disponible)}"

                    else

                        "Has utilizado el 100% de tu presupuesto."
            }


            // Entre 80% y menos de 100%.
            porcentajeReal >= 80 -> {

                aplicarColor(
                    R.color.teal
                )

                tvEstado.text =
                    "Estás cerca de alcanzar tu presupuesto mensual."
            }


            // Menos del 80%.
            else -> {

                aplicarColor(
                    R.color.income_green
                )

                tvEstado.text =
                    "Tu presupuesto está bajo control."
            }
        }
    }


    // -----------------------------
    // COLORES
    // -----------------------------

    // Aplica el mismo color al porcentaje,
    // disponible, estado y barra de progreso.
    private fun aplicarColor(
        colorId: Int
    ) {

        val color =
            color(colorId)

        progress.progressTintList =
            ColorStateList.valueOf(color)

        tvPorcentaje.setTextColor(color)
        tvDisponible.setTextColor(color)
        tvEstado.setTextColor(color)
    }


    // -----------------------------
    // FECHA
    // -----------------------------

    // Comprueba si la fecha de un movimiento
    // pertenece al mes seleccionado y al año actual.
    private fun fechaCorresponde(
        fecha: String
    ): Boolean {

        // Ejemplo:
        // 31/10/2026
        // ↓
        // [31, 10, 2026]
        val partes =
            fecha.split("/")


        return partes.size == 3 &&

                // Compara el mes.
                partes[1].toIntOrNull() == mes &&

                // Compara el año.
                partes[2].toIntOrNull() == anioActual
    }


    // Crea la clave utilizada para guardar
    // el presupuesto en SQLite.
    //
    // Ejemplo:
    // Octubre 2026 -> "2026-10"
    private fun claveMes() =
        String.format(
            Locale.US,
            "%04d-%02d",
            anioActual,
            mes
        )


    // Obtiene el nombre del mes seleccionado.
    private fun nombreMes() =
        spinnerMes.selectedItem
            ?.toString()
            .orEmpty()


    // Da formato de quetzales.
    //
    // Ejemplo:
    // 1500 -> Q1,500.00
    private fun quetzales(
        monto: Double
    ) =
        "Q${
            String.format(
                Locale.US,
                "%,.2f",
                monto
            )
        }"


    // Obtiene un color de los recursos.
    private fun color(
        colorId: Int
    ) =
        ContextCompat.getColor(
            requireContext(),
            colorId
        )


    // Muestra un mensaje Toast.
    private fun mensaje(
        texto: String
    ) =
        Toast.makeText(
            requireContext(),
            texto,
            Toast.LENGTH_SHORT
        ).show()
}