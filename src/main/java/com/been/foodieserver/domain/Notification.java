package com.been.foodieserver.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString(callSuper = true)
@Table(name = "notifications")
@Entity
public class Notification extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User receiver; //알림을 수신하는 유저

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(100)")
    private NotificationType type;

    @Column(nullable = false)
    private Long fromUserId; //알림을 발생시킨 유저

    @Column(nullable = false)
    private Long targetId; //알림이 발생된 주체

    @Column(nullable = false)
    private Boolean isRead;

    protected Notification() {
    }

    private Notification(User receiver, NotificationType type, Long fromUserId, Long targetId, Boolean isRead) {
        this.receiver = receiver;
        this.type = type;
        this.fromUserId = fromUserId;
        this.targetId = targetId;
        this.isRead = isRead;
    }

    public static Notification of(User receiver, NotificationType type, Long fromUserId, Long targetId, Boolean isRead) {
        return new Notification(receiver, type, fromUserId, targetId, isRead);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Notification notification)) {
            return false;
        }

        return Objects.equals(getId(), notification.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
