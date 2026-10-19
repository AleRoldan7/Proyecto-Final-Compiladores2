package test;


public class Persona {
    protected int edad;          // protected: visible para Profesor
    private String rol;          // private: solo dentro de Persona

    // Constructor sin argumentos (novus Persona())
    public Persona() {
        this.edad = 0;
        this.rol = "Sin rol";
    }

    // Constructor con argumentos (novus Persona(12, "Profesor"))
    public Persona(int edad, String rol) {
        this.edad = edad;
        this.rol = rol;
    }

    public String getRol() {
        return this.rol;
    }

    public String describir() {
        return "Persona(" + this.edad + ", " + this.rol + ")";
    }

    public void presentarse() {
        System.out.println("Soy una persona de " + this.edad + " anios, rol: " + this.rol);
    }
}
