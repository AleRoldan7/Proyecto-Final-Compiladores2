package ui.button_option;

import enums.Categoria;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import tablas.FilaTabla;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TablaSimbolosView extends BorderPane {

    private static final Map<String, String> CLASE_POR_CATEGORIA = Map.of(
            "Variable", "badge-teal",
            "Parámetro", "badge-ambar",
            "Función", "badge-azul",
            "Estructura", "badge-lila",
            "Campo de estructura", "badge-lila",
            "Atributo", "badge-teal",
            "Campo", "badge-teal",
            "Clase", "badge-ambar",
            "Constructor", "badge-verde",
            "Método", "badge-azul");

    private final ObservableList<FilaTabla> datos = FXCollections.observableArrayList();
    private final FilteredList<FilaTabla> filtradas = new FilteredList<>(datos, f -> true);
    private final TableView<FilaTabla> tableView = new TableView<>();
    private final ComboBox<String> filtroCategoria = new ComboBox<>();
    private final TextField campoBusqueda = new TextField();
    private final Label contadorLabel = new Label();
    private VBox contenido;

    public TablaSimbolosView() {
        getStyleClass().add("vista-codex");
        setPadding(new Insets(16));
        construirColumnas();
        configurarFiltros();
        setCenter(crearPlaceholder("Compila un programa para ver la tabla de símbolos"));
    }

    public void actualizar(List<FilaTabla> filas) {
        if (filas == null || filas.isEmpty()) {
            setCenter(crearPlaceholder("No se registraron símbolos"));
            return;
        }

        datos.setAll(filas);
        filtroCategoria.setValue("Todo");
        campoBusqueda.clear();
        aplicarFiltros();

        if (contenido == null) {
            contenido = construirContenido();
        }
        setCenter(contenido);
    }

    // ---------------- filtros ----------------

    private void configurarFiltros() {
        filtroCategoria.setItems(FXCollections.observableArrayList(
                "Todo", "Variables", "Parámetros", "Funciones", "Estructuras",
                "Campos de estructura", "Atributos", "Campos", "Clases",
                "Constructores", "Métodos"));
        filtroCategoria.setValue("Todo");
        filtroCategoria.getStyleClass().add("combo-codex");
        filtroCategoria.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());

        campoBusqueda.setPromptText("Buscar por nombre…");
        campoBusqueda.getStyleClass().add("campo-codex");
        campoBusqueda.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());

        tableView.setItems(filtradas);
        contadorLabel.getStyleClass().add("contador-vista");
        contadorLabel.textProperty().bind(Bindings.size(filtradas).asString("%d símbolo(s)"));
    }

    private void aplicarFiltros() {
        String categoria = filtroCategoria.getValue() == null ? "Todo" : filtroCategoria.getValue();
        String texto = campoBusqueda.getText() == null ? "" : campoBusqueda.getText().trim().toLowerCase();

        filtradas.setPredicate(f ->
                coincideCategoria(f.getCategoria(), categoria)
                        && (texto.isEmpty()
                        || (f.getNombre() != null && f.getNombre().toLowerCase().contains(texto))));
    }

    private boolean coincideCategoria(Categoria c, String filtro) {
        return switch (filtro) {
            case "Variables" -> c == Categoria.VARIABLE;
            case "Parámetros" -> c == Categoria.PARAMETRO;
            case "Funciones" -> c == Categoria.FUNCION;
            case "Estructuras" -> c == Categoria.ESTRUCTURA;
            case "Campos de estructura" -> c == Categoria.CAMPO_ESTRUCTURA;
            case "Atributos" -> c == Categoria.ATRIBUTO;
            case "Campos" -> c == Categoria.CAMPO;
            case "Clases" -> c == Categoria.CLASE;
            case "Constructores" -> c == Categoria.CONSTRUCTOR;
            case "Métodos" -> c == Categoria.METODO;
            default -> true;
        };
    }

    // ---------------- columnas ----------------

    private void construirColumnas() {
        tableView.getStyleClass().add("tabla-codex");
        tableView.setFixedCellSize(-1);

        TableColumn<FilaTabla, String> colNombre =
                columnaTexto("Nombre", FilaTabla::getNombre, "texto-nombre", 150);
        TableColumn<FilaTabla, String> colCategoria = columnaInsignia("Categoría",
                f -> formatearCategoria(f.getCategoria()), 150);
        TableColumn<FilaTabla, String> colTipo = columnaTexto("Tipo",
                f -> vacioComoRaya(f.getTipo()), "texto-tipo", 110);
        TableColumn<FilaTabla, String> colDetalle = columnaDetalle();
        TableColumn<FilaTabla, String> colAmbito =
                columnaTexto("Ámbito", FilaTabla::getAmbito, "texto-suave", 130);
        TableColumn<FilaTabla, String> colLinea = columnaTexto("Línea",
                f -> String.valueOf(f.getLinea()), "texto-numero", 70);

        tableView.getColumns().addAll(colNombre, colCategoria, colTipo, colDetalle, colAmbito, colLinea);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setPlaceholder(crearPlaceholder("Sin resultados para este filtro"));
    }

    private TableColumn<FilaTabla, String> columnaTexto(String titulo, Function<FilaTabla, String> extractor,
                                                        String claseCss, double ancho) {
        TableColumn<FilaTabla, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(d.getValue())));
        col.setPrefWidth(ancho);
        col.setCellFactory(c -> new TableCell<>() {
            {
                getStyleClass().add(claseCss);
            }

            @Override
            protected void updateItem(String item, boolean vacia) {
                super.updateItem(item, vacia);
                setText(vacia || item == null ? null : item);
            }
        });
        return col;
    }

    private TableColumn<FilaTabla, String> columnaInsignia(String titulo, Function<FilaTabla, String> extractor,
                                                           double ancho) {
        TableColumn<FilaTabla, String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(extractor.apply(d.getValue())));
        col.setPrefWidth(ancho);
        col.setCellFactory(c -> new TableCell<>() {
            private final Label insignia = new Label();

            {
                insignia.getStyleClass().add("badge");
            }

            @Override
            protected void updateItem(String item, boolean vacia) {
                super.updateItem(item, vacia);
                if (vacia || item == null) {
                    setGraphic(null);
                    return;
                }
                insignia.setText(item);
                insignia.getStyleClass().removeIf(s -> s.startsWith("badge-"));
                insignia.getStyleClass().add(CLASE_POR_CATEGORIA.getOrDefault(item, "badge-gris"));
                setGraphic(insignia);
            }
        });
        return col;
    }

    private TableColumn<FilaTabla, String> columnaDetalle() {
        TableColumn<FilaTabla, String> col = new TableColumn<>("Detalle");
        col.setCellValueFactory(d -> new SimpleStringProperty(vacioComoRaya(d.getValue().getDetalle())));
        col.setPrefWidth(340);
        col.setCellFactory(column -> new TableCell<>() {
            private final Label label = new Label();

            {
                label.setWrapText(true);
                label.setStyle("-fx-text-fill: #D8D8D8;");
                label.prefWidthProperty().bind(column.widthProperty().subtract(24));
            }

            @Override
            protected void updateItem(String item, boolean vacia) {
                super.updateItem(item, vacia);
                if (vacia || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    setGraphic(label);
                }
            }
        });
        return col;
    }

    private String formatearCategoria(Categoria c) {
        return switch (c) {
            case VARIABLE -> "Variable";
            case PARAMETRO -> "Parámetro";
            case FUNCION -> "Función";
            case ESTRUCTURA -> "Estructura";
            case CAMPO_ESTRUCTURA -> "Campo de estructura";
            case ATRIBUTO -> "Atributo";
            case CAMPO -> "Campo";
            case CLASE -> "Clase";
            case CONSTRUCTOR -> "Constructor";
            case METODO -> "Método";
        };
    }

    private String vacioComoRaya(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }


    private VBox construirContenido() {
        Label titulo = new Label("Tabla de símbolos");
        titulo.getStyleClass().add("titulo-vista");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(10, titulo, contadorLabel, espacio, campoBusqueda, filtroCategoria);
        barra.setAlignment(Pos.CENTER_LEFT);

        VBox caja = new VBox(12, barra, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return caja;
    }

    private Label crearPlaceholder(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("placeholder-vista");
        return label;
    }
}