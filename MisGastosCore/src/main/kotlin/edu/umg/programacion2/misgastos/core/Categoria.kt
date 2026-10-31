package edu.umg.programacion2.misgastos.core

enum class Categoria(
    val nombre: String,
    val tipo: TipoMovimiento
) {
    SALARIO("Salario", TipoMovimiento.INGRESO),
    NEGOCIO("Negocio", TipoMovimiento.INGRESO),
    VENTA("Venta", TipoMovimiento.INGRESO),
    REGALO("Regalo", TipoMovimiento.INGRESO),
    OTRO_INGRESO("Otro", TipoMovimiento.INGRESO),

    COMIDA("Comida", TipoMovimiento.GASTO),
    TRANSPORTE("Transporte", TipoMovimiento.GASTO),
    VIVIENDA("Vivienda", TipoMovimiento.GASTO),
    SERVICIOS("Servicios", TipoMovimiento.GASTO),
    SALUD("Salud", TipoMovimiento.GASTO),
    EDUCACION("Educación", TipoMovimiento.GASTO),
    ENTRETENIMIENTO("Entretenimiento", TipoMovimiento.GASTO),
    COMPRAS("Compras", TipoMovimiento.GASTO),
    OTRO_GASTO("Otro", TipoMovimiento.GASTO);

    companion object {
        fun obtenerPorTipo(tipo: TipoMovimiento): List<Categoria> {
            return entries.filter { it.tipo == tipo }
        }
    }
}