package figuras;

// Triángulo isósceles: con base y altura se pueden calcular los lados iguales
public class Triangulo implements Figura {
    private final double base;
    private final double altura;

    public Triangulo(double base, double altura) {
        this.base = base;
        this.altura = altura;
    }

    @Override
    public double calcularArea() {
        return base * altura / 2;                 // base × altura ÷ 2
    }

    @Override
    public double calcularPerimetro() {
        double lado = Math.sqrt(Math.pow(base / 2, 2) + altura * altura);
        return base + 2 * lado;
    }
}
