package view;

import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Cliente;
import model.DetallePedido;
import model.Pedido;
import services.PedidoDetalle;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidosView {

    private final BorderPane root = new BorderPane();

    // --- TABLAS ---
    private final TableView<Pedido> tablaPedidos = new TableView<>();
    private TableView<DetallePedido> tablaDetalles; // Se inicializa en configurarTablaDetalles

    // Lista observable para la tabla principal (Pedidos)
    private final ObservableList<Pedido> datosPedidos = FXCollections.observableArrayList();
    // La lista de detalles se gestionará directamente con tablaDetalles.getItems()

    // DAOS Y SERVICIOS
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO();
    private final PedidoDetalle pedidoService = new PedidoDetalle();

    // CAMPOS PEDIDO (Cabecera)
    private final TextField txtId = new TextField();
    private final TextField txtClienteId = new TextField();
    private final DatePicker txtFecha = new DatePicker();

    // CAMPOS DETALLE (Líneas)
    private final TextField txtProductoId = new TextField();
    private final TextField txtCantidad = new TextField();
    private final TextField txtPrecioU = new TextField();

    // BOTONES
    // Acciones sobre DETALLES (En memoria)
    private final Button btnAgregarDetalle = new Button("Añadir Línea (+)");
    private final Button btnQuitarDetalle = new Button("Quitar Línea (-)");

    // Acciones GLOBALES (Base de Datos)
    private final Button btnNuevo = new Button("Nuevo Pedido");
    private final Button btnGuardar = new Button("GUARDAR TODO (Transacción)");
    private final Button btnBorrar = new Button("Borrar Pedido");
    private final Button btnRecargar = new Button("Recargar");

    // Búsqueda
    private final TextField txtBuscar = new TextField();
    private final Button btnBuscar = new Button("Buscar");
    private final Button btnLimpiarBusqueda = new Button("Limpiar");

    public PedidosView() {
        configurarTablaPedidos();
        configurarTablaDetalles(); // Configuramos la segunda tabla
        configurarLayoutCentral(); // Unimos las dos tablas visualmente
        configurarFormulario();
        configurarEventos();
        recargarDatos();
    }

    public Parent getRoot() {
        return root;
    }

    /* =========================================================
      TABLAS
       ========================================================= */

    private void configurarTablaPedidos() {

        TableColumn<Pedido, Number> colId = new TableColumn<>("ID Pedido");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        TableColumn<Pedido, Number> colIdCli = new TableColumn<>("ID Cliente");
        colIdCli.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getClienteId()));
        TableColumn<Pedido, String> colNomCli = new TableColumn<>("Nombre Cliente");
        colNomCli.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCliente()));
        TableColumn<Pedido, String> colFec = new TableColumn<>("Fecha Pedido");
        colFec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha().toString()));
        TableColumn<Pedido, Number> colTotal = new TableColumn<>("Cantidad Total (€)");
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotalImporte()));

        // Formato moneda alineado a la derecha
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        tablaPedidos.getColumns().clear();
        tablaPedidos.getColumns().addAll(colId, colIdCli, colNomCli, colFec, colTotal);
        tablaPedidos.setItems(datosPedidos);
    }

    private void configurarTablaDetalles() {
        tablaDetalles = new TableView<>();
        tablaDetalles.setPlaceholder(new Label("Sin detalles. Añade productos abajo."));

        TableColumn<DetallePedido, Number> colProd = new TableColumn<>("Producto ID");
        colProd.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getProductoId()));

        TableColumn<DetallePedido, String> colNomProd = new TableColumn<>("Producto ");
        colNomProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreProducto()));

        TableColumn<DetallePedido, Number> colCant = new TableColumn<>("Cantidad");
        colCant.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()));

        TableColumn<DetallePedido, Number> colPre = new TableColumn<>("Precio Unit.");
        colPre.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecioUnit()));
        colPre.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Columna calculada para ver el subtotal
        TableColumn<DetallePedido, Number> colTotal = new TableColumn<>("Subtotal");
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(
                c.getValue().getCantidad() * c.getValue().getPrecioUnit()
        ));
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        tablaDetalles.getColumns().addAll(colProd, colNomProd, colCant, colPre, colTotal);

        // Ajustamos altura para que no ocupe demasiado si está vacía
        tablaDetalles.setPrefHeight(200);
    }

    private void configurarLayoutCentral() {
        // Un VBox que contiene ambas tablas apiladas
        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setPadding(new Insets(10));

        Label lblPedidos = new Label("1. Lista de Pedidos (Selecciona uno para ver/editar)");
        Label lblDetalles = new Label("2. Detalles del Pedido (Productos)");

        // Hacemos que la tabla de pedidos crezca más
        VBox.setVgrow(tablaPedidos, Priority.ALWAYS);

        panelIzquierdo.getChildren().addAll(lblPedidos, tablaPedidos, new Separator(), lblDetalles, tablaDetalles);
        root.setCenter(panelIzquierdo);
    }

    /* =========================================================
       FORMULARIO Y BOTONES
       ========================================================= */

    private void configurarFormulario() {
       //definimos panel derecho con 400 pixels
        VBox panelDerecho = new VBox(20);
        panelDerecho.setPadding(new Insets(10));
        panelDerecho.setPrefWidth(400);
       //botones globales arriba
        VBox botonesGlobal= new VBox(10,btnNuevo,btnGuardar,btnBorrar,btnRecargar);
        botonesGlobal.getChildren().forEach(node -> ((Button)node).setMaxWidth(Double.MAX_VALUE));
        //busqueda a continuacion
        VBox bloqueBusqueda = new VBox(5,
                new Label("Búsqueda:"),
                txtBuscar,
                new HBox(5, btnBuscar, btnLimpiarBusqueda)
        );
        //Formulario pedidos
        GridPane formPedidos = new GridPane();
        formPedidos.setHgap(10);
        formPedidos.setVgap(10);

        txtId.setPromptText("ID Auto/Manual");
        txtClienteId.setPromptText("ID Cliente");

        formPedidos.add(new Label("PEDIDOS"), 0, 0, 2, 1);
        formPedidos.add(new Label("ID Pedido:"), 0, 1); formPedidos.add(txtId, 1, 1);
        formPedidos.add(new Label("Cliente ID:"), 0, 2); formPedidos.add(txtClienteId, 1, 2);
        formPedidos.add(new Label("Fecha:"), 0, 3); formPedidos.add(txtFecha, 1, 3);
        //Formulario productos
        GridPane formDetalle = new GridPane();
        formDetalle.setHgap(10); formDetalle.setVgap(10);
        formDetalle.add(new Label("LÍNEA DE PRODUCTO"), 0, 0, 2, 1);
        formDetalle.add(new Label("ID Prod:"), 0, 1); formDetalle.add(txtProductoId, 1, 1);
        formDetalle.add(new Label("Cant:"), 0, 2); formDetalle.add(txtCantidad, 1, 2);
        formDetalle.add(new Label("Precio/u:"), 0, 3); formDetalle.add(txtPrecioU, 1, 3);
        txtProductoId.setPromptText("ID Prod");
        txtCantidad.setPromptText("Cant");
        txtPrecioU.setPromptText("Precio/unidad");

        // Botones de agregar/quitar detalle
        HBox botonesDetalle = new HBox(10, btnAgregarDetalle, btnQuitarDetalle);
        formDetalle.add(botonesDetalle, 3, 4, 2, 1);

        panelDerecho.getChildren().addAll(
                botonesGlobal,
                new Separator(),
                bloqueBusqueda,
                new Separator(),
                formPedidos,
                new Separator(),
                formDetalle,botonesDetalle
        );

        root.setRight(panelDerecho);
    }

    private void configurarEventos() {
        // SELECCIÓN EN TABLA PEDIDOS
        tablaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cargarPedidoEnFormulario(newSel);
            }
        });

        // BOTONES MEMORIA (Tabla Detalles)
        btnAgregarDetalle.setOnAction(e -> agregarDetalleEnMemoria());

        btnQuitarDetalle.setOnAction(e -> {
            DetallePedido seleccionado = tablaDetalles.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                tablaDetalles.getItems().remove(seleccionado);
            } else {
                mostrarAlerta("Aviso", "Selecciona una línea de detalle para quitarla.");
            }
        });

        // --- BOTONES BBDD ---
        btnNuevo.setOnAction(e -> limpiarFormularioCompleto());

        // Guardar todo junto
        btnGuardar.setOnAction(e -> guardarTransaccionCompleta());

        btnBorrar.setOnAction(e -> borrarPedidoActual());

        btnRecargar.setOnAction(e -> {
            txtBuscar.clear();
            recargarDatos();
        });

        btnBuscar.setOnAction(e -> buscarPedidos());
        btnLimpiarBusqueda.setOnAction(e -> { txtBuscar.clear(); recargarDatos(); });
    }

    private void cargarPedidoEnFormulario(Pedido p) {
        // Cargar Cabecera
        txtId.setText(String.valueOf(p.getId()));
        txtClienteId.setText(String.valueOf(p.getClienteId()));
        txtFecha.setValue(p.getFecha());
        txtId.setDisable(true); // Bloqueamos ID al editar

        // Cargar Detalles desde BBDD
        try {
            List<DetallePedido> detalles = detallePedidoDAO.findById(p.getId());
            if (detalles != null) {
                tablaDetalles.setItems(FXCollections.observableArrayList(detalles));
            } else {
                tablaDetalles.getItems().clear();
            }
        } catch (SQLException e) {
            mostrarError("Error cargando detalles", e);
        }
    }

    private void agregarDetalleEnMemoria() {
        // Validar campos de detalle
        if (txtProductoId.getText().isBlank() || txtCantidad.getText().isBlank() || txtPrecioU.getText().isBlank()) {
            mostrarAlerta("Datos incompletos", "Rellena Producto, Cantidad y Precio.");
            return;
        }

        try {
            int prodId = Integer.parseInt(txtProductoId.getText().trim());
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            double precio = Double.parseDouble(txtPrecioU.getText().trim());

            // Obtenemos el ID del pedido actual (si existe), o 0 si es nuevo
            int currentPedidoId = 0;
            if (!txtId.getText().isBlank()) {
                currentPedidoId = Integer.parseInt(txtId.getText());
            }

            // Creamos el objeto (NO SE GUARDA EN BD, SOLO EN TABLA)
            DetallePedido nuevoDetalle = new DetallePedido(currentPedidoId, prodId, cant, precio);

            // Añadimos a la tabla visual
            tablaDetalles.getItems().add(nuevoDetalle);

            // Limpiamos campos de detalle para meter el siguiente
            txtProductoId.clear();
            txtCantidad.clear();
            txtPrecioU.clear();
            txtProductoId.requestFocus();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Asegúrate de poner números válidos.");
        }
    }

    private void guardarTransaccionCompleta() {
        if (txtId.getText().isBlank() || txtClienteId.getText().isBlank() || txtFecha.getValue() == null) {
            mostrarAlerta("Faltan datos", "Debes rellenar la cabecera del pedido (ID, Cliente, Fecha).");
            return;
        }

        try {
            // Recoger datos Pedido
            int idPedido = Integer.parseInt(txtId.getText());
            int idCliente = Integer.parseInt(txtClienteId.getText());
            LocalDate fecha = txtFecha.getValue();

            Pedido pedido = new Pedido(idPedido, idCliente, fecha);
            List<DetallePedido> listaDetalles = new ArrayList<>(tablaDetalles.getItems());

            if (listaDetalles.isEmpty()) {
                mostrarAlerta("Pedido vacío", "No puedes guardar un pedido sin productos.");
                return;
            }

            // TRANSACCIÓN
            Pedido existente= pedidoDAO.findById(idPedido);
            if (existente == null) {
                pedidoService.insertPedidoCompleto(pedido, listaDetalles);
                mostrarInfo("Éxito", "Pedido y " + listaDetalles.size() + " líneas guardados correctamente.");

            }else{
                pedidoService.updatePedidoCompleto(pedido, listaDetalles);
                mostrarInfo("Éxito", "Pedido y " + listaDetalles.size() + " líneas actualizadas correctamente.");
            }

            limpiarFormularioCompleto();
            recargarDatos();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error ID", "Los IDs deben ser números enteros.");
        } catch (SQLException e) {
            mostrarError("Error BBDD", e);
        } catch (Exception e) {
            mostrarError("Error Desconocido", e);
        }
    }

    private void borrarPedidoActual() {
        Pedido sel = tablaPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        // Lógica de borrado (Pendiente implementar deleteById en DAO)
        mostrarInfo("Info", "Aquí llamarías a pedidoService.borrarPedido(" + sel.getId() + ")");
    }

    private void recargarDatos() {
        try {
            datosPedidos.setAll(pedidoDAO.findAll());
        } catch (SQLException e) {
            mostrarError("Error recargando", e);
        }
    }

    private void buscarPedidos() {
        String filtro = txtBuscar.getText().trim();
        if (filtro.isEmpty()) { recargarDatos(); return; }
        try {
            datosPedidos.setAll(pedidoDAO.search(filtro));
        } catch (SQLException e) { mostrarError("Error buscando", e); }
    }

    private void limpiarFormularioCompleto() {
        txtId.clear();
        txtClienteId.clear();
        txtFecha.setValue(LocalDate.now());
        txtId.setDisable(false);

        txtProductoId.clear();
        txtCantidad.clear();
        txtPrecioU.clear();

        tablaDetalles.getItems().clear();
        tablaPedidos.getSelectionModel().clearSelection();
    }

    /*===================
    *       AVISOS
    * ===================*/

    private void mostrarError(String titulo, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}