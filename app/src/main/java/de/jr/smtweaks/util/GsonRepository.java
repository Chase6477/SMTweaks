package de.jr.smtweaks.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import de.jr.smtweaks.UserData;
import de.jr.smtweaks.widgets.calendar.HolidayItem;
import de.jr.smtweaks.widgets.calendar.TableItem;

public class GsonRepository implements JsonInterface {
    private final Gson gson = new Gson();

    @Override
    public UserData jsonToUserData(byte[] json) {
        if (json == null)
            return null;
        return gson.fromJson(new String(json), UserData.class);
    }

    @Override
    public byte[] userDataToJson(UserData userData) {
        return gson.toJson(userData).getBytes();
    }

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

            row = object.getAsJsonObject("classHour").getAsJsonPrimitive("number").getAsInt();
            col = LocalDate.parse(object.getAsJsonPrimitive("date").getAsString()).getDayOfWeek().getValue();

            if (object.has("isCancelled")) {
                isCancelled = object.getAsJsonPrimitive("isCancelled").getAsBoolean();
            }

            if (object.has("originalLessons")) {
                JsonObject originalLesson = object.getAsJsonArray("originalLessons").get(0).getAsJsonObject();
                rightTop = originalLesson.getAsJsonArray("teachers").get(0).getAsJsonObject().getAsJsonPrimitive("abbreviation").getAsString();
                bottom = originalLesson.getAsJsonObject("room").getAsJsonObject().getAsJsonPrimitive("name").getAsString();
                if (isCancelled) {
                    leftTop = originalLesson.getAsJsonPrimitive("subjectLabel").getAsString();
                    tableItemList.add(new TableItem(leftTop, rightTop, rightTopAlternate, bottom, bottomAlternate, isCancelled, row, col));
                }
            }

            if (object.has("actualLesson") && !isCancelled) {
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
    public String getToken(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return root.getAsJsonPrimitive("jwt").getAsString();
    }

    @Override
    public String getStudent(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        return root.getAsJsonObject("user").getAsJsonObject("associatedStudent").toString();
    }

    @Override
    public HolidayItem[] schulmanagerFormatToHolidayItem(String json) {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray dates =  root.
                getAsJsonArray("results").get(0).getAsJsonObject().
                getAsJsonArray("data");

        ArrayList<HolidayItem> items = new ArrayList<>();

        for (JsonElement date : dates) {

            items.add(new HolidayItem(
                    date.getAsJsonObject().get("start").getAsString(),
                    date.getAsJsonObject().get("end").getAsString())
            );
        }
        return items.toArray(new HolidayItem[0]);
    }

    @Override
    public String holidayItemToJson(HolidayItem[] items) {
        return gson.toJson(items);
    }

    @Override
    public HolidayItem[] jsonToHolidayItem(String json) {
        return gson.fromJson(json, HolidayItem[].class);
    }


}
