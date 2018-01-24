package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;



/**
 * A factory for creating Split objects.
 */
public class SplitFactory {

    /**
     * Gets the split by name.
     *
     * @param name the name
     * @return the split by name
     */
    public static Split getSplitByName(String name) {
        switch (name.toLowerCase()) {
            case "even":
                return new EvenSplit();
            default:
                throw new UnknownSplitException("No split named " + name
                    + " known to this factory");
        }
    }

    /**
     * From JSON.
     *
     * @param splitArray the split array
     * @return the split
     * @throws JSONException the JSON exception
     */
    public static Split fromJson(JSONArray splitArray) throws JSONException {
        final Split outerSplit = toSplit(splitArray.getJSONObject(0));
        Split innermostSplit = outerSplit;
        for (int i = 1; i < splitArray.length(); i++) {
            final JSONObject splitObject = splitArray.getJSONObject(i);
            Split current = toSplit(splitObject);
            innermostSplit = innermostSplit.andThen(current);

        }
        return outerSplit;
    }

    /**
     * To split.
     *
     * @param splitObject the split object
     * @return the split
     * @throws JSONException the JSON exception
     */
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

    /**
     * To JSON array.
     *
     * @param split the split
     * @return the JSON array
     * @throws JSONException the JSON exception
     */
    public static JSONArray toJsonArray(Split split) throws JSONException {
        final JSONArray out = new JSONArray();
        Split innermost = split;
        out.put(innermost.toJson());
        while (innermost.hasNext()) {
            innermost = innermost.getNext();
            out.put(innermost.toJson());
        }
        return out;
    }

    /**
     * Builds the split from list.
     *
     * @param splitList the split list
     * @return the split
     */
    public static Split buildSplitFromList(List<Split> splitList) {
        final Split outerSplit = splitList.get(0);
        Split innermost = outerSplit;
        for (int i = 1; i < splitList.size(); i++) {
            innermost = innermost.andThen(splitList.get(i));
        }
        return outerSplit;
    }

    /**
     * The Class UnknownSplitException.
     */
    private static class UnknownSplitException extends RuntimeException {

        /**
         * Instantiates a new unknown split exception.
         *
         * @param s the s
         */
        UnknownSplitException(String s) {
            super(s);
        }
    }
}
