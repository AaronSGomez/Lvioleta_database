package view;

import dao.EnvioDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import model.Envio;
import services.AlmacenData;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EnvioView {

    private final BorderPane root = new BorderPane();

    // --- TABLA Y DATOS ---
    private final TableView<Envio> tabla = new TableView<>();
    private final ObservableList<Envio> datos = FXCollections.observableArrayList();

    // -- DAO
    private final EnvioDAO dao = new EnvioDAO();

    // --- FORMULARIO (Solo Estado es editable) ---
    private final TextField txtId = new TextField();
    private final TextField txtPedidoId = new TextField();
    private final TextField txtEmpresa = new TextField();
    private final TextField txtRepartidor = new TextField();
    private final TextField txtTracking = new TextField();

    // El único campo editable:
    private final ComboBox<String> comboEstado = new ComboBox<>();

    // --- BOTONES ---
    private final Button btnGuardarEstado = new Button("Actualizar Estado");
    private final Button btnRecargar = new Button("Recargar Lista");

    // --- BUSQUEDA ---
    // (Opcional, podrías implementarla igual que en productos)
    private final Button btnLimpiarSeleccion = new Button("Limpiar Selección");


    public EnvioView() {
        configurarTabla();
        configurarFormulario();
        configurarEventos();
        recargarDatos();
    }

    public Parent getRoot() {
        return root;
    }

    /* =========================================================
       1. CONFIGURACIÓN DE TABLA
       ========================================================= */
    private void configurarTabla() {

        // 1. ID Envío
        TableColumn<Envio, Number> colId = new TableColumn<>("ID Envío");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colId.setPrefWidth(60);

        // 2. ID Pedido (Referencia)
        TableColumn<Envio, Number> colPed = new TableColumn<>("ID Pedido");
        colPed.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getPedidoId()));
        colPed.setPrefWidth(70);

        // 3. Empresa (Dato Auxiliar)
        TableColumn<Envio, String> colEmp = new TableColumn<>("Empresa");
        colEmp.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreEmpresa()));

        // 4. Repartidor (Dato Auxiliar)
        TableColumn<Envio, String> colRep = new TableColumn<>("Repartidor");
        colRep.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreRepartidor()));

        // 5. Teléfono (Dato Auxiliar - Útil para llamar si hay problemas)
        TableColumn<Envio, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefonoRepartidor()));

        // 6. Tracking
        TableColumn<Envio, String> colTrack = new TableColumn<>("Tracking ID");
        colTrack.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroSeguimiento()));

        // 7. Estado (Lo más importante aquí)
        TableColumn<Envio, String> colEstado = new TableColumn<>("Estado Actual");
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado()));
        // Un toque visual: Negrita para el estado
        colEstado.setStyle("-fx-font-weight: bold;");

        // Datos cliente
        TableColumn<Envio, String> colCli = new TableColumn<>("Cliente Destino");
        colCli.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombreCliente()));
        colCli.setPrefWidth(120);

        TableColumn<Envio, String> colDir = new TableColumn<>("Dirección Entrega");
        colDir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDireccionCliente()));
        colDir.setPrefWidth(180); // Un poco más ancha para que quepa la calle

        // 8. Fecha Salida
        TableColumn<Envio, String> colFecha = new TableColumn<>("Fecha Salida");
        colFecha.setCellValueFactory(c -> {
            Date fecha = c.getValue().getFechaSalida();
            String texto = (fecha != null) ? new SimpleDateFormat("dd/MM/yyyy").format(fecha) : "";
            return new SimpleStringProperty(texto);
        });

        tabla.getColumns().addAll(colId, colPed, colCli, colDir, colEmp, colRep, colTrack, colEstado, colFecha);
        tabla.setItems(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Ajustar columnas

        root.setCenter(tabla);
    }

    /* =========================================================
       2. CONFIGURACIÓN DEL FORMULARIO (Panel Inferior)
       ========================================================= */
    private void configurarFormulario() {
        GridPane form = new GridPane();
        form.setPadding(new Insets(15));
        form.setHgap(15);
        form.setVgap(10);
        form.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 1px 0 0 0;");

        // Configuración de campos (MODO SOLO LECTURA para la mayoría)
        txtId.setEditable(false); txtId.setDisable(true);
        txtPedidoId.setEditable(false); txtPedidoId.setDisable(true);
        txtEmpresa.setEditable(false); txtEmpresa.setDisable(true);
        txtRepartidor.setEditable(false); txtRepartidor.setDisable(true);
        txtTracking.setEditable(false); txtTracking.setDisable(true); // El tracking no se cambia aquí, se cambia en el Dialog

        // Configuración del Combo (EDITABLE)
        comboEstado.setItems(FXCollections.observableArrayList(
                "EN_PREPARACION", "EN_REPARTO", "ENTREGADO", "INCIDENCIA", "DEVUELTO"
        ));
        comboEstado.setPromptText("Selecciona Estado");

        // Layout del Formulario
        // Fila 0: Identificación
        form.add(new Label("ID Envío:"), 0, 0); form.add(txtId, 1, 0);
        form.add(new Label("ID Pedido:"), 2, 0); form.add(txtPedidoId, 3, 0);

        // Fila 1: Logística Info
        form.add(new Label("Empresa:"), 0, 1); form.add(txtEmpresa, 1, 1);
        form.add(new Label("Repartidor:"), 2, 1); form.add(txtRepartidor, 3, 1);

        // Fila 2: Tracking y Acción
        form.add(new Label("Tracking:"), 0, 2); form.add(txtTracking, 1, 2);

        form.add(new Label("CAMBIAR ESTADO:"), 2, 2);
        form.add(comboEstado, 3, 2);

        // Botones
        HBox panelBotones = new HBox(10, btnGuardarEstado, btnRecargar, btnLimpiarSeleccion);
        panelBotones.setPadding(new Insets(10, 0, 0, 0));

        BorderPane bottomPane = new BorderPane();
        bottomPane.setCenter(form);
        bottomPane.setBottom(panelBotones);
        // Margen inferior
        BorderPane.setMargin(panelBotones, new Insets(10));

        root.setBottom(bottomPane);
    }

    /* =========================================================
       3. EVENTOS
       ========================================================= */
    private void configurarEventos() {
        // Selección en tabla -> Rellenar formulario
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtId.setText(String.valueOf(newSel.getId()));
                txtPedidoId.setText(String.valueOf(newSel.getPedidoId()));
                txtEmpresa.setText(newSel.getNombreEmpresa());
                txtRepartidor.setText(newSel.getNombreRepartidor());
                txtTracking.setText(newSel.getNumeroSeguimiento());

                // Ponemos el estado actual en el combo
                comboEstado.setValue(newSel.getEstado());

                // Habilitar botón de guardar
                btnGuardarEstado.setDisable(false);
            } else {
                limpiarFormulario();
            }
        });

        // Botón Actualizar Estado
        btnGuardarEstado.setOnAction(e -> guardarCambioEstado());

        // Botón Recargar
        btnRecargar.setOnAction(e -> recargarDatos());

        // Botón Limpiar
        btnLimpiarSeleccion.setOnAction(e -> {
            tabla.getSelectionModel().clearSelection();
            limpiarFormulario();
        });
    }

    /* =========================================================
       4. LÓGICA CRUD
       ========================================================= */

    private void recargarDatos() {
        try {
            // Usamos el DAO directamente.
            // Podrías poner esto en AlmacenData si quisieras caché,
            // pero para estados de envíos es mejor ver datos frescos.
            List<Envio> lista = AlmacenData.getEnvios();
            datos.setAll(lista);

        } catch (SQLException e) {
            mostrarError("Error cargando envíos", e);
        }
    }

    private void guardarCambioEstado() {
        Envio envioSeleccionado = tabla.getSelectionModel().getSelectedItem();

        if (envioSeleccionado == null) {
            mostrarAlerta("Selección", "Selecciona un envío de la lista.");
            return;
        }

        String nuevoEstado = comboEstado.getValue();
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            mostrarAlerta("Estado vacío", "Selecciona un estado válido.");
            return;
        }

        try {
            // 1. Modificamos el objeto en memoria
            envioSeleccionado.setEstado(nuevoEstado);

            // 2. Llamamos al DAO para actualizar
            // Nota: envioDAO.update actualiza todos los campos, pero como
            // el objeto tiene los datos cargados de la BD, no perdemos nada.
            dao.update(envioSeleccionado);

            mostrarInfo("Éxito", "Estado actualizado a: " + nuevoEstado);
            AlmacenData.setEnvios();
            tabla.refresh();

        } catch (SQLException e) {
            mostrarError("Error actualizando estado", e);
        }
    }

    private void limpiarFormulario() {
        txtId.clear();
        txtPedidoId.clear();
        txtEmpresa.clear();
        txtRepartidor.clear();
        txtTracking.clear();
        comboEstado.setValue(null);
        btnGuardarEstado.setDisable(true); // Desactivar si no hay selección
    }

    /* =========================================================
       UTILIDADES
       ========================================================= */
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
        alert.setTitle("Información");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}