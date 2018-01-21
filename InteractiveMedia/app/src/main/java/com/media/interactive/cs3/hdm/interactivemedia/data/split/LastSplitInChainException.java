package com.media.interactive.cs3.hdm.interactivemedia.data.split;


class LastSplitInChainException extends RuntimeException {
    public LastSplitInChainException() {
        super("Check access to next with hasNext() before calling this method!");
    }
}
