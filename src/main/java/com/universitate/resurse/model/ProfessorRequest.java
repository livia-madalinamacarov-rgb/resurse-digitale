package com.universitate.resurse.model;

import jakarta.persistence.*;

@Entity
@Table(name = "professor_requests")
public class ProfessorRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long professorId;
    private int tokensRequested;
    private boolean approved = false;

    public Long getId() { return id; }

    public Long getProfessorId() { return professorId; }
    public void setProfessorId(Long professorId) { this.professorId = professorId; }

    public int getTokensRequested() { return tokensRequested; }
    public void setTokensRequested(int tokensRequested) { this.tokensRequested = tokensRequested; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
}