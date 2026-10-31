package edu.umg.programacion2.misgastos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.umg.programacion2.misgastos.core.Movimiento
import edu.umg.programacion2.misgastos.core.TipoMovimiento
import java.util.Locale

// Adapter encargado de mostrar la lista de movimientos
// dentro de un RecyclerView.
//
// Recibe:
// 1. La lista de movimientos.
// 2. La acción que se realizará al presionar Editar.
// 3. La acción que se realizará al presionar Eliminar.
class MovimientoAdapter(
    movimientosIniciales: List<Movimiento>,
    private val onEditar: (Movimiento) -> Unit,
    private val onEliminar: (Movimiento) -> Unit
) : RecyclerView.Adapter<MovimientoAdapter.MovimientoViewHolder>() {

    // Convierte la lista recibida en una lista modificable
    // para poder actualizarla posteriormente.
    private val movimientos = movimientosIniciales.toMutableList()


    // --------------------------------
    // VIEW HOLDER
    // --------------------------------

    // Representa los componentes visuales de UN elemento
    // de la lista de movimientos.
    //
    // Estos componentes están definidos en item_movimiento.xml.
    class MovimientoViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        // Muestra si es INGRESO o GASTO y su monto.
        val tvTipoMonto: TextView =
            itemView.findViewById(R.id.tvItemTipoMonto)

        // Muestra la descripción.
        val tvDescripcion: TextView =
            itemView.findViewById(R.id.tvItemDescripcion)

        // Muestra la categoría.
        val tvCategoria: TextView =
            itemView.findViewById(R.id.tvItemCategoria)

        // Muestra la fecha.
        val tvFecha: TextView =
            itemView.findViewById(R.id.tvItemFecha)

        // Botón para editar el movimiento.
        val btnEditar: Button =
            itemView.findViewById(R.id.btnEditar)

        // Botón para eliminar el movimiento.
        val btnEliminar: Button =
            itemView.findViewById(R.id.btnEliminar)
    }


    // --------------------------------
    // CREAR ELEMENTO VISUAL
    // --------------------------------

    // Crea la vista que se utilizará para representar
    // cada movimiento dentro del RecyclerView.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = MovimientoViewHolder(

        // Convierte item_movimiento.xml
        // en una View que Android puede mostrar.
        LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_movimiento,
                parent,
                false
            )
    )


    // --------------------------------
    // MOSTRAR DATOS
    // --------------------------------

    // Coloca los datos de un Movimiento
    // dentro de su elemento visual correspondiente.
    override fun onBindViewHolder(
        holder: MovimientoViewHolder,
        position: Int
    ) {

        // Obtiene el movimiento correspondiente
        // a la posición actual de la lista.
        val movimiento = movimientos[position]

        // Comprueba si el movimiento es un ingreso.
        val esIngreso =
            movimiento.tipo == TipoMovimiento.INGRESO

        // Si es ingreso muestra INGRESO,
        // de lo contrario muestra GASTO.
        val tipo =
            if (esIngreso) "INGRESO" else "GASTO"

        // Los ingresos llevan + y los gastos -.
        val signo =
            if (esIngreso) "+" else "-"

        // Define el color dependiendo
        // del tipo de movimiento.
        val color =
            if (esIngreso) {
                R.color.income_green
            } else {
                R.color.expense_red
            }


        // Configura los componentes visuales
        // del elemento actual.
        holder.apply {

            // Ejemplo:
            // INGRESO   + Q2,000.00
            // GASTO     - Q150.00
            tvTipoMonto.text =
                "$tipo   $signo Q${formatoMonto(movimiento.monto)}"

            // Aplica verde para ingresos
            // y rojo para gastos.
            tvTipoMonto.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    color
                )
            )

            // Coloca la descripción.
            tvDescripcion.text =
                movimiento.descripcion

            // Coloca el nombre de la categoría.
            tvCategoria.text =
                movimiento.categoria.nombre

            // Coloca la fecha.
            tvFecha.text =
                movimiento.fecha


            // Cuando se presiona Editar,
            // envía el movimiento seleccionado.
            btnEditar.setOnClickListener {
                onEditar(movimiento)
            }

            // Cuando se presiona Eliminar,
            // envía el movimiento seleccionado.
            btnEliminar.setOnClickListener {
                onEliminar(movimiento)
            }
        }
    }


    // --------------------------------
    // CANTIDAD DE ELEMENTOS
    // --------------------------------

    // Indica al RecyclerView cuántos movimientos
    // debe mostrar.
    override fun getItemCount() =
        movimientos.size


    // --------------------------------
    // ACTUALIZAR LISTA
    // --------------------------------

    // Reemplaza la lista actual por una nueva.
    //
    // Se utiliza, por ejemplo, después de eliminar
    // un movimiento o aplicar un filtro.
    fun actualizarLista(
        nuevaLista: List<Movimiento>
    ) {

        // Elimina los datos anteriores.
        movimientos.clear()

        // Agrega los nuevos datos.
        movimientos.addAll(nuevaLista)

        // Le avisa al RecyclerView que los datos cambiaron
        // para que vuelva a mostrar la lista.
        notifyDataSetChanged()
    }


    // --------------------------------
    // FORMATO DEL MONTO
    // --------------------------------

    // Da formato al dinero con dos decimales.
    //
    // Ejemplo:
    // 2000 -> 2,000.00
    private fun formatoMonto(monto: Double) =
        String.format(
            Locale.US,
            "%,.2f",
            monto
        )
}