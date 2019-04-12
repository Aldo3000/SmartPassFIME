package com.smartpassfime.smartpassfime;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseOffline extends android.app.Application {

    @Override
    public  void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
