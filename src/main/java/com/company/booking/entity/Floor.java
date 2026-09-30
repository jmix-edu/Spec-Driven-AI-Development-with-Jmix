package com.company.booking.entity;

import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.DeletePolicy;
import io.jmix.core.FileRef;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@JmixEntity
@Entity
@Table(name = "FLOOR_", indexes = {
        @Index(name = "IDX_FLOOR__BUILDING", columnList = "BUILDING_ID")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_FLOOR_BUILDING_NUMBER", columnNames = {"BUILDING_ID", "NUMBER"})
})
public class Floor {

    @Id
    @Column(name = "ID", nullable = false)
    @JmixGeneratedValue
    private UUID id;

    @Version
    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "BUILDING_ID", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Building building;

    @Column(name = "NUMBER", nullable = false)
    private Integer number;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "PLAN", length = 1024)
    private FileRef plan;

    @OnDelete(DeletePolicy.CASCADE)
    @Composition
    @OneToMany(mappedBy = "floor")
    private List<Room> rooms;

    @OnDelete(DeletePolicy.CASCADE)
    @Composition
    @OneToMany(mappedBy = "floor")
    private List<Desk> desks;

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

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileRef getPlan() {
        return plan;
    }

    public void setPlan(FileRef plan) {
        this.plan = plan;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Desk> getDesks() {
        return desks;
    }

    public void setDesks(List<Desk> desks) {
        this.desks = desks;
    }

    @InstanceName
    @DependsOnProperties({"building", "number"})
    public String getDisplayName() {
        String buildingName = building != null ? building.getName() : "";
        return String.format("%s, floor %s", buildingName, number != null ? number : "").trim();
    }
}
