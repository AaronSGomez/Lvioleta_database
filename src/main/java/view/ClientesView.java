package view;

import dao.ClienteDAO;
import dao.DetalleClienteDAO;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import model.Cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import model.DetalleCliente;
import services.AlmacenData;
import services.ClienteDetalle;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Vista JavaFX para gestionar clientes.
 */
public class ClientesView{

    private final BorderPane root = new BorderPane();

    // Tabla y datos
    private final TableView<Cliente> tabla = new TableView<>();
    private final ObservableList<Cliente> datos = FXCollections.observableArrayList();

    private final DetalleClienteDAO detalleClienteDAO = new DetalleClienteDAO();

    // Caché en memoria: idCliente -> detalle
    private final Map<Integer, DetalleCliente> cacheDetalles = new HashMap<>();
    // Campos de formulario (Cliente)
    private final TextField txtId = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtEmail = new TextField();

    // Campos de formulario (DetalleCliente) – por ahora solo visuales
    private final TextField txtDireccion = new TextField();
    private final TextField txtTelefono  = new TextField();
    private final TextField txtNotas     = new TextField();

    // Botones CRUD
    private final Button btnNuevo    = new Button("Nuevo");
    private final Button btnGuardar  = new Button("Guardar");
    private final Button btnBorrar   = new Button("Borrar");
    private final Button btnRecargar = new Button("Recargar");

    // Búsqueda
    private final TextField txtBuscar          = new TextField();
    private final Button    btnBuscar          = new Button("Buscar");
    private final Button    btnLimpiarBusqueda = new Button("Limpiar");

    // DAO (acceso a BD)
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ClienteDetalle clienteService= new ClienteDetalle();


    public ClientesView() {
        configurarTabla();
        configurarFormulario();
        configurarEventos();
        recargarDatos(); // al iniciar la vista cargamos los clientes
    }

    public Parent getRoot() {
        return root;
    }

    /* =========================================================
       CONFIGURACIÓN INTERFAZ
       ========================================================= */

    private void configurarTabla() {
        TableColumn<Cliente, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));

        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));

        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        // ===== Columnas “placeholder” para DetalleCliente =====
        TableColumn<Cliente, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(c -> {
            DetalleCliente d = cacheDetalles.get(c.getValue().getId());
            String valor = (d != null) ? d.getDireccion() : "";
            return new javafx.beans.property.SimpleStringProperty(valor);
        });

        TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(c -> {
            DetalleCliente d = cacheDetalles.get(c.getValue().getId());
            String valor = (d != null) ? d.getTelefono() : "";
            return new javafx.beans.property.SimpleStringProperty(valor);
        });

        TableColumn<Cliente, String> colNotas = new TableColumn<>("Notas");
        colNotas.setCellValueFactory(c -> {
            DetalleCliente d = cacheDetalles.get(c.getValue().getId());
            String valor = (d != null) ? d.getNotas() : "";
            return new javafx.beans.property.SimpleStringProperty(valor);
        });

        tabla.getColumns().addAll(colId, colNombre, colEmail,
                colDireccion, colTelefono, colNotas);
        tabla.setItems(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.setCenter(tabla);
    }

    private void configurarFormulario() {
        // ZONA DE BÚSQUEDA
        txtBuscar.setPrefWidth(300);
        HBox zonaBusqueda = new HBox(10,
                new Label("🔍 Buscar:"), txtBuscar, btnBuscar, btnLimpiarBusqueda);
        zonaBusqueda.setAlignment(Pos.CENTER_LEFT);
        zonaBusqueda.setPadding(new Insets(0, 0, 10, 0));

        // --- Lado Izquierdo: Datos del Cliente ---
        GridPane gridCliente = new GridPane();
        gridCliente.setHgap(10);
        gridCliente.setVgap(10);

        // Configuración de columnas internas
        ColumnConstraints colLabels = new ColumnConstraints();
        colLabels.setMinWidth(60);
        ColumnConstraints colInputs = new ColumnConstraints();
        colInputs.setHgrow(Priority.ALWAYS); // Que los inputs llenen su hueco
        gridCliente.getColumnConstraints().addAll(colLabels, colInputs);

        // Campos
        txtId.setPromptText("Auto");
        txtNombre.setPromptText("Nombre completo");
        txtEmail.setPromptText("ejemplo@email.com");
        txtNombre.setMaxWidth(Double.MAX_VALUE);
        txtEmail.setMaxWidth(Double.MAX_VALUE);

        gridCliente.add(new Label("ID:"), 0, 0);
        gridCliente.add(txtId, 1, 0);
        gridCliente.add(new Label("Nombre:"), 0, 1);
        gridCliente.add(txtNombre, 1, 1);
        gridCliente.add(new Label("Email:"), 0, 2);
        gridCliente.add(txtEmail, 1, 2);

        VBox ladoIzquierdo = new VBox(10);
        Label lblTituloCliente = new Label("Datos Generales ");
        lblTituloCliente.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        ladoIzquierdo.getChildren().addAll(lblTituloCliente, gridCliente);


        // --- Lado Derecho: Detalles ---
        GridPane gridDetalle = new GridPane();
        gridDetalle.setHgap(10);
        gridDetalle.setVgap(10);
        gridDetalle.getColumnConstraints().addAll(colLabels, colInputs);

        // Campos
        txtDireccion.setPromptText("Calle, Número, Ciudad...");
        txtTelefono.setPromptText("Teléfono fijo o móvil");
        txtNotas.setPromptText("Observaciones...");
        txtDireccion.setMaxWidth(Double.MAX_VALUE);
        txtTelefono.setMaxWidth(Double.MAX_VALUE);
        txtNotas.setMaxWidth(Double.MAX_VALUE);

        gridDetalle.add(new Label("Dirección:"), 0, 0);
        gridDetalle.add(txtDireccion, 1, 0);
        gridDetalle.add(new Label("Teléfono:"), 0, 1);
        gridDetalle.add(txtTelefono, 1, 1);
        gridDetalle.add(new Label("Notas:"), 0, 2);
        gridDetalle.add(txtNotas, 1, 2);

        VBox ladoDerecho = new VBox(10);
        Label lblTituloDetalle = new Label("Detalles de Contacto ");
        lblTituloDetalle.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        ladoDerecho.getChildren().addAll(lblTituloDetalle, gridDetalle);

        // ===============================================================
        // EL CONTENEDOR MAESTRO (DIVISIÓN 35% / 65%)
        // ===============================================================
        GridPane splitContainer = new GridPane();
        splitContainer.setHgap(40); // Espacio entre los dos bloques

        // Definimos las columnas maestras con PORCENTAJES EXACTOS
        ColumnConstraints colMasterLeft = new ColumnConstraints();
        colMasterLeft.setPercentWidth(35); // <--- AQUÍ ESTÁ EL 35%

        ColumnConstraints colMasterRight = new ColumnConstraints();
        colMasterRight.setPercentWidth(65); // <--- AQUÍ ESTÁ EL 65%

        splitContainer.getColumnConstraints().addAll(colMasterLeft, colMasterRight);

        // Añadimos los paneles a sus columnas respectivas
        splitContainer.add(ladoIzquierdo, 0, 0);
        splitContainer.add(ladoDerecho, 1, 0);


        // 4. ZONA BOTONES
        HBox botonesCrud = new HBox(15, btnNuevo, btnGuardar, btnBorrar, btnRecargar);
        botonesCrud.setAlignment(Pos.CENTER);
        botonesCrud.setPadding(new Insets(15, 0, 0, 0));
        botonesCrud.getChildren().forEach(node -> ((Button)node).setPrefWidth(100));

        // 5. LAYOUT FINAL
        VBox layoutGlobal = new VBox(10);
        layoutGlobal.setPadding(new Insets(20, 50, 20, 50));

        layoutGlobal.getChildren().addAll(
                zonaBusqueda,
                new Separator(),
                splitContainer,
                new Separator(),
                botonesCrud
        );

        BorderPane.setAlignment(layoutGlobal, Pos.CENTER);
        root.setBottom(layoutGlobal);
    }

    private void configurarEventos() {
        // Cuando seleccionamos una fila en la tabla, pasamos los datos al formulario
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                // Cliente
                txtId.setText(String.valueOf(newSel.getId()));
                txtNombre.setText(newSel.getNombre());
                txtEmail.setText(newSel.getEmail());
                txtId.setDisable(true); // al editar, de momento, no dejamos cambiar el ID
                //DetalleCliente
                try {
                    DetalleCliente newDetalle =
                            AlmacenData.getDetallesCliente().stream()
                            .filter(c -> c.getId() == newSel.getId())
                            .findFirst()
                            .orElse(null);
                    if (newDetalle != null) {
                        txtDireccion.setText(newDetalle.getDireccion());
                        txtTelefono.setText(newDetalle.getTelefono());
                        txtNotas.setText(newDetalle.getNotas());
                    }   else{
                        txtDireccion.clear();
                        txtTelefono.clear();
                        txtNotas.clear();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        btnNuevo.setOnAction(e -> limpiarFormulario());

        btnGuardar.setOnAction(e -> guardarCliente());

        btnBorrar.setOnAction(e -> borrarClienteSeleccionado());

        btnRecargar.setOnAction(e -> {
            txtBuscar.clear();
            recargarDatos();
        });

        btnBuscar.setOnAction(e -> buscarClientesEnBBDD());

        btnLimpiarBusqueda.setOnAction(e -> {
            txtBuscar.clear();
            recargarDatos();
        });
    }

    /* =========================================================
       LÓGICA DE NEGOCIO
       ========================================================= */

    private void recargarDatos() {
        try {
            List<Cliente> clientes = AlmacenData.getClientes();
            List<DetalleCliente> detalles = AlmacenData.getDetallesCliente();

            // 3) Rellenar la caché id -> detalle
            cacheDetalles.clear();
            for (DetalleCliente d : detalles) {
                cacheDetalles.put(d.getId(), d);
            }

            datos.setAll(clientes);

        } catch (SQLException e) {
            mostrarError("Error al recargar datos", e);
        }
    }

    private void buscarClientesEnMemoria() {
        String filtro = txtBuscar.getText().trim();
        if (filtro.isEmpty()) {
            recargarDatos();
            return;
        }

        try {
            List<Cliente> lista = clienteDAO.findAll();
            String f = filtro.toLowerCase();

            List<Cliente> filtrados = lista.stream()
                    .filter(c ->
                            String.valueOf(c.getId()).contains(f) ||
                                    c.getNombre().toLowerCase().contains(f) ||
                                    c.getEmail().toLowerCase().contains(f)
                    )
                    .collect(Collectors.toList());

            datos.setAll(filtrados);
        } catch (SQLException e) {
            mostrarError("Error al buscar clientes", e);
        }
    }

    private void buscarClientesEnBBDD(){
        String filtro = txtBuscar.getText().trim();

        if ((filtro.isEmpty())){
            recargarDatos();
            return;
        }

        try {
            List<Cliente> lista = clienteDAO.search(filtro);
            datos.setAll(lista);

        } catch (SQLException e){
            mostrarError("Error al buscar", e);
        }

    }

    private void limpiarFormulario() {
        txtId.clear();
        txtNombre.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtNotas.clear();
        txtId.setDisable(false);
        tabla.getSelectionModel().clearSelection();
    }

    /**
     * Guardar cliente:
     *  - Si no existe en la BD → INSERT usando ClienteDAO.insert()
     */
    private void guardarCliente() {
        // Con ID manual, vuelve a ser obligatorio
        if (txtId.getText().isBlank() ||
                txtNombre.getText().isBlank() ||
                txtEmail.getText().isBlank()) {

            mostrarAlerta("Campos obligatorios",
                    "Debes rellenar ID, nombre y email.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(txtId.getText().trim());
        } catch (NumberFormatException ex) {
            mostrarAlerta("ID inválido", "El ID debe ser un número entero.");
            return;
        }

        // Cliente con ID escrito por el usuario
        Cliente c = new Cliente(
                id,
                txtNombre.getText().trim(),
                txtEmail.getText().trim()
        );

        // DetalleCliente con el MISMO ID
        DetalleCliente d = new DetalleCliente(
                id,
                txtDireccion.getText().trim(),
                txtTelefono.getText().trim(),
                txtNotas.getText().trim()
        );

        try {
            // Comprobamos en BD si ese ID ya existe
            Cliente existente = clienteDAO.findById(id);

            if (existente == null) {

                clienteService.guardarClienteCompleto(c,d);

                mostrarInfo("Insertado",
                        "Cliente y detalle creados (sin transacción).");
            } else {

                mostrarAlerta("Actualizar pendiente",
                        "El cliente ya existe.\n" +
                                "Más adelante aquí haremos UPDATE desde el Service.");
            }

            AlmacenData.setClientes();
            recargarDatos();
            limpiarFormulario();

        } catch (SQLException e) {
            mostrarError("Error al guardar cliente y detalle", e);
        }
    }

    private void borrarClienteSeleccionado() {
        Cliente sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Sin selección", "Selecciona un cliente en la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar borrado");
        confirm.setHeaderText("¿Eliminar cliente?");
        confirm.setContentText("Se borrará el cliente con ID " + sel.getId());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        mostrarAlerta("Borrado pendiente",
                "Aún no existe deleteById en ClienteDAO.\n" +
                        "Cuando lo implementemos, aquí se llamará al método.");

    }

    public void refresh() {
        try {
            // 1. Recuperar las listas de la Memoria (AlmacenData)
            List<Cliente> listaClientes = AlmacenData.getClientes();
            List<DetalleCliente> listaDetalles = AlmacenData.getDetallesCliente();

            // 2. CRUCIAL: Actualizar la caché (El diccionario ID -> Detalle)
            // Si no haces esto, las columnas de dirección/teléfono no sabrán qué pintar
            cacheDetalles.clear();
            for (DetalleCliente d : listaDetalles) {
                // Asumimos que d.getId() es igual al id del Cliente (PK compartida)
                cacheDetalles.put(d.getId(), d);
            }

            // 3. Actualizar los datos de la tabla
            // Usamos 'datos.setAll' porque 'tabla' ya está vinculada a 'datos' en el constructor
            datos.setAll(listaClientes);

            // 4. Forzar repintado visual
            tabla.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /* =========================================================
       DIÁLOGOS AUXILIARES
       ========================================================= */

    private void mostrarError(String titulo, Exception e) {
        e.printStackTrace();
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
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}