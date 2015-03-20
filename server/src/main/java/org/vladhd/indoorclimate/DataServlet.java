package org.vladhd.indoorclimate;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.vladhd.indoorclimate.domain.ClimateData;

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
                        .type(ClimateData.class)
                        .filter("code", code)
                        .order("-date")
                        .limit(250)
                        .list();
                break;
            case "history":
                DateTime from = DateTime.parse(req.getParameter("from"));
                DateTime to = DateTime.parse(req.getParameter("to"));
                response = ofy().load()
                        .type(ClimateData.class)
                        .filter("code", code)
                        .filter("date >", from)
                        .filter("date <", to)
                        .order("-date")
                        .limit(500)
                        .list();
                break;
            }
        resp.getWriter().println(gson.toJson(response));
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{

        final String code = req.getParameter("code");
        final int co2 = Integer.parseInt(req.getParameter("co2"));
        final double temp = Double.parseDouble(req.getParameter("temp"));

        ClimateData climateData = new ClimateData(code, DateTime.now(DateTimeZone.UTC), co2, temp);
        ofy().save().entity(climateData).now();

        MemcacheServiceFactory.getMemcacheService().put("LastClimateData_" + code, climateData);

        resp.setContentType("text/plain");
        resp.getWriter().println("ok");
    }
}
