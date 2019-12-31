package com.ballofknives.criminalintent;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.UUID;

public class CrimeLab {
    private static final String TAG = "Crime Lab";
    private static final String FILENAME = "crimes.json";

    private CriminalIntentJSONSerializer mSerializer;

    private static CrimeLab sCrimeLab;
    private Context mAppContext;
    private ArrayList<Crime> mCrimes;

    // common for Android classes to need a Context parameter
    // for singletons so they can start activities, access project
    // resources, find apps private storage, etc.
    private CrimeLab(Context appContext){
        mAppContext = appContext;

        mSerializer = new CriminalIntentJSONSerializer(mAppContext, FILENAME);
        try{
            mCrimes = mSerializer.loadCrimes();
        }
        catch( Exception e){
            mCrimes = new ArrayList<Crime>();
            Log.e(TAG, "Error loading crimes: ", e);
        }
    }

    public void addCrime(Crime c){
        mCrimes.add(c);
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

    public boolean saveCrimes(){
        try{
            mSerializer.saveCrimes(mCrimes);
            Log.d(TAG, "crimes saved to file");
            return true;
        } catch ( Exception e){
            Log.e(TAG, "Error saving crimes: ", e);
            return false;
        }
    }

    public void deleteCrime(Crime c){
        mCrimes.remove(c);
        saveCrimes(); // I added this. Seems android can close an app without going through onPause() so the crimes weren't being saved after having been deleted.
    }

}
