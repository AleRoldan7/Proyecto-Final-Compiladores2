package assembler.runtime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class SimuladorRiscV {

    public record Resultado(int codigoSalida, String salida, boolean tiempo, long milisegundos) {}

    private static final int MAXIMO_CARACTERES = 200_000_000;

    public static boolean disponible(Path path) {
        return path != null && Files.isRegularFile(path);
    }

    public static Resultado ejecutar(Path path, Path archivoRiscV, long limiteSegundos) throws IOException, InterruptedException {

        long inicio = System.currentTimeMillis();

        ProcessBuilder constructor = new ProcessBuilder(rutaJava(), "-jar", path.toAbsolutePath().toString(),
                "nc", archivoRiscV.toAbsolutePath().toString());constructor.redirectErrorStream(true);

        Process proceso = constructor.start();
        proceso.getOutputStream().close();

        CompletableFuture<String> lectura = CompletableFuture.supplyAsync(() -> leerSalida(proceso));

        boolean termino = proceso.waitFor(limiteSegundos, TimeUnit.SECONDS);
        if (!termino) {
            proceso.destroyForcibly();
            proceso.waitFor();
            String parcial = lectura.join();
            long ms = System.currentTimeMillis() - inicio;
            return new Resultado(-1, parcial + "Tiempo agotado: posible ciclo infinito", true, ms);
        }

        String salida = lectura.join();
        long ms = System.currentTimeMillis() - inicio;
        return new Resultado(proceso.exitValue(), salida, false, ms);
    }


    public static void abrirInterfaz(Path rutaRars) throws IOException {
        new ProcessBuilder(rutaJava(), "-jar", rutaRars.toAbsolutePath().toString()).start();
    }

    private static String rutaJava() {
        return Path.of(System.getProperty("java.home"), "bin", "java").toString();
    }

    private static String leerSalida(Process proceso) {
        StringBuilder sb = new StringBuilder();
        boolean truncado = false;
        try (InputStreamReader lector = new InputStreamReader(proceso.getInputStream(), StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int leidos;
            while ((leidos = lector.read(buffer)) != -1) {
                if (sb.length() < MAXIMO_CARACTERES) {
                    sb.append(buffer, 0, leidos);
                } else {
                    truncado = true;
                }
            }
        } catch (IOException e) {
            sb.append("\n[Error leyendo la salida: ").append(e.getMessage()).append("]");
        }
        if (truncado) sb.append("\n[... salida truncada ...]");
        return sb.toString();
    }

    public static Path localizarRars(Path carpetaLib) {
        if (carpetaLib == null || !Files.isDirectory(carpetaLib)) return null;
        try (var archivos = Files.list(carpetaLib)) {
            return archivos
                    .filter(p -> {
                        String nombre = p.getFileName().toString().toLowerCase();
                        return nombre.startsWith("rars") && nombre.endsWith(".jar");
                    })
                    .sorted()
                    .reduce((primero, segundo) -> segundo)
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }
}
