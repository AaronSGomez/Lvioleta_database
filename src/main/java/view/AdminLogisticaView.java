package view;

import dao.EmpresaRepartoDAO;
import dao.RepartidorDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.EmpresaReparto;
import model.Repartidor;

import java.sql.SQLException;
import java.util.List;

public class AdminLogisticaView {

    // Contenedor principal (faltaba declararlo en tu snippet)
    private final BorderPane root = new BorderPane();

    // --- TABLAS ---
    private final TableView<EmpresaReparto> tablaEmpresa = new TableView<>();
    private TableView<Repartidor> tablaRepartidor;

    // Lista observable para la tabla principal (Empresas)
    private final ObservableList<EmpresaReparto> datosEmpresas = FXCollections.observableArrayList();

    // DAOS
    private final EmpresaRepartoDAO empresaRepartoDAO = new EmpresaRepartoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();

    // CAMPOS EMPRESA (Cabecera)
    private final TextField txtEmId = new TextField();
    private final TextField txtRazonS = new TextField(); // Nombre
    private final TextField txtDireccion = new TextField();
    private final TextField txtEmTelefono = new TextField();

    // CAMPOS REPARTIDOR (Líneas)
    private final TextField txtReId = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtReTelefono = new TextField();
    private final TextField txtEmpresaId = new TextField();

    // BOTONES EMPRESA
    private final Button btnNuevo = new Button("Nueva Empresa");
    private final Button btnGuardar = new Button("Guardar Empresa");
    private final Button btnBorrar = new Button("Borrar Empresa");
    private final Button btnRecargar = new Button("Recargar Todo");

    // BOTONES REPARTIDOR
    private final Button btnNuevoR = new Button("Nuevo Repartidor");
    private final Button btnGuardarR = new Button("Guardar Repartidor");
    private final Button btnBorrarR = new Button("Borrar Repartidor");

    // Búsqueda (Opcional, implementada básica)
    private final TextField txtBuscar = new TextField();
    private final Button btnBuscar = new Button("Buscar");
    private final Button btnLimpiarBusqueda = new Button("Limpiar");

    public AdminLogisticaView() {
        configurarTablaEmpresas();
        configurarTablaRepartidor();
        configurarLayoutCentral();
        configurarFormulario();
        configurarEventos();
        recargarDatos(); // Carga inicial
    }

    public Parent getRoot() {
        return root;
    }

    /* =========================================================
       1. CONFIGURACIÓN DE TABLAS
       ========================================================= */

    private void configurarTablaEmpresas() {
        TableColumn<EmpresaReparto, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<EmpresaReparto, String> colNom = new TableColumn<>("Razón Social");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRazonSocial()));

        TableColumn<EmpresaReparto, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));

        TableColumn<EmpresaReparto, String> colDir = new TableColumn<>("Dirección");
        colDir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccion()));

        tablaEmpresa.getColumns().addAll(colId, colNom, colTel, colDir);
        tablaEmpresa.setItems(datosEmpresas);
        tablaEmpresa.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarTablaRepartidor() {
        tablaRepartidor = new TableView<>();
        tablaRepartidor.setPlaceholder(new Label("Selecciona una empresa para ver sus repartidores"));

        TableColumn<Repartidor, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Repartidor, String> colNom = new TableColumn<>("Nombre Repartidor");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));

        TableColumn<Repartidor, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefono()));

        // Opcional: Columna Empresa ID (aunque ya filtramos, ayuda a confirmar)
        TableColumn<Repartidor, Number> colEmpId = new TableColumn<>("Empresa ID");
        colEmpId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getEmpresaId()));

        tablaRepartidor.getColumns().addAll(colId, colNom, colTel, colEmpId);
        tablaRepartidor.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /* =========================================================
       2. LAYOUT (DISEÑO)
       ========================================================= */

    private void configurarLayoutCentral() {
        VBox panelCentral = new VBox(10);
        panelCentral.setPadding(new Insets(10));

        Label lblEmpresas = new Label("1. Empresas de Transporte");
        lblEmpresas.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label lblRepartidores = new Label("2. Repartidores de la Empresa seleccionada");
        lblRepartidores.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Hacemos que la tabla de empresas ocupe espacio, pero compartiendo con la de abajo
        VBox.setVgrow(tablaEmpresa, Priority.ALWAYS);
        VBox.setVgrow(tablaRepartidor, Priority.ALWAYS);

        panelCentral.getChildren().addAll(lblEmpresas, tablaEmpresa, new Separator(), lblRepartidores, tablaRepartidor);
        root.setCenter(panelCentral);
    }

    private void configurarFormulario() {
        VBox panelDerecho = new VBox(15);
        panelDerecho.setPadding(new Insets(10));
        panelDerecho.setPrefWidth(320); // Ancho fijo para el panel derecho
        panelDerecho.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd;");

        // --- SECCIÓN EMPRESA ---
        Label lblTitEmp = new Label("GESTIÓN EMPRESA");
        lblTitEmp.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane gridEmp = new GridPane();
        gridEmp.setHgap(10); gridEmp.setVgap(10);

        txtEmId.setEditable(false); // ID no editable
        txtEmId.setPromptText("Auto");

        gridEmp.add(new Label("ID:"), 0, 0);       gridEmp.add(txtEmId, 1, 0);
        gridEmp.add(new Label("Razón S.:"), 0, 1); gridEmp.add(txtRazonS, 1, 1);
        gridEmp.add(new Label("Teléfono:"), 0, 2); gridEmp.add(txtEmTelefono, 1, 2);
        gridEmp.add(new Label("Dirección:"), 0, 3); gridEmp.add(txtDireccion, 1, 3);

        HBox boxBotonesEmp = new HBox(5, btnNuevo, btnGuardar, btnBorrar);
        // Hacemos que los botones se ajusten
        btnNuevo.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setMaxWidth(Double.MAX_VALUE);

        // --- SECCIÓN REPARTIDOR ---
        Label lblTitRep = new Label("GESTIÓN REPARTIDOR");
        lblTitRep.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane gridRep = new GridPane();
        gridRep.setHgap(10); gridRep.setVgap(10);

        txtReId.setEditable(false); txtReId.setPromptText("Auto");
        txtEmpresaId.setEditable(false); // No se edita manual, se coge de la selección de arriba
        txtEmpresaId.setStyle("-fx-background-color: #e0e0e0;");

        gridRep.add(new Label("ID:"), 0, 0);        gridRep.add(txtReId, 1, 0);
        gridRep.add(new Label("ID Emp:"), 0, 1);    gridRep.add(txtEmpresaId, 1, 1);
        gridRep.add(new Label("Nombre:"), 0, 2);    gridRep.add(txtNombre, 1, 2);
        gridRep.add(new Label("Teléfono:"), 0, 3);  gridRep.add(txtReTelefono, 1, 3);

        HBox boxBotonesRep = new HBox(5, btnNuevoR, btnGuardarR, btnBorrarR);

        // --- BOTÓN RECARGAR GLOBAL ---
        btnRecargar.setMaxWidth(Double.MAX_VALUE);

        // AÑADIR TODO AL PANEL
        panelDerecho.getChildren().addAll(
                btnRecargar,
                new Separator(),
                lblTitEmp, gridEmp, boxBotonesEmp,
                new Separator(),
                lblTitRep, gridRep, boxBotonesRep
        );

        root.setRight(panelDerecho);
    }

    /* =========================================================
       3. LÓGICA Y EVENTOS
       ========================================================= */

    private void configurarEventos() {

        // --- EVENTO SELECCIÓN EMPRESA (MAESTRO) ---
        tablaEmpresa.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // 1. Rellenar formulario Empresa
                txtEmId.setText(String.valueOf(newVal.getId()));
                txtRazonS.setText(newVal.getNombre());
                txtEmTelefono.setText(newVal.getTelefono());
                txtDireccion.setText(newVal.getDireccion());

                // 2. Preparar formulario Repartidor (Vincular ID)
                txtEmpresaId.setText(String.valueOf(newVal.getId()));
                limpiarFormularioRepartidor(false); // Limpiamos datos del repartidor pero mantenemos ID Empresa

                // 3. Cargar Repartidores asociados
                cargarRepartidores(newVal.getId());
            } else {
                tablaRepartidor.getItems().clear();
                limpiarFormularioEmpresa();
                limpiarFormularioRepartidor(true); // Limpiar todo, incluido ID empresa
            }
        });

        // --- EVENTO SELECCIÓN REPARTIDOR (DETALLE) ---
        tablaRepartidor.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtReId.setText(String.valueOf(newVal.getId()));
                txtNombre.setText(newVal.getNombre());
                txtReTelefono.setText(newVal.getTelefono());
                // El ID de empresa ya debería estar puesto, pero por seguridad:
                txtEmpresaId.setText(String.valueOf(newVal.getEmpresaId()));
            }
        });

        // --- BOTONES EMPRESA ---
        btnNuevo.setOnAction(e -> {
            tablaEmpresa.getSelectionModel().clearSelection();
            limpiarFormularioEmpresa();
            limpiarFormularioRepartidor(true);
            tablaRepartidor.getItems().clear();
        });

        btnGuardar.setOnAction(e -> guardarEmpresa());

        btnBorrar.setOnAction(e -> borrarEmpresa());

        btnRecargar.setOnAction(e -> {
            recargarDatos();
            limpiarFormularioEmpresa();
            limpiarFormularioRepartidor(true);
        });

        // --- BOTONES REPARTIDOR ---
        btnNuevoR.setOnAction(e -> {
            tablaRepartidor.getSelectionModel().clearSelection();
            // Limpiamos campos de texto pero MANTENEMOS el ID de la empresa seleccionada
            limpiarFormularioRepartidor(false);
        });

        btnGuardarR.setOnAction(e -> guardarRepartidor());

        btnBorrarR.setOnAction(e -> borrarRepartidor());
    }

    /* =========================================================
       4. OPERACIONES CRUD Y CARGA
       ========================================================= */

    private void recargarDatos() {
        try {
            List<EmpresaReparto> lista = empresaRepartoDAO.findAll();
            datosEmpresas.setAll(lista);
        } catch (SQLException e) {
            mostrarError("Error cargando empresas", e);
        }
    }

    private void cargarRepartidores(int idEmpresa) {
        try {
            // Asumiendo que has creado este método en RepartidorDAO
            List<Repartidor> lista = repartidorDAO.findByEmpresaId(idEmpresa);
            tablaRepartidor.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            mostrarError("Error cargando repartidores", e);
        }
    }

    // --- LÓGICA EMPRESA ---

    private void guardarEmpresa() {
        if (txtRazonS.getText().isBlank()) {
            mostrarAlerta("Faltan datos", "El nombre de la empresa es obligatorio.");
            return;
        }

        try {
            EmpresaReparto emp = new EmpresaReparto();
            emp.setRazonSocial(txtRazonS.getText());
            emp.setTelefono(txtEmTelefono.getText());
            emp.setDireccion(txtDireccion.getText());

            if (txtEmId.getText().isBlank()) {
                // INSERT
                empresaRepartoDAO.insert(emp);
                mostrarInfo("Éxito", "Empresa creada.");
            } else {
                // UPDATE
                emp.setId(Integer.parseInt(txtEmId.getText()));
                empresaRepartoDAO.update(emp);
                mostrarInfo("Éxito", "Empresa actualizada.");
            }
            recargarDatos();
            limpiarFormularioEmpresa();

        } catch (SQLException e) {
            mostrarError("Error guardando empresa", e);
        }
    }

    private void borrarEmpresa() {
        if (txtEmId.getText().isBlank()) return;
        try {
            int id = Integer.parseInt(txtEmId.getText());
            // OJO: Si borras empresa, los repartidores se quedan sin empresa (set null) o se borran según tu SQL
            empresaRepartoDAO.delete(id);
            recargarDatos();
            limpiarFormularioEmpresa();
            mostrarInfo("Borrado", "Empresa eliminada.");
        } catch (SQLException e) {
            mostrarError("Error borrando", e);
        }
    }

    // --- LÓGICA REPARTIDOR ---

    private void guardarRepartidor() {
        if (txtEmpresaId.getText().isBlank()) {
            mostrarAlerta("Error", "Debes seleccionar una empresa primero.");
            return;
        }
        if (txtNombre.getText().isBlank()) {
            mostrarAlerta("Faltan datos", "El nombre del repartidor es obligatorio.");
            return;
        }

        try {
            Repartidor rep = new Repartidor();
            rep.setNombre(txtNombre.getText());
            rep.setTelefono(txtReTelefono.getText());
            rep.setEmpresaId(Integer.parseInt(txtEmpresaId.getText()));

            if (txtReId.getText().isBlank()) {
                // INSERT
                repartidorDAO.insert(rep);
            } else {
                // UPDATE
                rep.setId(Integer.parseInt(txtReId.getText()));
                repartidorDAO.update(rep);
            }

            // Recargamos solo la tabla de abajo
            cargarRepartidores(rep.getEmpresaId());
            limpiarFormularioRepartidor(false);

        } catch (SQLException e) {
            mostrarError("Error guardando repartidor", e);
        }
    }

    private void borrarRepartidor() {
        if (txtReId.getText().isBlank()) return;
        try {
            int id = Integer.parseInt(txtReId.getText());
            int idEmp = Integer.parseInt(txtEmpresaId.getText()); // Para recargar luego

            repartidorDAO.delete(id);
            cargarRepartidores(idEmp);
            limpiarFormularioRepartidor(false);

        } catch (SQLException e) {
            mostrarError("Error borrando repartidor", e);
        }
    }

    /* =========================================================
       UTILIDADES
       ========================================================= */

    private void limpiarFormularioEmpresa() {
        txtEmId.clear();
        txtRazonS.clear();
        txtDireccion.clear();
        txtEmTelefono.clear();
    }

    private void limpiarFormularioRepartidor(boolean borrarIdEmpresa) {
        txtReId.clear();
        txtNombre.clear();
        txtReTelefono.clear();
        if (borrarIdEmpresa) {
            txtEmpresaId.clear();
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