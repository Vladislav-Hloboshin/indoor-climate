package org.vladhd.indoorclimate;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.vladhd.indoorclimate.domain.ClimateData;
import org.vladhd.indoorclimate.domain.PackedClimateData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.factory;
import static com.googlecode.objectify.ObjectifyService.ofy;

public class DataServlet extends HttpServlet {

    private static int saveDeltaInMinutes = 1;

    public void init(){
        JodaTimeTranslators.add(factory());
        factory().register(PackedClimateData.class);
    }

    @Override
    public void doGet(HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {

        MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();

        resp.setContentType("text/plain");
        final Gson gson = Converters.registerDateTime(new GsonBuilder()).create();

        final String code = req.getParameter("code");
        String method = req.getParameter("method");
        if(method==null || method.length()==0){
            method = "actual";
        }

        Object response = null;
        switch(method){
            case "actual":
                response = memcacheService.get("LastClimateData_" + code);
                break;
            case "last":
                List<Key<PackedClimateData>> keys = ofy().load()
                        .type(PackedClimateData.class)
                        .filter("code", code)
                        .order("-date")
                        .limit(144)//12 hours (saveDeltaInMinutes = 5)
                        .keys()
                        .list();
                List<Long> ids = new ArrayList<>();
                for(Key<PackedClimateData> key: keys){
                    ids.add(key.getId());
                }
                /*PackedClimateData packedClimateData = (PackedClimateData)memcacheService.get("LastPackedClimateData_" + code);
                if(packedClimateData!=null){
                    dataList.add(0, packedClimateData);
                }*/
                response = ids;
                break;
            case "history":
                DateTime from = DateTime.parse(req.getParameter("from"));
                DateTime to = DateTime.parse(req.getParameter("to"));
                response = ofy().load()
                        .type(PackedClimateData.class)
                        .filter("code", code)
                        .filter("date >", from)
                        .filter("date <", to)
                        .order("-date")
                        .limit(576)//two days (saveDeltaInMinutes = 5)
                        .list();
                break;
            case "data":
                String[] idsValues = req.getParameterValues("id");
                response = ofy().load()
                        .type(PackedClimateData.class)
                        .
                break;
            }
        resp.getWriter().println(gson.toJson(response));
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{

        MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();

        String code = req.getParameter("code");
        int co2 = 0;
        double temp = 0;
        int humidity = 0;

        try {
            co2 = Integer.parseInt(req.getParameter("co2"));
        }catch(NumberFormatException e){}
        try {
            temp = Double.parseDouble(req.getParameter("temp"));
        }catch(NumberFormatException e){}
        try{
            humidity = Integer.parseInt(req.getParameter("humidity"));
        }catch(NumberFormatException e){}

        DateTime now = DateTime.now(DateTimeZone.UTC);
        DateTime roundedNow = now.minuteOfHour().roundFloorCopy().minusMinutes(now.getMinuteOfHour() % saveDeltaInMinutes);

        ClimateData climateData = new ClimateData(now, co2, temp, humidity);

        memcacheService.put("LastClimateData_" + code, climateData);

        PackedClimateData packedClimateData = (PackedClimateData)memcacheService.get("LastPackedClimateData_" + code);

        if(packedClimateData==null){
            packedClimateData = new PackedClimateData(code, roundedNow);
        } else if(packedClimateData.date.equals(roundedNow)==false){
            ofy().save().entity(packedClimateData).now();
            packedClimateData = new PackedClimateData(code, roundedNow);
        }
        packedClimateData.data.add(climateData);

        MemcacheServiceFactory.getMemcacheService().put("LastPackedClimateData_" + code, packedClimateData);

        resp.setContentType("text/plain");
        resp.getWriter().println("ok");
    }
}
