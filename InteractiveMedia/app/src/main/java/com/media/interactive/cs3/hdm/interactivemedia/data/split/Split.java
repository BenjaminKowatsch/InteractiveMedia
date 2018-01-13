package com.media.interactive.cs3.hdm.interactivemedia.data.split;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;

import java.util.List;

public interface Split {

    List<Debt> split(Transaction transaction);
}
