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
        return ApplicationDto.builder()
                .id(a.getId())
                .userId(a.getUser().getId())
                .eventId(a.getEvent().getId())
                .status(a.getStatus())
                .applicationOrder(a.getApplicationOrder())
                .lotteryRound(a.getLotteryRound())
                .createdAt(a.getCreatedAt())
                .userEmail(a.getUser().getEmail())
                .userDisplayName(a.getUser().getDisplayName())
                .build();
    }
}
