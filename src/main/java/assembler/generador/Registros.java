package assembler.generador;

import enums.Registro;
import exceptiones.RegistroException;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Registros {


    public static boolean esRegistroC3D(String nombreRegistro) {
        return "fp".equals(nombreRegistro) || "sp".equals(nombreRegistro) || "retaddr".equals(nombreRegistro);
    }

    public static Registro deC3D(String nombreRegistro) {

        return switch(nombreRegistro) {
            case "fp" -> Registro.FP;
            case "sp" -> Registro.SP;
            case "retaddr" -> Registro.RETADDR;

            default -> throw new RegistroException("' " + nombreRegistro + "' no es un registro de C3D");
        };
    }
}
