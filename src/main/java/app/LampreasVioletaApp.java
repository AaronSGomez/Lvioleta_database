package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import view.ClientesView;
import view.PedidosView;
import view.ProductosView;
import view.AdminLogisticaView;

public class LampreasVioletaApp extends Application {
    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();
        Tab cliente = new Tab(" Gestion Clientes ");
        Tab producto = new Tab(" Gestion Productos ");
        Tab pedido = new Tab(" Gestion Pedidos");
        Tab reparto = new Tab(" Gestion Repartos");

        ClientesView vistaClientes = new ClientesView();
        ProductosView vistaProductos = new ProductosView();
        PedidosView vistaPedidos = new PedidosView();
        AdminLogisticaView vistaReparto = new AdminLogisticaView();

        cliente.setContent(vistaClientes.getRoot());
        producto.setContent(vistaProductos.getRoot());
        pedido.setContent(vistaPedidos.getRoot());
        reparto.setContent(vistaReparto.getRoot());

        cliente.setClosable(false);
        producto.setClosable(false);
        pedido.setClosable(false);
        reparto.setClosable(false);

        //añadimos el panel
        tabPane.getTabs().add(cliente);
        tabPane.getTabs().add(producto);
        tabPane.getTabs().add(pedido);
        tabPane.getTabs().add(reparto);

        Scene scene = new Scene(tabPane, 1300, 800);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}