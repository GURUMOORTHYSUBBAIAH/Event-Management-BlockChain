package com.eventchain.service;

import com.eventchain.dto.AnnouncementDto;
import com.eventchain.dto.CreateAnnouncementRequest;
import com.eventchain.entity.Announcement;
import com.eventchain.entity.Event;
import com.eventchain.entity.User;
import com.eventchain.exception.ForbiddenException;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.AnnouncementRepository;
import com.eventchain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Transactional
    public AnnouncementDto create(CreateAnnouncementRequest request, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        if (user == null) throw new ForbiddenException("Not authenticated");

        Event event = request.getEventId() != null && request.getEventId() > 0
                ? eventRepository.findById(request.getEventId()).orElse(null) : null;

        Announcement announcement = new Announcement();
        announcement.setEvent(event);
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setCreatedBy(user);
        announcement = announcementRepository.save(announcement);
        return toDto(announcement);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> getByEvent(Long eventId, Authentication auth) {
        return announcementRepository.findByEventIdOrderByCreatedAtDesc(eventId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDto> getPublic() {
        return announcementRepository.findByEventIdIsNullOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private AnnouncementDto toDto(Announcement a) {
        AnnouncementDto dto = new AnnouncementDto();
        dto.setId(a.getId());
        dto.setEventId(a.getEvent() != null ? a.getEvent().getId() : null);
        dto.setTitle(a.getTitle());
        dto.setContent(a.getContent());
        dto.setType(a.getType());
        dto.setCreatedBy(a.getCreatedBy() != null ? a.getCreatedBy().getId() : null);
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
