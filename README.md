# Distributed-Systems
Projekt Distributed Systems
GitHub-Link: https://github.com/wi23b186/Distributed-Systems

Ordnerstruktur:
Distributed-Systems/
│
├── JavaFX/src/main/
│   ├── java/
│   │   └──module-info.java
│   ├── /resources/org/edxample/demo/
│   │   └──hello-view.fxml
│   ├── java/org/example/demo/
│   │   └──HelloApplication.java
│   │   └──HelloController.java
│
├── API/src/
│   ├── main/java/com/example/api/controller/
│   │   └── EnergyController.java
│   ├── main/java/com/example/api/model/
│   │   ├── CurrentPercentage.java
│   │   └── EnergyHistory.java
│   ├── main/java/com/example/api/repository/
│   │   ├── CurrentPercentageRepository.java
│   │   └── EnergyHistoryRepository.java
│   ├── main/java/com/example/api/DataInitializer.java
│   └── main/java/com/example/api/Application.java
│
├── producer/src/main/java/com/example/
│   ├── Main.java
│   └── model/EnergyMessage.java
│
├── user/src/main/java/com/example/
│   ├── Main.java
│   └── model/EnergyMessage.java
│
├── usage-service/src/main/java/com/example/
│   ├── Main.java
│   ├── JdbcConnection.java
│   └── model/PercentageMessage.java
│
├── percentage-service/src/main/java/com/example/
│   ├── Main.java
│   ├── JdbcConnection.java
│   └── model/PercentageMessage.java
