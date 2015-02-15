package org.vladhd.indoorclimate;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.objectify.impl.translate.opt.joda.JodaTimeTranslators;
import org.vladhd.indoorclimate.domain.ActualData;
import org.vladhd.indoorclimate.domain.HistoryData;
import org.joda.time.DateTime;

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
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/plain");
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();

        String code = req.getParameter("code");
        String method = req.getParameter("method");
        if(method==null || method.length()==0){
            method = "actual";
        }
        switch(method){
            case "actual":
                ActualData actualData = ofy().load().type(ActualData.class).id(code).now();
                resp.getWriter().println(gson.toJson(actualData));
                break;
            case "history":
                List<HistoryData> historyDataList = ofy().load().type(HistoryData.class).filter("code", code).order("-date").limit(100).list();
                resp.getWriter().println(gson.toJson(historyDataList));
                break;
        }

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{

        String code = req.getParameter("code");
        DateTime date = DateTime.parse(req.getParameter("date"));
        int co2 = Integer.parseInt(req.getParameter("co2"));
        double temp = Double.parseDouble(req.getParameter("temp"));

        HistoryData historyData = new HistoryData(code, date, co2, temp);

        ofy().save().entity(historyData).now();

        ActualData actualData = ofy().load().type(ActualData.class).id(code).now();
        if(actualData==null){
            actualData = new ActualData(code, date, co2, temp);
            ofy().save().entity(actualData).now();
        } else if(actualData.date.isBefore(date)) {
            actualData.date = date;
            actualData.co2 = co2;
            actualData.temp = temp;
            ofy().save().entity(actualData).now();
        }

        resp.setContentType("text/plain");
        resp.getWriter().println("ok");
    }
}
