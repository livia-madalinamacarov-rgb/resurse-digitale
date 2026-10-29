package com.universitate.resurse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resource_requests")
public class ResourceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;
    private boolean approved = false;
    private int requestedTokens;
    private int requestedHelpRequests;
    private boolean needsAdminApproval = false;

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public int getRequestedTokens() {
        return requestedTokens;
    }

    public void setRequestedTokens(int requestedTokens) {
        this.requestedTokens = requestedTokens;
    }

    public int getRequestedHelpRequests() {
        return requestedHelpRequests;
    }

    public void setRequestedHelpRequests(int requestedHelpRequests) {
        this.requestedHelpRequests = requestedHelpRequests;
    }
    public boolean isNeedsAdminApproval() {
        return needsAdminApproval;
    }

    public void setNeedsAdminApproval(boolean needsAdminApproval) {
        this.needsAdminApproval = needsAdminApproval;
    }
}