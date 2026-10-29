package com.universitate.resurse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private int maxStudents;

    private Long professorId; // legătura cu profesorul

    private int tokensPerStudent;
    private int subscriptionsPerStudent;



    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
    }


    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public int getTokensPerStudent() {
        return tokensPerStudent;
    }

    public void setTokensPerStudent(int tokensPerStudent) {
        this.tokensPerStudent = tokensPerStudent;
    }

    public int getSubscriptionsPerStudent() {
        return subscriptionsPerStudent;
    }

    public void setSubscriptionsPerStudent(int subscriptionsPerStudent) {
        this.subscriptionsPerStudent = subscriptionsPerStudent;
    }
}