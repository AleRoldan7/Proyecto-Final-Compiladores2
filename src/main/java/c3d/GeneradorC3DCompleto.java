package c3d;

import assembler.generador.GeneradorRiscV;
import ast.NodoAST;
import ast.Programa;
import ast.declaraciones.DeclaracionFuncion;

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

        for (ArchivoFuente archivo : archivos) {

            if (archivo.raiz() instanceof Programa programa
                    && programa.getClases() != null) {

                for (ast.clases.Clase clase : programa.getClases()) {
                    clase.registrarDisposicion(ctx);
                }
            }
        }

        ctx.resolverEstructuras();

        for (ArchivoFuente archivo : archivos) {
            if (archivo.raiz() instanceof Programa programa && programa.getFunciones() != null) {
                for (DeclaracionFuncion f : programa.getFunciones()) {
                    int aridad = f.getParametros() == null ? 0 : f.getParametros().size();
                    ctx.registrarRetorno(f.getNombreFuncion(), aridad,
                            ConversorTipos.aTipoDato(f.getTipoRetorno()));
                }
            }
        }


        boolean hayPrincipal = archivos.stream().anyMatch(ArchivoFuente::principal);
        if (hayPrincipal) {
            ctx.iniciarPrograma();
        }

        for (ArchivoFuente archivo : archivos) {

            if (!archivo.principal()) {
                ctx.emitirComentario("==== " + archivo.nombre() + " ====");
                archivo.raiz().generarC3D(ctx);
            }
        }


        for (ArchivoFuente archivo : archivos) {

            if (archivo.principal()) {
                ctx.emitirComentario("==== " + archivo.nombre() + " ====");
                archivo.raiz().generarC3D(ctx);
            }
        }

        if (hayPrincipal) {
            ctx.finalizarPrograma();
        }

        return ctx.getCuartetas();
    }

    public static String comoTexto(List<Cuarteta> cuartetas) {

        StringBuilder sb = new StringBuilder();

        for (Cuarteta q : cuartetas) {

            boolean sinSangria ="label".equals(q.getOperador()) || "comment".equals(q.getOperador());

            sb.append(sinSangria ? "" : "    ").append(q).append("\n");
        }

        return sb.toString();
    }

    public static void exportar(Path carpeta, String nombreBase, List<Cuarteta> cuartetas) throws IOException {

        Files.createDirectories(carpeta);

        Files.writeString(carpeta.resolve(nombreBase + ".c3d"), comoTexto(cuartetas));

        Files.writeString(carpeta.resolve(nombreBase + ".s"), GeneradorRiscV.generar(cuartetas, ultimoContexto.getTipoTemporales()));
    }


}