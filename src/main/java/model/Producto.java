package model;

/**
 * Producto vendible. Participa en N:M con Pedido a través de DetallePedido.
 */
public class Producto {
    private Integer id;     // PK
    private String nombre;
    private double precio;

    public Producto() {}
    public Producto(Integer id, String nombre, double precio) {
        this.id = id; this.nombre = nombre; this.precio = precio;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    @Override public String toString() {
        return "Producto{id=%d, nombre='%s', precio=%.2f}".formatted(id, nombre, precio);
    }
}
