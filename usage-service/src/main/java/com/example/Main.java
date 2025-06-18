package com.example;

import com.example.model.EnergyMessage;
import com.example.model.PercentageMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;

public class Main {
    private static final String RAW_QUEUE = "community";
    private static final String AGG_QUEUE = "percentage";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (com.rabbitmq.client.Connection rmqConn = factory.newConnection();
             Channel rmqCh = rmqConn.createChannel()) {

            rmqCh.queueDeclare(RAW_QUEUE, false, false, false, null);
            rmqCh.queueDeclare(AGG_QUEUE, false, false, false, null);
            System.out.println("Listening on queue: " + RAW_QUEUE);

            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule());

            DeliverCallback callback = (tag, delivery) -> {
                String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
                EnergyMessage msg = mapper.readValue(body, EnergyMessage.class);

                PercentageMessage result;
                if ("usage".equalsIgnoreCase(msg.getType())) {
                    result = handleConsumption(msg);
                } else if ("produce".equalsIgnoreCase(msg.getType())) {
                    result = handleProduction(msg);
                } else {
                    System.out.println("Unsupported type: " + msg.getType());
                    return;
                }

                if (result != null) {
                    sendAggregateUpdate(result, rmqCh, mapper);
                }
            };

            rmqCh.basicConsume(RAW_QUEUE, true, callback, consumerTag -> { });
            Thread.currentThread().join();
        }
    }

    private static PercentageMessage handleConsumption(EnergyMessage msg) {
        try (Connection db = JdbcConnection.open()) {
            LocalDateTime ts = LocalDateTime.parse(msg.getDatetime());
            LocalDateTime hourKey = ts.withMinute(0).withSecond(0).withNano(0);

            String select = "SELECT id, community_produced, community_used, grid_used "
                    + "FROM energy_history WHERE hour = ?";
            long id;
            double produced, used, grid;

            try (PreparedStatement ps = db.prepareStatement(select)) {
                ps.setObject(1, hourKey);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        id       = rs.getLong("id");
                        produced = rs.getDouble("community_produced");
                        used     = rs.getDouble("community_used");
                        grid     = rs.getDouble("grid_used");
                    } else {
                        String ins = "INSERT INTO energy_history(hour, community_produced, community_used, grid_used) "
                                + "VALUES (?, 0, 0, 0) RETURNING id";
                        try (PreparedStatement insPs = db.prepareStatement(ins)) {
                            insPs.setObject(1, hourKey);
                            try (ResultSet keys = insPs.executeQuery()) {
                                keys.next();
                                id = keys.getLong(1);
                            }
                        }
                        produced = 0; used = 0; grid = 0;
                    }
                }
            }

            double newUsed  = used + msg.getKwh();
            double overflow = Math.max(0, newUsed - produced);
            double finalUsed = Math.min(newUsed, produced);
            double finalGrid = grid + overflow;

            String upd = "UPDATE energy_history SET community_used = ?, grid_used = ? WHERE id = ?";
            try (PreparedStatement upPs = db.prepareStatement(upd)) {
                upPs.setDouble(1, finalUsed);
                upPs.setDouble(2, finalGrid);
                upPs.setLong(3, id);
                upPs.executeUpdate();
            }

            // DTO zusammenbauen für die Rückmeldung
            PercentageMessage rec = new PercentageMessage();
            rec.setHour(hourKey);
            rec.setCommunityProduced(produced);
            rec.setCommunityUsed(finalUsed);
            rec.setGridUsed(finalGrid);

            System.out.printf("\n[Consumption] %s → used=%.2f kWh%n",
                    hourKey, msg.getKwh());
            return rec;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PercentageMessage handleProduction(EnergyMessage msg) {
        try (Connection db = JdbcConnection.open()) {
            LocalDateTime ts = LocalDateTime.parse(msg.getDatetime());
            LocalDateTime hourKey = ts.withMinute(0).withSecond(0).withNano(0);

            String select = """
                SELECT id, community_produced, community_used, grid_used
                  FROM energy_history
                 WHERE hour = ?
            """;
            long id;
            double produced, used, grid;

            try (PreparedStatement ps = db.prepareStatement(select)) {
                ps.setObject(1, hourKey);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        id       = rs.getLong("id");
                        produced = rs.getDouble("community_produced");
                        used     = rs.getDouble("community_used");
                        grid     = rs.getDouble("grid_used");
                    } else {
                        String ins = """
                            INSERT INTO energy_history(hour,
                                                       community_produced,
                                                       community_used,
                                                       grid_used)
                                 VALUES (?, 0, 0, 0)
                             RETURNING id
                        """;
                        try (PreparedStatement insPs = db.prepareStatement(ins)) {
                            insPs.setObject(1, hourKey);
                            try (ResultSet keys = insPs.executeQuery()) {
                                keys.next();
                                id = keys.getLong(1);
                            }
                        }
                        produced = 0;
                        used     = 0;
                        grid     = 0;
                    }
                }
            }

            double newProd = produced + msg.getKwh();
            String upd = """
                UPDATE energy_history
                   SET community_produced = ?
                 WHERE id = ?
            """;
            try (PreparedStatement upPs = db.prepareStatement(upd)) {
                upPs.setDouble(1, newProd);
                upPs.setLong(2, id);
                upPs.executeUpdate();
            }

            PercentageMessage rec = new PercentageMessage();
            rec.setHour(hourKey);
            rec.setCommunityProduced(newProd);
            rec.setCommunityUsed(used);
            rec.setGridUsed(grid);

            System.out.printf("\n[Production] %s → produced=%.2f kWh%n",
                    hourKey, msg.getKwh());
            return rec;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void sendAggregateUpdate(PercentageMessage rec,
                                            Channel ch,
                                            ObjectMapper mapper) {
        try {
            var update = new com.example.model.PercentageMessage(
                    rec.getHour(),
                    rec.getCommunityProduced(),
                    rec.getCommunityUsed(),
                    rec.getGridUsed());
            String out = mapper.writeValueAsString(update);
            ch.basicPublish("", AGG_QUEUE, null, out.getBytes(StandardCharsets.UTF_8));
            System.out.println("[Aggregate Sent] " + out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}