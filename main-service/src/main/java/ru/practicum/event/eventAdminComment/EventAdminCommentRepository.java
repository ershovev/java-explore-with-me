package ru.practicum.event.eventAdminComment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventAdminCommentRepository extends JpaRepository<EventAdminComment, Long> {
    @Query("SELECT e FROM EventAdminComment e WHERE e.event.id = ?1 ORDER BY e.created ASC")
    List<EventAdminComment> findAllByEventId(long eventId);
}
