package figuras

// Pentágono regular definido en Groovy: se compila en tiempo de ejecución
class Pentagono implements Figura {
    private final double lado

    Pentagono(double lado) {
        this.lado = lado
    }

    @Override
    double calcularArea() {
        return (5 * lado * lado) / (4 * Math.tan(Math.PI / 5))
    }

    @Override
    double calcularPerimetro() {
        return 5 * lado
    }
}
