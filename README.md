# Distributed Energy Management System – Komponentenübersicht

Dieses Projekt bildet ein verteiltes Energiemanagementsystem ab, das verschiedene Softwarekomponenten nutzt, um Energiedaten von Produzenten und Konsumenten zu verarbeiten, zu speichern und anzuzeigen.

 Komponentenübersicht
 
 JavaFX GUI
- Benutzeroberfläche zur Anzeige aktueller und historischer Energiedaten
- Kommuniziert über REST mit dem Backend
- Stellt Anfragen wie:
  - GET /energy/current
  - GET /energy/historical?start=...&end=...

Spring Boot REST API
- Backend-Service
- Bietet REST-Endpunkte für die GUI
- Liest Daten aus der PostgreSQL-Datenbank (z. B. Nutzungsdaten, Prozentwerte)

 RabbitMQ
- Message Broker für asynchrone Kommunikation
- Leitet Nachrichten von:
  - Energy Producer
  - Energy User
- An:
  - Usage Service
- Leitet Nachrichten von:
  - Usage Service
- An:
   -percentage Service

Usage Service
- Verarbeitet generierten daten 
- Empfängt Nachrichten über RabbitMQ
- Aktualisiert die history-Tabelle in PostgreSQL


Current Percentage Service
- Empfängt Nachrichten über RabbitMQ
- Berechnet und speichert prozentuale Verteilungen
- Aktualisiert die Current percentage -Tabelle in PostgreSQL

 PostgreSQL
- Zentrale Datenbank
- Enthält Tabellen für:
  - Energieverbrauch („usage“)
  - Energieanteile („percentage“)
- Wird sowohl schreibend von Services als auch lesend von der REST-API verwendet

Energy Producer / Energy User
- Simulieren Datenquellen
- Senden Energie-Nachrichten an RabbitMQ
  - Producer → z. B. erzeugte Energie
  - User → z. B. verbrauchte Energie
