package org.vladhd.indoorclimate.domain;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Vladislav on 12.02.2015.
 */
@Entity
public class ClimateData implements Serializable {
    @Id
    public Long id;

    @Index
    public String code;

    @Index
    public DateTime date;

    int co2;
    public double temp;

    private ClimateData() {}

    public ClimateData(String code, DateTime date, int co2, double temp){
        this.code = code;
        this.date = date;
        this.co2 = co2;
        this.temp = temp;
    }
}
