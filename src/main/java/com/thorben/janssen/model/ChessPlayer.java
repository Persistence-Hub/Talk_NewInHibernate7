package com.thorben.janssen.model;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Entity
@Cacheable
@NamedQuery(name = "findPlayersByFirstName", query = "SELECT p FROM ChessPlayer p WHERE p.firstName = :firstName")
public class ChessPlayer {
    
    @Id
    @GeneratedValue
    private Long id;

    private String firstName;

    private String lastName;

    @Enumerated(EnumType.STRING)
    private PlayerType playerType;

    @ManyToOne
    private ChessClub club;

    @Version
    private int version;

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getVersion() {
        return version;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChessClub getClub() {
        return club;
    }

    public void setClub(ChessClub club) {
        this.club = club;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }
}