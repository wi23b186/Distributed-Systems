package com.example.api.repository;

import com.example.api.model.EnergyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EnergyHistoryRepository
        extends JpaRepository<EnergyHistory, Integer>
{
    @Query("""
            SELECT SUM(e.communityProduced), SUM(e.communityUsed), SUM(e.gridUsed)
            FROM EnergyHistory e
            WHERE e.hour >= :start AND e.hour <= :end
        """)
    List<Object[]> sumHistoricalByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
