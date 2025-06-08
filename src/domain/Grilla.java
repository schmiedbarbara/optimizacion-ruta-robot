public class Grilla {
    int cantidadFilas = 0;
    int cantidadColumnas = 0;
    int[][] contenido;


    public Grilla(int cantidadFilas, int cantidadColumnas){
        this.cantidadFilas = cantidadFilas;
        this.cantidadColumnas = cantidadColumnas;
         this.contenido = new int[cantidadFilas][cantidadColumnas];
    }

    public int getCantidadFilas() {
        return cantidadFilas;
    }

    public void setCantidadFilas(int cantidadFilas) {
        this.cantidadFilas = cantidadFilas;
    }

    public int getCantidadColumnas() {
        return cantidadColumnas;
    }

    public void setCantidadColumnas(int cantidadColumnas) {
        this.cantidadColumnas = cantidadColumnas;
    }

    public void setContenido(int[][] contenido) {
        this.contenido = contenido;
    }


    public void setValor(int fila, int columna, int valor) {
        this.contenido[fila][columna] = valor;
    }
    public int getValor(int fila, int columna) {
    return this.contenido[fila][columna];
}
  

    public int[][] getContenido() {
        return contenido;
    }
    
}
