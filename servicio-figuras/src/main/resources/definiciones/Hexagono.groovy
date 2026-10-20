package figuras

// Hexágono regular entregado por el servicio externo
class Hexagono implements Figura {
    private final double lado

    Hexagono(double lado) {
        this.lado = lado
    }

    @Override
    double calcularArea() {
        return (3 * Math.sqrt(3) / 2) * lado * lado
    }

    @Override
    double calcularPerimetro() {
        return 6 * lado
    }
}
