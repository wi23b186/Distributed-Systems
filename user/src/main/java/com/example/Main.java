package com.example;

import com.example.model.EnergyMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    private static final String ROUTING_KEY = "community";

    public static void main(String[] args) throws Exception {
        var factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection conn = factory.newConnection();
             Channel ch = conn.createChannel()) {

            ch.queueDeclare(ROUTING_KEY, false, false, false, null);

            ObjectMapper jsonMapper = new ObjectMapper();
            DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            while (true) {
                LocalDateTime timestamp = LocalDateTime.now();
                String timeString = timestamp.format(isoFormatter);
                double usedKwh = calculateOutput(timestamp.getHour());

                EnergyMessage message = new EnergyMessage("usage", "community", usedKwh, timeString);
                String payload = jsonMapper.writeValueAsString(message);

                ch.basicPublish("", ROUTING_KEY, null, payload.getBytes(StandardCharsets.UTF_8));
                System.out.println("[send] " + payload);

                int pause = ThreadLocalRandom.current().nextInt(1, 6);
                Thread.sleep(pause * 1000L);
            }
        }
    }

    private static double calculateOutput(int hourOfDay) {
        if ((hourOfDay >= 6 && hourOfDay <= 8) || (hourOfDay >= 18 && hourOfDay <= 22)) {
            return ThreadLocalRandom.current().nextDouble(1.0, 2.0);
        } else if (hourOfDay >= 9 && hourOfDay <= 17) {
            return ThreadLocalRandom.current().nextDouble(0.1, 0.4);
        } else {
            return ThreadLocalRandom.current().nextDouble(0.01, 0.1);
        }
    }
}