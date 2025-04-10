package org.example.demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class HelloApplication extends Application {

    // Labels for current usage percentages
    private Label communityPoolLabel;
    private Label gridPortionLabel;

    // Controls for picking a date/time range
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    // Results labels for the historical data
    private Label communityProducedLabel;
    private Label communityUsedLabel;
    private Label gridUsedLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Community Energy Overview");

        // --- Current percentages (top section) ---
        communityPoolLabel = new Label("Community Pool: 78.54% used");
        gridPortionLabel   = new Label("Grid Portion: 7.23%");

        Button refreshButton = new Button("Refresh");
        // Suppose 'refresh' just updates these values with the newest data from somewhere
        refreshButton.setOnAction(event -> refreshCurrentData());

        VBox currentBox = new VBox(10, communityPoolLabel, gridPortionLabel, refreshButton);
        currentBox.setAlignment(Pos.CENTER_LEFT);

        // --- Date/time range controls (middle section) ---
        Label startLabel = new Label("Start:");
        startDatePicker = new DatePicker(LocalDate.now().minusDays(1));
        Label endLabel = new Label("End:");
        endDatePicker = new DatePicker(LocalDate.now());

        HBox dateBox = new HBox(10,
                startLabel, startDatePicker,
                endLabel, endDatePicker);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Button showDataButton = new Button("Show Data");
        showDataButton.setOnAction(event -> loadHistoricalData());

        VBox dateSection = new VBox(10, dateBox, showDataButton);
        dateSection.setAlignment(Pos.CENTER_LEFT);

        // --- Historical data results (bottom section) ---
        communityProducedLabel = new Label("Community produced: 143.024 kWh");
        communityUsedLabel     = new Label("Community used: 130.101 kWh");
        gridUsedLabel          = new Label("Grid used: 14.75 kWh");

        VBox historicalDataBox = new VBox(10,
                communityProducedLabel,
                communityUsedLabel,
                gridUsedLabel);
        historicalDataBox.setAlignment(Pos.CENTER_LEFT);

        // --- Layout ---
        VBox root = new VBox(20, currentBox, dateSection, historicalDataBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        // Create the scene and show
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Simulate refreshing current data (e.g. calling some backend service)
    private void refreshCurrentData() {
        // Replace these with actual logic or calls to retrieve the current usage
        double communityUsedPercent = Math.random() * 100; // example
        double gridPortionPercent   = 100 - communityUsedPercent; // example

        communityPoolLabel.setText(String.format("Community Pool: %.2f%% used", communityUsedPercent));
        gridPortionLabel.setText(String.format("Grid Portion: %.2f%%", gridPortionPercent));
    }

    // Simulate loading historical data based on the chosen date range
    private void loadHistoricalData() {
        // Retrieve start/end from date pickers
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        // Here you might call an API, query a database, etc., to get historical usage data.
        // For the example, we’re using made-up numbers:
        double produced = Math.random() * 200;
        double used     = Math.random() * 150;
        double gridUsed = produced - used;

        communityProducedLabel.setText(
                String.format("Community produced: %.3f kWh (from %s to %s)",
                        produced, start, end)
        );
        communityUsedLabel.setText(
                String.format("Community used: %.3f kWh", used)
        );
        gridUsedLabel.setText(
                String.format("Grid used: %.3f kWh", gridUsed)
        );
    }
}
