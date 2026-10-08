package ui.button_option;

import c3d.Cuarteta;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class TablaCuartetasView {

    private TableView<Cuarteta> crearTablaCuartetas(List<Cuarteta> cuartetas) {

        TableView<Cuarteta> tabla = new TableView<>();

        TableColumn<Cuarteta, Number> columnaNumero =
                new TableColumn<>("#");

        columnaNumero.setCellValueFactory(
                celda -> new javafx.beans.property.SimpleIntegerProperty(
                        cuartetas.indexOf(celda.getValue())
                )
        );

        TableColumn<Cuarteta, String> columnaOperador =
                new TableColumn<>("Operador");

        columnaOperador.setCellValueFactory(
                celda -> new javafx.beans.property.SimpleStringProperty(
                        celda.getValue().getOperador()
                )
        );

        TableColumn<Cuarteta, String> columnaArg1 =
                new TableColumn<>("Arg 1");

        columnaArg1.setCellValueFactory(
                celda -> new javafx.beans.property.SimpleStringProperty(
                        celda.getValue().getArg1()
                )
        );

        TableColumn<Cuarteta, String> columnaArg2 =
                new TableColumn<>("Arg 2");

        columnaArg2.setCellValueFactory(
                celda -> new javafx.beans.property.SimpleStringProperty(
                        celda.getValue().getArg2()
                )
        );

        TableColumn<Cuarteta, String> columnaResultado =
                new TableColumn<>("Resultado");

        columnaResultado.setCellValueFactory(
                celda -> new javafx.beans.property.SimpleStringProperty(
                        celda.getValue().getResultado()
                )
        );

        TableColumn<Cuarteta, String> columnaTipo =
                new TableColumn<>("Tipo");

        columnaTipo.setCellValueFactory(
                celda -> new javafx.beans.property.SimpleStringProperty(
                        celda.getValue().getTipoDeclarado() == null
                                ? ""
                                : celda.getValue().getTipoDeclarado().name()
                )
        );

        TableColumn<Cuarteta, String> columnaC3D =
                new TableColumn<>("C3D");

        columnaC3D.setCellValueFactory(
                celda -> new javafx.beans.property.SimpleStringProperty(
                        celda.getValue().toString()
                )
        );

        tabla.getColumns().addAll(
                columnaNumero,
                columnaOperador,
                columnaArg1,
                columnaArg2,
                columnaResultado,
                columnaTipo,
                columnaC3D
        );

        tabla.getItems().addAll(cuartetas);

        tabla.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        );

        tabla.setStyle(
                "-fx-background-color: #141414;" +
                        "-fx-control-inner-background: #141414;" +
                        "-fx-text-fill: #ECECEC;"
        );

        return tabla;
    }
}
