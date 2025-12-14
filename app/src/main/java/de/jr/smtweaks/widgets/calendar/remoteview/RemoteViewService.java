package de.jr.smtweaks.widgets.calendar.remoteview;

import android.content.Intent;
import android.widget.RemoteViewsService;

import de.jr.smtweaks.widgets.calendar.RemoteViewFactory;

public class RemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewFactory(this.getApplicationContext());
    }
}
