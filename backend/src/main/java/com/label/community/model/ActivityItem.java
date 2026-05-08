package com.label.community.model;

import java.time.LocalDateTime;

public class ActivityItem {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private Long organizerId;
    private String organizerRole;
    private boolean joined;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public String getOrganizerRole() {
        return organizerRole;
    }

    public void setOrganizerRole(String organizerRole) {
        this.organizerRole = organizerRole;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
}
