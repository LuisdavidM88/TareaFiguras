package figuras;

public class Circulo implements Figura {
    private final double radio;

    public Circulo(double radio) {
        this.radio = radio;
    }

    @Override
    public double calcularArea() {
        return Math.PI * radio * radio;           // π × radio²
    }

    @Override
    public double calcularPerimetro() {
        return 2 * Math.PI * radio;               // 2 × π × radio
    }
}
