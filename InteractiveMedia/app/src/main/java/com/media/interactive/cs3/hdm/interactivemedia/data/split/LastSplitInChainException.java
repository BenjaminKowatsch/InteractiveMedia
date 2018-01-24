package com.media.interactive.cs3.hdm.interactivemedia.data.split;




/**
 * The Class LastSplitInChainException.
 */
class LastSplitInChainException extends RuntimeException {

    /**
     * Instantiates a new last split in chain exception.
     */
    public LastSplitInChainException() {
        super("Check access to next with hasNext() before calling this method!");
    }
}
