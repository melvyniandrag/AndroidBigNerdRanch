package com.ballofknives.criminalintent;

import android.content.Context;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

public class CrimeLab {
    private static CrimeLab sCrimeLab;
    private Context mAppContext;
    private ArrayList<Crime> mCrimes;

    // common for Android classes to need a Context parameter
    // for singletons so they can start activities, access project
    // resources, find apps private storage, etc.
    private CrimeLab(Context appContext){
        mAppContext = appContext;
        mCrimes = new ArrayList<>();
        for( int i = 0; i < 100; ++i){
            Crime c = new Crime();
            c.setTitle("Crime #" + i);
            c.setSolved(i%2 == 0);
            mCrimes.add(c);
        }
    }

    public ArrayList<Crime> getCrimes(){
        return mCrimes;
    }

    public Crime getCrime(UUID id){
        for(Crime c : mCrimes ){
            if( c.getId().equals(id))
                return c;
        }
        return null;
    }

    public static CrimeLab get(Context c){
        if(sCrimeLab == null){
            // whenever you have an app-wide singleton, you need
            // to get the ApplicationContext, because other contexts
            // may be transient and may not be there later.
            // The applicationContext will last for the lifetime of the application.
            sCrimeLab = new CrimeLab(c.getApplicationContext());
        }
        return sCrimeLab;
    }
}
