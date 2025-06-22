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

    // Sliders como atributos para acceso global
    private final Slider luzSlider = crearSlider(0, 1000, 10); // lux
    private final Slider sonidoSlider = crearSlider(20, 80, 30); // dB
    private final Slider estresSlider = crearSlider(0, 10, 2); // escala 0-10
    private final Slider actividadSlider = crearSlider(0, 1, 0.1); // Actividad física normalizada (0-1)
    private final Button autoBtn = new Button("Auto 12 pasos");
    private final XYChart.Series<Number, Number> enmoSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> anglezSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> umbralSeries = new XYChart.Series<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Simulación de Probabilidad de Movimiento y Sueño");
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Controles de entrada mejorados
        VBox inputBox = new VBox(10);
        HBox slidersBox = new HBox(20);
        VBox luzBox = crearSliderBox("Luz (lux)", luzSlider, luzValue);
        VBox sonidoBox = crearSliderBox("Sonido (dB)", sonidoSlider, sonidoValue);
        VBox estresBox = crearSliderBox("Estrés", estresSlider, estresValue);
        VBox actividadBox = crearSliderBox("Actividad física (proxy ENMO)", actividadSlider, actividadSliderValue);
        slidersBox.getChildren().addAll(luzBox, sonidoBox, estresBox, actividadBox);
        inputBox.getChildren().addAll(new Label("Ajusta los estímulos para la simulación:"), slidersBox);
        HBox umbralBox = new HBox(10, new Label("Umbral dinámico actual:"), umbralValue);
        inputBox.getChildren().add(umbralBox);
        HBox resultadoBox = new HBox(20,
            new Label("ENMO actual (g):"), enmoValue,
            new Label("Anglez actual (°):"), anglezValue
        );
        inputBox.getChildren().add(resultadoBox);

        // Botones
        Button simularBtn = new Button("Simular Paso");
        Button autoBtn = new Button("Auto 12 pasos");
        Button resetBtn = new Button("Reiniciar");
        HBox buttonBox = new HBox(10, simularBtn, autoBtn, resetBtn);

        // Gráfica de probabilidad de movimiento y probabilidad de estar dormido
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Paso");
        NumberAxis yAxis = new NumberAxis(0, 1, 0.1);
        yAxis.setLabel("Probabilidad");
        LineChart<Number, Number> chartProb = new LineChart<>(xAxis, yAxis);
        chartProb.setTitle("Evolución de la Probabilidad de Movimiento y de Estar Dormido");
        chartProb.setLegendVisible(true);
        chartProb.setAnimated(false);
        probSeries.setName("Probabilidad de Movimiento");
        dormidoSeries.setName("Probabilidad de Estar Dormido");
        umbralSeries.setName("Umbral dinámico");
        chartProb.getData().addAll(probSeries, dormidoSeries, umbralSeries);
        // Solo el umbral debe ser línea punteada y sin puntos
        umbralSeries.getNode().lookup(".chart-series-line").setStyle("-fx-stroke-dash-array: 8 8; -fx-stroke-width: 2; -fx-stroke: #888;");
        // Ocultar los puntos del umbral
        umbralSeries.getNode().lookupAll(".chart-line-symbol").forEach(n -> n.setStyle("-fx-background-color: transparent; -fx-shape: none;"));

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

        // Gráfica de Anglez
        NumberAxis xAxisAnglez = new NumberAxis();
        xAxisAnglez.setLabel("Paso");
        NumberAxis yAxisAnglez = new NumberAxis(0, 180, 20);
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

        // Soporte de zoom horizontal con la rueda del mouse en cada gráfico
        addZoomOnScroll(chartProb);
        addZoomOnScroll(chartEnmo);
        addZoomOnScroll(chartAnglez);

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
        });

        // Layout final: controles arriba, luego botones, luego el TabPane con las gráficas
        root.getChildren().addAll(inputBox, buttonBox, tabPane);
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

    // Permite hacer zoom horizontal con la rueda del mouse en el eje X
    private void addZoomOnScroll(LineChart<Number, Number> chart) {
        NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        chart.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double lower = xAxis.getLowerBound();
            double upper = xAxis.getUpperBound();
            double range = upper - lower;
            double factor = (deltaY > 0) ? 0.8 : 1.25; // acercar o alejar
            double newRange = Math.max(5, range * factor);
            double center = (lower + upper) / 2.0;
            xAxis.setLowerBound(center - newRange / 2.0);
            xAxis.setUpperBound(center + newRange / 2.0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
