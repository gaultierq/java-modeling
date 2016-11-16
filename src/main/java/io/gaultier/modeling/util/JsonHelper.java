package io.gaultier.modeling.util;

import java.text.*;
import java.util.*;

import org.json.*;

import io.gaultier.modeling.model.data.DataDefinition;
import io.gaultier.modeling.model.data.DataList;
import io.gaultier.modeling.model.data.FieldDefinition;
import io.gaultier.modeling.model.data.FieldType;
import io.gaultier.modeling.model.data.ModelData;

public class JsonHelper {

    public static <D extends ModelData<D>> String dataToJson(D data) {
        return dataToJsonOnly(data, data.getDefinition().getFields());
    }

    public static <D extends ModelData<D>, T> String dataToJsonExcept(D data, Collection<FieldDefinition<T, D>> except) {
        List<FieldDefinition<?, D>> fields = new ArrayList<FieldDefinition<?, D>>(data.getDefinition().getFields());
        if (except != null) {
            fields.removeAll(except);
        }
        return dataToJsonOnly(data, fields);
    }

    public static <D extends ModelData<D>, T> String dataToJsonOnly(D data, Collection<FieldDefinition<?, D>> fields) {
        return new JsonDataParser().dataToJsonOnly(data, fields);
    }

    public static <L extends ModelData<L>> JSONArray dataListToJsonArray(DataList<L> l) {
        String jsons = dataListToJson(l);
        try {
            return new org.json.JSONArray(jsons);
        }
        catch (JSONException e) {
            return null;
        }
    }

    public static <L extends ModelData<L>> String dataListToJson(DataList<L> l) {
        return new JsonDataParser().dataListToJson(l);
    }


    public static <T extends ModelData<T>> T dataFromJson(String input, DataDefinition<T> definition) throws JSONException {
        if (input == null || input.isEmpty()) {
            return null;
        }
        JSONObject obj = new JSONObject(input);
        return dataFromJson(obj, definition);
    }

    public static <T extends ModelData<T>> T dataFromJson(JSONObject input, DataDefinition<T> definition) throws JSONException {
        return new JsonDataParser().dataFromJson(input, definition);
    }

    @SuppressWarnings("rawtypes")
    public static <T extends ModelData<T>> Object fieldValueFromString(FieldDefinition<?, T> f, String v) {
        Object value = null;
        FieldType type = f.getType();
        if (type.isEnum()) {
            for (Enum e : f.getType().getEnumType().getEnumConstants()) {
                if (e.name().equals(v)) {
                    value = e;
                    break;
                }
            }
        }
        else {
            assert type.getType() != null : f;
            value = type.getType().fromString(v + "", new SimpleDateFormat("yyyy-MM-dd"));
        }
        return value;
    }

    public static <L extends ModelData<L>> DataList<L> dataListFromJsonSafe(String input, DataDefinition<L> dataDefinition) {
        try {
            return dataListFromJson(input, dataDefinition);
        }
        catch (JSONException e) {
            //Log.severe("Error while json deserializing for " + dataDefinition, e);
            return null;
        }
    }

    public static <L extends ModelData<L>> DataList<L> dataListFromJson(String input, DataDefinition<L> dataDefinition) throws JSONException {
        if (input == null || input.isEmpty()) {
            return null;
        }
        JSONArray arr = new JSONArray(input);
        return dataListFromJson(arr, dataDefinition);
    }

    public static <L extends ModelData<L>> DataList<L> dataListFromJson(JSONArray arr, DataDefinition<L> dataDefinition) throws JSONException {
        int l = arr.length();
        DataList<L> list = dataDefinition.createList();
        for (int i = 0; i < l; i++) {
            list.add(dataFromJson(arr.getJSONObject(i), dataDefinition));
        }
        return list;
    }

}
