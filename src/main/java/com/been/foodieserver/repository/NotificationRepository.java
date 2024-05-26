package com.been.foodieserver.repository;

import com.been.foodieserver.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"receiver"})
    Page<Notification> findAllWithReceiverByReceiver_LoginId(Pageable pageable, String loginId);
}
