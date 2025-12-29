package view;

import dao.EmpresaRepartoDAO;
import dao.EnvioDAO;
import dao.RepartidorDAO;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import model.EmpresaReparto;
import model.Envio;
import model.Repartidor;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class EnvioDialog extends Dialog<Boolean> {

    // DAOS
    private final EnvioDAO envioDAO = new EnvioDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();
    private final EmpresaRepartoDAO empresaDAO = new EmpresaRepartoDAO(); // <--- NUEVO

    // DATOS
    private final int pedidoId;
    private Envio envioExistente = null;

    // CONTROLES
    private final ComboBox<EmpresaReparto> comboEmpresa = new ComboBox<>(); // <--- NUEVO
    private final ComboBox<Repartidor> comboRepartidor = new ComboBox<>();
    private final DatePicker dateSalida = new DatePicker(LocalDate.now());
    private final TextField txtTracking = new TextField();
    private final ComboBox<String> comboEstado = new ComboBox<>();

    public EnvioDialog(int pedidoId) {
        this.pedidoId = pedidoId;

        setTitle("Gestión de Envío - Pedido #" + pedidoId);
        setHeaderText("Selecciona Empresa, Repartidor y Seguimiento.");

        ButtonType btnGuardarType = new ButtonType("Guardar Envío", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        // --- LAYOUT ---
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        comboEstado.setItems(FXCollections.observableArrayList("EN_PREPARACION", "EN_REPARTO", "ENTREGADO", "INCIDENCIA"));
        comboEstado.setValue("EN_PREPARACION");
        txtTracking.setPromptText("Ej: ES-123456789");

        // Configuración inicial de combos
        comboRepartidor.setDisable(true); // Desactivado hasta que elijas empresa

        // Fila 0: Empresa
        grid.add(new Label("Empresa:"), 0, 0);
        grid.add(comboEmpresa, 1, 0);

        // Fila 1: Repartidor (Ahora depende de Empresa)
        grid.add(new Label("Repartidor:"), 0, 1);
        grid.add(comboRepartidor, 1, 1);

        // Fila 2: Fecha
        grid.add(new Label("Fecha Salida:"), 0, 2);
        grid.add(dateSalida, 1, 2);

        // Fila 3: Tracking
        grid.add(new Label("Nº Seguimiento:"), 0, 3);
        grid.add(txtTracking, 1, 3);

        // Fila 4: Estado
        grid.add(new Label("Estado:"), 0, 4);
        grid.add(comboEstado, 1, 4);

        getDialogPane().setContent(grid);

        // --- LÓGICA DE CARGA ---
        configurarCombos();      // Configura los StringConverters y Listeners
        cargarEmpresas();        // Llena el primer combo
        cargarDatosSiExiste();   // Si es edición, autorrellena todo

        // Resultado
        setResultConverter(dialogButton -> {
            if (dialogButton == btnGuardarType) return guardarLogica();
            return null;
        });
    }

    private void configuringCombos() {
        // 1. Converter para EMPRESA (Ver nombre bonito)
        comboEmpresa.setConverter(new StringConverter<EmpresaReparto>() {
            @Override
            public String toString(EmpresaReparto e) { return (e == null) ? null : e.getRazonSocial(); }
            @Override
            public EmpresaReparto fromString(String s) { return null; }
        });

        // 2. Converter para REPARTIDOR
        comboRepartidor.setConverter(new StringConverter<Repartidor>() {
            @Override
            public String toString(Repartidor r) { return (r == null) ? null : r.getNombre(); }
            @Override
            public Repartidor fromString(String s) { return null; }
        });

        // 3. EVENTO: Cuando cambias de Empresa -> Cargar Repartidores
        comboEmpresa.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarRepartidoresDeEmpresa(newVal.getId());
                comboRepartidor.setDisable(false);
            } else {
                comboRepartidor.getItems().clear();
                comboRepartidor.setDisable(true);
            }
        });
    }

    private void cargarEmpresas() {
        try {
            List<EmpresaReparto> lista = empresaDAO.findAll();
            comboEmpresa.setItems(FXCollections.observableArrayList(lista));
        } catch (SQLException e) {
            mostrarError("Error cargando empresas", e);
        }
    }

    private void cargarRepartidoresDeEmpresa(int idEmpresa) {
        try {
            // Limpiamos selección previa para evitar errores
            comboRepartidor.getSelectionModel().clearSelection();

            // Usamos el método que creamos en RepartidorDAO (findByEmpresaId)
            List<Repartidor> lista = repartidorDAO.findByEmpresaId(idEmpresa);
            comboRepartidor.setItems(FXCollections.observableArrayList(lista));

        } catch (SQLException e) {
            mostrarError("Error cargando repartidores", e);
        }
    }

    private void cargarDatosSiExiste() {
        try {
            envioExistente = envioDAO.findByPedidoId(pedidoId);

            if (envioExistente != null) {
                // Rellenar campos simples
                dateSalida.setValue(convertToLocalDate(envioExistente.getFechaSalida()));
                txtTracking.setText(envioExistente.getNumeroSeguimiento());
                comboEstado.setValue(envioExistente.getEstado());

                // --- LOGICA INVERSA PARA COMBOS ---
                // Tenemos el ID del Repartidor. Necesitamos saber su Empresa para pre-seleccionar los combos.

                // 1. Buscamos al repartidor completo en BD para saber su empresa_id
                Repartidor repartidorActual = repartidorDAO.findById(envioExistente.getRepartidorId());

                if (repartidorActual != null) {
                    // 2. Seleccionamos la Empresa en el primer combo
                    // (Esto disparará el listener que carga los repartidores automáticamente)
                    for (EmpresaReparto emp : comboEmpresa.getItems()) {
                        if (emp.getId() == repartidorActual.getEmpresaId()) {
                            comboEmpresa.setValue(emp);
                            break;
                        }
                    }

                    // 3. Seleccionamos el Repartidor en el segundo combo
                    // Nota: Como el listener de empresa es asíncrono/rápido, a veces hay que asegurar
                    // que la lista esté cargada, pero en JavaFX "single thread" suele funcionar directo.
                    // Si fallara, habría que seleccionar el repartidor dentro del listener de empresa.
                    comboRepartidor.setValue(repartidorActual);

                    // IMPORTANTE: setValue usa equals(). Asegúrate de que Repartidor tenga equals() por ID
                    // O busca manual en la lista del combo:
                    for (Repartidor r : comboRepartidor.getItems()) {
                        if (r.getId() == repartidorActual.getId()) {
                            comboRepartidor.setValue(r);
                            break;
                        }
                    }
                }
            }

        } catch (SQLException e) {
            mostrarError("Error cargando datos existentes", e);
        }
    }

    private boolean guardarLogica() {
        if (comboEmpresa.getValue() == null || comboRepartidor.getValue() == null) {
            mostrarError("Datos incompletos", new Exception("Debes seleccionar Empresa y Repartidor."));
            return false;
        }

        try {
            Repartidor rep = comboRepartidor.getValue();
            Date fecha = java.sql.Date.valueOf(dateSalida.getValue());
            String track = txtTracking.getText();
            String estado = comboEstado.getValue();

            if (envioExistente == null) {
                // INSERT
                Envio nuevo = new Envio(0, pedidoId, rep.getId(), fecha);
                nuevo.setNumeroSeguimiento(track);
                nuevo.setEstado(estado);
                envioDAO.insert(nuevo);
            } else {
                // UPDATE
                envioExistente.setRepartidorId(rep.getId());
                envioExistente.setFechaSalida(fecha);
                envioExistente.setNumeroSeguimiento(track);
                envioExistente.setEstado(estado);
                envioDAO.update(envioExistente);
            }
            return true;

        } catch (Exception e) {
            mostrarError("Error guardando", e);
            return false;
        }
    }

    private void configurarCombos() {
        // He movido la lógica de configuración aquí para tener el constructor limpio
        configuringCombos();
    }

    private LocalDate convertToLocalDate(Date dateToConvert) {
        if (dateToConvert == null) return LocalDate.now();
        if (dateToConvert instanceof java.sql.Date) {
            return ((java.sql.Date) dateToConvert).toLocalDate();
        }
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void mostrarError(String header, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}