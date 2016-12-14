package tsodev.MeteoDesServices;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.List;

/**
 * Created by tsoulie on 09 Nov 2016
 **/

public class MyITapi {

    private final static Logger logger = Logger.getLogger(MeteoDesServices.class);

    public String getProtocol() {
        return Protocol;
    }

    public void setProtocol(String protocol) {
        Protocol = protocol;
    }

    public String getServer() {
        return Server;
    }

    public void setServer(String server) {
        Server = server;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    private String Token = null;
    private String Protocol = null;
    private String Server = null;
    private String port = null;
    private JSONObject result = null;

    public void MyITSetSetverInfo(String protocol, String server, String port){

        this.setProtocol(protocol);
        this.setServer(server);
        this.setPort(port);
    }

    public String MyITLogin(String user, String password) {

        // (LOGIN) - User Sessions (POST )

        HttpClient httpClient = new DefaultHttpClient();
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        try {

            String url = Protocol + "://" + Server + ":" + port + "/ux/restapi/users/sessions/" + user;


//            // Create request
//            Content content = Request.Post(Protocol + "://" + Server + ":" + port + "/ux/restapi/users/sessions/" + user)
//
//                    // Add headers
////                    .addHeader("Cookie", "JSESSIONID=E4E94B0B2F923B286AEE86A466023265")
//                    .addHeader("Content-Type", "application/json; charset=utf-8")
//
//                    // Add body
//                    .bodyString("{\"locale\": \"fr\",\"password\": \"password\",\"os\": \"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:44.0) Gecko/20100101 Firefox/44.0\",\"appName\": \"MyIT\",\"model\": \"Web Client\",\"apiVersion\": 3020000,\"deviceToken\": \"dummyToken\"}", ContentType.APPLICATION_JSON)
//
//                    // Fetch request and return content
//                    .execute().returnContent();
//
//            // Print content
//            System.out.println(content);

            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("Content-Type", "application/json; charset=utf-8");

            JSONObject body = new JSONObject();
                body.put("locale" , "fr");
                body.put("password" , password);
                body.put("os" , "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:44.0) Gecko/20100101 Firefox/44.0");
                body.put("appName", "MyIT");
                body.put("model", "Web Client");
                body.put("apiVersion", "3020000");
                body.put("deviceToken", "dummyToken");
            StringEntity entity = new StringEntity(body.toString());
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse response1 = httpClient.execute(httpPost, httpContext);

            List<Cookie> cookies = cookieStore.getCookies();

            return (cookies.get(0).getValue());
        }
        catch (IOException e) {
            System.out.println(e);
        }

        return null;
    }

    public JSONArray MyIT_My_Services(String protocol, String server, String port ,String SessionId){

        // Request MYIT_MY_SERVICES (POST )

        try {

            // Create request
            Content content = Request.Post(protocol + "://" + server + ":" + port +"/ux/rest/search")

                    // Add headers
                    .addHeader("Cookie", "JSESSIONID=" + SessionId)
                    .addHeader("Content-Type", "application/json; charset=utf-8")

                    // Add body
                    .bodyString("{\"attributes\": {\"ServiceAvailability\": [\"id\",\"subscribed\",\"notificationSetting\",\"name\",\"desc\",\"iconId\",\"annotation\",\"status\",\"isEnabled\",\"isAutoSubscribe\",\"userId\",\"createDate\",\"modifiedDate\"]},\"queryName\": \"MYIT_MY_SERVICES\"}", ContentType.APPLICATION_JSON)

                    // Fetch request and return content
                    .execute().returnContent();

            // Print content
            String result = content.asString();
            JSONParser parser = new JSONParser();

            try {
                Object obj = parser.parse(result);
                JSONArray array = (JSONArray) obj;

                return array;
            }
            catch(ParseException pe){

                System.out.println("position: " + pe.getPosition());
                System.out.println(pe);
            }



        }
        catch (IOException exception) {
            System.out.println(exception);
            JSONArray result = null;
            JSONObject data = new JSONObject();
            data.put("message", exception.getMessage());
            data.put("trace", exception.getStackTrace().toString());
            result.add(data);
            return result;
        }

        return null;
    }

    public JSONObject MyIT_Update_Service(String protocol, String server , String port , String ServiceId, String command, String value, String SessionId) {

        // Request Update Service Availability (POST )

        String url = protocol + "://" + server + ":" + port + "/ux/rest/update";

        try {

            // Create request
            Content content = Request.Post(url)

//                    // Add headers
                    .addHeader("Cookie", "JSESSIONID=" + SessionId)
                    .addHeader("Content-Type", "application/json; charset=utf-8")

                    // Add body
                    .bodyString("{\"ServiceAvailability\": {\"id\": \"" + ServiceId + "\",\"changes\": {\"" + command + "\": \"" + value + "\"}}}", ContentType.APPLICATION_JSON)

                    // Fetch request and return content
                    .execute().returnContent();

            // Print content
            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("message", content);
            return result;
        }
        catch (IOException e)
        {
            System.out.println(e);
            JSONObject result = new JSONObject();
            result.put("status", "Error");
            result.put("message", e.getMessage());
            return result;
        }
    }

    public JSONObject MyIT_Post_Message_Service(String protocol, String server, String port, String ServiceName, String ServiceId, String message, String SessionId)

    {

        // Request Post Message (POST )

        String url = protocol + "://" + server + ":" + port + "/ux/rest/v2/activity";

        try {

            // Create request
            Content content = Request.Post(url)

                    // Add headers
                    .addHeader("Cookie", "JSESSIONID=" + SessionId)
                    .addHeader("Content-Type", "application/json; charset=utf-8")

                    // Add body
                    .bodyString("{\"type\": \"microblog\",\"text\": \"@[" + ServiceName + "]|" + ServiceId + "|(service) " + message + "\"}", ContentType.APPLICATION_JSON)

                    // Fetch request and return content
                    .execute().returnContent();

            // Print content
            JSONObject result = new JSONObject();
            result.put("status", "OK");
            result.put("message", content);
            return result;
        } catch (IOException e) {
            System.out.println(e);
            JSONObject result = new JSONObject();
            result.put("status", "Error");
            result.put("message", e.getMessage());
            return result;
        }
    }
}
