package com.example.bookapp.models.firebase;

import com.google.firebase.database.FirebaseDatabase;

public class Firebase {
    private static FirebaseDatabase firebaseDatabase;

    private Firebase(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseDatabase.setPersistenceEnabled(true);
    }

    public static FirebaseDatabase getInstance(){
        if(firebaseDatabase == null)
            new Firebase();
        return firebaseDatabase;
    }
}
