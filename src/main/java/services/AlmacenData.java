package services;

import dao.*;
import model.*;

import java.sql.SQLException;
import java.util.List;

public class AlmacenData {

    private static AppData data = new AppData();

    // Getter

    public static List<Producto> getProductos() throws SQLException {
        if (data.getProductos().isEmpty()) {
            // Si está vacía, es la primera vez. Cargamos desde BD.
            setProductos();
        }
        return data.getProductos();
    }

    public static List<Cliente> getClientes() throws SQLException {
        if (data.getClientes().isEmpty()) {
            setClientes();
        }
        return data.getClientes();
    }

    public static List<Pedido> getPedidos() throws SQLException {
        if (data.getPedidos().isEmpty()) {
            setPedidos();
        }
        return data.getPedidos();
    }

    public static List<DetallePedido> getDetallesPedido() throws SQLException {
        if (data.getDetallesPedido().isEmpty()) {
            setPedidos();
        }
        return data.getDetallesPedido();
    }

    public static List<DetalleCliente> getDetallesCliente() throws SQLException {
        if (data.getDetallesCliente().isEmpty()) {
            setClientes();
        }
        return data.getDetallesCliente();
    }

    //Setter

    public static void setProductos() throws SQLException {
        ProductoDAO dao = new ProductoDAO();

        List<Producto> nuevosDatos = dao.findAll();
        // Limpiamos y recargar
        data.getProductos().clear();
        data.setProductos(nuevosDatos);
        System.out.println("✅ Productos recargados en memoria.");

    }

    public static void setClientes() throws SQLException {
        ClienteDAO dao = new ClienteDAO();
        DetalleClienteDAO detDao = new DetalleClienteDAO();

        List<Cliente> nuevosDatos = dao.findAll();
        List<DetalleCliente> detallesCliente = detDao.findAll();
        // Limpiar y recargar
        data.setClientes(nuevosDatos);
        data.setDetallesCliente(detallesCliente);
        System.out.println("✅ Clientes y Detalles recargados en memoria.");
    }

    public static void setPedidos() throws SQLException {
        PedidoDAO dao = new PedidoDAO();
        DetallePedidoDAO detDao = new DetallePedidoDAO();

        List<Pedido> nuevosDatos = dao.findAll();
        List<DetallePedido> detallePedidos = detDao.findAll();
        // Limpiar y recargar
        data.setPedidos(nuevosDatos);
        data.setDetallesPedido(detallePedidos);
        System.out.println("✅ Pedidos y Detalles recargados en memoria.");
    }


    // Método para acceder al objeto completo (para Exportar a JSON)
    public static AppData getAppData() {
        return data;
    }

    // Método para sobrescribir todo (para Importar desde JSON)
    public static void setAppData(AppData nuevosDatos) {
        data = nuevosDatos;
    }
}