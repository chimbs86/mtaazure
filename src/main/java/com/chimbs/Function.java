package com.chimbs;

import com.google.transit.realtime.GtfsRealtime;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    private final Map<String, String> STATION_MAP = new LinkedHashMap<>();

    public Function() {
        try {
            getStationName();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FunctionName("line7")
    public HttpResponseMessage line7(@HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                                     final ExecutionContext context) throws IOException {

        return request.createResponseBuilder(HttpStatus.OK).body(getFeed("51","710","711","712", "713","714","715","716")).build();
    }

    private void getStationName() throws IOException {
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("/stations.csv"));
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);

        for (CSVRecord record : records) {
            String stopId = record.get("GTFS Stop ID");
            String name = record.get("Stop Name");
            this.STATION_MAP.put(stopId,name);
        }

    }

    private String getFeed(String feedId, String ... references) throws IOException {
        URL url = new URL("http://datamine.mta.info/mta_esi.php?key=6d7a62bcc6d3552c1ef4595a6b4cacec&feed_id=1");
        StringBuffer stops = new StringBuffer();
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
        for (GtfsRealtime.FeedEntity trip : feed.getEntityList()) {

            try {
                String stopId = trip.getVehicle().getStopId();
                String stop = trip.getTripUpdate().getStopTimeUpdate(0).getStopId();
                for(String ref : references){
                    if(stop.contains(ref)){
                        String stopcode = stop.replace("N","").replace("S","");
                        stops.append("Train is at  :" + STATION_MAP.get(stopcode) + " ");
                        stops.append(stop.replace(stopcode,""));
                        stops.append("\n");
                    }
                }
            } catch (Exception e) {

            }

        }
        return stops.toString();
    }

    @FunctionName("line1")
    public HttpResponseMessage line1(@HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST},
            authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                                     final ExecutionContext context) throws IOException {

        return request.createResponseBuilder(HttpStatus.OK).body(getFeed("1", "136","125","126","135")).build();
    }
}
