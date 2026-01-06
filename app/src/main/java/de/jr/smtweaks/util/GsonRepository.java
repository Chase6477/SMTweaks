package de.jr.smtweaks.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;

public class GsonRepository implements JsonInterface {
    private final Gson gson = new Gson();
    @Override
    public TableItem[] jsonToTableItemList(String json) {
        return gson.fromJson(json, TableItem[].class);
    }
    @Override
    public String tableItemListToJson(TableItem[] items) {
        return gson.toJson(items);
    }

    @Override
    public String holidayItemListToJson(HolidayItem[] items) {
        return gson.toJson(items);
    }

    @Override
    public HolidayItem[] jsonTHolidayItemList(String json) {
        return gson.fromJson(json, HolidayItem[].class);
    }

    @Override
    public HolidayItem[] getCroppedHolidayList(String holidays) {

        JsonObject root = JsonParser.parseString(holidays).getAsJsonObject();
        JsonArray jsonArray = root.getAsJsonArray("data");

        HolidayItem[] items = new HolidayItem[jsonArray.size()];

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject =  jsonArray.get(i).getAsJsonObject();
            String startDate = jsonObject.get("starts_on").getAsString();
            String endDate = jsonObject.get("ends_on").getAsString();
            items[i] = new HolidayItem(startDate, endDate);
        }


        return items;
    }


}
