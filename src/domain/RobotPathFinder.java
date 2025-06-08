import java.awt.Point; 
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class RobotPathFinder {
    private int[][] grilla;
    private List<Integer[][]> listaDeGrillas;
    private int numeroFilas;
    private int numeroColumnas;
    private List<List<Point>> caminosValidosEncontrados; 
    private List<Point> caminoActualParaMostrar; 

    private long contadorLlamadasRecursivas;
    private long contadorCaminosExplorados; 
    private long tiempoDeEjecucionMilisegundos;

    public RobotPathFinder() {
        this.caminosValidosEncontrados = new ArrayList<>();
        this.caminoActualParaMostrar = new ArrayList<>();
    }

    /**
     * carga la grilla desde un archivo de texto
     * el formato esperado es:
     * primera linea: n m (num de filas y col)
     * siguientes n lineas: m num (1 o -1) separados por espacio
     * @param rutaArchivo ruta al archivo de la grilla
     * @return true si la carga fue exitosa sino false en caso contrario
     */
    

    public List<Grilla> cargarGrillasDesdeArchivo(String rutaArchivo) {
    List<Grilla> grillas = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
        String linea;
        while ((linea = br.readLine()) != null) {
            String[] dimensiones = linea.trim().split("\\s+");

            // Si tiene exactamente dos valores y son distintos de 1 y -1, es una nueva grilla
            if (dimensiones.length == 2) {
                int filas = Integer.parseInt(dimensiones[0]);
                int columnas = Integer.parseInt(dimensiones[1]);

                if ((filas == 1 || filas == -1) || (columnas == 1 || columnas == -1)) {
                    // No sigue estructura de grilla, el programa continua buscando
                    continue;
                }

                Grilla grilla = new Grilla(filas, columnas);
                for (int i = 0; i < filas; i++) {
                    linea = br.readLine();
                    if (linea == null) throw new IOException("Faltan filas para la grilla");

                    String[] valores = linea.trim().split("\\s+");
                    if (valores.length != columnas) {
                        throw new IOException("Número incorrecto de columnas en la fila " + i);
                    }

                    for (int j = 0; j < columnas; j++) {
                        int valor = Integer.parseInt(valores[j]);
                        if (valor != 1 && valor != -1) {
                            throw new IOException("Valor inválido en la grilla: " + valor + ". Solo se permite 1 o -1.");
                        }
                        grilla.setValor(i, j, valor);
                    }
                }

                grillas.add(grilla);
            }
        }

    } catch (IOException | NumberFormatException e) {
        System.err.println("Error al cargar las grillas desde el archivo: " + rutaArchivo);
        e.printStackTrace();
    }

    return grillas;
}
    /**
     * inicia la busqueda de caminos.
     * @param usarPoda true para usar estrategias de poda - false para fuerza bruta pura
     */
    public void encontrarCaminos(boolean usarPoda) {
        if (this.grilla == null) {
            System.err.println("La grilla no ha sido cargada. Por favor, cargue una grilla primero.");
            return;
        }

        this.caminosValidosEncontrados.clear();
        this.caminoActualParaMostrar.clear();
        this.contadorLlamadasRecursivas = 0;
        this.contadorCaminosExplorados = 0;

        List<Point> caminoActual = new ArrayList<>();
        //El robot comienza en la esquina superior izquierda (0,0)
        caminoActual.add(new Point(0, 0));
        long tiempoInicio = System.currentTimeMillis();
        explorar(0, 0, this.grilla[0][0], caminoActual, usarPoda);
        this.tiempoDeEjecucionMilisegundos = System.currentTimeMillis() - tiempoInicio;
        if (!this.caminosValidosEncontrados.isEmpty()) {
            this.caminoActualParaMostrar = new ArrayList<>(this.caminosValidosEncontrados.get(0)); // guarda el primer camino valido encontrado
        }
    }


    private void explorar(int fila, int columna, int sumaActual, List<Point> caminoActual, boolean usarPoda) {
        this.contadorLlamadasRecursivas++;

        if (usarPoda) {
            // caso de poda 1: imposibilidad de alcanzar suma cero por distancia al objetivo.
            // el robot debe llegar a la esquina inferior derecha (numeroFilas-1, numeroColumnas-1)
            int pasosRestantes = (numeroFilas - 1 - fila) + (numeroColumnas - 1 - columna);
            if (Math.abs(sumaActual) > pasosRestantes) {
                // si la suma acumulada es mayor que los pasos que quedan
                // es imposible que la suma llegue a cero
                return;
            }

            // poda 2: paridad de la suma y los pasos restantes
            // para que la suma final sea cero (par), (pasosRestantes - sumaActual) tiene que ser par
            // si es impar--> no hay forma de que la suma de pasosRestantes unos y menos unos anule 'sumaActual'
            if ((pasosRestantes - sumaActual) % 2 != 0) {
                return;
            }
        }
        // ---- SIN PODAS DE ACA PARA ABAJO ----
        // caso base: el robot llego a la esquina inferior derecha
        if (fila == numeroFilas - 1 && columna == numeroColumnas - 1) {
            this.contadorCaminosExplorados++; // Se exploró un camino hasta el final
            // la suma total de cargas en el recorrido debe ser cero
            if (sumaActual == 0) {
                this.caminosValidosEncontrados.add(new ArrayList<>(caminoActual)); // aca se guarda una copia del camino valido
            }
            return;
        }

        //moverse hacia abajo
        if (fila + 1 < numeroFilas) {
            caminoActual.add(new Point(fila + 1, columna));
            explorar(fila + 1, columna, sumaActual + this.grilla[fila + 1][columna], caminoActual, usarPoda);
            caminoActual.remove(caminoActual.size() - 1); // mete backtracking
        }

        //moverse hacia la derecha
        if (columna + 1 < numeroColumnas) {
            caminoActual.add(new Point(fila, columna + 1));
            explorar(fila, columna + 1, sumaActual + this.grilla[fila][columna + 1], caminoActual, usarPoda);
            caminoActual.remove(caminoActual.size() - 1); // mete backtracking
        }
    }

 
    public List<Point> obtenerPrimerCaminoValido() {
        return caminoActualParaMostrar.isEmpty() ? Collections.emptyList() : new ArrayList<>(caminoActualParaMostrar);
    }


    public List<List<Point>> obtenerTodosLosCaminosValidos() {
        return new ArrayList<>(caminosValidosEncontrados); //devuelve una copia por las dudas
    }

    public long obtenerContadorLlamadasRecursivas() {
        return contadorLlamadasRecursivas;
    }

    public long obtenerContadorCaminosExplorados() {
        return contadorCaminosExplorados;
    }

    public long obtenerTiempoDeEjecucionMilisegundos() {
        return tiempoDeEjecucionMilisegundos;
    }

    public int obtenerNumeroFilas() {
        return numeroFilas;
    }

    public int obtenerNumeroColumnas() {
        return numeroColumnas;
    }

    public int[][] obtenerGrilla() {
        return grilla;
    }

    // metodo para imprimir un camino
    public static String caminoAString(List<Point> camino) {
        if (camino == null || camino.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < camino.size(); i++) {
            Point p = camino.get(i);
            sb.append("(").append(p.x).append(",").append(p.y).append(")");
            if (i < camino.size() - 1) {
                sb.append(" -> ");
            }
        }
        sb.append("]");
        return sb.toString();
    }


    public void setGrilla(Grilla grillaObjeto) {
    this.numeroFilas = grillaObjeto.getCantidadFilas();
    this.numeroColumnas = grillaObjeto.getCantidadColumnas();
    this.grilla = new int[numeroFilas][numeroColumnas];

    for (int i = 0; i < numeroFilas; i++) {
        for (int j = 0; j < numeroColumnas; j++) {
            this.grilla[i][j] = grillaObjeto.getValor(i, j);
        }
    }
}
    //borra visualización del camino al cambiar de grilla
    public void borrarCaminosAnteriores(Grilla grilla){
        this.caminosValidosEncontrados.clear();
        this.caminoActualParaMostrar.clear();
        this.contadorCaminosExplorados = 0;
        this.contadorLlamadasRecursivas = 0;
        this.tiempoDeEjecucionMilisegundos = 0;
    }

    public void cargarGrillaDesdeMatriz(int[][] grilla2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cargarGrillaDesdeMatriz'");
    }
  

}


