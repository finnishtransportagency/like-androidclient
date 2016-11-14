package fi.livi.like.client.android.backgroundservice.service;

import android.os.Binder;

/**
 * ServiceBinder links the Activity and Service objects together.
 * Replace when using own service.
 */
public class ServiceBinder extends Binder {

    private final BackgroundService backgroundService;

    public ServiceBinder(BackgroundService backgroundService) {
        this.backgroundService = backgroundService;
    }

    public BackgroundService getService() {
        return backgroundService;
    }
}
