package c3d;

import assembler.generador.GeneradorRiscV;
import ast.NodoAST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GeneradorC3DCompleto {

    public record ArchivoFuente(String nombre, NodoAST raiz, boolean principal) {}

    private static ContextoC3D ultimoContexto;

    public GeneradorC3DCompleto() {
    }

    public static ContextoC3D getUltimoContexto() {
        return ultimoContexto;
    }

    public static List<Cuarteta> generar(List<ArchivoFuente> archivos) {

        ContextoC3D ctx = new ContextoC3D();
        ultimoContexto = ctx;

        // ==========================================
        // 1. Registrar disposición de las clases
        // ==========================================
        for (ArchivoFuente archivo : archivos) {

            if (archivo.raiz() instanceof ast.Programa programa
                    && programa.getClases() != null) {

                for (ast.clases.Clase clase : programa.getClases()) {
                    clase.registrarDisposicion(ctx);
                }
            }
        }

        // Resolver layouts de estructuras
        ctx.resolverEstructuras();

        // ==========================================
        // 2. Inicio del programa: fp = 0, sp = 0, goto func_main
        //    (debe ser lo PRIMERO que se emite, así se salta todo el código de las funciones)
        // ==========================================
        boolean hayPrincipal = archivos.stream().anyMatch(ArchivoFuente::principal);
        if (hayPrincipal) {
            ctx.iniciarPrograma();
        }

        // ==========================================
        // 3. Archivos auxiliares (.y y .z)
        // ==========================================
        for (ArchivoFuente archivo : archivos) {

            if (!archivo.principal()) {
                ctx.emitirComentario("==== " + archivo.nombre() + " ====");
                archivo.raiz().generarC3D(ctx);
            }
        }

        // ==========================================
        // 4. Archivo principal (.pig): su main se genera como una función más
        // ==========================================
        for (ArchivoFuente archivo : archivos) {

            if (archivo.principal()) {
                ctx.emitirComentario("==== " + archivo.nombre() + " ====");
                archivo.raiz().generarC3D(ctx);
            }
        }

        // ==========================================
        // 5. Fin: ret_dispatch y fin_programa
        // ==========================================
        if (hayPrincipal) {
            ctx.finalizarPrograma();
        }

        return ctx.getCuartetas();
    }

    public static String comoTexto(List<Cuarteta> cuartetas) {

        StringBuilder sb = new StringBuilder();

        for (Cuarteta q : cuartetas) {

            boolean sinSangria =
                    "label".equals(q.getOperador())
                            || "comment".equals(q.getOperador());

            sb.append(sinSangria ? "" : "    ")
                    .append(q)
                    .append("\n");
        }

        return sb.toString();
    }

    public static void exportar(
            Path carpeta,
            String nombreBase,
            List<Cuarteta> cuartetas
    ) throws IOException {

        Files.createDirectories(carpeta);

        Files.writeString(
                carpeta.resolve(nombreBase + ".c3d"),
                comoTexto(cuartetas)
        );

        Files.writeString(
                carpeta.resolve(nombreBase + ".c"),
                GenerarCodigoC.traducir(cuartetas)
        );

        Files.writeString(carpeta.resolve(nombreBase + ".s"),
                GeneradorRiscV.generar(cuartetas, ultimoContexto.getTipoTemporales()));
    }


}