package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.AppData;
import services.AlmacenData;
import services.JsonService;
import view.*;

import java.io.File;
import java.sql.SQLException;

public class LampreasVioletaApp extends Application {

    // Necesitamos referencia al Stage para los diálogos de abrir/guardar
    private Stage primaryStage;

    // El botón "nuclear" lo definimos aquí para habilitarlo/deshabilitarlo
    private MenuItem itemRestaurarBD;

    //  INSTANCIAMOS LAS VISTAS
    private ClientesView vistaClientes;
    private ProductosView vistaProductos;
    private PedidosView vistaPedidos;
    private AdminLogisticaView vistaReparto; // o como se llame tu clase
    private EnvioView vistaEnvio;

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
        vistaClientes = new ClientesView();
        vistaProductos = new ProductosView();
        vistaPedidos = new PedidosView();
        vistaReparto = new AdminLogisticaView();
        vistaEnvio = new EnvioView();

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

    // --- EXPORTAR --- ESTO YA FUNCIONA PERFECTAMENTE
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

    // --- IMPORTAR (SOLO MEMORIA) ---
    private void accionImportar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Cargar Copia de Seguridad");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));

        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try {
                // LEER JSON
                AppData datosCargados = JsonService.importarJson(file);

                // Esto reemplaza los datos que "ve" la aplicación
                AlmacenData.setAppData(datosCargados);

                // Activar el botón de guardar en BD y avisar
                itemRestaurarBD.setDisable(false);

                // Cargamos datos en memoria y actualizamos vista con datos de memoria
                if (vistaClientes != null) vistaClientes.refresh();
                if (vistaProductos != null) vistaProductos.refresh();
                if (vistaPedidos != null) vistaPedidos.refresh();
                if (vistaReparto != null) vistaReparto.refresh();
                if (vistaEnvio != null) vistaEnvio.refresh();

                mostrarInfo("Memoria Actualizada",
                        "Datos cargados visualmente.\n" +
                                "La base de datos SQL sigue intacta.\n" +
                                "Si quieres guardar estos cambios permanentemente, usa la opción 'Guardar en BD'.");

            } catch (Exception e) {
                mostrarError("Error al importar", e);
            }
        }
    }

    // --- RESTAURAR BD (PELIGROSO) ---
    private void accionRestaurarBD() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("PELIGRO - Restauración de Base de Datos");
        alert.setHeaderText("¿Estás absolutamente seguro?");
        alert.setContentText(
                "Esta acción ELIMINARÁ TODOS LOS DATOS actuales de la base de datos SQL\n" +
                        "y los reemplazará por los del archivo JSON que acabas de cargar.\n\n" +
                        "Esta acción no se puede deshacer.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
           /*
           aqui llamo JsonService para importar todo a la base de datos directamente con el metodo
           insertJson cogiendo los datos cargados en AlmacenData

           TODO: DEJAR LISTOS LOS REFRESH, Cambiar metodo de consulta detalles pedido y repartidores
           */
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