package ru.practicum.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.enums.State;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT e FROM Event e JOIN FETCH e.category AS c JOIN FETCH e.initiator AS i JOIN FETCH e.location AS l " +
            "WHERE (:users IS NULL OR i.id IN :users) AND (:states IS NULL or e.state IN :states) " +
            "AND (:categories IS NULL OR c.id in :categories) AND e.eventDate BETWEEN :rangeStart AND :rangeEnd")
    List<Event> findAllByParams(List<Long> users, List<State> states, List<Long> categories,
                                LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN FETCH e.category AS c JOIN FETCH e.initiator AS i JOIN FETCH e.location AS l " +
            "WHERE e.state = 'PUBLISHED' AND (:text IS NULL OR LOWER(e.title) LIKE LOWER(concat('%', :text, '%')) " +
            "OR LOWER(e.annotation) " +
            "LIKE LOWER(concat('%', :text, '%'))) AND (:categories IS NULL OR c.id in :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) AND e.eventDate BETWEEN :rangeStart AND :rangeEnd " +
            "AND (:onlyAvailable IS NULL OR e.confirmedRequests < e.participantLimit)")
    List<Event> findPublishedEventsByParams(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, Boolean onlyAvailable, Pageable pageable);

    @Query("SELECT e.id FROM Event e " +
            "WHERE e.state = 'PUBLISHED' AND (:text IS NULL OR LOWER(e.title) LIKE LOWER(concat('%', :text, '%')) " +
            "OR LOWER(e.annotation) " +
            "LIKE LOWER(concat('%', :text, '%'))) AND (:categories IS NULL OR e.category.id in :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) AND e.eventDate BETWEEN :rangeStart AND :rangeEnd " +
            "AND (:onlyAvailable IS NULL OR e.confirmedRequests < e.participantLimit)")
    List<Long> findAllPublishedEventsIdsByParams(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd, Boolean onlyAvailable);

    @Query("SELECT e FROM Event e JOIN FETCH e.category AS c JOIN FETCH e.initiator AS i JOIN FETCH e.location AS l " +
            "WHERE e.id IN :eventsIds")
    List<Event> findAllByIdIn(List<Long> eventsIds);

    @Query("SELECT e FROM Event e JOIN FETCH e.category AS c JOIN FETCH e.initiator AS i JOIN FETCH e.location AS l " +
            "WHERE e.id = :eventId")
    Optional<Event> findById(long eventId);

    @Query("SELECT e FROM Event e JOIN FETCH e.category AS c JOIN FETCH e.initiator AS i JOIN FETCH e.location AS l " +
            "WHERE i.id = :userId")
    List<Event> findByInitiatorId(long userId, Pageable pageable);
}