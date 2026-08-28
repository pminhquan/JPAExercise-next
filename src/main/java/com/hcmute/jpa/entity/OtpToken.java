package com.hcmute.jpa.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "otp_tokens")
@NamedQueries({
    @NamedQuery(
        name = "OtpToken.findLatestValid",
        query = "SELECT t FROM OtpToken t WHERE t.user = :user AND t.purpose = :purpose AND t.used = false AND t.expiresAt > :now AND t.attempts < 5 ORDER BY t.createdAt DESC"
    )
})
public class OtpToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "Purpose", nullable = false, length = 50)
    private OtpPurpose purpose;

    @Column(name = "CodeHash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "ExpiresAt", nullable = false)
    private Timestamp expiresAt;

    @Column(name = "Attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "Used", nullable = false)
    private boolean used = false;

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = new Timestamp(System.currentTimeMillis());
        }
    }

    public OtpToken() {
    }

    public OtpToken(User user, OtpPurpose purpose, String codeHash, Timestamp expiresAt) {
        this.user = user;
        this.purpose = purpose;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.attempts = 0;
        this.used = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OtpPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(OtpPurpose purpose) {
        this.purpose = purpose;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
