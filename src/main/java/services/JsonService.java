package services;

import model.*;

import services.JsonIO;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
     * Importa JSON a la BD haciendo INSERT en orden correcto
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

}
