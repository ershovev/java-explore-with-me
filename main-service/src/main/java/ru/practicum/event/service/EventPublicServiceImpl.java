package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.Category;
import ru.practicum.category.CategoryRepository;
import ru.practicum.enums.EventSort;
import ru.practicum.enums.State;
import ru.practicum.event.Event;
import ru.practicum.event.EventMapper;
import ru.practicum.event.EventRepository;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.exception.CategoryDoesntExistException;
import ru.practicum.exception.EventNotFoundException;
import ru.practicum.statistics.StatisticsService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventPublicServiceImpl implements EventPublicService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatisticsService statisticsService;

    @Override
    public List<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, int from, int size,
                                         HttpServletRequest request) {


        Pageable pageable;
        List<Event> eventList = new ArrayList<>();

        if (categories != null) {
            List<Category> categoryList = categoryRepository.findAllByIdIn(categories);
            if (categoryList.size() != categories.size()) {
                throw new CategoryDoesntExistException("Категория/категории не найдены");
            }
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }

        if (sort == EventSort.EVENT_DATE) {
            pageable = PageRequest.of((from / size), size, Sort.by("eventDate").ascending());

            eventList = eventRepository.findPublishedEventsByParams(text, categories, paid,
                    rangeStart, rangeEnd, onlyAvailable, pageable);
        } else {
            List<Long> allEventsIds = eventRepository.findAllPublishedEventsIdsByParams(text, categories, paid,
                    rangeStart, rangeEnd, onlyAvailable);

            List<Long> filteredEventsIds = statisticsService.getPopularFilteredEvents(allEventsIds, from, size,
                    LocalDateTime.now().minusYears(100), LocalDateTime.now().plusYears(100));

            eventList = eventRepository.findAllByIdIn(filteredEventsIds);

            eventList.sort(Comparator.comparing(Event::getViews, Comparator.reverseOrder()));
        }

        eventList = statisticsService.findAndSetViewsToEvents(eventList, LocalDateTime.now().minusYears(100), LocalDateTime.now().plusYears(100));

        statisticsService.addEndpointHit(request);

        return EventMapper.toEventShortDtoList(eventList);
    }

    @Override
    public EventFullDto getEventById(long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Событие не найдено"));

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new EventNotFoundException("Событие не найдено");
        }

        List<Event> eventList = new ArrayList<>();
        eventList = statisticsService.findAndSetViewsToEvents(List.of(event), LocalDateTime.now().minusYears(100), LocalDateTime.now().plusYears(100));
        statisticsService.addEndpointHit(request);

        return EventMapper.toEventFullDto(eventList.get(0));
    }
}
