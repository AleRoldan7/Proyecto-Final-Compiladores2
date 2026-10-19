package test;

public class Profesor extends Persona {
    private Persona mentor;       // puede ser null
    private Persona asistente;

    public Profesor(int edad, Persona mentor, Persona asistente) {
        super(edad, "Profesor");// constructor de Persona con argumentos
        this.mentor = mentor;
        this.asistente = asistente;
    }

    public boolean tieneMentor() {
        return this.mentor != null;
    }

    @Override
    public void presentarse() {
        super.presentarse();      // primero la version de Persona (llamada estatica)
        if (this.mentor == null) {
            System.out.println("Sin mentor asignado");
        } else {
            System.out.println("Mi mentor: " + this.mentor.describir());
        }
        System.out.println("Asistente: " + this.asistente.describir());
    }
}
