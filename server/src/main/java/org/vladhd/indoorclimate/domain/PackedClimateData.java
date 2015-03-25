package org.vladhd.indoorclimate.domain;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hloboschin on 20.03.2015.
 */
@Entity
public class PackedClimateData implements Serializable {

    @Id
    public Long id;

    @Index
    public String code;

    @Index
    public DateTime date;

    public List<ClimateData> data;

    private PackedClimateData(){}

    public PackedClimateData(String code, DateTime date){
        this.code = code;
        this.date = date;
        this.data = new ArrayList<>();
    }
}
