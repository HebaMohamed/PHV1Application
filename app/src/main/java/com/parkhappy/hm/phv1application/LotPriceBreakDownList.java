package com.parkhappy.hm.phv1application;

/**
 * Created by dell on 12/2/2016.
 */
public class LotPriceBreakDownList {

    public double amount;
    public String amountText;
    public double duration;
    public String label;

    public LotPriceBreakDownList(double amount, String amountText, double duration, String label){
        this.amount=amount;
        this.amountText=amountText;
        this.duration=duration;
        this.label=label;
    }

}
