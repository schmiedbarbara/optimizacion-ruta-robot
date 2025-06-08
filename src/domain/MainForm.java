import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;


public class MainForm extends JFrame {
	private RobotPathFinder pathFinder; 
    private JTabbedPane pestañasGrillas;
    private List<Grilla> grillasCargadas = new ArrayList<>();

    private JButton botonCargarGrilla;
    private JButton botonBuscarSinPoda;
    private JButton botonBuscarConPoda;

    private JPanel panelGrillaVisual; 
    private JTable tablaResultados;
    private DefaultTableModel modeloTablaResultados;
    private JLabel etiquetaEstado;
    private JLabel etiquetaDimensionGrilla;
    private JTextArea areaCaminoEncontrado; //  muestra las coordenadas del camino

    private final int TAMANO_CELDA_GRILLA = 40; 

    public MainForm() {
        this.pathFinder = new RobotPathFinder(); 
        inicializarUI();
    }

    private void inicializarUI() {
        setTitle("TP3 - Optimizador de Ruta de Robot - Programación III");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10)); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null); 

        // --- Panel Superior ---> Controles--- 
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botonCargarGrilla = new JButton("Cargar Grilla desde Archivo");
        botonBuscarSinPoda = new JButton("Buscar Sin Poda");
        botonBuscarConPoda = new JButton("Buscar Con Poda");
        etiquetaDimensionGrilla = new JLabel("Grilla no cargada");

        botonBuscarSinPoda.setEnabled(false); //hasta que se cargue una grilla
        botonBuscarConPoda.setEnabled(false);

        panelControles.add(botonCargarGrilla);
        panelControles.add(botonBuscarSinPoda);
        panelControles.add(botonBuscarConPoda);
        panelControles.add(etiquetaDimensionGrilla);
        add(panelControles, BorderLayout.NORTH);
        
        

        
        // --- Panel Central: Grilla y Camino ---
        JPanel panelCentral = new JPanel();
        pestañasGrillas = new JTabbedPane();
        panelCentral.setLayout(new BorderLayout(10, 10));
        panelGrillaVisual = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarGrilla(g); 
            }
        };
        
        pestañasGrillas.addChangeListener(e -> {
        int index = pestañasGrillas.getSelectedIndex();
        if (index >= 0 && index < grillasCargadas.size()) {
            Grilla grillaSeleccionada = grillasCargadas.get(index);
            pathFinder.setGrilla(grillaSeleccionada);
             //actualiza vista cuando se visualiza otra grilla
            pathFinder.borrarCaminosAnteriores(grillaSeleccionada); //
            panelGrillaVisual.revalidate();
            panelGrillaVisual.repaint();
    
    }
});
        panelGrillaVisual.setPreferredSize(new Dimension(300, 500)); 
        panelGrillaVisual.setBorder(BorderFactory.createEtchedBorder());
        panelCentral.add(pestañasGrillas, BorderLayout.CENTER);
        //Mostrar camino encontrado
        areaCaminoEncontrado = new JTextArea(5, 30);
        areaCaminoEncontrado.setEditable(false);
        areaCaminoEncontrado.setLineWrap(true);
        areaCaminoEncontrado.setWrapStyleWord(true);
        JScrollPane scrollCamino = new JScrollPane(areaCaminoEncontrado);
        scrollCamino.setBorder(BorderFactory.createTitledBorder("Camino Encontrado: (Suma 0)"));
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.X_AXIS));
        panelCentral.add(panelGrillaVisual);
        panelCentral.add(scrollCamino);

        add(panelCentral, BorderLayout.CENTER);
        


        JPanel panelInferior = new JPanel(new BorderLayout(10,10));
        //Resultados del Camino Generado
        String[] columnasTabla = {"Descripción", "Sin Poda", "Con Poda"};
        modeloTablaResultados = new DefaultTableModel(columnasTabla, 0);
        tablaResultados = new JTable(modeloTablaResultados);
        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Resultados de Ejecución"));
        panelInferior.add(pestañasGrillas);
        panelInferior.add(scrollTabla, BorderLayout.CENTER);
        etiquetaEstado = new JLabel("Listo. Cargue una grilla para comenzar.");
        etiquetaEstado.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        panelInferior.setLayout(new BoxLayout(panelInferior, BoxLayout.X_AXIS));
        //panelInferior.add(etiquetaEstado, BorderLayout.SOUTH);
        add(panelInferior, BorderLayout.SOUTH);

        // --- botones ---
        botonCargarGrilla.addActionListener(e -> cargarGrilla());
        botonBuscarSinPoda.addActionListener(e -> ejecutarBusqueda(false));
        botonBuscarConPoda.addActionListener(e -> ejecutarBusqueda(true));
        setVisible(true);
    }
    

    private void cargarGrilla() {
        JFileChooser selectorArchivo = new JFileChooser();
        selectorArchivo.setDialogTitle("Seleccione el archivo de la grilla");
        selectorArchivo.setFileFilter(new FileNameExtensionFilter("Archivos de Texto (*.txt)", "txt"));
        int resultado = selectorArchivo.showOpenDialog(this);

        if (resultado == JFileChooser.APPROVE_OPTION) {
            File archivoSeleccionado = selectorArchivo.getSelectedFile();
            List<Grilla> grillasCargadas = pathFinder.cargarGrillasDesdeArchivo(archivoSeleccionado.getAbsolutePath());
            this.grillasCargadas = grillasCargadas;

            //dibujo de todas las grillas
            if (grillasCargadas!= null && !grillasCargadas.isEmpty()) {
                pestañasGrillas.removeAll();
                for (int i = 0; i < grillasCargadas.size(); i++) {
                    Grilla g = grillasCargadas.get(i);
                    pathFinder.setGrilla(g);
                    pathFinder.setGrilla(g);
                    panelGrillaVisual.repaint();
                    JPanel panelGrilla = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g2) {
                    super.paintComponent(g2);
                    dibujarGrilla(g2, g); 
                }
        };
        panelGrilla.setPreferredSize(new Dimension(
                g.cantidadColumnas * TAMANO_CELDA_GRILLA,
                g.cantidadFilas * TAMANO_CELDA_GRILLA
        ));
        panelGrilla.setBorder(BorderFactory.createEtchedBorder());

        JScrollPane scrollGrilla = new JScrollPane(panelGrilla);
        pestañasGrillas.addTab("Grilla " + (i + 1), scrollGrilla);
    }

                etiquetaEstado.setText("Grilla cargada exitosamente: " + archivoSeleccionado.getName());
                etiquetaDimensionGrilla.setText("Grilla: " + pathFinder.obtenerNumeroFilas() + "x" + pathFinder.obtenerNumeroColumnas());
                botonBuscarSinPoda.setEnabled(true);
                botonBuscarConPoda.setEnabled(true);
                areaCaminoEncontrado.setText(""); // limpia area de camino
                int panelWidth = pathFinder.obtenerNumeroColumnas() * TAMANO_CELDA_GRILLA;
                int panelHeight = pathFinder.obtenerNumeroFilas() * TAMANO_CELDA_GRILLA;
                panelGrillaVisual.setPreferredSize(new Dimension(panelWidth, panelHeight));
                panelGrillaVisual.revalidate();
                panelGrillaVisual.repaint(); 
                limpiarTablaResultados();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al cargar o procesar el archivo de la grilla.",
                        "Error de Carga", JOptionPane.ERROR_MESSAGE);
                etiquetaEstado.setText("Error al cargar la grilla.");
                botonBuscarSinPoda.setEnabled(false);
                botonBuscarConPoda.setEnabled(false);
            }
        }
    }

    private void ejecutarBusqueda(boolean conPoda) {
    int indiceSeleccionado = pestañasGrillas.getSelectedIndex();
    if (indiceSeleccionado < 0 || grillasCargadas.isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "Por favor, cargue una grilla primero.",
                "Grilla no cargada", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Carga la grilla seleccionada en el pathFinder para poder mostrarla
    Grilla grillaSeleccionada = grillasCargadas.get(indiceSeleccionado);
    pathFinder.setGrilla(grillaSeleccionada); 
    String tipoBusqueda = conPoda ? "Con Poda" : "Sin Poda";
    etiquetaEstado.setText("Ejecutando búsqueda " + tipoBusqueda + "...");
    areaCaminoEncontrado.setText(""); // limpia la busqueda de antes

    SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
        List<Point> primerCamino = null;
        long tiempo = 0;
        long llamadas = 0;
        long explorados = 0;
        int totalValidos = 0;

        @Override
        protected Void doInBackground() throws Exception {
            pathFinder.encontrarCaminos(conPoda);
            primerCamino = pathFinder.obtenerPrimerCaminoValido();
            tiempo = pathFinder.obtenerTiempoDeEjecucionMilisegundos();
            llamadas = pathFinder.obtenerContadorLlamadasRecursivas();
            explorados = pathFinder.obtenerContadorCaminosExplorados();
            totalValidos = pathFinder.obtenerTodosLosCaminosValidos().size();
            return null;
        }

        @Override
        protected void done() {
            try {
                get(); // para capturar excepciones
                etiquetaEstado.setText("Búsqueda " + tipoBusqueda + " completada en " + tiempo + " ms.");
                if (primerCamino != null && !primerCamino.isEmpty()) {
                    areaCaminoEncontrado.setText(RobotPathFinder.caminoAString(primerCamino));
                } else {
                    areaCaminoEncontrado.setText("No se encontró un camino válido con suma cero.");
                }
                actualizarTablaResultados(
                        tipoBusqueda,
                        pathFinder.obtenerNumeroFilas() + "x" + pathFinder.obtenerNumeroColumnas(),
                        tiempo,
                        llamadas,
                        explorados,
                        totalValidos
                );
                panelGrillaVisual.repaint(); // volver a dibujar y resaltar el camino
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(MainForm.this,
                        "Ocurrió un error durante la búsqueda: " + e.getMessage(),
                        "Error de Búsqueda", JOptionPane.ERROR_MESSAGE);
                etiquetaEstado.setText("Error durante la búsqueda.");
            }
        }
    };
    worker.execute();
}


    private void dibujarGrilla(Graphics g, Grilla grilla) {
    int[][] contenido = grilla.getContenido();
    int filas = grilla.cantidadFilas;
    int columnas = grilla.cantidadColumnas;

    for (int i = 0; i < filas; i++) {
        for (int j = 0; j < columnas; j++) {
            int valor = contenido[i][j];
            g.setColor(Color.WHITE);
            g.fillRect(j * TAMANO_CELDA_GRILLA, i * TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA);

            g.setColor(Color.BLACK);
            g.drawRect(j * TAMANO_CELDA_GRILLA, i * TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA);

            String strValor = String.valueOf(valor);
            g.setColor(valor > 0 ? Color.GREEN.darker() : (valor < 0 ? Color.RED.darker() : Color.GRAY));
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(strValor);
            int textHeight = fm.getAscent();
            g.drawString(strValor,
                    j * TAMANO_CELDA_GRILLA + (TAMANO_CELDA_GRILLA - textWidth) / 2,
                    i * TAMANO_CELDA_GRILLA + (TAMANO_CELDA_GRILLA + textHeight) / 2 - fm.getDescent() / 2);
        }
    }
}

    private void dibujarGrilla(Graphics g) {
        int[][] grilla = pathFinder.obtenerGrilla();
        if (grilla == null) {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, panelGrillaVisual.getWidth(), panelGrillaVisual.getHeight());
            g.setColor(Color.BLACK);
            g.drawString("Cargue una grilla para visualizarla.", 25, 25);
            return;
        }
        int numFilas = pathFinder.obtenerNumeroFilas();
        int numColumnas = pathFinder.obtenerNumeroColumnas();

        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < numColumnas; j++) {
                //Fondo de la celda
                g.setColor(Color.WHITE);
                g.fillRect(j * TAMANO_CELDA_GRILLA, i * TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA);

                //Borde de la celda
                g.setColor(Color.BLACK);
                g.drawRect(j * TAMANO_CELDA_GRILLA, i * TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA, TAMANO_CELDA_GRILLA);

                //Dibuja valor -1 o 1
                String valor = String.valueOf(grilla[i][j]);
                if (grilla[i][j] > 0) {
                    g.setColor(Color.GREEN.darker());
                } else {
                    g.setColor(Color.RED.darker());
                }
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(valor);
                int textHeight = fm.getAscent();
                g.drawString(valor,
                        j * TAMANO_CELDA_GRILLA + (TAMANO_CELDA_GRILLA - textWidth) / 2,
                        i * TAMANO_CELDA_GRILLA + (TAMANO_CELDA_GRILLA + textHeight) / 2 - fm.getDescent() /2 ); 
            }
        }

        //Dibujo de camino encontrado
        List<Point> camino = pathFinder.obtenerPrimerCaminoValido();
        if (camino != null && !camino.isEmpty()) {
            g.setColor(Color.BLUE); 
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(3)); 
            for (Point p : camino) {
                int centerX = p.y * TAMANO_CELDA_GRILLA + TAMANO_CELDA_GRILLA / 2;
                int centerY = p.x * TAMANO_CELDA_GRILLA + TAMANO_CELDA_GRILLA / 2;

                 g.setColor(new Color(0, 0, 255, 100)); 
                 g.fillRect(p.y * TAMANO_CELDA_GRILLA + 2, p.x * TAMANO_CELDA_GRILLA + 2, TAMANO_CELDA_GRILLA - 4, TAMANO_CELDA_GRILLA - 4);
            }
            g.setColor(Color.ORANGE);
            for (int i = 0; i < camino.size() - 1; i++) {
                Point p1 = camino.get(i);
                Point p2 = camino.get(i + 1);
                int x1 = p1.y * TAMANO_CELDA_GRILLA + TAMANO_CELDA_GRILLA / 2;
                int y1 = p1.x * TAMANO_CELDA_GRILLA + TAMANO_CELDA_GRILLA / 2;
                int x2 = p2.y * TAMANO_CELDA_GRILLA + TAMANO_CELDA_GRILLA / 2;
                int y2 = p2.x * TAMANO_CELDA_GRILLA + TAMANO_CELDA_GRILLA / 2;
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
    }
    
    private void limpiarTablaResultados() {
        modeloTablaResultados.setRowCount(0); // esto limpia las filas anteriores
        modeloTablaResultados.addRow(new Object[]{"Tamaño de la Grilla (NxM)", "", ""});
        modeloTablaResultados.addRow(new Object[]{"Tiempo de Ejecución (ms)", "", ""});
        modeloTablaResultados.addRow(new Object[]{"Llamadas Recursivas", "", ""});
        modeloTablaResultados.addRow(new Object[]{"Caminos Explorados (hasta el final)", "", ""});
        modeloTablaResultados.addRow(new Object[]{"Caminos Válidos (Suma 0)", "", ""});
    }

    private void actualizarTablaResultados(String tipoEjecucion, String tamanoGrilla, long tiempo, long llamadas, long explorados, int totalValidos) {
        int columna = (tipoEjecucion.equals("Sin Poda")) ? 1 : 2;

        if (modeloTablaResultados.getRowCount() == 0 || modeloTablaResultados.getValueAt(0, columna).toString().isEmpty() || !modeloTablaResultados.getValueAt(0,columna).equals(tamanoGrilla) ){
            limpiarTablaResultados(); 
        }
        
        modeloTablaResultados.setValueAt(tamanoGrilla, 0, columna);
        modeloTablaResultados.setValueAt(String.valueOf(tiempo), 1, columna);
        modeloTablaResultados.setValueAt(String.valueOf(llamadas), 2, columna);
        modeloTablaResultados.setValueAt(String.valueOf(explorados), 3, columna);
        modeloTablaResultados.setValueAt(String.valueOf(totalValidos), 4, columna);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainForm();
        });
    }
}