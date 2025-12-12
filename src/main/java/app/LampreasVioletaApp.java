package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class LampreasVioletaApp extends Application {
    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();
        Tab cliente = new Tab(" Gestion Clientes ");
        Tab producto = new Tab(" Gestion Productos ");

        ClientesView vistaClientes = new ClientesView();
        ProductosView vistaProductos = new ProductosView();

        cliente.setContent(vistaClientes.getRoot());
        producto.setContent(vistaProductos.getRoot());
        cliente.setClosable(false);
        producto.setClosable(false);

        //añadimos el panel
        tabPane.getTabs().add(cliente);
        tabPane.getTabs().add(producto);
        Scene scene = new Scene(tabPane, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}