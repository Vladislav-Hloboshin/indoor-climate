package org.vladhd.indoorclimate.domain;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Vladislav on 12.02.2015.
 */
@Entity
public class ActualData implements Serializable {
    @Id
    public String code;

    public DateTime date;
    public int co2;
    public double temp;

    private ActualData() {}

    public ActualData(String code, DateTime date, int co2, double temp){
        this.code = code;
        this.date = date;
        this.co2 = co2;
        this.temp = temp;
    }
}
