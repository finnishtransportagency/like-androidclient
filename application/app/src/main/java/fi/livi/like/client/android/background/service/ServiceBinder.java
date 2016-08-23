package fi.livi.like.client.android.background.service;

import android.os.Binder;

import fi.livi.like.client.android.background.service.BackgroundService;

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
