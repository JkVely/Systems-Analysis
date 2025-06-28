package edu.system_analysis.view;

import edu.system_analysis.controller.SimulationController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SimulationView extends Application {
    private final SimulationController controller = new SimulationController();
    private final XYChart.Series<Number, Number> probSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> dormidoSeries = new XYChart.Series<>();
    private final Label luzValue = new Label();
    private final Label sonidoValue = new Label();
    private final Label estresValue = new Label();
    private final Label actividadSliderValue = new Label();
    private final Label umbralValue = new Label();
    private final Label enmoValue = new Label();
    private final Label anglezValue = new Label();
    private final Label periodoCircadianoValue = new Label();

    // Sliders como atributos para acceso global
    private final Slider luzSlider = crearSlider(0, 1000, 10); // lux
    private final Slider sonidoSlider = crearSlider(20, 80, 30); // dB
    private final Slider estresSlider = crearSlider(0, 10, 2); // escala 0-10
    private final Slider actividadSlider = crearSlider(0, 1, 0.1); // Actividad física normalizada (0-1)
    private final Button autoBtn = new Button("Auto 12 pasos");
    private final Button simularBtn = new Button("Simular Paso");
    private final Button resetBtn = new Button("Reiniciar");
    private final XYChart.Series<Number, Number> enmoSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> anglezSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> umbralSeries = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Simulación de Probabilidad de Movimiento y Sueño");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // --- INTERFAZ MINIMALISTA Y LIMPIA ---
        // Controles compactos
        VBox inputBox = new VBox(6);
        HBox slidersBox = new HBox(12);
        VBox luzBox = crearSliderBox("Luz", luzSlider, luzValue);
        VBox sonidoBox = crearSliderBox("Sonido", sonidoSlider, sonidoValue);
        VBox estresBox = crearSliderBox("Estrés", estresSlider, estresValue);
        VBox actividadBox = crearSliderBox("Actividad", actividadSlider, actividadSliderValue);
        slidersBox.getChildren().addAll(luzBox, sonidoBox, estresBox, actividadBox);
        inputBox.getChildren().add(slidersBox);
        HBox resultadoBox = new HBox(16,
            new Label("ENMO:"), enmoValue,
            new Label("Anglez:"), anglezValue,
            new Label("Umbral:"), umbralValue
        );
        inputBox.getChildren().add(resultadoBox);
        HBox circadianoBox = new HBox(10,
            new Label("Período Circadiano:"), periodoCircadianoValue
        );
        inputBox.getChildren().add(circadianoBox);
        // Botones compactos
        HBox buttonBox = new HBox(8, simularBtn, autoBtn, resetBtn);

        // Gráfica de probabilidad de movimiento y probabilidad de estar dormido
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Paso");
        NumberAxis yAxis = new NumberAxis(0, 1, 0.1);
        yAxis.setLabel("");
        LineChart<Number, Number> chartProb = new LineChart<>(xAxis, yAxis);
        chartProb.setTitle("");
        chartProb.setLegendVisible(true);
        chartProb.setAnimated(false);
        probSeries.setName("Probabilidad de Movimiento");
        dormidoSeries.setName("Probabilidad de Estar Dormido");
        umbralSeries.setName("Umbral dinámico (límite sueño/vigilia)");
        chartProb.getData().addAll(probSeries, dormidoSeries, umbralSeries);
        // Aplicar estilos después de que la escena esté lista
        chartProb.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                umbralSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke-dash-array: 8 8; -fx-stroke-width: 2; -fx-stroke: #888;");
                umbralSeries.getNode().lookupAll(".chart-line-symbol").forEach(n -> n.setStyle("-fx-background-color: transparent; -fx-shape: none;"));
            }
        });

        // Gráfica de ENMO
        NumberAxis xAxisEnmo = new NumberAxis();
        xAxisEnmo.setLabel("Paso");
        NumberAxis yAxisEnmo = new NumberAxis(0, 0.2, 0.02);
        yAxisEnmo.setLabel("ENMO (g)");
        LineChart<Number, Number> chartEnmo = new LineChart<>(xAxisEnmo, yAxisEnmo);
        chartEnmo.setTitle("Evolución de ENMO");
        chartEnmo.setLegendVisible(false);
        chartEnmo.setAnimated(false);
        enmoSeries.setName("ENMO (g)");
        chartEnmo.getData().add(enmoSeries);

        // Gráfica de Anglez (rango correcto -90 a 90)
        NumberAxis xAxisAnglez = new NumberAxis();
        xAxisAnglez.setLabel("Paso");
        NumberAxis yAxisAnglez = new NumberAxis(-90, 90, 30);
        yAxisAnglez.setLabel("Anglez (°)");
        LineChart<Number, Number> chartAnglez = new LineChart<>(xAxisAnglez, yAxisAnglez);
        chartAnglez.setTitle("Evolución de Anglez");
        chartAnglez.setLegendVisible(false);
        chartAnglez.setAnimated(false);
        anglezSeries.setName("Anglez (°)");
        chartAnglez.getData().add(anglezSeries);

        // Crear pestañas para cada gráfica
        TabPane tabPane = new TabPane();
        Tab tabProb = new Tab("Probabilidad", chartProb);
        Tab tabEnmo = new Tab("ENMO", chartEnmo);
        Tab tabAnglez = new Tab("Anglez", chartAnglez);
        tabProb.setClosable(false);
        tabEnmo.setClosable(false);
        tabAnglez.setClosable(false);
        tabPane.getTabs().addAll(tabProb, tabEnmo, tabAnglez);

        // Soporte de zoom solo cuando la gráfica está visible (tab seleccionada)
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == tabProb) {
                addZoomBothAxes(chartProb);
            } else if (newTab == tabEnmo) {
                addZoomBothAxes(chartEnmo);
            } else if (newTab == tabAnglez) {
                addZoomBothAxes(chartAnglez);
            }
        });
        // Inicializar zoom en la pestaña activa al inicio
        addZoomBothAxes(chartProb);

        // Listeners y acciones
        luzSlider.valueProperty().addListener((obs, oldVal, newVal) -> { luzValue.setText(String.format("%.0f lux", newVal.doubleValue())); actualizarValoresBase(); });
        sonidoSlider.valueProperty().addListener((obs, oldVal, newVal) -> { sonidoValue.setText(String.format("%.0f dB", newVal.doubleValue())); actualizarValoresBase(); });
        estresSlider.valueProperty().addListener((obs, oldVal, newVal) -> { estresValue.setText(String.format("%.1f", newVal.doubleValue())); actualizarValoresBase(); });
        actividadSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            actividadSliderValue.setText(String.format("%.2f", newVal.doubleValue()));
            controller.setActividadManual(newVal.doubleValue());
        });
        luzValue.setText(String.format("%.0f lux", luzSlider.getValue()));
        sonidoValue.setText(String.format("%.0f dB", sonidoSlider.getValue()));
        estresValue.setText(String.format("%.1f", estresSlider.getValue()));
        actividadSliderValue.setText(String.format("%.2f", actividadSlider.getValue()));
        umbralValue.setText(String.format("%.2f", controller.getLastUmbral()));
        enmoValue.setText(String.format("%.3f", controller.getEnmoActual()));
        anglezValue.setText(String.format("%.0f", controller.getAnglezActual()));
        periodoCircadianoValue.setText(controller.getNombrePeriodoCircadiano());
        periodoCircadianoValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e8b57;");

        simularBtn.setOnAction(e -> {
            double prob = controller.simularPaso();
            int paso = controller.getProbHistory().size();
            probSeries.getData().add(new XYChart.Data<>(paso, prob));
            double dormidoProb = controller.getUltimaProbDormido();
            dormidoSeries.getData().add(new XYChart.Data<>(paso, dormidoProb));
            enmoSeries.getData().add(new XYChart.Data<>(paso, controller.getEnmoActual()));
            anglezSeries.getData().add(new XYChart.Data<>(paso, controller.getAnglezActual()));
            umbralSeries.getData().add(new XYChart.Data<>(paso, controller.getLastUmbral()));
            umbralValue.setText(String.format("%.2f", controller.getLastUmbral()));
            luzValue.setText(String.format("%.0f lux", controller.getLuzActual()));
            sonidoValue.setText(String.format("%.0f dB", controller.getSonidoActual()));
            estresValue.setText(String.format("%.1f", controller.getEstresActual()));
            enmoValue.setText(String.format("%.3f", controller.getEnmoActual()));
            anglezValue.setText(String.format("%.0f", controller.getAnglezActual()));
            actividadSliderValue.setText(String.format("%.2f", controller.getActividadActual()));
            periodoCircadianoValue.setText(controller.getNombrePeriodoCircadiano());
        });
        autoBtn.setOnAction(e -> {
            for (int i = 0; i < 12; i++) simularBtn.fire();
        });
        resetBtn.setOnAction(e -> {
            controller.reset();
            probSeries.getData().clear();
            dormidoSeries.getData().clear();
            enmoSeries.getData().clear();
            anglezSeries.getData().clear();
            umbralSeries.getData().clear();
            umbralValue.setText(String.format("%.2f", controller.getLastUmbral()));
            luzValue.setText(String.format("%.0f lux", luzSlider.getValue()));
            sonidoValue.setText(String.format("%.0f dB", sonidoSlider.getValue()));
            estresValue.setText(String.format("%.1f", estresSlider.getValue()));
            enmoValue.setText(String.format("%.3f", controller.getEnmoActual()));
            anglezValue.setText(String.format("%.0f", controller.getAnglezActual()));
            actividadSliderValue.setText(String.format("%.2f", controller.getActividadActual()));
            periodoCircadianoValue.setText(controller.getNombrePeriodoCircadiano());
        });

        // Layout final: controles arriba, luego botones, luego el TabPane con las gráficas
        root.getChildren().setAll(inputBox, buttonBox, tabPane);
        Scene scene = new Scene(root, 1000, 800);
        scene.getStylesheets().add(getClass().getResource("/edu/system_analysis/view/simulation.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void actualizarValoresBase() {
        controller.setBases(luzSlider.getValue(), sonidoSlider.getValue(), estresSlider.getValue(), 0, 0);
    }

    private Slider crearSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.2);
        slider.setBlockIncrement(0.05);
        return slider;
    }

    private VBox crearSliderBox(String label, Slider slider, Label valueLabel) {
        VBox box = new VBox(5);
        Label l = new Label(label);
        valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4f8cff;");
        slider.setPrefWidth(180);
        box.getChildren().addAll(l, slider, valueLabel);
        return box;
    }

    // Zoom solo en eje X (horizontal) con la rueda del ratón (fix multiplataforma)
    private void addZoomBothAxes(LineChart<Number, Number> chart) {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();

        chart.setOnScroll(event -> {
            // Verificar que hay datos
            if (chart.getData().isEmpty()) return;
            boolean hasData = false;
            for (var series : chart.getData()) {
                if (!series.getData().isEmpty()) {
                    hasData = true;
                    break;
                }
            }
            if (!hasData) return;

            // Solo responder a scroll vertical (rueda ratón)
            if (event.getEventType().getName().equals("SCROLL") && event.isControlDown()) {
                // Si el usuario mantiene Ctrl, dejar zoom default (ambos ejes)
                return;
            }
            if (Math.abs(event.getDeltaY()) < 1e-3) return;
            double factor = (event.getDeltaY() > 0) ? 0.85 : 1.18;

            // Zoom SOLO en X
            double xLower = xAxis.getLowerBound();
            double xUpper = xAxis.getUpperBound();
            double xRange = xUpper - xLower;
            double newXRange = Math.max(5, xRange * factor);
            double xCenter = (xLower + xUpper) / 2.0;
            xAxis.setLowerBound(Math.max(0, xCenter - newXRange / 2.0));
            xAxis.setUpperBound(xCenter + newXRange / 2.0);

            event.consume();
        });

        // Paneo con botón derecho en ambos ejes (opcional: solo X si quieres)
        final double[] lastMouse = {0, 0};
        chart.setOnMousePressed(event -> {
            if (event.isSecondaryButtonDown()) {
                lastMouse[0] = event.getX();
                lastMouse[1] = event.getY();
            }
        });
        chart.setOnMouseDragged(event -> {
            if (event.isSecondaryButtonDown()) {
                double dx = event.getX() - lastMouse[0];
                double xScale = (xAxis.getUpperBound() - xAxis.getLowerBound()) / chart.getWidth();

                double newXLower = xAxis.getLowerBound() - dx * xScale;
                double newXUpper = xAxis.getUpperBound() - dx * xScale;
                xAxis.setLowerBound(Math.max(0, newXLower));
                xAxis.setUpperBound(newXUpper);

                lastMouse[0] = event.getX();
                lastMouse[1] = event.getY();
            }
        });

        // Doble clic para auto-ajustar a todos los datos
        chart.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                xAxis.setAutoRanging(true);
                yAxis.setAutoRanging(true);
                // Pequeño delay para permitir que el auto-ranging tome efecto
                javafx.application.Platform.runLater(() -> {
                    xAxis.setAutoRanging(false);
                    yAxis.setAutoRanging(false);
                });
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
