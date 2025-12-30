package view;

import dao.DetallePedidoDAO;
import dao.PedidoDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.DetallePedido;
import model.Envio;
import model.Pedido;
import model.Producto;
import services.AlmacenData;
import services.PedidoDetalle;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidosView {

    private final BorderPane root = new BorderPane();

    // --- TABLAS ---
    private final TableView<Pedido> tablaPedidos = new TableView<>();
    private TableView<DetallePedido> tablaDetalles;

    // Lista observable para la tabla principal (Pedidos)
    private final ObservableList<Pedido> datosPedidos = FXCollections.observableArrayList();

    // DAOS Y SERVICIOS
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetallePedidoDAO detallePedidoDAO = new DetallePedidoDAO();
    private final PedidoDetalle pedidoService = new PedidoDetalle();

    // CAMPOS PEDIDO (Cabecera)
    private final TextField txtId = new TextField();
    private final TextField txtClienteId = new TextField();
    private final DatePicker txtFecha = new DatePicker();

    // CAMPOS DETALLE (Líneas)
    private final ComboBox<Producto> comboProducto = new ComboBox<>();
    private final TextField txtCantidad = new TextField();
    private final TextField txtPrecioU = new TextField();

    // BOTONES
    private final Button btnAgregarDetalle = new Button("Añadir Línea (+)");
    private final Button btnQuitarDetalle = new Button("Quitar Línea (-)");

    // Acciones GLOBALES
    private final Button btnNuevo = new Button("Nuevo Pedido");
    private final Button btnGuardar = new Button("GUARDAR TODO (Transacción)");
    private final Button btnBorrar = new Button("Borrar Pedido");
    private final Button btnRecargar = new Button("Recargar");
    private final Button btnGestionarEnvio = new Button("🚚 Gestión Envío");

    // Búsqueda
    private final TextField txtBuscar = new TextField();
    private final Button btnBuscar = new Button("Buscar");
    private final Button btnLimpiarBusqueda = new Button("Limpiar");

    public PedidosView() {
        configurarTablaPedidos();
        configurarTablaDetalles();
        configurarLayoutCentral();
        configurarComboProductos();
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
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        tablaPedidos.getColumns().clear();
        tablaPedidos.getColumns().addAll(colId, colIdCli, colNomCli, colFec, colTotal);
        tablaPedidos.setItems(datosPedidos);
        tablaPedidos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarTablaDetalles() {
        tablaDetalles = new TableView<>();
        tablaDetalles.setPlaceholder(new Label("Sin detalles. Añade productos abajo."));

        TableColumn<DetallePedido, Number> colProd = new TableColumn<>("ID Prod");
        colProd.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getProductoId()));

        TableColumn<DetallePedido, String> colNomProd = new TableColumn<>("Producto");
        colNomProd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreProducto()));

        TableColumn<DetallePedido, Number> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getCantidad()));

        TableColumn<DetallePedido, Number> colPre = new TableColumn<>("Precio U.");
        colPre.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrecioUnit()));
        colPre.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<DetallePedido, Number> colTotal = new TableColumn<>("Subtotal");
        colTotal.setCellValueFactory(c -> new SimpleDoubleProperty(
                c.getValue().getCantidad() * c.getValue().getPrecioUnit()
        ));
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");

        tablaDetalles.getColumns().addAll(colProd, colNomProd, colCant, colPre, colTotal);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaDetalles.setPrefHeight(200);
    }

    private void configurarLayoutCentral() {
        VBox panelIzquierdo = new VBox(10);
        panelIzquierdo.setPadding(new Insets(10));

        Label lblPedidos = new Label("1. Lista de Pedidos");
        Label lblDetalles = new Label("2. Detalles del Pedido");

        VBox.setVgrow(tablaPedidos, Priority.ALWAYS);

        panelIzquierdo.getChildren().addAll(lblPedidos, tablaPedidos, new Separator(), lblDetalles, tablaDetalles);
        root.setCenter(panelIzquierdo);
    }

    /* =========================================================
       FORMULARIO Y BOTONES
       ========================================================= */

    private void configurarFormulario() {
        VBox panelDerecho = new VBox(20);
        panelDerecho.setPadding(new Insets(10));
        panelDerecho.setPrefWidth(400);

        VBox botonesGlobal = new VBox(10, btnNuevo, btnGuardar, btnBorrar, btnRecargar, btnGestionarEnvio);
        botonesGlobal.getChildren().forEach(node -> ((Button)node).setMaxWidth(Double.MAX_VALUE));

        VBox bloqueBusqueda = new VBox(5,
                new Label("Búsqueda:"),
                txtBuscar,
                new HBox(5, btnBuscar, btnLimpiarBusqueda)
        );

        // Formulario pedidos
        GridPane formPedidos = new GridPane();
        formPedidos.setHgap(10); formPedidos.setVgap(10);

        txtId.setPromptText("ID Auto/Manual");
        txtClienteId.setPromptText("ID Cliente");

        formPedidos.add(new Label("DATOS PEDIDO"), 0, 0, 2, 1);
        formPedidos.add(new Label("ID Pedido:"), 0, 1); formPedidos.add(txtId, 1, 1);
        formPedidos.add(new Label("Cliente ID:"), 0, 2); formPedidos.add(txtClienteId, 1, 2);
        formPedidos.add(new Label("Fecha:"), 0, 3); formPedidos.add(txtFecha, 1, 3);

        // Formulario productos
        GridPane formDetalle = new GridPane();
        formDetalle.setHgap(10); formDetalle.setVgap(10);
        formDetalle.add(new Label("AÑADIR PRODUCTO"), 0, 0, 2, 1);
        formDetalle.add(new Label("Producto:"), 0, 1); formDetalle.add(comboProducto, 1, 1);
        formDetalle.add(new Label("Cant:"), 0, 2); formDetalle.add(txtCantidad, 1, 2);
        formDetalle.add(new Label("Precio/u:"), 0, 3); formDetalle.add(txtPrecioU, 1, 3);

        txtCantidad.setPromptText("Cant");
        txtPrecioU.setPromptText("Precio/unidad");

        HBox botonesDetalle = new HBox(10, btnAgregarDetalle, btnQuitarDetalle);
        formDetalle.add(botonesDetalle, 0, 4, 2, 1);

        panelDerecho.getChildren().addAll(
                botonesGlobal, new Separator(),
                bloqueBusqueda, new Separator(),
                formPedidos, new Separator(),
                formDetalle
        );
        root.setRight(panelDerecho);
    }

    private void configurarEventos() {
        tablaPedidos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                cargarPedidoEnFormulario(newSel);
            }
        });

        btnAgregarDetalle.setOnAction(e -> agregarDetalleEnMemoria());

        btnQuitarDetalle.setOnAction(e -> {
            DetallePedido seleccionado = tablaDetalles.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                tablaDetalles.getItems().remove(seleccionado);
            } else {
                mostrarAlerta("Aviso", "Selecciona una línea para quitarla.");
            }
        });

        btnNuevo.setOnAction(e -> limpiarFormularioCompleto());
        btnGuardar.setOnAction(e -> guardarTransaccionCompleta());
        btnBorrar.setOnAction(e -> borrarPedidoActual());
        btnRecargar.setOnAction(e -> { txtBuscar.clear(); recargarDatos(); });

        // --- LOGICA GESTIÓN ENVÍO ---
        btnGestionarEnvio.setOnAction(e -> {
            Pedido seleccionado = tablaPedidos.getSelectionModel().getSelectedItem();

            if (seleccionado == null) {
                mostrarAlerta("Atención", "Selecciona un pedido de la lista primero.");
                return;
            }

            EnvioDialog dialog = new EnvioDialog(seleccionado.getId());

            dialog.showAndWait().ifPresent(guardadoExitoso -> {
                if (guardadoExitoso) {
                    try {
                        // 1. FORZAMOS LA ACTUALIZACIÓN DE LA MEMORIA DE ENVÍOS
                        AlmacenData.setEnvios();

                        // 2. Refrescamos el formulario actual para que se bloquee visualmente
                        cargarPedidoEnFormulario(seleccionado);

                        mostrarInfo("Logística", "Envío gestionado. El pedido ahora está bloqueado.");

                    } catch (SQLException ex) {
                        mostrarError("Error actualizando caché", ex);
                    }
                }
            });
        });

        btnBuscar.setOnAction(e -> buscarPedidos());
        btnLimpiarBusqueda.setOnAction(e -> { txtBuscar.clear(); recargarDatos(); });
    }

    /* =========================================================
       LÓGICA DE CARGA Y BLOQUEO
       ========================================================= */

    private void cargarPedidoEnFormulario(Pedido p) {
        // 1. Cargar Cabecera
        txtId.setText(String.valueOf(p.getId()));
        txtClienteId.setText(String.valueOf(p.getClienteId()));
        txtFecha.setValue(p.getFecha());
        txtId.setDisable(true); // ID bloqueado siempre al editar

        // 2. LÓGICA DE BLOQUEO (Consulta a AlmacenData)
        try {
            List<Envio> todosLosEnvios = AlmacenData.getEnvios();

            // Buscamos si este pedido tiene envío
            Envio envioEncontrado = todosLosEnvios.stream()
                    .filter(e -> e.getPedidoId() == p.getId())
                    .findFirst()
                    .orElse(null);

            if (envioEncontrado != null) {
                // ¡TIENE ENVÍO! -> BLOQUEAR TODO
                setModoEdicion(false);
                mostrarInfo("Solo Lectura",
                        "Pedido en estado: " + envioEncontrado.getEstado() +
                                ". No se puede modificar.");
            } else {
                // NO TIENE ENVÍO -> PERMITIR EDICIÓN
                setModoEdicion(true);
            }

        } catch (SQLException e) {
            mostrarError("Error comprobando envíos", e);
        }

        // 3. Cargar Detalles
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

    // --- EL MÉTODO QUE TE FALTABA ---
    private void setModoEdicion(boolean activo) {
        // Si activo es true -> Botones habilitados (false disable)
        // Si activo es false -> Botones deshabilitados (true disable)
        boolean desactivar = !activo;

        btnGuardar.setDisable(desactivar);
        btnBorrar.setDisable(desactivar);
        btnAgregarDetalle.setDisable(desactivar);
        btnQuitarDetalle.setDisable(desactivar);

        comboProducto.setDisable(desactivar);
        txtCantidad.setDisable(desactivar);
        txtPrecioU.setDisable(desactivar);

        // También bloqueamos los campos de cabecera si está enviado
        txtClienteId.setDisable(desactivar);
        txtFecha.setDisable(desactivar);
    }

    private void limpiarFormularioCompleto() {
        txtId.clear();
        txtClienteId.clear();
        txtFecha.setValue(LocalDate.now());

        // IMPORTANTE: Al crear nuevo, desbloqueamos todo
        setModoEdicion(true);
        txtId.setDisable(false); // Permitimos escribir ID si es manual

        comboProducto.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtPrecioU.clear();

        tablaDetalles.getItems().clear();
        tablaPedidos.getSelectionModel().clearSelection();
    }

    /* =========================================================
       CRUD Y OTROS MÉTODOS
       ========================================================= */

    private void agregarDetalleEnMemoria() {
        Producto prodSeleccionado = comboProducto.getValue();

        if (prodSeleccionado == null || txtCantidad.getText().isBlank() || txtPrecioU.getText().isBlank()) {
            mostrarAlerta("Datos incompletos", "Selecciona Producto, Cantidad y Precio.");
            return;
        }

        try {
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            double precio = Double.parseDouble(txtPrecioU.getText().trim());

            int currentPedidoId = 0;
            if (!txtId.getText().isBlank()) {
                currentPedidoId = Integer.parseInt(txtId.getText());
            }

            DetallePedido nuevoDetalle = new DetallePedido(
                    currentPedidoId,
                    prodSeleccionado.getId(),
                    cant,
                    precio
            );
            nuevoDetalle.setNombreProducto(prodSeleccionado.getNombre());

            tablaDetalles.getItems().add(nuevoDetalle);

            comboProducto.getSelectionModel().clearSelection();
            txtCantidad.clear();
            txtPrecioU.clear();
            comboProducto.requestFocus();

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de formato", "Cantidad y Precio deben ser números.");
        }
    }

    private void guardarTransaccionCompleta() {
        if (txtId.getText().isBlank() || txtClienteId.getText().isBlank() || txtFecha.getValue() == null) {
            mostrarAlerta("Faltan datos", "Rellena ID, Cliente y Fecha.");
            return;
        }

        try {
            int idPedido = Integer.parseInt(txtId.getText());
            int idCliente = Integer.parseInt(txtClienteId.getText());
            LocalDate fecha = txtFecha.getValue();

            Pedido pedido = new Pedido(idPedido, idCliente, fecha);
            List<DetallePedido> listaDetalles = new ArrayList<>(tablaDetalles.getItems());

            if (listaDetalles.isEmpty()) {
                mostrarAlerta("Pedido vacío", "No puedes guardar sin productos.");
                return;
            }

            Pedido existente = pedidoDAO.findById(idPedido);
            if (existente == null) {
                pedidoService.insertPedidoCompleto(pedido, listaDetalles);
                mostrarInfo("Éxito", "Pedido guardado.");
            } else {
                pedidoService.updatePedidoCompleto(pedido, listaDetalles);
                mostrarInfo("Éxito", "Pedido actualizado.");
            }

            AlmacenData.setPedidos(); // Refrescar memoria
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
        mostrarInfo("Info", "Funcionalidad borrar pendiente de implementar en DAO.");
    }

    private void recargarDatos() {
        try {
            datosPedidos.setAll(AlmacenData.getPedidos());
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

    private void configurarComboProductos() {
        try {
            List<Producto> lista = AlmacenData.getProductos();
            comboProducto.setItems(FXCollections.observableArrayList(lista));

            comboProducto.setConverter(new StringConverter<Producto>() {
                @Override
                public String toString(Producto p) {
                    if (p == null) return null;
                    return p.getNombre() + " (" + p.getPrecio() + " €)";
                }

                @Override
                public Producto fromString(String string) { return null; }
            });

            comboProducto.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    txtPrecioU.setText(String.valueOf(newVal.getPrecio()));
                }
            });

        } catch (SQLException e) {
            mostrarError("Error cargando productos", e);
        }
    }

    public void refresh() {
        try {
            //  Recuperar las listas de la Memoria (AlmacenData)
            List<Pedido> lista = AlmacenData.getPedidos();

            // Actualizar los datos de la tabla
            // Usamos 'datos.setAll' porque 'tabla' ya está vinculada a 'datos' en el constructor
            datosPedidos.setAll(lista);

            // 4. Forzar repintado visual
            tablaPedidos.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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