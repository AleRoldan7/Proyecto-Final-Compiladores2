package semantico.coordinadorsemantico;


import ast.expresiones.Expresion;
import ast.expresiones.LlamadaConstructorPadre;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class InferidorLlamadaConstructorPadre implements InferirTipo<LlamadaConstructorPadre> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(LlamadaConstructorPadre nodo, AnalisisContexto ctx) {

        int linea = nodo.getLinea();
        int columna = nodo.getColumna();
        Tipo vacio = Tipos.simple(linea, columna, Tipos.VOID);

        InformeTipo actual = ctx.getClaseActual();
        String padre = actual == null ? null : actual.getNombreSuperClase();

        if (padre == null) {
            ctx.reportarError(linea, columna, "'super(...)' solo puede usarse en una clase que tiene extends");
            return vacio;
        }

        if (!ctx.superPermitido(nodo)) {
            ctx.reportarError(linea, columna, "'super(...)' debe ser la primera sentencia del constructor");
            return vacio;
        }

        List<Expresion> argumentos = nodo.getArgumentos() == null ? List.of() : nodo.getArgumentos();
        List<Tipo> tipos = new ArrayList<>();

        for (Expresion argumento : argumentos) {
            Tipo tipo = inferirTipoCoordinador.inferir(argumento, ctx);
            if (tipo == null) {
                return vacio;
            }
            tipos.add(tipo);
        }

        InformeTipo info = ctx.getTablaTipos().obtener(padre);
        List<MetodoRecord> constructores = (info == null || info.getConstructores() == null)
                ? List.of() : info.getConstructores();

        if (constructores.isEmpty()) {
            if (!argumentos.isEmpty()) {
                ctx.reportarError(linea, columna,
                        "'" + padre + "' no tiene constructor con " + argumentos.size() + " parámetro(s)");
            }
            return vacio;
        }

        MetodoRecord firma = Tipos.resolverSobrecarga(constructores, tipos, ctx);

        if (firma == null) {
            ctx.reportarError(linea, columna,
                    "No existe un constructor de '" + padre + "' que reciba esos argumentos");
        }
        return vacio;
    }
}
