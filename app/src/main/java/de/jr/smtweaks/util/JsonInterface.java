package de.jr.smtweaks.util;

import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;

public interface JsonInterface {
    TableItem[] jsonToTableItemList(String json);

    String tableItemListToJson(TableItem[] items);

    String holidayItemListToJson(HolidayItem[] items);

    HolidayItem[] jsonTHolidayItemList(String json);

    HolidayItem[] getCroppedHolidayList(String holidays);
}
