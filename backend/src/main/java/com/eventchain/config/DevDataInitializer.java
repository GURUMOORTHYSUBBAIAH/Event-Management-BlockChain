package com.eventchain.config;

import com.eventchain.entity.*;
import com.eventchain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements org.springframework.boot.CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;
    private final CertificateRepository certificateRepository;
    private final AnnouncementRepository announcementRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Seed only once (idempotent)
        if (userRepository.findByEmail("admin@eventchain.io").isPresent()) {
            return;
        }

        // Ensure roles
        Role superAdmin = ensureRole("SUPER_ADMIN");
        Role orgAdmin = ensureRole("ORG_ADMIN");
        Role eventHead = ensureRole("EVENT_HEAD");
        Role teamMember = ensureRole("TEAM_MEMBER");
        Role userRole = ensureRole("USER");

        // Admin user
        User admin = new User();
        admin.setEmail("admin@eventchain.io");
        admin.setDisplayName("Admin");
        admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
        admin.setEnabled(true);
        admin.setRoles(Set.of(superAdmin, orgAdmin, eventHead, teamMember));
        admin = userRepository.save(admin);

        // Demo normal user
        User demoUser = new User();
        demoUser.setEmail("user@eventchain.io");
        demoUser.setDisplayName("Demo User");
        demoUser.setPasswordHash(passwordEncoder.encode("User@123"));
        demoUser.setEnabled(true);
        demoUser.setRoles(Set.of(userRole));
        demoUser = userRepository.save(demoUser);

        // Demo event
        Event event = new Event();
        event.setTitle("Demo Blockchain Meetup");
        event.setDescription("Sample event seeded in dev profile to test end-to-end flow.");
        event.setCategory("Tech");
        event.setEventDate(LocalDateTime.now().plusDays(7));
        event.setLocation("Online");
        event.setPrice(new BigDecimal("99.00"));
        event.setMaxSeats(50);
        event.setLotteryDeadline(LocalDateTime.now().plusDays(3));
        event.setStatus("OPEN");
        event.setCreatedBy(admin);
        event = eventRepository.save(event);

        // Demo application for demo user
        Application app = new Application();
        app.setUser(demoUser);
        app.setEvent(event);
        app.setStatus("PAID");
        app.setApplicationOrder(1);
        app.setLotteryRound(1);
        app = applicationRepository.save(app);

        // Demo payment linked to application
        Payment payment = new Payment();
        payment.setApplication(app);
        payment.setAmount(event.getPrice());
        payment.setCurrency("INR");
        payment.setStatus("COMPLETED");
        payment.setRazorpayOrderId("order_DEMO_1");
        payment.setRazorpayPaymentId("pay_DEMO_1");
        payment.setTransactionHash("0xDEMO_PAYMENT");
        paymentRepository.save(payment);

        // Demo ticket for demo user
        Ticket ticket = new Ticket();
        ticket.setApplication(app);
        ticket.setEvent(event);
        ticket.setUser(demoUser);
        ticket.setTokenId(1L);
        ticket.setTransactionHash("0xDEMO_TICKET");
        ticket.setCheckedIn(true);
        ticket.setCheckedInAt(LocalDateTime.now().minusDays(1));
        ticket = ticketRepository.save(ticket);

        // Demo certificate for the ticket
        Certificate cert = new Certificate();
        cert.setTicket(ticket);
        cert.setUser(demoUser);
        cert.setEvent(event);
        cert.setCertificateId("CERT-DEMO-0001");
        cert.setFileHash("demo-file-hash-0001-demo-file-hash-0001-demo-file-hash-0001");
        cert.setTransactionHash("0xDEMO_CERT");
        certificateRepository.save(cert);

        // Global announcement
        Announcement global = new Announcement();
        global.setTitle("Welcome to EventChain (Demo)");
        global.setContent("This is a demo announcement seeded for local testing.");
        global.setType("INFO");
        global.setCreatedBy(admin);
        announcementRepository.save(global);

        // Event-specific announcement
        Announcement eventAnn = new Announcement();
        eventAnn.setEvent(event);
        eventAnn.setTitle("Event Schedule Published");
        eventAnn.setContent("Check the event page for the latest agenda.");
        eventAnn.setType("EVENT");
        eventAnn.setCreatedBy(admin);
        announcementRepository.save(eventAnn);
    }

    private Role ensureRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            return roleRepository.save(r);
        });
    }
}

