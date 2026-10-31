package edu.umg.programacion2.misgastos

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import edu.umg.programacion2.misgastos.core.Movimiento

// Activity principal de la aplicación.
// Funciona como contenedor de los diferentes Fragments
// y controla la navegación entre las pantallas.
class MainActivity : AppCompatActivity() {

    // -----------------------------
    // OPCIONES DE NAVEGACIÓN
    // -----------------------------

    // Contenedores que funcionan como botones
    // de la barra de navegación inferior.
    private lateinit var navInicio: LinearLayout
    private lateinit var navPresupuesto: LinearLayout
    private lateinit var navMovimientos: LinearLayout

    // Iconos de cada opción de navegación.
    private lateinit var iconInicio: TextView
    private lateinit var iconPresupuesto: TextView
    private lateinit var iconMovimientos: TextView

    // Textos de cada opción de navegación.
    private lateinit var textInicio: TextView
    private lateinit var textPresupuesto: TextView
    private lateinit var textMovimientos: TextView


    // Se ejecuta cuando se crea la Activity.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Carga el diseño principal definido
        // en activity_main.xml.
        setContentView(R.layout.activity_main)

        // Conecta las variables Kotlin con
        // los elementos de activity_main.xml.
        iniciarNavegacion()

        // Configura qué ocurre cuando el usuario
        // presiona cada opción de navegación.
        configurarNavegacion()

        // Si la Activity se está creando por primera vez,
        // muestra InicioFragment como pantalla inicial.
        if (savedInstanceState == null) {
            mostrarFragment(
                InicioFragment(),
                Pantalla.INICIO
            )
        }
    }


    // -----------------------------
    // INICIALIZAR NAVEGACIÓN
    // -----------------------------

    // Conecta las variables con los componentes
    // definidos en activity_main.xml.
    private fun iniciarNavegacion() {

        // Botones/contenedores de navegación.
        navInicio = findViewById(R.id.navInicio)
        navPresupuesto = findViewById(R.id.navPresupuesto)
        navMovimientos = findViewById(R.id.navMovimientos)

        // Iconos.
        iconInicio = findViewById(R.id.iconInicio)
        iconPresupuesto = findViewById(R.id.iconPresupuesto)
        iconMovimientos = findViewById(R.id.iconMovimientos)

        // Textos.
        textInicio = findViewById(R.id.textInicio)
        textPresupuesto = findViewById(R.id.textPresupuesto)
        textMovimientos = findViewById(R.id.textMovimientos)
    }


    // -----------------------------
    // CONFIGURAR NAVEGACIÓN
    // -----------------------------

    // Define qué Fragment debe mostrarse
    // cuando el usuario selecciona una opción.
    private fun configurarNavegacion() {

        // Al presionar Inicio,
        // muestra InicioFragment.
        navInicio.setOnClickListener {
            mostrarFragment(
                InicioFragment(),
                Pantalla.INICIO
            )
        }

        // Al presionar Presupuesto,
        // muestra PresupuestoFragment.
        navPresupuesto.setOnClickListener {
            mostrarFragment(
                PresupuestoFragment(),
                Pantalla.PRESUPUESTO
            )
        }

        // Al presionar Movimientos,
        // muestra MovimientosFragment.
        navMovimientos.setOnClickListener {
            mostrarFragment(
                MovimientosFragment(),
                Pantalla.MOVIMIENTOS
            )
        }
    }


    // -----------------------------
    // EDITAR MOVIMIENTO
    // -----------------------------

    // Se utiliza cuando el usuario quiere editar
    // un movimiento desde otra pantalla.
    fun editarMovimiento(movimiento: Movimiento) {

        // Abre InicioFragment enviándole
        // el movimiento que debe editar.
        mostrarFragment(
            InicioFragment.nuevaInstanciaParaEditar(movimiento),
            Pantalla.INICIO
        )
    }


    // -----------------------------
    // CAMBIAR FRAGMENT
    // -----------------------------

    // Reemplaza el Fragment que se encuentra actualmente
    // dentro del contenedor principal.
    private fun mostrarFragment(
        fragment: Fragment,
        pantalla: Pantalla
    ) {

        // FragmentManager administra los Fragments.
        supportFragmentManager
            .beginTransaction()

            // Reemplaza la pantalla actual
            // por el Fragment recibido.
            .replace(
                R.id.contenedorPrincipal,
                fragment
            )

            // Confirma y ejecuta el cambio.
            .commit()

        // Actualiza visualmente la barra de navegación.
        actualizarNavegacion(pantalla)
    }


    // -----------------------------
    // ACTUALIZAR NAVEGACIÓN
    // -----------------------------

    // Determina cuál opción de navegación
    // debe aparecer como seleccionada.
    private fun actualizarNavegacion(pantalla: Pantalla) {

        actualizarItem(
            iconInicio,
            textInicio,
            pantalla == Pantalla.INICIO
        )

        actualizarItem(
            iconPresupuesto,
            textPresupuesto,
            pantalla == Pantalla.PRESUPUESTO
        )

        actualizarItem(
            iconMovimientos,
            textMovimientos,
            pantalla == Pantalla.MOVIMIENTOS
        )
    }


    // Cambia el color del icono y texto
    // dependiendo de si la opción está activa.
    private fun actualizarItem(
        icono: TextView,
        texto: TextView,
        activo: Boolean
    ) {

        // Si está activo utiliza el color teal.
        // Si no está activo utiliza el color secundario.
        val color = getColor(
            if (activo)
                R.color.teal
            else
                R.color.text_secondary
        )

        // Aplica el color al icono y al texto.
        icono.setTextColor(color)
        texto.setTextColor(color)
    }


    // Enum que representa las tres pantallas
    // principales disponibles en la aplicación.
    private enum class Pantalla {
        INICIO,
        PRESUPUESTO,
        MOVIMIENTOS
    }
}