package com.example;

import com.example.model.PercentageMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

public class Main {
    private static final String READ_QUEUE = "percentage";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (com.rabbitmq.client.Connection rmqConn = factory.newConnection();
             Channel rmqCh = rmqConn.createChannel()) {

            rmqCh.queueDeclare(READ_QUEUE, false, false, false, null);
            System.out.println("Listening for percentage updates on queue: " + READ_QUEUE);

            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule());

            DeliverCallback callback = (tag, delivery) -> {
                String json = new String(delivery.getBody(), StandardCharsets.UTF_8);
                PercentageMessage msg = mapper.readValue(json, PercentageMessage.class);

                LocalDateTime hour = msg.getHour().withMinute(0).withSecond(0).withNano(0);
                double used    = msg.getCommunityUsed() + msg.getGridUsed();
                double communityPct = used > 0
                        ? msg.getCommunityUsed() / msg.getCommunityProduced() * 100.0
                        : 0.0;
                double gridPct      = msg.getGridUsed() / (msg.getCommunityUsed() + msg.getGridUsed()) * 100.0;

                upsertCurrent(hour, communityPct, gridPct);
                System.out.printf("[Current] %s → community=%.2f%%, grid=%.2f%%%n",
                        hour, communityPct, gridPct);
            };

            rmqCh.basicConsume(READ_QUEUE, true, callback, _ct -> {});
            Thread.currentThread().join();
        }
    }

    private static void upsertCurrent(LocalDateTime hour,
                                      double communityPct,
                                      double gridPct) {
        String clearSql  = "TRUNCATE TABLE current_percentage";
        String insertSql = """
            INSERT INTO current_percentage(hour, community_depleted, grid_portion)
              VALUES (?, ?, ?)
        """;

        try (Connection db = JdbcConnection.open();
             PreparedStatement clear = db.prepareStatement(clearSql);
             PreparedStatement ins   = db.prepareStatement(insertSql)) {

            clear.executeUpdate();

            ins.setObject(1, hour);
            ins.setDouble(2, communityPct);
            ins.setDouble(3, gridPct);
            ins.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}