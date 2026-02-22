package com.eventchain.service;

import com.eventchain.entity.Application;
import com.eventchain.entity.Event;
import com.eventchain.exception.BadRequestException;
import com.eventchain.exception.NotFoundException;
import com.eventchain.repository.ApplicationRepository;
import com.eventchain.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class LotteryService {
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final UserService userService;

    @Transactional
    public void triggerLottery(Long eventId, Authentication auth) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (!"OPEN".equals(event.getStatus())) {
            throw new BadRequestException("Lottery can only be triggered for OPEN events");
        }
        if (LocalDateTime.now().isBefore(event.getLotteryDeadline())) {
            throw new BadRequestException("Lottery deadline has not passed");
        }

        List<Application> applicants = applicationRepository.findApplicantsForLottery(eventId);
        int maxSeats = event.getMaxSeats();

        Collections.shuffle(applicants, ThreadLocalRandom.current());

        for (int i = 0; i < applicants.size(); i++) {
            Application app = applicants.get(i);
            if (i < maxSeats) {
                app.setStatus("SELECTED");
                app.setApplicationOrder(i + 1);
                app.setLotteryRound(1);
            } else {
                app.setStatus("WAITLISTED");
                app.setApplicationOrder(i + 1);
                app.setLotteryRound(1);
            }
            applicationRepository.save(app);
        }

        event.setStatus("LOTTERY_DONE");
        eventRepository.save(event);
    }
}
