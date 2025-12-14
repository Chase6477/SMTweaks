package de.jr.smtweaks.util;

import de.jr.smtweaks.widgets.calendar.TableItem;

public interface JsonParser {
    TableItem[] jsonToTableItemList(String json);
    String tableItemListToJson(TableItem[] items);
}
