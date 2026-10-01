package enums;

public enum RegistroAssembler {

    ZERO("zero"),

    RA("ra"),
    SP("sp"),
    FP("s0"),

    A0("a0"),
    A1("a1"),
    A2("a2"),
    A3("a3"),
    A4("a4"),
    A5("a5"),
    A6("a6"),
    A7("a7"),

    T0("t0"),
    T1("t1"),
    T2("t2"),
    T3("t3"),
    T4("t4"),
    T5("t5"),
    T6("t6");

    private final String nombre;

    RegistroAssembler(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
