package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.AppData;
import services.AlmacenData;
//import services.DataRestoreService;
import services.JsonService;
import view.*;

import java.io.File;
import java.sql.SQLException;

public class LampreasVioletaApp extends Application {

    // Necesitamos referencia al Stage para los diálogos de abrir/guardar
    private Stage primaryStage;

    // El botón "nuclear" lo definimos aquí para habilitarlo/deshabilitarlo
    private MenuItem itemRestaurarBD;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // Usamos BorderPane como raíz para agregar la barra de menus
        BorderPane root = new BorderPane();

        // CONFIGURAMOS EL MENÚ SUPERIOR
        configurarMenu(root);

        // CONFIGURAMOS LOS TABS
        TabPane tabPane = new TabPane();

        Tab cliente = new Tab(" Gestion Clientes ");
        Tab producto = new Tab(" Gestion Productos ");
        Tab pedido = new Tab(" Gestion Pedidos");
        Tab reparto = new Tab(" Gestion Repartos");
        Tab envio = new Tab(" Envios");

        // Instanciamos las vistas
        ClientesView vistaClientes = new ClientesView();
        ProductosView vistaProductos = new ProductosView();
        PedidosView vistaPedidos = new PedidosView();
        AdminLogisticaView vistaReparto = new AdminLogisticaView();
        EnvioView vistaEnvio = new EnvioView();

        cliente.setContent(vistaClientes.getRoot());
        producto.setContent(vistaProductos.getRoot());
        pedido.setContent(vistaPedidos.getRoot());
        reparto.setContent(vistaReparto.getRoot());
        envio.setContent(vistaEnvio.getRoot());

        // Configuración de tabs
        cliente.setClosable(false);
        producto.setClosable(false);
        pedido.setClosable(false);
        reparto.setClosable(false);
        envio.setClosable(false);

        tabPane.getTabs().addAll(cliente, producto, pedido, reparto, envio);

        // COLOCAMOS EL TABPANE EN EL CENTRO
        root.setCenter(tabPane);

        // ESCENA Y MOSTRAR
        Scene scene = new Scene(root, 1300, 800);
        stage.setTitle("ERP Lampreas Violeta - Gestión Integral");
        stage.setScene(scene);
        stage.show();
    }

    /* =========================================================
       LOGICA DEL MENÚ (IMPORTAR / EXPORTAR / RESTAURAR)
       ========================================================= */

    private void configurarMenu(BorderPane root) {
        MenuBar menuBar = new MenuBar();
        Menu menuArchivo = new Menu("Archivo");
        Menu menuAyuda = new Menu("Ayuda");

        // --- ITEMS DEL MENÚ ---
        MenuItem itemExportar = new MenuItem("📤 Exportar Copia Seguridad (JSON)");
        MenuItem itemImportar = new MenuItem("📥 Importar Datos (Solo Memoria)");

        // El botón peligroso: Inicialmente deshabilitado y rojo
        itemRestaurarBD = new MenuItem("☢️ GUARDAR EN BD (Sobrescribir)");
        itemRestaurarBD.setDisable(true);
        itemRestaurarBD.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        MenuItem itemSalir = new MenuItem("Salir");

        // --- EVENTOS ---
        itemExportar.setOnAction(e -> accionExportar());
        itemImportar.setOnAction(e -> accionImportar());
        itemRestaurarBD.setOnAction(e -> accionRestaurarBD());
        itemSalir.setOnAction(e -> System.exit(0));

        // Armamos el menú
        menuArchivo.getItems().addAll(itemExportar, itemImportar, new SeparatorMenuItem(), itemRestaurarBD, new SeparatorMenuItem(), itemSalir);
        menuBar.getMenus().addAll(menuArchivo, menuAyuda);

        // Colocamos el menú en la parte SUPERIOR del BorderPane
        root.setTop(menuBar);
    }

    // --- 1. EXPORTAR ---
    private void accionExportar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Copia de Seguridad");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName("backup_lampreas_" + java.time.LocalDate.now() + ".json");

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try {
                // Guardamos
                JsonService.exportarJson(file);
                mostrarInfo("Exportación Exitosa", "Datos guardados en:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                mostrarError("Error al exportar", e);
            }
        }
    }

    // --- 2. IMPORTAR (SOLO MEMORIA) ---
    private void accionImportar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Copia de Seguridad");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try {
                // Cargamos el JSON
               // AppData datosCargados = JsonService.cargarJson(file);

                // Sobrescribimos la memoria RAM (AlmacenData)
                //AlmacenData.setAppData(datosCargados);

                // HABILITAMOS EL BOTÓN DE GUARDAR EN BD
                itemRestaurarBD.setDisable(false);

                mostrarInfo("Datos Cargados en Memoria",
                        "Se han cargado los datos del archivo JSON para visualización.\n\n" +
                                "⚠️ NOTA: La base de datos SQL NO ha cambiado aún.\n" +
                                "Si quieres hacer estos cambios permanentes, ve a 'Archivo > GUARDAR EN BD'.\n" +
                                "Pulsa el botón 'Recargar' en las pestañas para ver los datos.");

            } catch (Exception e) {
                mostrarError("Error al importar", e);
            }
        }
    }

    // --- 3. RESTAURAR BD (PELIGROSO) ---
    private void accionRestaurarBD() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("PELIGRO - Restauración de Base de Datos");
        alert.setHeaderText("¿Estás absolutamente seguro?");
        alert.setContentText(
                "Esta acción ELIMINARÁ TODOS LOS DATOS actuales de la base de datos SQL\n" +
                        "y los reemplazará por los del archivo JSON que acabas de cargar.\n\n" +
                        "Esta acción no se puede deshacer.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            /*try {
                DataRestoreService service = new DataRestoreService();
                service.restaurarBaseDeDatosDesdeMemoria();

                mostrarInfo("Restauración Completada", "La base de datos ha sido actualizada correctamente.");

                // Volvemos a deshabilitar el botón por seguridad
                itemRestaurarBD.setDisable(true);

            } catch (SQLException ex) {
                mostrarError("Error crítico en restauración", ex);
            }*/
        }
    }

    // --- UTILIDADES ---
    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}