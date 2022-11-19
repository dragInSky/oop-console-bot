package org.bot;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.MessageFormat;

public class HttpRequest {
    private final String USER_AGENT = "Mozilla/5.0";

    public String sendGetGeo(String addr) {
        String url = MessageFormat.format(
                "https://catalog.api.2gis.com/3.0/items/geocode?q={0}&fields=items.point&key={1}",
                addr, get2GisGetKey());
        return sendGet(url);
    }

    public String sendPostRoute() {
        String url = MessageFormat.format(
                "https://routing.api.2gis.com/carrouting/6.0.0/global?key={0}",
                get2GisPostKey());
        return sendPost(url);
    }

    // HTTP GET request
    private String sendGet(String url) {
        try {
            URL obj = new URL(url);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", USER_AGENT);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            int firstIdx = response.indexOf("lat");
            int lastIdx = response.indexOf("purpose_name") - 3;
            String substr = response.substring(firstIdx, lastIdx);
            String[] coordinates = substr.split(",");

            return coordinates[0].substring(coordinates[0].indexOf(":") + 1) +
                    " " + coordinates[1].substring(coordinates[1].indexOf(":") + 1);
        } catch (Exception e) {
            return null;
        }
    }

    private String sendPost(String url) {
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = """
                    {
                       "points": [
                           {
                               "type": "walking",
                               "x": 82.93057,
                               "y": 54.943207
                           },
                           {
                               "type": "walking",
                               "x": 82.945039,
                               "y": 55.033879
                           }
                       ]
                    }
                    """;
            urlParameters = urlParameters.replace("API_KEY", get2GisPostKey());

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return findInformation(response);
        } catch (Exception e) {
            return null;
        }
    }

    public String get2GisPostKey() {
        return System.getenv("2GIS_POST_KEY");
    }

    public String get2GisGetKey() {
        return System.getenv("2GIS_GET_KEY");
    }

    private String findInformation(StringBuilder response)
    {
        int start = response.indexOf("total_distance");
        int finish = response.indexOf("type", start);

        return response.substring(start - 1, finish - 2);
    }
}