package ru.practicum.event.eventAdminComment;

import lombok.*;
import ru.practicum.event.Event;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_admin_comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAdminComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "description")
    private String description;

    @Column(name = "created_date")
    private LocalDateTime created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;
}