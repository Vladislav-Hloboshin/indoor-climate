package org.vladhd.indoorclimate.domain;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Vladislav on 12.02.2015.
 */

public class ClimateData implements Serializable {

    public DateTime date;

    public int co2;

    public double temp;

    public int humidity;

    private ClimateData() {}

    public ClimateData(DateTime date, int co2, double temp, int humidity){
        this.date = date;
        this.co2 = co2;
        this.temp = temp;
        this.humidity = humidity;
    }
}
