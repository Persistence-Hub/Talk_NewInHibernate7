package com.thorben.janssen.envers;

import org.hibernate.envers.RevisionListener;

public class MyRevisionListener implements RevisionListener {

    @Override
    public void newRevision(Object revision) {
        ((MyRevision) revision).setUser("Current User");
    }
}
