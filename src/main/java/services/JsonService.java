package services;

import dao.*;
import db.Db;
import javafx.scene.control.Alert;
import model.*;

import services.JsonIO;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
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
     * Importa JSON al AlmacenData
     * solo datos para visualizar en memoria, no guardados, quiero que se vea antes de poder guardarlo
     */
    public static AppData importarJson(File file) throws IOException {
        if (!file.exists()) {
            System.out.println("No existe el JSON: " + file.getAbsolutePath());
            return null;
        }

       return JsonIO.read(file, AppData.class);
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
    public static void guardarEnBDJson() throws SQLException {

        ClienteDAO clienteDAO = new ClienteDAO();
        ProductoDAO productoDAO = new ProductoDAO();
        DetalleClienteDAO detalleClienteDAO = new DetalleClienteDAO();
        PedidoDAO pedidoDAO = new PedidoDAO();
        DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO();
        EmpresaRepartoDAO empresaRepartoDAO = new EmpresaRepartoDAO();
        RepartidorDAO repartidorDAO = new RepartidorDAO();
        EnvioDAO envioDAO = new EnvioDAO();

        // hay que hacer rollback, para evitar fallos al cargar en la bd. Primera prueba falló por null en repartidor campo empresa_id

        if (AlmacenData.getAppData() == null) {
            mostrarAlerta("Error en importacion de Json","No existn datos en AlmacenData, no es posible realizar la importacion. ");
            return;
        }

        try(Connection con = Db.getConnection();){

            //solo esta permitido en bases que acepten control de transacciones
            con.setAutoCommit(false);

            try{

                // 1) Clientes
                for (Cliente c : AlmacenData.getClientes()) {
                    // Podrías comprobar si existe para evitar error, pero lo dejamos simple:
                    // si existe, fallará por PK/unique -> perfecto para explicar integridad.
                    clienteDAO.insert(c);
                }

                // 2) Detalles cliente (requieren cliente previo)
                for (DetalleCliente d : AlmacenData.getDetallesCliente()) {
                    detalleClienteDAO.insert(d);
                }

                // 3) Productos
                for (Producto p : AlmacenData.getProductos()) {
                    productoDAO.insert(p);
                }

                // 4) Pedidos (requieren cliente previo)
                for (Pedido pe : AlmacenData.getPedidos()) {
                    pedidoDAO.insert(pe);
                }

                // 5) Detalles pedido (requieren pedido y producto previos)
                for (DetallePedido dp : AlmacenData.getDetallesPedido()) {
                    detallePedidoDAO.insert(dp);
                }

                //6) Empresa Reparto
                for (EmpresaReparto er : AlmacenData.getEmpresasReparto()){
                    empresaRepartoDAO.insertID(er);
                }

                // 7) Repartidor
                for (Repartidor r : AlmacenData.getRepartidores()) {
                    repartidorDAO.insertID(r);
                }

                // 8) Envio
                for (Envio envio : AlmacenData.getEnvios()) {
                    envioDAO.insert(envio);
                }

                con.commit(); //si las todas fucionan se hacen

            }catch(SQLException e){
                con.rollback(); //si da fallo volvemos a punto anterior
                throw e;
            }
            finally {
                mostrarInfo("Importación finalizada.", "Todos los campos añadidos a la base de datos. Restauración Completa");
                con.setAutoCommit(true);
            }

        }

    }

    // --- UTILIDADES ---
    private static void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private static void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }


}
