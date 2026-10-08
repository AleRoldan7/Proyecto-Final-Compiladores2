package assembler.generador;

public class ASMContexto {

    private final StringBuilder datos = new StringBuilder();
    private final StringBuilder texto = new StringBuilder();
    private int contadorEtiquetas = 0;

    public void instruccion(String instruccion) {
        texto.append("    ").append(instruccion).append("\n");
    }

    public void etiqueta(String etiqueta) {
        texto.append(etiqueta).append(":\n");
    }

    public void comentario(String comentario) {
        texto.append("    # ").append(comentario.replace('\n', ' ')).append('\n');
    }

    public void crudo(String bloque) {
        texto.append(bloque);
    }

    public void dato(String linea) {
        datos.append(linea).append('\n');
    }

    public String nuevaEtiqueta(String prefijo) {
        return prefijo + "_" + contadorEtiquetas++;
    }

    public String construir() {
        return ".data\n" + datos + "\n.text\n.globl main\n" + texto;
    }
}
