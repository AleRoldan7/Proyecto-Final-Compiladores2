package assembler.generador;

import enums.RegistroAssembler;
import exceptiones.RegistroException;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
public class AdminsitradorRegistro {

    private final Set<RegistroAssembler> ocupados = new HashSet<>();


    public RegistroAssembler obtenerRegistro() {
        for (RegistroAssembler registro : RegistroAssembler.values()) {

            if (registro.name().startsWith("T") && !ocupados.contains(registro)) {
                ocupados.add(registro);
                return registro;
            }
        }
        throw new RegistroException("No hay ningun registro disponible");
    }

    public RegistroAssembler obtenerRegistro(RegistroAssembler registro) {

        if (!ocupados.contains(registro)) {
            ocupados.add(registro);
            return registro;
        }
        return obtenerRegistro();
    }

    public void liberarRegistro(RegistroAssembler registro) {
        ocupados.remove(registro);
    }

    public void liberarTodo() {
        ocupados.clear();
    }

}
