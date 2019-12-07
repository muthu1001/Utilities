package com.thanthi.dtnext.dtnextapplication;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.comscore.Analytics;
import com.comscore.PublisherConfiguration;
import com.comscore.UsagePropertiesAutoUpdateMode;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class DtNextApplication extends MultiDexApplication {

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        //Comscore Analytics
        HashMap<String, String> labels = new HashMap<>();
        labels.put("cs_ucfr", ""); //this is for GDPR empty string is default value
        labels.put("ns_ap_an", "DTNEXTNews");

        PublisherConfiguration publisherConfiguration = new PublisherConfiguration.Builder()
                .publisherId(getString(R.string.publisherId))
                .persistentLabels(labels)
                .build();

        Analytics.getConfiguration().addClient(publisherConfiguration);
        Analytics.getConfiguration().setApplicationName("DTNEXT");
        Analytics.getConfiguration().setUsagePropertiesAutoUpdateMode(UsagePropertiesAutoUpdateMode.FOREGROUND_ONLY);
        Analytics.getConfiguration().enableImplementationValidationMode();
        Analytics.start(this);
    }

    private static final String PROPERTY_ID = "UA-69390380-2";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.ecommerce_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

}
