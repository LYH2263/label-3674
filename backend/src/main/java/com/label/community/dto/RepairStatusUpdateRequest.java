package com.label.community.dto;

public class RepairStatusUpdateRequest {
    private String status;
    private Long assignedProviderId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAssignedProviderId() {
        return assignedProviderId;
    }

    public void setAssignedProviderId(Long assignedProviderId) {
        this.assignedProviderId = assignedProviderId;
    }
}
