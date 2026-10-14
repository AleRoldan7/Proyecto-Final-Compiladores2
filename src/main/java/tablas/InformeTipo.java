package tablas;

import ast.tipos.Tipo;
import enums.ModificadorAcceso;
import enums.TipoDato;
import estructuras.TablaHash;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class InformeTipo {

    private final String nombre;
    private String nombreSuperClase;
    private final TipoDato categoria;
    private final TablaHash<Tipo> atributos = new TablaHash<>();
    private final TablaHash<List<MetodoRecord>> metodos = new TablaHash<>();
    private final List<MetodoRecord> constructores = new ArrayList<>();
    private final List<String> ordenAtributos = new ArrayList<>();

    private String paquete = "";
    private final Map<String, ModificadorAcceso> modificadoresAtributos = new HashMap<>();


    public InformeTipo(String nombre) {
        this(nombre, TipoDato.DESCONOCIDO);
    }


    public InformeTipo(String nombre, TipoDato categoria) {
        this.nombre = nombre;
        this.categoria = categoria;
    }

    /*
    public void agregarAtributo(String nombre, Tipo tipo) {
        atributos.put(nombre, tipo);
        ordenAtributos.add(nombre);
    }
    /*
    public void agregarMetodo(MetodoRecord metodo) {
        metodos.put(metodo.nombre(), metodo);
    }
     */

    public void agregarAtributo(String nombre, Tipo tipo) {
        atributos.put(nombre, tipo);
        if (!ordenAtributos.contains(nombre)) {
            ordenAtributos.add(nombre);
        }
    }

    public void agregarAtributo(String nombre, Tipo tipo, ModificadorAcceso modificador) {
        agregarAtributo(nombre, tipo);
        modificadoresAtributos.put(nombre, modificador == null ? ModificadorAcceso.DEFAULT : modificador);
    }

    public ModificadorAcceso modificadorDeAtributo(String nombre) {
        return modificadoresAtributos.getOrDefault(nombre, ModificadorAcceso.DEFAULT);
    }

    public void agregarMetodo(MetodoRecord metodo) {

        List<MetodoRecord> firmas = metodos.get(metodo.nombre());

        if (firmas == null) {
            firmas = new ArrayList<>();
            metodos.put(metodo.nombre(), firmas);
        }

        firmas.add(metodo);
    }

    public void agregarConstructor(MetodoRecord constructor) {
        constructores.add(constructor);
    }

    public boolean tieneAtributo(String nombre) {
        return atributos.containsKey(nombre);
    }

    public Tipo tipoDeAtributo(String nombre) {
        return atributos.get(nombre);
    }

    public boolean tieneMetodo(String nombre) {
        return metodos.containsKey(nombre);
    }

    public List<MetodoRecord> firmasDe(String nombre) {
        List<MetodoRecord> firmas = metodos.get(nombre);
        return firmas != null ? firmas : Collections.emptyList();
    }

    public List<String> getOrdenAtributos() {
        return Collections.unmodifiableList(ordenAtributos);
    }
}
