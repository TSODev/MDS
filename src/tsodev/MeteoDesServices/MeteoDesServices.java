package tsodev.MeteoDesServices;


import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.*;



public class MeteoDesServices
{

    public final static Logger logger = Logger.getLogger(MeteoDesServices.class);

    /**
     *	Global Visualisation field variables
     */

    private static String strUser;
    private static String strPassword;
    private static String strMyITServer;
    private static String strMyITProtocol;
    private static String strMyITPort;

//    private static Boolean goodToGo;

    private static Map<String, JSONObject> ListOfServices;

    private static Map ListOfStatus = new HashMap();
    private static Map ListOfActions = new HashMap();

    public static void main(String[] args) {


        String searchName = "";
        Boolean goodToGo = false;

        ListOfStatus.put(10, "Available");
        ListOfStatus.put(20, "Information");
        ListOfStatus.put(30, "Performance Issue");
        ListOfStatus.put(40, "Unknow");
        ListOfStatus.put(50, "Maintenance");
        ListOfStatus.put(60, "Disruption");

        ListOfActions.put("status", "status");
        ListOfActions.put("annotation", "annotation");
        ListOfActions.put("desc", "desc");

        logger.info("MeteoDesServices : version 1.0 - by Thierry Soulie - Nov 2016");
        logger.info("=============================================================\n");

// create the command line parser
        CommandLineParser parser = new DefaultParser();

// create the Options
        Options options = new Options();

        options.addOption( "h", "help", false, "print help");
//        options.addOption( "s", "servername", true, "MyIT Server Name");
        final Option optserver = Option.builder("s")
                .longOpt("servername")
                .hasArg(true)
                .desc("MyIT Server Name")
                .build();
        options.addOption( optserver);
        options.addOption( "p", "port", true, "MyIT port number 'default is 80'");
        final Option optsecure = Option.builder("https")
                .longOpt("secure")
                .hasArg(false)
                .desc("use https instead of http")
                .build();
        options.addOption( optsecure);
//        options.addOption( "u", "username", true, "Username used to connect the MyIT server");
        final Option optuser = Option.builder("u")
                .longOpt("username")
                .hasArg(true)
                .desc("Username for login in MyIT")
                .build();
        options.addOption( optuser);
        options.addOption( "pwd", "password", true, "Password for the user");
        final Option optlist = Option.builder("l")
                .longOpt("list")
                .hasArg(false)
                .desc("list of Services")
                .build();
        options.addOption( optlist);
        options.addOption( "t", "status", true, "Status of the service");
        options.addOption( "a", "update", true, "update status of the service");
        final Option optupdt = Option.builder("upd")
                .longOpt("update")
                .hasArg(true)
                .desc("update status of a service")
                .build();
        optupdt.setArgs(3);
        options.addOption( optupdt);
        final Option optpost = Option.builder("m")
                .longOpt("post")
                .hasArg(true)
                .desc("Post a message on a Service")
                .build();
        optpost.setArgs(2);
        options.addOption( optpost);
        options.addOption( "c", "command-file", true, "read command file and execute");


// automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
//        formatter.printHelp( "mds", options );

        goodToGo = true;

     parseline:   try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if (line.hasOption('h'))                                        // Help
            {
                formatter.printHelp( "mds", options );
                goodToGo = false;
                break parseline;
            }

            if (line.hasOption("servername")) {                             // Server Name
                strMyITServer = line.getOptionValue("servername");
            }
            else
            {
                logger.debug("No server specified");
                goodToGo = false;
                break parseline;
            }

            if (line.hasOption("port"))                                     // Port Number
            {
                strMyITPort = line.getOptionValue("port");
            }
            else {
                strMyITPort = "80";
            }

            if (line.hasOption("secure"))                                   // https
                {
                    strMyITProtocol = "https";
                }
                else
                {
                    strMyITProtocol = "http";
                }

            if (line.hasOption("password"))                                 // password
            {
                strPassword = line.getOptionValue("password");
            }

            if (line.hasOption("username")){                                // User Name
                strUser = line.getOptionValue("username");
            }
            else {
                logger.debug("No user specified");
                goodToGo = false;
                break parseline;
            }


        }
        catch( ParseException exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
        }


        if (goodToGo) {

            execution: try {

                // parse the command line arguments
                CommandLine line = parser.parse(options, args);

                logger.debug("Server = " + strMyITServer);
                logger.debug("User = " + strUser);
                logger.debug("Port = " + strMyITPort);
                logger.debug("Protocol = " + strMyITProtocol);

                MyITapi myit = new MyITapi();
                myit.MyITSetSetverInfo(strMyITProtocol, strMyITServer, strMyITPort);

                //Get Cookie : SessionId

                String SessionId = myit.MyITLogin(strUser, strPassword);
                logger.debug("JSESSIONID : " + SessionId);

                //Store MyIT Services

                JSONArray MyServices = myit.MyIT_My_Services(strMyITProtocol, strMyITServer, strMyITPort, SessionId);
                ListOfServices =  storeServiceInfo(MyServices);
                logger.debug(ListOfServices.toString());

                if (line.hasOption("list")) {
                    for(Map.Entry<String, JSONObject> entry : ListOfServices.entrySet()){
                        System.out.printf("Id : %s \t  Name: %s%n",
                                entry.getValue().get("id"),
//                                entry.getValue().get("status"),
                                entry.getValue().get("name"));
                    }
                    break execution;
                }

                if (line.hasOption("status")) {
                    String Service = line.getOptionValue("status");
                    JSONObject ServiceInfo = ListOfServices.get(Service);
                    if (ServiceInfo != null) {
                        logger.debug("Service Infos for " + Service + " : " + ServiceInfo.toString());
                        Long ServiceStatus = (Long) ServiceInfo.get("status");
                        System.out.printf("Service : %s \t - \t Status : %s \n", Service, ListOfStatus.get(ServiceStatus.intValue()));
                    }
                    else
                    {
                        System.out.printf("Service : %s not found\n", Service);
                    }
                    break execution;
                }

                if (line.hasOption("update")) {
                    String[] update_options = line.getOptionValues("update");
                    String servicename = update_options[0];
                    String newarg = update_options[1];
                    String action = (String)ListOfActions.get(newarg);
                    if (action != null) {
                        String newvalue = update_options[2];
                        JSONObject ServiceInfo = ListOfServices.get(servicename);
                        if (ServiceInfo != null){
                            String serviceid = (String) ServiceInfo.get("id");
                            JSONObject update_result = myit.MyIT_Update_Service(strMyITProtocol, strMyITServer, strMyITPort, serviceid, newarg, newvalue, SessionId);
                            logger.debug("Update Command : Service: " + servicename + " Action: " + newarg + " Value: " + newvalue);
                            if (update_result.get("status").toString() != "OK") {
                                System.out.printf("Error : %s", update_result.get("message").toString());
                            } else {
                                System.out.printf("Service %s %s updated to %s\n", servicename, newarg, newvalue);
                            }
                        }
                        else
                        {
                           System.out.printf("Service : %s not found\n", servicename);
                        }
                    }
                    else
                    {
                        System.out.printf("Command : %s not found\n", newarg);
                    }
                    break execution;
                }

                if (line.hasOption("post")) {
                    String[] update_options = line.getOptionValues("post");
                    String servicename = update_options[0];
                    String message = update_options[1];
                    String serviceid = (String) ListOfServices.get(servicename).get("id");
                    JSONObject update_result = myit.MyIT_Post_Message_Service(strMyITProtocol, strMyITServer, strMyITPort, servicename, serviceid, message, SessionId);
                    if (update_result.get("status").toString() != "OK")
                    {
                        System.out.printf("Error : %s", update_result.get("message").toString());
                    }
                    else
                    {
                        System.out.printf("Message %s posted to %s\n", message, servicename);
                    }
                    break execution;
                }

            }
            catch( ParseException exp ) {
                System.out.println( "Unexpected exception:" + exp.getMessage() );
            }

        }

    }


    private static Map<String, JSONObject> storeServiceInfo(JSONArray TheServices) {

        JSONObject data = (JSONObject) TheServices.get(0);
        JSONArray items = (JSONArray) data.get("items");

        HashMap<String, JSONObject> los = new HashMap<String,JSONObject>() ;
        Iterator<JSONObject> elements = items.iterator();

        while (elements.hasNext()){
            JSONObject value = elements.next();
            logger.debug("Store Service Info : " + value.toString());
            los.put((String) value.get("name"), value);
        }

        return los;
    }

}