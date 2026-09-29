package com.company.booking.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDateTime;
import java.util.UUID;

@JmixEntity
@Entity
@Table(name = "BOOKING", indexes = {
        @Index(name = "IDX_BOOKING_USER", columnList = "USER_ID"),
        @Index(name = "IDX_BOOKING_ROOM_START", columnList = "ROOM_ID, START_AT"),
        @Index(name = "IDX_BOOKING_DESK_START", columnList = "DESK_ID, START_AT")
})
public class Booking {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(name = "ROOM_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Room room;

    @JoinColumn(name = "DESK_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Desk desk;

    @FutureOrPresent
    @Column(name = "START_AT", nullable = false)
    private LocalDateTime startAt;

    @FutureOrPresent
    @Column(name = "END_AT", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status = BookingStatus.CONFIRMED.getId();

    @Column(name = "PURPOSE")
    private String purpose;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Desk getDesk() {
        return desk;
    }

    public void setDesk(Desk desk) {
        this.desk = desk;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public BookingStatus getStatus() {
        return status == null ? null : BookingStatus.fromId(status);
    }

    public void setStatus(BookingStatus status) {
        this.status = status == null ? null : status.getId();
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
