package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Group;

public class SplitFactory {

    public static Split getSplitByName(String name, Group group) {
        switch (name.toLowerCase()) {
            case "even":
                return new EvenSplit(group);
            default:
                throw new UnknownSplitException("No split named " + name
                        + " known to this factory");
        }
    }

    private static class UnknownSplitException extends RuntimeException {
        UnknownSplitException(String s) {
            super(s);
        }
    }
}
