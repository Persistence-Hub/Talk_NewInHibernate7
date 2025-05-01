package com.thorben.janssen.envers;

import jakarta.persistence.Entity;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionMapping;

import java.io.Serializable;

//@Entity
//@RevisionEntity(MyRevisionListener.class)
public class MyRevision
        extends RevisionMapping
//        extends DefaultRevisionEntity
{

    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
