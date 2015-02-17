package org.vladhd.indoorclimate;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import org.joda.time.DateTime;
import org.vladhd.indoorclimate.domain.ActualData;
import org.vladhd.indoorclimate.domain.HistoryData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.factory;
import static com.googlecode.objectify.ObjectifyService.ofy;

public class DataServlet extends HttpServlet {

    public void init(){
        JodaTimeTranslators.add(factory());
        factory().register(ActualData.class);
        factory().register(HistoryData.class);
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
        final String finalMethod = method;

        switch(finalMethod){
            case "actual":
                MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
                ActualData actualData = (ActualData) syncCache.get("ActualData_" + code);
                resp.getWriter().println(gson.toJson(actualData));
                /*ofy().transact(new VoidWork() {
                    @Override
                    public void vrun() {
                        ActualData actualData = ofy().load().type(ActualData.class).id(code).now();
                        try {
                            resp.getWriter().println(gson.toJson(actualData));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }});*/
                break;
            case "history":
                ofy().transact(new VoidWork() {
                    @Override
                    public void vrun() {
                        List<HistoryData> historyDataList = ofy().load().type(HistoryData.class).filter("code", code).order("-date").limit(100).list();
                        try {
                            resp.getWriter().println(gson.toJson(historyDataList));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }});
                break;
            }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{

        final String code = req.getParameter("code");
        final DateTime date = DateTime.parse(req.getParameter("date"));
        final int co2 = Integer.parseInt(req.getParameter("co2"));
        final double temp = Double.parseDouble(req.getParameter("temp"));

        ofy().transact(new VoidWork() {
            @Override
            public void vrun() {
                HistoryData historyData = new HistoryData(code, date, co2, temp);

                ofy().save().entity(historyData);

                ActualData actualData = ofy().load().type(ActualData.class).id(code).now();
                boolean needSaveActualData = false;
                if(actualData==null){
                    actualData = new ActualData(code, date, co2, temp);
                    needSaveActualData = true;
                } else if(actualData.date.isBefore(date)) {
                    actualData.date = date;
                    actualData.co2 = co2;
                    actualData.temp = temp;
                    needSaveActualData = true;
                }
                if(needSaveActualData){
                    ofy().save().entity(actualData);
                    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
                    syncCache.put("ActualData_" + actualData.code, actualData);
                }
            }
        });

        resp.setContentType("text/plain");
        resp.getWriter().println("ok");
    }
}
