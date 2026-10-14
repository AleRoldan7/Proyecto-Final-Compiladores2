package ui.button_option;

import assembler.runtime.SimuladorRiscV;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class PanelRiscV extends BorderPane {

    private static final Path CARPETA_LIB = Path.of("lib");
    private static final long LIMITE_SEGUNDOS = 15;

    private final Path archivoAssembler;
    private final TextArea consola = new TextArea();
    private final Label estado = new Label("Listo");
    private final Button botonEjecutar = new Button("▶  Ejecutar");
    private final Button botonRars = new Button("Abrir RARS");

    public PanelRiscV(Path archivoAssembler) {
        this.archivoAssembler = archivoAssembler;
        getStyleClass().add("vista-codex");

        TextArea codigo = new TextArea(leer(archivoAssembler));
        codigo.setEditable(false);
        codigo.getStyleClass().add("area-codigo");

        consola.setEditable(false);
        consola.setPromptText("La salida del programa aparecerá aquí");
        consola.getStyleClass().add("area-consola");

        botonEjecutar.getStyleClass().add("boton-primario");
        botonRars.getStyleClass().add("boton-secundario");
        botonEjecutar.setOnAction(e -> ejecutar());
        botonRars.setOnAction(e -> abrirRars());

        estado.getStyleClass().add("contador-vista");

        Label titulo = new Label("RISC-V Assembler");
        titulo.getStyleClass().add("titulo-vista");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        HBox barra = new HBox(10, titulo, estado, espacio, botonRars, botonEjecutar);
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.setPadding(new Insets(0, 0, 10, 0));

        Label tituloSalida = new Label("Salida");
        tituloSalida.getStyleClass().add("seccion-vista");
        VBox cajaSalida = new VBox(6, tituloSalida, consola);
        VBox.setVgrow(consola, Priority.ALWAYS);

        SplitPane division = new SplitPane(codigo, cajaSalida);
        division.setOrientation(Orientation.VERTICAL);
        division.setDividerPositions(0.62);

        setPadding(new Insets(12));
        setTop(barra);
        setCenter(division);
    }

    private void ejecutar() {
        Path rars = SimuladorRiscV.localizarRars(CARPETA_LIB);
        if (rars == null) {
            consola.setText("No se encontró ningún rars*.jar en " + CARPETA_LIB.toAbsolutePath() + "\n\n"
                    + "Descarga RARS de https://github.com/TheThirdOne/rars/releases\n"
                    + "y copia el .jar dentro de la carpeta lib/ del proyecto.");
            estado.setText("RARS no disponible");
            return;
        }

        botonEjecutar.setDisable(true);
        estado.setText("Ejecutando…");
        consola.clear();

        Thread hilo = new Thread(() -> {
            try {
                SimuladorRiscV.Resultado r = SimuladorRiscV.ejecutar(rars, archivoAssembler, LIMITE_SEGUNDOS);
                Platform.runLater(() -> {
                    consola.setText(r.salida());
                    estado.setText(r.tiempo()
                            ? "Tiempo agotado"
                            : "Terminó en " + r.milisegundos() + " ms (código " + r.codigoSalida() + ")");
                    botonEjecutar.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    consola.setText("Error al ejecutar RARS: " + ex.getMessage());
                    estado.setText("Error");
                    botonEjecutar.setDisable(false);
                });
            }
        }, "ejecucion-rars");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void abrirRars() {
        Path rars = SimuladorRiscV.localizarRars(CARPETA_LIB);
        if (rars == null) {
            estado.setText("RARS no disponible");
            consola.setText("Copia el .jar de RARS dentro de lib/ para poder abrirlo.");
            return;
        }
        try {
            SimuladorRiscV.abrirInterfaz(rars);
            ClipboardContent contenido = new ClipboardContent();
            contenido.putString(archivoAssembler.toAbsolutePath().toString());
            Clipboard.getSystemClipboard().setContent(contenido);
            estado.setText("RARS abierto: ruta del .s copiada (File > Open y pegar)");
        } catch (IOException ex) {
            estado.setText("No se pudo abrir RARS");
        }
    }

    private static String leer(Path archivo) {
        try {
            return Files.readString(archivo);
        } catch (IOException e) {
            return "No se pudo leer " + archivo + ": " + e.getMessage();
        }
    }
}