package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SplitFactory {

    public static Split getSplitByName(String name) {
        switch (name.toLowerCase()) {
            case "even":
                return new EvenSplit();
            default:
                throw new UnknownSplitException("No split named " + name
                        + " known to this factory");
        }
    }

    public static Split fromJSON(JSONArray splitArray) throws JSONException {
        final Split outerSplit = toSplit(splitArray.getJSONObject(0));
        Split innermostSplit = outerSplit;
        for (int i = 1; i < splitArray.length(); i++) {
            final JSONObject splitObject = splitArray.getJSONObject(i);
            Split current = toSplit(splitObject);
            innermostSplit = innermostSplit.andThen(current);

        }
        return outerSplit;
    }

    @NonNull
    private static Split toSplit(JSONObject splitObject) throws JSONException {
        final String type = splitObject.getString("type");
        switch (type) {
            case "even":
                return new EvenSplit();
            case "constant deduction":
                final double amount = splitObject.getDouble("amount");
                final String userId = splitObject.getString("userId");
                return new ConstantDeduction(amount, userId);
            default:
                throw new UnknownSplitException("Type " + type + " is not known to this factory");
        }
    }

    public static JSONArray toArray(Split split) throws JSONException {
        JSONArray out = new JSONArray();
        Split innermost = split;
        while (innermost.hasNext()) {
            out.put(split.toJson());
            innermost = innermost.getNext();
        }
        return out;
    }

    public static Split buildSplitFromList(List<Split> splitList) {
        final Split outerSplit = splitList.get(0);
        Split innermost = outerSplit;
        for (int i = 1; i < splitList.size(); i++) {
            innermost.andThen(splitList.get(i));
        }
        return outerSplit;
    }

    private static class UnknownSplitException extends RuntimeException {
        UnknownSplitException(String s) {
            super(s);
        }
    }
}
