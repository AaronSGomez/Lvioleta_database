package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido realizado por un cliente (FK cliente_id).
 * Relación N:M con Producto -> lista de DetallePedido.
 */
public class Pedido {
    private Integer id;                // PK
    private Integer clienteId;         // FK a Cliente (lado N de 1:N)
    private LocalDate fecha;

    //campos auxiliares no se guardan en tabla pedido
    private String nombreCliente;
    private double totalImporte;

/*    // N:M mediante filas en la tabla detalle_pedido
    private List<DetallePedido> lineas = new ArrayList<>();*/

    public Pedido() {}
    public Pedido(Integer id, Integer clienteId, LocalDate fecha) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = fecha;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public double getTotalImporte() {
        return totalImporte;
    }

    public void setTotalImporte(double totalImporte) {
        this.totalImporte = totalImporte;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

 /*   public List<DetallePedido> getLineas() { return lineas; }
    public void setLineas(List<DetallePedido> lineas) { this.lineas = lineas; }

    public double getTotal() {
        return lineas.stream().mapToDouble(DetallePedido::getImporte).sum();
    }*/

    @Override public String toString() {
        return "Pedido{id=%d, clienteId=%d, fecha=%s, total=%.2f}"
                .formatted(id, clienteId, fecha/*, getTotal()*/);
    }
}
