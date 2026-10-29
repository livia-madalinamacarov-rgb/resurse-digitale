package com.universitate.resurse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_resources")
public class StudentResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    private Long courseId;

    private int remainingTokens;
    private int remainingSubscriptions;
    private boolean subscriptionValidated = false;

    private int allocatedTokens;
    private int allocatedSubscriptions;
    private int usedSubscriptions;

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

    public int getRemainingTokens() {
        return remainingTokens;
    }

    public void setRemainingTokens(int remainingTokens) {
        this.remainingTokens = remainingTokens;
    }

    public int getRemainingSubscriptions() {
        return remainingSubscriptions;
    }

    public void setRemainingSubscriptions(int remainingSubscriptions) {
        this.remainingSubscriptions = remainingSubscriptions;
    }

    public boolean isSubscriptionValidated() {
        return subscriptionValidated;
    }

    public void setSubscriptionValidated(boolean subscriptionValidated) {
        this.subscriptionValidated = subscriptionValidated;
    }

    public int getAllocatedTokens() {
        return allocatedTokens;
    }

    public void setAllocatedTokens(int allocatedTokens) {
        this.allocatedTokens = allocatedTokens;
    }

    public int getAllocatedSubscriptions() {
        return allocatedSubscriptions;
    }

    public void setAllocatedSubscriptions(int allocatedSubscriptions) {
        this.allocatedSubscriptions = allocatedSubscriptions;
    }

    public int getUsedSubscriptions() {
        return usedSubscriptions;
    }

    public void setUsedSubscriptions(int usedSubscriptions) {
        this.usedSubscriptions = usedSubscriptions;
    }


}