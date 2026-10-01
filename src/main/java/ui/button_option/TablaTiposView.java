package ui.button_option;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TablaTiposView extends BorderPane {

    private static final String SUPERFICIE = "#1A1A1A";
    private static final String PANEL = "#1E1E1E";
    private static final String TEXTO = "#ECECEC";
    private static final String TEXTO_SECUNDARIO = "#9A9A9A";
    private static final String BORDE = "#2A2A2A";

    private final ObservableList<InformeTipo> datosTipos = FXCollections.observableArrayList();
    private final TableView<InformeTipo> tablaTipos = new TableView<>();

    private final ObservableList<Map.Entry<String, String>> datosAtributos = FXCollections.observableArrayList();
    private final TableView<Map.Entry<String, String>> tablaAtributos = new TableView<>();

    private final ObservableList<MetodoRecord> datosMetodos = FXCollections.observableArrayList();
    private final TableView<MetodoRecord> tablaMetodos = new TableView<>();

    private final ObservableList<MetodoRecord> datosConstructores = FXCollections.observableArrayList();
    private final TableView<MetodoRecord> tablaConstructores = new TableView<>();

    public TablaTiposView() {
        setPadding(new Insets(16));
        setStyle("-fx-background-color: transparent;");
        construirTablaPrincipal();
        construirTablasDetalle();
        setCenter(crearPlaceholder("Compila un programa para ver la tabla de tipos"));
    }

    public void actualizar(List<InformeTipo> informes) {
        if (informes == null || informes.isEmpty()) {
            setCenter(crearPlaceholder("No se registraron tipos"));
            return;
        }

        datosTipos.setAll(informes);
        tablaTipos.setItems(datosTipos);

        limpiarDetalle();
        setCenter(envolver());
    }

    // ============================================================
    // TABLA PRINCIPAL
    // ============================================================

    private void construirTablaPrincipal() {
        TableColumn<InformeTipo, String> colNombre = columna(tablaTipos, "Nombre", InformeTipo::getNombre);
        TableColumn<InformeTipo, String> colCategoria = columna(tablaTipos, "Categoría",
                t -> t.getCategoria() != null ? t.getCategoria().toString() : "—");
        TableColumn<InformeTipo, String> colAtributos = columna(tablaTipos, "# Atributos",
                t -> String.valueOf(t.getOrdenAtributos().size()));
        TableColumn<InformeTipo, String> colMetodos = columna(tablaTipos, "# Métodos",
                t -> String.valueOf(contarMetodos(t)));
        TableColumn<InformeTipo, String> colConstructores = columna(tablaTipos, "# Constructores",
                t -> String.valueOf(t.getConstructores().size()));

        tablaTipos.getColumns().addAll(colNombre, colCategoria, colAtributos, colMetodos, colConstructores);
        tablaTipos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaTipos.setPrefHeight(220);
        tablaTipos.setPlaceholder(crearPlaceholder("Sin tipos registrados"));
        estilizarTabla(tablaTipos);

        tablaTipos.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null) mostrarDetalleDe(nuevo);
            else limpiarDetalle();
        });
    }

    private int contarMetodos(InformeTipo t) {
        int total = 0;
        for (var entry : t.getMetodos().entradas()) {
            total += entry.getValue().size();
        }
        return total;
    }

    // ============================================================
    // TABLAS DE DETALLE
    // ============================================================

    private void construirTablasDetalle() {
        // --- Atributos: nombre / tipo ---
        TableColumn<Map.Entry<String, String>, String> colAttrNombre =
                columna(tablaAtributos, "Nombre", Map.Entry::getKey);
        TableColumn<Map.Entry<String, String>, String> colAttrTipo =
                columna(tablaAtributos, "Tipo", Map.Entry::getValue);
        tablaAtributos.getColumns().addAll(colAttrNombre, colAttrTipo);
        tablaAtributos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaAtributos.setPrefHeight(160);
        tablaAtributos.setPlaceholder(crearPlaceholder("Sin atributos"));
        estilizarTabla(tablaAtributos);

        // --- Métodos: nombre / retorno / parámetros (una fila por sobrecarga) ---
        construirTablaFirmas(tablaMetodos, "Sin métodos");

        // --- Constructores: retorno / parámetros ---
        construirTablaFirmas(tablaConstructores, "Sin constructores");
    }

    private void construirTablaFirmas(TableView<MetodoRecord> tabla, String placeholder) {
        TableColumn<MetodoRecord, String> colNombre = columna(tabla, "Nombre", MetodoRecord::nombre);
        TableColumn<MetodoRecord, String> colRetorno = columna(tabla, "Retorno",
                m -> m.tipoRetorno() != null ? m.tipoRetorno().toString() : "void");
        TableColumn<MetodoRecord, String> colParametros = columna(tabla, "Parámetros",
                m -> formatearParametros(m));
        colParametros.setPrefWidth(280);

        tabla.getColumns().addAll(colNombre, colRetorno, colParametros);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabla.setPrefHeight(160);
        tabla.setPlaceholder(crearPlaceholder(placeholder));
        estilizarTabla(tabla);
    }

    private String formatearParametros(MetodoRecord metodo) {
        if (metodo.parametros() == null || metodo.parametros().isEmpty()) {
            return "(sin parámetros)";
        }
        StringBuilder sb = new StringBuilder();
        for (var p : metodo.parametros()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.getTipoParametro()).append(" ").append(p.getNombreParametro());
        }
        return sb.toString();
    }

    private void mostrarDetalleDe(InformeTipo t) {
        datosAtributos.setAll(
                t.getOrdenAtributos().stream()
                        .map(nombre -> Map.entry(nombre, String.valueOf(t.tipoDeAtributo(nombre))))
                        .toList()
        );
        tablaAtributos.setItems(datosAtributos);

        datosMetodos.clear();
        for (var entry : t.getMetodos().entradas()) {
            datosMetodos.addAll(entry.getValue());
        }
        tablaMetodos.setItems(datosMetodos);

        datosConstructores.setAll(t.getConstructores());
        tablaConstructores.setItems(datosConstructores);
    }

    private void limpiarDetalle() {
        datosAtributos.clear();
        datosMetodos.clear();
        datosConstructores.clear();
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private <T> TableColumn<T, String> columna(TableView<T> tablaDestino, String titulo, Function<T, String> extractor) {
        TableColumn<T, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(d.getValue())));
        return col;
    }

    private void estilizarTabla(TableView<?> tabla) {
        tabla.setStyle(
                "-fx-control-inner-background: " + PANEL + ";" +
                        "-fx-background-color: " + PANEL + ";" +
                        "-fx-table-cell-border-color: " + BORDE + ";" +
                        "-fx-text-fill: " + TEXTO + ";"
        );
        tabla.setFixedCellSize(-1);
    }

    private VBox envolver() {
        Label tituloAtributos = etiquetaSeccion("Atributos");
        Label tituloMetodos = etiquetaSeccion("Métodos");
        Label tituloConstructores = etiquetaSeccion("Constructores");

        VBox contenedor = new VBox(10,
                tablaTipos,
                tituloAtributos, tablaAtributos,
                tituloMetodos, tablaMetodos,
                tituloConstructores, tablaConstructores);
        contenedor.setPadding(new Insets(10));
        contenedor.setStyle("-fx-background-color: " + SUPERFICIE + ";");
        return contenedor;
    }

    private Label etiquetaSeccion(String texto) {
        Label label = new Label(texto);
        label.setStyle("-fx-text-fill: " + TEXTO_SECUNDARIO + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        return label;
    }

    private Label crearPlaceholder(String texto) {
        Label label = new Label(texto);
        label.setStyle(
                "-fx-text-fill: " + TEXTO_SECUNDARIO + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-family: 'Arial';"
        );
        return label;
    }
}