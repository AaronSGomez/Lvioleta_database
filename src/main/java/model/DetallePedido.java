package model;

/**
 * Fila de un pedido: une un Pedido con un Producto + cantidad + precio.
 * PK compuesta (pedido_id, producto_id) en la BBDD.
 */
public class DetallePedido {
    private Integer pedidoId;     // FK
    private Integer productoId;   // FK
    private int cantidad;
    private double precioUnit;    // redundancia histórica: precio del producto en el momento

    public DetallePedido() {}
    public DetallePedido(Integer pedidoId, Integer productoId, int cantidad, double precioUnit) {
        this.pedidoId = pedidoId; this.productoId = productoId;
        this.cantidad = cantidad; this.precioUnit = precioUnit;
    }

    public Integer getPedidoId() { return pedidoId; }
    public void setPedidoId(Integer pedidoId) { this.pedidoId = pedidoId; }

    public Integer getProductoId() { return productoId; }
    public void setProductoId(Integer productoId) { this.productoId = productoId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnit() { return precioUnit; }
    public void setPrecioUnit(double precioUnit) { this.precioUnit = precioUnit; }

    public double getImporte() { return cantidad * precioUnit; }

    @Override public String toString() {
        return "Detalle{pedidoId=%d, prodId=%d, cant=%d, pUnit=%.2f, importe=%.2f}"
                .formatted(pedidoId, productoId, cantidad, precioUnit, getImporte());
    }
}
