package ui.button_option;

import c3d.Cuarteta;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TablaCuartetasView extends BorderPane {

    private static final Set<String> CONTROL = Set.of("label", "goto", "if_true", "if_false", "halt");
    private static final Set<String> LLAMADAS = Set.of("call", "param", "param_decl", "return");
    private static final Set<String> MEMORIA = Set.of("index_get", "index_set", "new");
    private static final Set<String> ENTRADA_SALIDA = Set.of("print", "read");

    private final TableView<Cuarteta> tabla = new TableView<>();
    private final Map<Cuarteta, Integer> numeros = new IdentityHashMap<>();

    public TablaCuartetasView(List<Cuarteta> cuartetas) {
        getStyleClass().add("vista-codex");
        setPadding(new Insets(12));

        for (int i = 0; i < cuartetas.size(); i++) {
            numeros.put(cuartetas.get(i), i);
        }

        FilteredList<Cuarteta> filtradas =
                new FilteredList<>(FXCollections.observableArrayList(cuartetas), q -> true);

        construirColumnas();
        tabla.setItems(filtradas);
        tabla.getStyleClass().add("tabla-codex");
        tabla.setFixedCellSize(34);
        tabla.setPlaceholder(placeholder("Sin cuartetas para mostrar"));
        tabla.setRowFactory(t -> new TableRow<>() {
            @Override
            protected void updateItem(Cuarteta q, boolean vacia) {
                super.updateItem(q, vacia);
                getStyleClass().remove("fila-etiqueta");
                if (!vacia && q != null && "label".equals(q.getOperador())) {
                    getStyleClass().add("fila-etiqueta");
                }
            }
        });

        TextField busqueda = new TextField();
        busqueda.setPromptText("Buscar en el C3D…");
        busqueda.getStyleClass().add("campo-codex");
        busqueda.textProperty().addListener((obs, anterior, nuevo) -> {
            String texto = nuevo == null ? "" : nuevo.trim().toLowerCase();
            filtradas.setPredicate(q -> texto.isEmpty()
                    || String.valueOf(q).toLowerCase().contains(texto)
                    || (q.getOperador() != null && q.getOperador().toLowerCase().contains(texto)));
        });

        Label titulo = new Label("Cuartetas");
        titulo.getStyleClass().add("titulo-vista");

        Label contador = new Label();
        contador.getStyleClass().add("contador-vista");
        contador.textProperty().bind(Bindings.size(filtradas).asString("%d cuarteta(s)"));

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(10, titulo, contador, espacio, busqueda);
        barra.setAlignment(Pos.CENTER_LEFT);

        VBox caja = new VBox(12, barra, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        setCenter(caja);
    }

    private void construirColumnas() {
        TableColumn<Cuarteta, String> colNumero =
                columnaTexto("#", q -> String.valueOf(numeros.get(q)), "texto-numero", 56);
        TableColumn<Cuarteta, String> colOperador = columnaOperador();
        TableColumn<Cuarteta, String> colArg1 =
                columnaTexto("Arg 1", q -> valor(q.getArg1()), "texto-mono", 130);
        TableColumn<Cuarteta, String> colArg2 =
                columnaTexto("Arg 2", q -> valor(q.getArg2()), "texto-mono", 130);
        TableColumn<Cuarteta, String> colResultado =
                columnaTexto("Resultado", q -> valor(q.getResultado()), "texto-mono", 130);
        TableColumn<Cuarteta, String> colTipo = columnaTexto("Tipo",
                q -> q.getTipoDeclarado() == null ? "—" : q.getTipoDeclarado().name(), "texto-tipo", 90);
        TableColumn<Cuarteta, String> colC3D =
                columnaTexto("C3D", q -> String.valueOf(q), "texto-mono", 280);

        tabla.getColumns().addAll(colNumero, colOperador, colArg1, colArg2, colResultado, colTipo, colC3D);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private TableColumn<Cuarteta, String> columnaTexto(String titulo, Function<Cuarteta, String> extractor,
                                                       String claseCss, double ancho) {
        TableColumn<Cuarteta, String> col = new TableColumn<>(titulo);
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

    private TableColumn<Cuarteta, String> columnaOperador() {
        TableColumn<Cuarteta, String> col = new TableColumn<>("Operador");
        col.setCellValueFactory(d -> new SimpleStringProperty(valor(d.getValue().getOperador())));
        col.setPrefWidth(120);
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
                insignia.getStyleClass().add(claseOperador(item));
                setGraphic(insignia);
            }
        });
        return col;
    }

    private static String claseOperador(String op) {
        if (CONTROL.contains(op)) return "badge-ambar";
        if (LLAMADAS.contains(op)) return "badge-azul";
        if (MEMORIA.contains(op)) return "badge-lila";
        if (ENTRADA_SALIDA.contains(op)) return "badge-verde";
        if ("comment".equals(op) || "—".equals(op)) return "badge-gris";
        return "badge-teal"; // aritméticos, lógicos y asignaciones
    }

    private static String valor(String texto) {
        return texto == null || texto.isBlank() ? "—" : texto;
    }

    private Label placeholder(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("placeholder-vista");
        return label;
    }
}