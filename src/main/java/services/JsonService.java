package services;

import model.*;

import services.JsonIO;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class JsonService {

    // =========================================================
    // JSON EXPORT / IMPORT
    // =========================================================

    /**
     * Exporta una "foto" de la BD a JSON.
     * Lee todas las tablas y las serializa.
     */
    public static void exportarJson(File file) throws SQLException, IOException {
        // ACTUALIZO TODOS LOS DATOS ANTES DE EXPORTAR, PARA GARANTIZAR DATOS
        AlmacenData.setClientes();
        AlmacenData.setProductos();
        AlmacenData.setPedidos();
        AlmacenData.setRepartidores();
        AlmacenData.setEmpresasReparto();
        AlmacenData.setEnvios();

        JsonIO.write(file, AlmacenData.getAppData());
        System.out.println("Exportado JSON en: " + file.getAbsolutePath());
    }

    /**
     * Importa JSON a la BD haciendo INSERT en orden correcto por FKs:
     *
     *  1) cliente
     *  2) detalle_cliente
     *  3) producto
     *  4) pedido
     *  5) detalle_pedido
     *
     * IMPORTANTE:
     * - No borra lo existente (si ya hay IDs repetidos, fallará por PK).
     * - En clase podéis añadir luego una opción "vaciar tablas" o "upsert".
     */

    /*private static void importarJson() throws IOException, SQLException {
        if (!JSON_FILE.exists()) {
            System.out.println("No existe el JSON: " + JSON_FILE.getAbsolutePath());
            return;
        }

        AppData data = JsonIO.read(JSON_FILE, AppData.class);

        // 1) Clientes
        for (Cliente c : data.getClientes()) {
            // Podrías comprobar si existe para evitar error, pero lo dejamos simple:
            // si existe, fallará por PK/unique -> perfecto para explicar integridad.
            clienteDAO.insert(c);
        }

        // 2) Detalles cliente (requieren cliente previo)
        for (DetalleCliente d : data.getDetallesCliente()) {
            detalleClienteDAO.insert(d);
        }

        // 3) Productos
        for (Producto p : data.getProductos()) {
            productoDAO.insert(p);
        }

        // 4) Pedidos (requieren cliente previo)
        for (Pedido pe : data.getPedidos()) {
            pedidoDAO.insert(pe);
        }

        // 5) Detalles pedido (requieren pedido y producto previos)
        for (DetallePedido dp : data.getDetallesPedido()) {
            detallePedidoDAO.insert(dp);
        }

        System.out.println("Importación finalizada.");
    }*/





}
