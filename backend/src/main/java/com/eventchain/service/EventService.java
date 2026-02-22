package com.eventchain.service;

import com.eventchain.dto.*;
import com.eventchain.entity.Event;
import com.eventchain.entity.User;
import com.eventchain.exception.BadRequestException;
import com.eventchain.exception.ForbiddenException;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final TicketRepository ticketRepository;
    private final CertificateRepository certificateRepository;
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Page<EventDto> getEvents(String status, Pageable pageable) {
        return eventRepository.findByStatus(status != null ? status : "OPEN", pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public EventDto getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return toDto(event);
    }

    @Transactional
    public EventDto createEvent(CreateEventRequest request, Authentication auth) {
        User creator = userService.getCurrentUser(auth);
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setEventDate(request.getEventDate());
        event.setLocation(request.getLocation());
        event.setPrice(request.getPrice());
        event.setMaxSeats(request.getMaxSeats());
        event.setLotteryDeadline(request.getLotteryDeadline());
        event.setStatus("DRAFT");
        event.setCreatedBy(creator);
        event = eventRepository.save(event);
        return toDto(event);
    }

    @Transactional
    public EventDto updateEvent(Long id, CreateEventRequest request, Authentication auth) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!"DRAFT".equals(event.getStatus())) {
            throw new BadRequestException("Can only update draft events");
        }
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setEventDate(request.getEventDate());
        event.setLocation(request.getLocation());
        event.setPrice(request.getPrice());
        event.setMaxSeats(request.getMaxSeats());
        event.setLotteryDeadline(request.getLotteryDeadline());
        event = eventRepository.save(event);
        return toDto(event);
    }

    @Transactional
    public EventDto publishEvent(Long id, Authentication auth) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!"DRAFT".equals(event.getStatus())) {
            throw new BadRequestException("Only draft events can be published");
        }
        event.setStatus("OPEN");
        event = eventRepository.save(event);
        return toDto(event);
    }

    @Transactional(readOnly = true)
    public EventAnalyticsDto getAnalytics(Long eventId, Authentication auth) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        long totalApplicants = applicationRepository.findByEventIdOrderByCreatedAtAsc(eventId).size();
        long selectedCount = applicationRepository.findByEventIdAndStatus(eventId, "SELECTED").size();
        long waitlistedCount = applicationRepository.findByEventIdAndStatus(eventId, "WAITLISTED").size();
        long paidCount = applicationRepository.findByEventIdAndStatus(eventId, "PAID").size();
        long nftsMinted = ticketRepository.countByEventId(eventId);
        long checkedInCount = ticketRepository.countByEventIdAndCheckedIn(eventId, true);
        long certificatesIssued = certificateRepository.countByEventId(eventId);

        var payments = paymentRepository.findCompletedByEventId(eventId);
        var revenue = payments.stream()
                .map(p -> p.getAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        double paymentPct = selectedCount > 0 ? (paidCount * 100.0 / selectedCount) : 0;
        double noShowRate = nftsMinted > 0 ? ((nftsMinted - checkedInCount) * 100.0 / nftsMinted) : 0;

        return EventAnalyticsDto.builder()
                .eventId(eventId)
                .eventTitle(event.getTitle())
                .status(event.getStatus())
                .totalApplicants(totalApplicants)
                .selectedCount(selectedCount)
                .waitlistedCount(waitlistedCount)
                .paidCount(paidCount)
                .nftsMinted(nftsMinted)
                .checkedInCount(checkedInCount)
                .certificatesIssued(certificatesIssued)
                .revenue(revenue)
                .paymentPercentage(paymentPct)
                .noShowRate(noShowRate)
                .build();
    }

    private EventDto toDto(Event e) {
        return EventDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .category(e.getCategory())
                .eventDate(e.getEventDate())
                .location(e.getLocation())
                .price(e.getPrice())
                .maxSeats(e.getMaxSeats())
                .lotteryDeadline(e.getLotteryDeadline())
                .status(e.getStatus())
                .createdBy(e.getCreatedBy() != null ? e.getCreatedBy().getId() : null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
