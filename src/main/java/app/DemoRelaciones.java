package app;
import dao.ClienteDAO;
import dao.ProductoDAO;
import model.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Crea datos en memoria para explicar:
 *  - 1:1  Cliente ↔ DetalleCliente
 *  - 1:N  Cliente ↔ Pedido
 *  - N:M  Pedido ↔ Producto (vía DetallePedido)
 */
public class DemoRelaciones {
    public static void main(String[] args) {
        try {
            ClienteDAO clienteDAO = new ClienteDAO();
            ProductoDAO productoDAO = new ProductoDAO();
            //cargarDeDatos(clienteDAO);
            //mostrarDatos(clienteDAO);

            cargarDeDatosPro(productoDAO);
            mostrarDatosPro(productoDAO);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static void cargarDeDatos(ClienteDAO clienteDAO) throws SQLException {
        System.out.println("=== Cargando datos ===");
        Cliente c1 = new Cliente(1,"Aaron Gomez", "nomeacuerdobien@gmail.com");
        Cliente c2 = new Cliente(2, "Roberto Roodriguez", "robert@terra.com");

            clienteDAO.insert(c1);
            clienteDAO.insert(c2);
        System.out.println("=== Datos cargado correctamente ===");
    }

    private static void mostrarDatos(ClienteDAO cdao) throws SQLException {
        System.out.println("=== Mostrando datos ===");
        List<Cliente> clientes = cdao.findAll();
        //clientes.forEach(c -> System.out.println(c));
         clientes.forEach(System.out::println);
    }

    private static void cargarDeDatosPro(ProductoDAO productoDAO) throws SQLException {
        System.out.println("=== Cargando datos ===");
        Producto p1 = new Producto(1,"Logitech ERGO M575S", 37.99);
        Producto p2 = new Producto(2, "PERIBOARD-335BL", 46.99);

        productoDAO.insert(p1);
        productoDAO.insert(p2);
        System.out.println("=== Datos cargado correctamente ===");
    }

    private static void mostrarDatosPro(ProductoDAO pdao) throws SQLException {
        System.out.println("=== Mostrando datos ===");
        List<Producto> productos = pdao.findAll();
        //clientes.forEach(c -> System.out.println(c));
        productos.forEach(System.out::println);
    }


}

