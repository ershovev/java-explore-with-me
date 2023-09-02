package ru.practicum.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.EndpointHit;
import ru.practicum.model.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("SELECT NEW ru.practicum.model.ViewStats(e.app, e.uri, COUNT(e)) FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 GROUP BY e.app, e.uri")
    List<ViewStats> getViewStats(LocalDateTime start, LocalDateTime end);

    @Query("SELECT NEW ru.practicum.model.ViewStats(e.app, e.uri, COUNT(DISTINCT (e))) FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 GROUP BY e.app, e.uri")
    List<ViewStats> getViewStatsUnique(LocalDateTime start, LocalDateTime end);

    @Query("SELECT NEW ru.practicum.model.ViewStats(e.app, e.uri, COUNT(e)) FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 AND e.uri IN ?3 GROUP BY e.app, e.uri")
    List<ViewStats> getViewStatsByUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT NEW ru.practicum.model.ViewStats(e.app, e.uri, COUNT(DISTINCT (e))) FROM EndpointHit e " +
            "WHERE e.timestamp BETWEEN ?1 AND ?2 AND e.uri IN ?3 GROUP BY e.app, e.uri")
    List<ViewStats> getViewStatsByUrisUnique(LocalDateTime start, LocalDateTime end, List<String> uris);
}