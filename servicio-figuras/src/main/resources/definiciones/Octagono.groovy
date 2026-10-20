package figuras

// Octágono regular entregado por el servicio externo
class Octagono implements Figura {
    private final double lado

    Octagono(double lado) {
        this.lado = lado
    }

    @Override
    double calcularArea() {
        return 2 * (1 + Math.sqrt(2)) * lado * lado
    }

    @Override
    double calcularPerimetro() {
        return 8 * lado
    }
}
