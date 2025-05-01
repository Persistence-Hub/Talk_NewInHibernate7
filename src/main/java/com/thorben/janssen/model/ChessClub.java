package com.thorben.janssen.model;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
public class ChessClub {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @Embedded
    private Address address;

    @NotAudited
    @OneToMany(mappedBy = ChessPlayer_.CLUB)
    private List<ChessPlayer> players = new ArrayList<>();

    @Version
    private int version;


    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ChessPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<ChessPlayer> players) {
        this.players = players;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getVersion() {
        return version;
    }
}
