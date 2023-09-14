package ru.practicum.participationrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event.Event;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequesterId(long userId);

    ParticipationRequest findByRequesterIdAndEventId(long userId, long eventId);


    List<ParticipationRequest> findAllByIdIn(List<Long> ids);

    List<ParticipationRequest> findAllByEvent(Event event);
}
