package semantico.analizadores;

import ast.clases.Atributo;
import ast.clases.Clase;
import ast.clases.Constructor;
import ast.clases.Metodo;
import ast.declaraciones.Parametro;
import ast.tipos.Tipo;
import enums.Categoria;
import enums.ModificadorAcceso;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Herencia;
import semantico.Tipos;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;
import tablas.InformeTipo;
import tablas.MetodoRecord;
import tablas.TablaTipos;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorClase implements AnalizadorSemantico<Clase> {

    private final AnalizadorSemanticoCoordinador coordinador;

    public void registrarFirma(Clase nodoClase, AnalisisContexto analisisContexto) {

        if (analisisContexto.getTablaTipos().existeTipo(nodoClase.getNombreClase())) {
            return;
        }

        analisisContexto.getTablaTipos().registrar(construirInforme(nodoClase, analisisContexto));
    }

    @Override
    public void analizar(Clase nodoClase, AnalisisContexto analisisContexto) {

        String nombreClase = nodoClase.getNombreClase();
        InformeTipo infoClase;

        if (analisisContexto.getTablaTipos().existeTipo(nombreClase)) {
            infoClase = analisisContexto.getTablaTipos().obtener(nombreClase);
        } else {
            infoClase = construirInforme(nodoClase, analisisContexto);
            analisisContexto.getTablaTipos().registrar(infoClase);
        }

        validarHerencia(nodoClase, analisisContexto);
        AnalizadorOverride.validar(nodoClase, infoClase, analisisContexto);

        InformeTipo claseAnterior = analisisContexto.getClaseActual();
        analisisContexto.setClaseActual(infoClase);

        analisisContexto.getTablaSimbolos().entrarAmbito("Clase " + nombreClase);
        declararAtributosHeredados(nodoClase, infoClase, analisisContexto);

        validarMetodosDuplicados(nodoClase.getMetodos(), analisisContexto);
        validarConstructoresDuplicados(nodoClase.getConstructores(), nombreClase, analisisContexto);

        nodoClase.getAtributos().forEach(a -> coordinador.analizar(a, analisisContexto));
        nodoClase.getConstructores().forEach(c -> coordinador.analizar(c, analisisContexto));
        nodoClase.getMetodos().forEach(m -> coordinador.analizar(m, analisisContexto));

        analisisContexto.getTablaSimbolos().salirAmbito();
        analisisContexto.setClaseActual(claseAnterior);
    }

    private InformeTipo construirInforme(Clase nodoClase, AnalisisContexto contexto) {

        String nombreClase = nodoClase.getNombreClase();

        InformeTipo info = new InformeTipo(nombreClase, TipoDato.OBJETO);
        info.setNombreSuperClase(nodoClase.getSuperClase());
        info.setPaquete(contexto.getPaqueteActual());

        for (Atributo atributo : nodoClase.getAtributos()) {
            info.agregarAtributo(atributo.getNombreAtributo(), atributo.getTipo(), atributo.getModificadorAcceso());
        }

        for (Metodo metodo : nodoClase.getMetodos()) {
            info.agregarMetodo(new MetodoRecord(metodo.getNombreMetodo(), metodo.getTipoRetorno(),
                    metodo.getParametros(), metodo.getModificadorAcceso()));
        }

        for (Constructor constructor : nodoClase.getConstructores()) {
            info.agregarConstructor(new MetodoRecord(nombreClase, null, constructor.getParametros(), null));
        }

        return info;
    }

    private void declararAtributosHeredados(Clase nodoClase, InformeTipo infoClase, AnalisisContexto ctx) {

        TablaTipos tipos = ctx.getTablaTipos();

        Set<String> propios = nodoClase.getAtributos().stream()
                .map(Atributo::getNombreAtributo)
                .collect(Collectors.toSet());

        for (InformeTipo ancestro : Herencia.ancestros(infoClase, tipos)) {
            for (String nombre : ancestro.getOrdenAtributos()) {

                if (propios.contains(nombre) || ctx.getTablaSimbolos().existeEnAmbitoActual(nombre)) {
                    continue;
                }

                ModificadorAcceso modificador = ancestro.modificadorDeAtributo(nombre);
                if (!AnalizadorModificadorAcceso.puedeAcceder(modificador, ancestro, infoClase, tipos)) {
                    continue;
                }

                ctx.getTablaSimbolos().declarar(nombre, Categoria.ATRIBUTO,
                        Tipos.describir(ancestro.tipoDeAtributo(nombre)),
                        "heredado de " + ancestro.getNombre(), nodoClase.getLinea());
            }
        }
    }

    private void validarMetodosDuplicados(List<Metodo> metodos, AnalisisContexto analisisContexto) {

        Set<String> firmasVistas = new HashSet<>();

        for (Metodo metodo : metodos) {

            String firma = construirFirma(metodo.getNombreMetodo(), metodo.getParametros());

            if (firmasVistas.contains(firma)) {

                analisisContexto.reportarError(metodo.getLinea(), metodo.getColumna(), "El método '" + metodo.getNombreMetodo() +
                        "' ya fue declarado con la misma firma");

            } else {
                firmasVistas.add(firma);
            }
        }
    }

    private void validarConstructoresDuplicados(List<Constructor> constructores, String nombreClase, AnalisisContexto analisisContexto) {

        Set<String> firmasVistas = new HashSet<>();

        for (Constructor constructor : constructores) {

            String firma = construirFirma(nombreClase, constructor.getParametros());

            if (firmasVistas.contains(firma)) {

                analisisContexto.reportarError(constructor.getLinea(), constructor.getColumna(), "El constructor ya fue declarado con la misma firma");

            } else {
                firmasVistas.add(firma);
            }
        }
    }

    private void validarHerencia(Clase nodoClase, AnalisisContexto analisisContexto) {

        String nombreSuperClase = nodoClase.getSuperClase();

        if (nombreSuperClase == null) {
            return;
        }

        String nombreClase = nodoClase.getNombreClase();

        if (nombreClase.equals(nombreSuperClase)) {
            analisisContexto.reportarError(nodoClase.getLinea(), nodoClase.getColumna(), "La clase '" + nombreClase +
                    "' no puede heredar de si misma");
            return;
        }

        if (!analisisContexto.getTablaTipos().existeTipo(nombreSuperClase)) {
            analisisContexto.reportarError(nodoClase.getLinea(), nodoClase.getColumna(), "La clase padre '" + nombreSuperClase +
                    "' no existe");
            return;
        }

        InformeTipo info = analisisContexto.getTablaTipos().obtener(nombreClase);

        if (info != null && Herencia.tieneCiclo(info, analisisContexto.getTablaTipos())) {
            analisisContexto.reportarError(nodoClase.getLinea(), nodoClase.getColumna(),
                    "Herencia circular: '" + nombreClase + "' termina heredando de si misma");
        }
    }

    private String construirFirma(String nombre, List<Parametro> parametros) {

        StringBuilder sb = new StringBuilder(nombre);
        sb.append("(");

        for (int i = 0; i < parametros.size(); i++) {

            if (i > 0) sb.append(",");

            Parametro p = parametros.get(i);
            Tipo tipo = p.getTipoParametro();

            sb.append(Tipos.describir(tipo));
        }

        sb.append(")");

        return sb.toString();
    }

    /*
    public void registrarFirma(Clase nodoClase, AnalisisContexto analisisContexto) {

        String nombreClase = nodoClase.getNombreClase();

        if (analisisContexto.getTablaTipos().existeTipo(nombreClase)) {
            return;
        }

        InformeTipo infoClase = new InformeTipo(nombreClase, TipoDato.OBJETO);
        infoClase.setNombreSuperClase(nodoClase.getSuperClase());

        for (var atributo : nodoClase.getAtributos()) {
            infoClase.agregarAtributo(atributo.getNombreAtributo(), atributo.getTipo());
        }

        for (Metodo metodo : nodoClase.getMetodos()) {
            infoClase.agregarMetodo(new MetodoRecord(metodo.getNombreMetodo(), metodo.getTipoRetorno(), metodo.getParametros(),
                    metodo.getModificadorAcceso()));
        }

        for (Constructor constructor : nodoClase.getConstructores()) {
            infoClase.agregarConstructor(new MetodoRecord(nombreClase, null, constructor.getParametros(), null));
        }

        analisisContexto.getTablaTipos().registrar(infoClase);
    }

    @Override
    public void analizar(Clase nodoClase, AnalisisContexto analisisContexto) {

        String nombreClase = nodoClase.getNombreClase();
        InformeTipo infoClase;

        if (analisisContexto.getTablaTipos().existeTipo(nombreClase)) {

            infoClase = analisisContexto.getTablaTipos().obtener(nombreClase);
            validarHerencia(nodoClase, analisisContexto);

        } else {

            infoClase = new InformeTipo(nombreClase, TipoDato.OBJETO);

            for (var atributo : nodoClase.getAtributos()) {
                infoClase.agregarAtributo(
                        atributo.getNombreAtributo(),
                        atributo.getTipo()
                );
            }

            for (Metodo metodo : nodoClase.getMetodos()) {
                infoClase.agregarMetodo(new MetodoRecord(
                        metodo.getNombreMetodo(),
                        metodo.getTipoRetorno(),
                        metodo.getParametros(),
                        metodo.getModificadorAcceso()
                ));
            }

            for (Constructor constructor : nodoClase.getConstructores()) {
                infoClase.agregarConstructor(new MetodoRecord(
                        nombreClase,
                        null,
                        constructor.getParametros(),
                        null
                ));
            }

            analisisContexto.getTablaTipos().registrar(infoClase);
        }

        InformeTipo claseAnterior = analisisContexto.getClaseActual();
        analisisContexto.setClaseActual(infoClase);

        analisisContexto.getTablaSimbolos().entrarAmbito("Clase " + nombreClase);
        validarMetodosDuplicados(nodoClase.getMetodos(), analisisContexto);
        validarConstructoresDuplicados(nodoClase.getConstructores(), nombreClase, analisisContexto);

        nodoClase.getAtributos().forEach(a -> coordinador.analizar(a, analisisContexto));
        nodoClase.getConstructores().forEach(c -> coordinador.analizar(c, analisisContexto));
        nodoClase.getMetodos().forEach(m -> coordinador.analizar(m, analisisContexto));

        analisisContexto.getTablaSimbolos().salirAmbito();
        analisisContexto.setClaseActual(claseAnterior);
    }


    private void validarMetodosDuplicados(List<Metodo> metodos, AnalisisContexto analisisContexto) {

        Set<String> firmasVistas = new HashSet<>();

        for (Metodo metodo : metodos) {

            String firma = construirFirma(metodo.getNombreMetodo(), metodo.getParametros());

            if (firmasVistas.contains(firma)) {

                analisisContexto.reportarError(metodo.getLinea(), metodo.getColumna(), "El método '" + metodo.getNombreMetodo() +
                        "' ya fue declarado con la misma firma");

            } else {
                firmasVistas.add(firma);
            }
        }
    }


    private void validarConstructoresDuplicados(List<Constructor> constructores, String nombreClase, AnalisisContexto analisisContexto) {

        Set<String> firmasVistas = new HashSet<>();

        for (Constructor constructor : constructores) {

            String firma = construirFirma(nombreClase, constructor.getParametros());

            if (firmasVistas.contains(firma)) {

                analisisContexto.reportarError(constructor.getLinea(), constructor.getColumna(), "El constructor ya fue declarado con la misma firma");

            } else {
                firmasVistas.add(firma);
            }
        }
    }

    private void validarHerencia(Clase nodoClase, AnalisisContexto analisisContexto) {

        String nombreSuperClase = nodoClase.getSuperClase();

        if (nombreSuperClase == null) {
            return;
        }

        String nombreClase = nodoClase.getNombreClase();

        if (nombreClase.equals(nombreSuperClase)) {
            analisisContexto.reportarError(nodoClase.getLinea(), nodoClase.getColumna(), "La clase '" + nombreClase +
                    "' no puede heredar de si misma");
            return;
        }

        if (!analisisContexto.getTablaTipos().existeTipo(nombreSuperClase)) {
            analisisContexto.reportarError(nodoClase.getLinea(), nodoClase.getColumna(), "La clase padre '" + nombreSuperClase +
                    "' no existe");
        }
    }

    private String construirFirma(String nombre, List<Parametro> parametros) {

        StringBuilder sb = new StringBuilder(nombre);
        sb.append("(");

        for (int i = 0; i < parametros.size(); i++) {

            if (i > 0) sb.append(",");

            Parametro p = parametros.get(i);
            Tipo tipo = p.getTipoParametro();

            sb.append(Tipos.describir(tipo));
        }

        sb.append(")");

        return sb.toString();
    }
     */
}