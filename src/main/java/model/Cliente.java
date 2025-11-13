package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad principal "Cliente".
 * Relaciones:
 *  - 1:1 con DetalleCliente (detalle opcional ampliado).
 *  - 1:N con Pedido (un cliente hace muchos pedidos).
 */
public class Cliente {
    private Integer id;            // PK
    private String nombre;
    private String email;

    // 1:1
    private DetalleCliente detalle; // puede ser null si aún no hay detalle

    // 1:N
    private List<Pedido> pedidos = new ArrayList<>();

    public Cliente() {}
    public Cliente(Integer id, String nombre, String email) {
        this.id = id; this.nombre = nombre; this.email = email;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public DetalleCliente getDetalle() { return detalle; }
    public void setDetalle(DetalleCliente detalle) { this.detalle = detalle; }

    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }

    @Override public String toString() {
        return "Cliente{id=%d, nombre='%s', email='%s'}".formatted(id, nombre, email);
    }
}
