package com.media.interactive.cs3.hdm.interactivemedia.data.split;


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

    private static class UnknownSplitException extends RuntimeException {
        UnknownSplitException(String s) {
            super(s);
        }
    }
}
