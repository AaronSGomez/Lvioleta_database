package model;


import java.util.ArrayList;
import java.util.List;

/**
 * Contenedor para exportación/importación:
 * una "instantánea" del estado de la BD.
 *
 * IMPORTANTE:
 * - Debe tener constructor vacío + getters/setters
 *   para que Jackson pueda deserializar.
 */
public class AppData {

    private List<Cliente> clientes = new ArrayList<>();
    private List<DetalleCliente> detallesCliente = new ArrayList<>();
    private List<Producto> productos = new ArrayList<>();
    private List<Pedido> pedidos = new ArrayList<>();
    private List<DetallePedido> detallesPedido = new ArrayList<>();
    private List<EmpresaReparto> empresasReparto = new ArrayList<>();
    private List<Repartidor> repartidor = new ArrayList<>();
    private List<Envio>  envio = new ArrayList<>();

    public AppData() { }

    public List<Cliente> getClientes() { return clientes; }
    public void setClientes(List<Cliente> clientes) { this.clientes = clientes; }

    public List<DetalleCliente> getDetallesCliente() { return detallesCliente; }
    public void setDetallesCliente(List<DetalleCliente> detallesCliente) { this.detallesCliente = detallesCliente; }

    public List<Producto> getProductos() { return productos; }
    public void setProductos(List<Producto> productos) { this.productos = productos; }

    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }

    public List<DetallePedido> getDetallesPedido() { return detallesPedido; }
    public void setDetallesPedido(List<DetallePedido> detallesPedido) { this.detallesPedido = detallesPedido; }

    public List<EmpresaReparto> getEmpresasReparto() { return empresasReparto; }
    public void setEmpresasReparto(List<EmpresaReparto> empresasReparto) { this.empresasReparto = empresasReparto;}

    public List<Repartidor> getRepartidor() { return repartidor; }
    public void setRepartidor(List<Repartidor> repartidor) { this.repartidor = repartidor; }

    public List<Envio> getEnvio() { return envio; }
    public void setEnvio(List<Envio> envio) { this.envio = envio; }
}