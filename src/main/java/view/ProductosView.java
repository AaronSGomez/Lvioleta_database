package view;

import dao.ProductoDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Producto;
import services.AlmacenData;

import java.sql.SQLException;
import java.util.List;

public class ProductosView {
    private final BorderPane root = new BorderPane();

    //Tabla y datos de producto
    private final TableView<Producto> tabla = new TableView<>();
    private final ObservableList<Producto> datos = FXCollections.observableArrayList();

    //Formulario (Producto)
    private final TextField txtId = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtPrecio = new TextField();

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
    private final ProductoDAO  productoDAO = new ProductoDAO();

    public ProductosView() {
        configurarTabla();
        configurarFormulario();
        configurarEventos();
        recargarDatos(); // al iniciar la vista cargamos los Productos
    }

    public Parent getRoot() {
        return root;
    }

    private void configurarTabla() {

        TableColumn<Producto, Number> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));

        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));

        TableColumn<Producto, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getPrecio()+""));



        tabla.getColumns().addAll(colId, colNombre, colPrecio);
        tabla.setItems(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        root.setCenter(tabla);
    }

    private void configurarFormulario() {
        VBox panelDerecho = new VBox(15);
        GridPane form = new GridPane();
        panelDerecho.setPadding(new Insets(10));
        panelDerecho.setPrefWidth(500); // Ancho fijo para el panel derecho
        panelDerecho.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd;");

        form.setHgap(10);form.setVgap(10);
        // ----- Cliente -----
        txtId.setPromptText("ID (entero)");
        txtNombre.setPromptText("Nombre");
        txtPrecio.setPromptText("Precio");

        form.add(new Label("ID:"), 0, 0);
        form.add(txtId, 1, 0);
        form.add(new Label("Nombre:"), 0, 1);
        form.add(txtNombre, 1, 1);
        form.add(new Label("Precio:"), 0, 2);
        form.add(txtPrecio, 1, 2);

        // Zona botones CRUD
        HBox botonesCrud = new HBox(10, btnNuevo, btnGuardar, btnBorrar, btnRecargar);
        botonesCrud.setPadding(new Insets(10, 0, 0, 0));

        // Zona de búsqueda
        HBox zonaBusqueda = new HBox(10,
                new Label("Buscar:"), txtBuscar, btnBuscar, btnLimpiarBusqueda);
        zonaBusqueda.setPadding(new Insets(10, 0, 10, 0));

        panelDerecho.getChildren().addAll(
                zonaBusqueda,
                new Separator(),
                form,
                new Separator(),
                botonesCrud,
                new Separator()
        );

        root.setRight(panelDerecho);
    }

    private void configurarEventos() {
        // Cuando seleccionamos una fila en la tabla, pasamos los datos al formulario
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                // Producto
                txtId.setText(String.valueOf(newSel.getId()));
                txtNombre.setText(newSel.getNombre());
                txtPrecio.setText(newSel.getPrecio()+"");
                txtId.setDisable(true); // al editar, de momento, no dejamos cambiar el ID
            }
        });

        btnNuevo.setOnAction(e -> limpiarFormulario());

        btnGuardar.setOnAction(e -> guardarProducto());

        btnBorrar.setOnAction(e -> borrarProductoSeleccionado());

        btnRecargar.setOnAction(e -> {
            txtBuscar.clear();
            recargarDatos();
        });

        btnBuscar.setOnAction(e -> buscarProductosEnBBDD());

        btnLimpiarBusqueda.setOnAction(e -> {
            txtBuscar.clear();
            recargarDatos();
        });
    }

    private void recargarDatos() {
        try {
            // 1) Cargar todos los Productos
            List<Producto> productos = AlmacenData.getProductos();

            // Refrescar la tabla  👈 AHORA SÍ
            datos.setAll(productos);

        } catch (SQLException e) {
            mostrarError("Error al recargar datos", e);
        }
    }

    private void buscarProductosEnBBDD(){
        String filtro = txtBuscar.getText().trim();

        if ((filtro.isEmpty())){
            recargarDatos();
            return;
        }

        try {
            List<Producto> lista = productoDAO.search(filtro);
            datos.setAll(lista);

        } catch (SQLException e){
            mostrarError("Error al buscar", e);
        }

    }

    private void limpiarFormulario() {
        txtId.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtId.setDisable(false);
        tabla.getSelectionModel().clearSelection();
    }

    private void guardarProducto() {
        // Con ID manual, vuelve a ser obligatorio
        if (txtId.getText().isBlank() ||
                txtNombre.getText().isBlank() ||
                txtPrecio.getText().isBlank()) {

            mostrarAlerta("Campos obligatorios",
                    "Debes rellenar ID, nombre y precio.");
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
        Producto p = new Producto(
                id,
                txtNombre.getText().trim(),
                Double.parseDouble(txtPrecio.getText().trim())
        );

        try {
            // Comprobamos en BD si ese ID ya existe
            Producto existente = productoDAO.findById(id);

            if (existente == null) {
                productoDAO.insert(p);
                mostrarInfo("Insertado",
                        "Cliente y detalle creados (sin transacción).");
            } else {
                mostrarAlerta("Actualizar pendiente",
                        "El cliente ya existe.\n" +
                                "Más adelante aquí haremos UPDATE desde el Service.");
            }

            AlmacenData.setProductos();
            recargarDatos();
            limpiarFormulario();

        } catch (SQLException e) {
            mostrarError("Error al guardar cliente y detalle", e);
        }
    }

    private void borrarProductoSeleccionado() {
        Producto sel = tabla.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarAlerta("Sin selección", "Selecciona un producto en la tabla.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar borrado");
        confirm.setHeaderText("¿Eliminar producto?");
        confirm.setContentText("Se borrará el producto con ID " + sel.getId());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // TODO: implementar ProductoDAO.deleteById(int id) y llamarlo aquí.

        mostrarAlerta("Borrado pendiente",
                "Aún no existe deleteById en ProductoDAO.\n" +
                        "Cuando lo implementemos, aquí se llamará al método.");


    }

    public void refresh() {
        try {
            //  Recuperar las listas de la Memoria (AlmacenData)
            List<Producto> lista = AlmacenData.getProductos();

            // Actualizar los datos de la tabla
            // Usamos 'datos.setAll' porque 'tabla' ya está vinculada a 'datos' en el constructor
            datos.setAll(lista);

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
