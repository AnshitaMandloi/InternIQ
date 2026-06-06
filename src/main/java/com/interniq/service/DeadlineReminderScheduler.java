package com.interniq.service;

import com.interniq.dao.ApplicationRepository;
import com.interniq.dao.DeadlineReminderRepository;
import com.interniq.entity.Application;
import com.interniq.entity.DeadlineReminder;
import com.interniq.entity.InternshipListing;
import com.interniq.entity.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Runs every hour and sends reminder emails for applications whose deadline
 * is approaching in ~48 hours or ~24 hours.
 *
 * Idempotency: the deadline_reminders table has a UNIQUE constraint on
 * (application_id, reminder_type), so even if the scheduler fires multiple
 * times inside the same window it will never send a duplicate email.
 */
@Service
public class DeadlineReminderScheduler {

    private static final String REMINDER_48H = "48H";
    private static final String REMINDER_24H = "24H";

    // How many minutes either side of the target window we scan.
    // The cron runs every hour, so ±30 min gives us a 1-hour scan window.
    private static final long WINDOW_MINUTES = 30;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final ApplicationRepository applicationRepository;
    private final DeadlineReminderRepository reminderRepository;
    private final JavaMailSender mailSender;

    public DeadlineReminderScheduler(ApplicationRepository applicationRepository,
                                     DeadlineReminderRepository reminderRepository,
                                     JavaMailSender mailSender) {
        this.applicationRepository = applicationRepository;
        this.reminderRepository = reminderRepository;
        this.mailSender = mailSender;
    }

    // ── 48-hour reminder — runs every hour at :00 ──────────────────────────

  @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void send48HourReminders() {
	  LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusHours(48);

        List<Application> upcoming = applicationRepository.findApplicationsWithDeadlineBetween(
                target.minusMinutes(WINDOW_MINUTES),
                target.plusMinutes(WINDOW_MINUTES)
        );

        System.out.println("[DeadlineScheduler] 48H check — found " + upcoming.size() + " application(s) in window");

        for (Application app : upcoming) {
            if (!reminderRepository.existsByApplicationIdAndReminderType(app.getId(), REMINDER_48H)) {
                sendReminderEmail(app, 48);
                saveReminderRecord(app, REMINDER_48H);
            }
        }
    }

    // ── 24-hour reminder — runs every hour at :00 ──────────────────────────

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void send24HourReminders() {
    	LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now.plusHours(24);

        List<Application> upcoming = applicationRepository.findApplicationsWithDeadlineBetween(
                target.minusMinutes(WINDOW_MINUTES),
                target.plusMinutes(WINDOW_MINUTES)
        );

        System.out.println("[DeadlineScheduler] 24H check — found " + upcoming.size() + " application(s) in window");

        for (Application app : upcoming) {
            if (!reminderRepository.existsByApplicationIdAndReminderType(app.getId(), REMINDER_24H)) {
                sendReminderEmail(app, 24);
                saveReminderRecord(app, REMINDER_24H);
            }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void sendReminderEmail(Application app, int hoursLeft) {
        User user = app.getUser();
        InternshipListing listing = app.getInternshipListing();

        String role    = listing != null ? listing.getTitle()   : "your tracked internship";
        String company = listing != null ? listing.getCompany() : "the company";
        String deadline = app.getDeadline() != null
                ? app.getDeadline().format(DISPLAY_FMT)
                : "soon";

        String subject = "⏰ InternIQ: " + hoursLeft + "h left to apply — " + role + " at " + company;

        String body = String.join("\n",
                "Hi " + user.getName() + ",",
                "",
                "This is a friendly reminder from InternIQ.",
                "",
                "Your application deadline for the role below is in approximately " + hoursLeft + " hours:",
                "",
                "  Role    : " + role,
                "  Company : " + company,
                "  Deadline: " + deadline,
                "  Status  : " + app.getStatus(),
                "",
                (listing != null && listing.getApplyUrl() != null
                        ? "Apply here: " + listing.getApplyUrl()
                        : ""),
                "",
                "Good luck! 🚀",
                "",
                "– The InternIQ Team"
                //"https://interniq.app"
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("[DeadlineScheduler] Sent " + hoursLeft + "H reminder to " + user.getEmail()
                    + " for application id=" + app.getId());
        } catch (Exception e) {
            // Log and continue — one failed email must not roll back the whole batch
            System.out.println("[DeadlineScheduler] ERROR sending email to " + user.getEmail()
                    + ": " + e.getMessage());
        }
    }

    private void saveReminderRecord(Application app, String type) {
        DeadlineReminder record = new DeadlineReminder();
        record.setApplication(app);
        record.setReminderType(type);
        record.setSentAt(LocalDateTime.now());
        reminderRepository.save(record);
    }
}