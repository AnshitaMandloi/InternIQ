package com.interniq.dao;

import com.interniq.entity.DeadlineReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeadlineReminderRepository extends JpaRepository<DeadlineReminder, Long> {

    // True if we already sent a reminder of this type for this application
    boolean existsByApplicationIdAndReminderType(Long applicationId, String reminderType);
}