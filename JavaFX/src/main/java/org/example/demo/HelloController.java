package org.example.demo;

import javafx.scene.control.Button;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;public class HelloController {

    @FXML
    private Button fetchButton;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label gridPortionLabel;
    @FXML
    private Label communityDepletedLabel;
    @FXML
    private Label totalProducedLabel;
    @FXML
    private Label totalUsedLabel;
    @FXML
    private Label totalGridLabel;
    @FXML
    private TableView<Map<String, String>> tableView;
    @FXML
    private TableColumn<Map<String, String>, String> hourColumn;
    @FXML
    private TableColumn<Map<String, String>, String> producedColumn;
    @FXML
    private TableColumn<Map<String, String>, String> usedColumn;
    @FXML
    private TableColumn<Map<String, String>, String> gridColumn;

    @FXML
    public void initialize() {
        hourColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get("hour")));
        producedColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get("communityProduced")));
        usedColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get("communityUsed")));
        gridColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().get("gridUsed")));
    }

    @FXML
    private void onFetchClicked() {
        fetchCurrentData();
        fetchHistoricalData();
    }

    public void fetchHistoricalData() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) return;

            String url = "http://localhost:8080/energy/historical?start=" + startDate.atStartOfDay() + "&end=" + endDate.atTime(23, 59);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        try {
                            JSONArray array;
                            if (response.trim().startsWith("[")) {
                                array = new JSONArray(response);
                            } else {
                                array = new JSONArray();
                                array.put(new JSONObject(response));
                            }

                            List<Map<String, String>> list = new ArrayList<>();
                            double totalProduced = 0;
                            double totalUsed = 0;
                            double totalGrid = 0;

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                double produced = obj.getDouble("communityProduced");
                                double used = obj.getDouble("communityUsed");
                                double grid = obj.getDouble("gridUsed");

                                totalProduced += produced;
                                totalUsed += used;
                                totalGrid += grid;

                                Map<String, String> row = new HashMap<>();
                                row.put("hour", obj.getString("hour"));
                                row.put("communityProduced", String.valueOf(produced));
                                row.put("communityUsed", String.valueOf(used));
                                row.put("gridUsed", String.valueOf(grid));
                                list.add(row);
                            }

                            double finalProduced = totalProduced;
                            double finalUsed = totalUsed;
                            double finalGrid = totalGrid;

                            Platform.runLater(() -> {
                                tableView.getItems().setAll(list);
                                totalProducedLabel.setText("Gesamt produziert: " + finalProduced + " kWh");
                                totalUsedLabel.setText("Gesamt verbraucht: " + finalUsed + " kWh");
                                totalGridLabel.setText("Gesamt Grid: " + finalGrid + " kWh");
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchCurrentData() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String url = "http://localhost:8080/energy/current";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(response -> {
                        try {
                            JSONObject json = new JSONObject(response);
                            Platform.runLater(() -> {
                                gridPortionLabel.setText("Grid: " + json.get("gridPortion") + "%");
                                communityDepletedLabel.setText("Community: " + json.get("communityDepleted") + "%");
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
