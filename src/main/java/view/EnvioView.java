package view;

import dao.EnvioDAO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import model.Envio;
import model.Producto;
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
       2. CONFIGURACIÓN DEL FORMULARIO
       ========================================================= */
    private void configurarFormulario() {

        // Definición de columnas para los formularios
        ColumnConstraints colLabels = new ColumnConstraints();
        colLabels.setMinWidth(80); // Un poco más ancho para "Repartidor:"
        ColumnConstraints colInputs = new ColumnConstraints();
        colInputs.setHgrow(Priority.ALWAYS);

        // --- Lado Izquierdo: Datos Identificativos y Estado (35%) ---
        GridPane gridDatos = new GridPane();
        gridDatos.setHgap(10);
        gridDatos.setVgap(10);
        gridDatos.getColumnConstraints().addAll(colLabels, colInputs);

        // Configuración de campos (SOLO LECTURA)
        txtId.setEditable(false); txtId.setDisable(true);
        txtPedidoId.setEditable(false); txtPedidoId.setDisable(true);

        // Configuración de campos (EDITABLE - El Estado)
        comboEstado.setItems(FXCollections.observableArrayList(
                "EN_PREPARACION", "EN_REPARTO", "ENTREGADO", "INCIDENCIA", "DEVUELTO"
        ));
        comboEstado.setPromptText("Selecciona Estado");
        comboEstado.setMaxWidth(Double.MAX_VALUE); // Para que ocupe todo el ancho
        txtId.setMaxWidth(Double.MAX_VALUE);
        txtPedidoId.setMaxWidth(Double.MAX_VALUE);

        // Añadir al grid izquierdo
        gridDatos.add(new Label("ID Envío:"), 0, 0);
        gridDatos.add(txtId, 1, 0);
        gridDatos.add(new Label("ID Pedido:"), 0, 1);
        gridDatos.add(txtPedidoId, 1, 1);
        gridDatos.add(new Label("ESTADO:"), 0, 2);
        gridDatos.add(comboEstado, 1, 2);

        // Contenedor Izquierdo con Título
        VBox ladoIzquierdo = new VBox(10);
        Label lblTituloDatos = new Label("Datos del Envío");
        lblTituloDatos.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        ladoIzquierdo.getChildren().addAll(lblTituloDatos, gridDatos);

        // --- Lado Derecho: Detalles Logísticos (65%) ---
        GridPane gridLogistica = new GridPane();
        gridLogistica.setHgap(10);
        gridLogistica.setVgap(10);
        gridLogistica.getColumnConstraints().addAll(colLabels, colInputs);

        // Configuración de campos (SOLO LECTURA)
        txtEmpresa.setEditable(false); txtEmpresa.setDisable(true);
        txtRepartidor.setEditable(false); txtRepartidor.setDisable(true);
        txtTracking.setEditable(false); txtTracking.setDisable(true); // Se edita en Dialog, aquí solo se ve

        txtEmpresa.setMaxWidth(Double.MAX_VALUE);
        txtRepartidor.setMaxWidth(Double.MAX_VALUE);
        txtTracking.setMaxWidth(Double.MAX_VALUE);

        // Añadir al grid derecho
        gridLogistica.add(new Label("Empresa:"), 0, 0);
        gridLogistica.add(txtEmpresa, 1, 0);
        gridLogistica.add(new Label("Repartidor:"), 0, 1);
        gridLogistica.add(txtRepartidor, 1, 1);
        gridLogistica.add(new Label("Tracking:"), 0, 2);
        gridLogistica.add(txtTracking, 1, 2);

        // Contenedor Derecho con Título
        VBox ladoDerecho = new VBox(10);
        Label lblTituloLogistica = new Label("Detalles Logísticos");
        lblTituloLogistica.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        ladoDerecho.getChildren().addAll(lblTituloLogistica, gridLogistica);

        //CONTENEDOR PADRE
        GridPane formEnvio = new GridPane();
        formEnvio.setHgap(40); // Espacio entre bloques

        ColumnConstraints colMasterLeft = new ColumnConstraints();
        colMasterLeft.setPercentWidth(35);
        ColumnConstraints colMasterRight = new ColumnConstraints();
        colMasterRight.setPercentWidth(65);

        formEnvio.getColumnConstraints().addAll(colMasterLeft, colMasterRight);
        formEnvio.add(ladoIzquierdo, 0, 0);
        formEnvio.add(ladoDerecho, 1, 0);

        // ZONA BOTONES
        HBox botonesCrud = new HBox(15, btnGuardarEstado, btnRecargar, btnLimpiarSeleccion);
        botonesCrud.setAlignment(Pos.CENTER);
        botonesCrud.setPadding(new Insets(15, 0, 0, 0));
        // Estilo unificado de botones (Ancho fijo)
        botonesCrud.getChildren().forEach(node -> ((Button)node).setPrefWidth(140)); // Un poco más anchos por los textos

        // LAYOUT FINAL - MONTAJE VENTANA
        VBox layoutGlobal = new VBox(10);
        layoutGlobal.setPadding(new Insets(20, 50, 20, 50)); // Márgenes laterales amplios

        layoutGlobal.getChildren().addAll(
                formEnvio,
                new Separator(),
                botonesCrud
        );

        // Centrar en pantalla si es muy ancho
        layoutGlobal.setMaxWidth(1400);
        BorderPane.setAlignment(layoutGlobal, Pos.CENTER);

        root.setBottom(layoutGlobal);
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

    public void refresh() {
        try {
            //  Recuperar las listas de la Memoria (AlmacenData)
            List<Envio> lista = AlmacenData.getEnvios();

            // Actualizar los datos de la tabla
            // Usamos 'datos.setAll' porque 'tabla' ya está vinculada a 'datos' en el constructor
            datos.setAll(lista);

            // 4. Forzar repintado visual
            tabla.refresh();

        } catch (SQLException e) {
            e.printStackTrace();
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
            dao.updateEstado(envioSeleccionado);

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