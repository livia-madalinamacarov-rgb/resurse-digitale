package com.universitate.resurse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "professor_resources")
public class ProfessorResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long professorId;
    private int tokens;

    public Long getId() { return id; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }

    public int getTokens() { return tokens; }
    public void setTokens(int tokens) { this.tokens = tokens; }
}