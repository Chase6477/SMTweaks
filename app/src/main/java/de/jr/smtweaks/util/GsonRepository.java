package de.jr.smtweaks.util;

import com.google.gson.Gson;

import de.jr.smtweaks.widgets.calendar.TableItem;

public class GsonRepository implements JsonParser{
    private final Gson gson = new Gson();
    @Override
    public TableItem[] jsonToTableItemList(String json) {
        return gson.fromJson(json, TableItem[].class);
    }
}
