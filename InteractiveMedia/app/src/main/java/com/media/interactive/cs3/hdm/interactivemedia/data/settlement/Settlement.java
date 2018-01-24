package com.media.interactive.cs3.hdm.interactivemedia.data.settlement;


import com.media.interactive.cs3.hdm.interactivemedia.data.Debt;

import java.util.List;

/**
 * Describes the method to resolve the debts from multiple transactions into a set of payments,
 * where each recipient/sender combination may only appear once.
 * Does not define how the settlement is done
 */
public interface Settlement {

    /**
     * Method to resolve the debts from multiple transactions into a set of payments.
     *
     * @param debts to resolve
     * @return payments where each recipient/sender combination may only appear once
     */
    List<Payment> settle(List<Debt> debts);
}
