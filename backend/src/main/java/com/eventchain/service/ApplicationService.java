package com.eventchain.service;

import com.eventchain.dto.ApplicationDto;
import com.eventchain.entity.Application;
import com.eventchain.entity.Event;
import com.eventchain.entity.User;
import com.eventchain.exception.BadRequestException;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.ApplicationRepository;
import com.eventchain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final EventRepository eventRepository;
    private final UserService userService;

    @Transactional
    public ApplicationDto apply(Long eventId, org.springframework.security.core.Authentication auth) {
        User user = userService.getCurrentUser(auth);
        if (user == null) throw new BadRequestException("User not authenticated");

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!"OPEN".equals(event.getStatus())) {
            throw new BadRequestException("Applications are closed for this event");
        }
        if (applicationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new BadRequestException("Already applied");
        }

        Application app = new Application();
        app.setUser(user);
        app.setEvent(event);
        app.setStatus("APPLIED");
        app = applicationRepository.save(app);
        return toDto(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> getApplicationsByEvent(Long eventId) {
        return applicationRepository.findByEventIdOrderByCreatedAtAsc(eventId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto> getMyApplications(Long userId) {
        return applicationRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private ApplicationDto toDto(Application a) {
        ApplicationDto dto = new ApplicationDto();
        dto.setId(a.getId());
        dto.setUserId(a.getUser() != null ? a.getUser().getId() : null);
        dto.setEventId(a.getEvent() != null ? a.getEvent().getId() : null);
        dto.setStatus(a.getStatus());
        dto.setApplicationOrder(a.getApplicationOrder());
        dto.setLotteryRound(a.getLotteryRound());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUserEmail(a.getUser() != null ? a.getUser().getEmail() : null);
        dto.setUserDisplayName(a.getUser() != null ? a.getUser().getDisplayName() : null);
        return dto;
    }
}
