package org.example.demo;

import javafx.scene.control.Button;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
public class HelloController {

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
                            JSONObject obj = new JSONObject(response);

                            double sumProduced = obj.optDouble("communityProduced", 0.0);
                            double sumUsed     = obj.optDouble("communityUsed",    0.0);
                            double sumGrid     = obj.optDouble("gridUsed",         0.0);

                            Platform.runLater(() -> {
                                totalProducedLabel.setText("Community Produktion: " + sumProduced + " kWh");
                                totalUsedLabel.setText(    "Community Verbrauch: " + sumUsed     + " kWh");
                                totalGridLabel.setText(    "Grid Verbrauch: "      + sumGrid    + " kWh");
                            });
                        } catch (JSONException e) {
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
