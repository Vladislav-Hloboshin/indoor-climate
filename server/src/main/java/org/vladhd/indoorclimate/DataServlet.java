package org.vladhd.indoorclimate;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import static com.googlecode.objectify.ObjectifyService.factory;
import static com.googlecode.objectify.ObjectifyService.ofy;

public class DataServlet extends HttpServlet {

    public void init(){
        JodaTimeTranslators.add(factory());
        factory().register(ClimateData.class);
    }

    @Override
    public void doGet(HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
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
                response = MemcacheServiceFactory.getMemcacheService().get("LastClimateData_" + code);
                break;
            case "last":
                response = ofy().load()
                        .type(PackedClimateData.class)
                        .filter("code", code)
                        .order("-date")
                        .limit(100)
                        .list();
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
                        .limit(300)
                        .list();
                break;
            }
        resp.getWriter().println(gson.toJson(response));
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{

        String code = req.getParameter("code");
        int co2 = Integer.parseInt(req.getParameter("co2"));
        double temp = Double.parseDouble(req.getParameter("temp"));

        DateTime now = DateTime.now(DateTimeZone.UTC);
        DateTime roundedNow = now.minuteOfHour().roundFloorCopy().minusMinutes(now.getMinuteOfHour() % 5);

        ClimateData climateData = new ClimateData(now, co2, temp, 0);

        MemcacheServiceFactory.getMemcacheService().put("LastClimateData_" + code, climateData);

        PackedClimateData packedClimateData = (PackedClimateData)MemcacheServiceFactory
                .getMemcacheService()
                .get("LastPackedClimateData_" + code);

        if(packedClimateData==null){
            packedClimateData = new PackedClimateData(code, roundedNow);
        } else if(packedClimateData.date == null){
            ofy().save().entity(packedClimateData).now();
            packedClimateData = new PackedClimateData(code, roundedNow);
        }
        packedClimateData.data.add(climateData);

        MemcacheServiceFactory.getMemcacheService().put("LastPackedClimateData_" + code, packedClimateData);

        resp.setContentType("text/plain");
        resp.getWriter().println("ok");
    }
}
