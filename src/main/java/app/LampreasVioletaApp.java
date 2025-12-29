package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import view.*;

public class LampreasVioletaApp extends Application {
    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();
        Tab cliente = new Tab(" Gestion Clientes ");
        Tab producto = new Tab(" Gestion Productos ");
        Tab pedido = new Tab(" Gestion Pedidos");
        Tab reparto = new Tab(" Gestion Repartos");
        Tab envio = new Tab(" Envios");

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

        cliente.setClosable(false);
        producto.setClosable(false);
        pedido.setClosable(false);
        reparto.setClosable(false);
        envio.setClosable(false);

        //añadimos el panel
        tabPane.getTabs().add(cliente);
        tabPane.getTabs().add(producto);
        tabPane.getTabs().add(pedido);
        tabPane.getTabs().add(reparto);
        tabPane.getTabs().add(envio);

        Scene scene = new Scene(tabPane, 1300, 800);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}