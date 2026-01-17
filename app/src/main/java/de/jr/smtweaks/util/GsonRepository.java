package de.jr.smtweaks.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;

public class GsonRepository implements JsonInterface {
    private final Gson gson = new Gson();

    @Override
    public TableItem[] schulmanagerFormatToTableItemList(String json) {

        JsonArray lessons = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray("data");

        List<TableItem> tableItemList = new ArrayList<>();

        for (int i = 0; i < lessons.size(); i++) {
            JsonObject object = lessons.get(i).getAsJsonObject();

            String leftTop;
            String rightTop = null;
            String bottom = null;
            int col;
            int row;
            String rightTopAlternate = null;
            String bottomAlternate = null;
            boolean isCancelled = false;

//            Es muss auf Ausfallende Stunde muss gewartet werden!
//
//            if (object.has("isSubstitution")) {
//                isCancelled = object.getAsJsonPrimitive("isSubstitution").getAsBoolean();
//            }

            if (object.has("originalLessons")) {
                JsonObject originalLesson = object.getAsJsonArray("originalLessons").get(0).getAsJsonObject();
                rightTop = originalLesson.getAsJsonArray("teachers").get(0).getAsJsonObject().getAsJsonPrimitive("abbreviation").getAsString();
                bottom = originalLesson.getAsJsonObject("room").getAsJsonObject().getAsJsonPrimitive("name").getAsString();
            }

            if (object.has("actualLesson")) {
                JsonObject actualLesson = object.getAsJsonObject("actualLesson");
                leftTop = actualLesson.getAsJsonPrimitive("subjectLabel").getAsString();
                row = object.getAsJsonObject("classHour").getAsJsonPrimitive("number").getAsInt();
                col = LocalDate.parse(object.getAsJsonPrimitive("date").getAsString()).getDayOfWeek().getValue();
                JsonArray teachers = actualLesson.getAsJsonArray("teachers");
                if (!teachers.isEmpty())
                    rightTopAlternate = teachers.get(0).getAsJsonObject().getAsJsonPrimitive("abbreviation").getAsString();
                if (!actualLesson.get("room").isJsonNull())
                    bottomAlternate = actualLesson.getAsJsonObject("room").getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                if (rightTop == null) {
                    rightTop = rightTopAlternate;
                    bottom = bottomAlternate;
                    rightTopAlternate = null;
                    bottomAlternate = null;
                }

                if (leftTop != null && rightTop != null && bottom != null)
                    tableItemList.add(new TableItem(leftTop, rightTop, rightTopAlternate, bottom, bottomAlternate, isCancelled, row, col));
            }
        }
        TableItem[] tableItemArray = new TableItem[tableItemList.size()];
        for (int i = 0; i < tableItemList.size(); i++) {
            tableItemArray[i] = tableItemList.get(i);
        }
        return tableItemArray;
    }

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
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String startDate = jsonObject.get("starts_on").getAsString();
            String endDate = jsonObject.get("ends_on").getAsString();
            items[i] = new HolidayItem(startDate, endDate);
        }


        return items;
    }

    @Override
    public String getToken(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return root.getAsJsonPrimitive("jwt").getAsString();
    }

    @Override
    public String getStudent(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return root.getAsJsonObject("user").getAsJsonObject("associatedStudent").toString();
    }


}
