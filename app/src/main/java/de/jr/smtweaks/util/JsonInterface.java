package de.jr.smtweaks.util;

import de.jr.smtweaks.UserData;
import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;

public interface JsonInterface {

    UserData jsonToUserData(byte[] json);

    byte[] userDataToJson(UserData userData);

    TableItem[] schulmanagerFormatToTableItemList(String json);

    TableItem[] jsonToTableItemList(String json);

    String tableItemListToJson(TableItem[] items);

    String getToken(String json);

    String getStudent(String json);

    HolidayItem[] schulmanagerFormatToHolidayItem(String json);

    String holidayItemToJson(HolidayItem[] items);

    HolidayItem[] jsonToHolidayItem(String json);
}
