package figuras;

public class Rectangulo implements Figura {
    private final double base;
    private final double altura;

    public Rectangulo(double base, double altura) {
        this.base = base;
        this.altura = altura;
    }

    @Override
    public double calcularArea() {
        return base * altura;                     // base × altura
    }

    @Override
    public double calcularPerimetro() {
        return 2 * (base + altura);
    }
}
