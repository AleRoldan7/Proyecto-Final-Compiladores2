package enums;

public enum Registro {

    FP("s1"),
    SP("s2"),
    HP("s3"),
    RETADDR("s4"),
    BASE_PILA("s5"),
    BASE_HEAP("s6"),
    TEXTO_TEMPORAL("s7");

    private final String nombreRegistro;

    Registro(String nombreRegistro) {
        this.nombreRegistro = nombreRegistro;
    }

    public String getNombreRegistro() {
        return nombreRegistro;
    }

    @Override
    public String toString() {
        return nombreRegistro;
    }
}
