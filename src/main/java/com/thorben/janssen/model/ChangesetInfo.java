package com.thorben.janssen.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Changelog;

@Changelog
@Entity
@Table(name = "REVINFO")
public class ChangesetInfo {
    @Id
    @GeneratedValue
    @Changelog.ChangesetId
    @Column(name = "REV")
    int id;

    @Changelog.Timestamp
    @Column(name = "REVTSTMP")
    long timestamp;

//    String userName;
}