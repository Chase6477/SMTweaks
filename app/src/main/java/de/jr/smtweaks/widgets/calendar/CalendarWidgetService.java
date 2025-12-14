package de.jr.smtweaks.widgets.calendar;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class CalendarWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CalendarWidgetRemoteViews(this.getApplicationContext());
    }
}
