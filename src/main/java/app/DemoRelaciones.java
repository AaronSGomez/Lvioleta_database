package app;
import dao.ClienteDAO;
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
            cargarDeDatos(clienteDAO);

            mostrarDatos(clienteDAO);

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


}

