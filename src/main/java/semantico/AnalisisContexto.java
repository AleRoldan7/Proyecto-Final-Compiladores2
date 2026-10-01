package semantico;

import ast.tipos.Tipo;
import enums.TipoArchivo;
import enums.TipoDato;
import enums.TipoErrorSemantico;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tablas.InformeTipo;
import tablas.TablaSimbolos;
import tablas.TablaTipos;
import semantico.dialecto.Dialecto;
import semantico.dialecto.Dialectos;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class AnalisisContexto {

    private  TablaSimbolos tablaSimbolos;
    private  TablaTipos tablaTipos;
    private int sizeCiclo = 0;
    private int sizeSwitch = 0;
    private String archivoActual = "desconocido";
    private final List<ErrorSemantico> errores = new ArrayList<>();
    private InformeTipo claseActual;
    private Tipo tipoRetorno;
    private Dialecto dialecto = Dialectos.ZETARIANO;

    public AnalisisContexto(TablaSimbolos tablaSimbolos, TablaTipos tablaTipos) {
        this.tablaSimbolos = tablaSimbolos;
        this.tablaTipos = tablaTipos;
    }

    public static AnalisisContexto paraArchivo(String nombreArchivo, TipoArchivo tipo) {

        AnalisisContexto ctx = new AnalisisContexto();
        ctx.setArchivoActual(nombreArchivo);
        ctx.setDialecto(Dialectos.de(tipo));
        ctx.setTablaSimbolos(new TablaSimbolos());
        ctx.setTablaTipos(new TablaTipos());

        return ctx;
    }

    public void importarDe(AnalisisContexto otro) {

        if (otro == null || otro.getTablaTipos() == null) {
            return;
        }

        this.tablaTipos.importarDe(otro.getTablaTipos());

        if (otro.getTablaSimbolos() != null) {
            this.tablaSimbolos.importarDe(otro.getTablaSimbolos());
        }
    }

    public void entrarCiclo() {
        sizeCiclo++;
    }

    public void salirCiclo() {
        sizeCiclo--;
    }

    public boolean dentroDeCiclo() {
        return sizeCiclo > 0;
    }

    public void entrarSwitch() {
        sizeSwitch++;
    }

    public void salirSwitch() {
        sizeSwitch--;
    }

    public boolean dentroDeSwitch() {
        return sizeSwitch > 0;
    }

    public boolean exigir(boolean condicion, int linea, int columna, String queEstaMal) {

        if (!condicion) {
            reportarError(linea, columna, "En " + dialecto.nombre() + " " + queEstaMal);
        }

        return condicion;
    }

    public void reportarError(int linea, int columna, String mensaje) {
        errores.add(ErrorSemantico.error(archivoActual, linea, columna, mensaje));
    }

    public void reportarAdvertencia(int linea, String mensaje) {
        errores.add(ErrorSemantico.advertencia(archivoActual, linea, 0, mensaje));
    }

    public boolean tieneErrores() {
        return errores.stream().anyMatch(e -> e.tipoError() == TipoErrorSemantico.ERROR);
    }

    public static TipoDato clasificar(String nombre) {
        if (nombre == null) return null;

        for (TipoArchivo archivo : TipoArchivo.values()) {
            Dialecto dialecto = Dialectos.de(archivo);
            for (TipoDato tipoDato : TipoDato.values()) {
                if (nombre.equals(dialecto.nombrarTipo(tipoDato))) {
                    return tipoDato;
                }
            }
        }
        return null;
    }
}