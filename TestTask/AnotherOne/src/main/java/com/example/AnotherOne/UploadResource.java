package com.example.AnotherOne;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

import java.sql.Connection;
import java.sql.Statement;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Path("/upload")
public class UploadResource {
    private DataSource ds;
    private Connection connection;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Response upload(@MultipartForm FileUploadForm form) {
        String filename = "input.xml";
        String filepath;

        //Setting up download directory
        if (!form.getFilePath().isEmpty()) {
            filepath = form.getFilePath() + filename;
        }
        else {
            filepath = "D:/input/" + filename;
        }

        //Writing data from input file
        try {
            File file = new File(filepath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(form.getFileData());
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //Datasource connection
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/magenta/datasource/test-distance-calculator");
            if (ds != null) {
                connection = ds.getConnection();
            }
            Statement statement = connection.createStatement();

            //XML parser
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(filepath));
            doc.getDocumentElement().normalize();
            NodeList listOfCities = doc.getElementsByTagName("City");
            NodeList listOfDistances = doc.getElementsByTagName("Distance");

            //Execution sql query for every "City" node from XML file
            for (int s = 0; s < listOfCities.getLength(); s++) {
                Node CityNode = listOfCities.item(s);
                if (CityNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element CityElement = (Element) CityNode;

                    NodeList nameList = CityElement.getElementsByTagName("name");
                    Element nameElement = (Element) nameList.item(0);

                    NodeList textNameList = nameElement.getChildNodes();
                    String name = (textNameList.item(0)).getNodeValue().trim();

                    NodeList latitudeList = CityElement.getElementsByTagName("latitude");
                    Element latitudeElement = (Element) latitudeList.item(0);

                    NodeList textLatitudeList = latitudeElement.getChildNodes();
                    String latitude = (textLatitudeList.item(0)).getNodeValue().trim();

                    NodeList longitudeList = CityElement.getElementsByTagName("longitude");
                    Element longitudeElement = (Element) longitudeList.item(0);

                    NodeList textLongitudeList = longitudeElement.getChildNodes();
                    String longitude = (textLongitudeList.item(0)).getNodeValue().trim();

                    statement.executeUpdate("insert into city(name,latitude, longitude) values('" + name + "','" + latitude + "','" + longitude + "')");
                }
            }

            //Execution sql query for every "Distance" node from XML file
            for (int s = 0; s < listOfDistances.getLength(); s++) {
                Node DistanceNode = listOfDistances.item(s);
                if (DistanceNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element DistanceElement = (Element) DistanceNode;

                    NodeList fromCityList = DistanceElement.getElementsByTagName("from_city");
                    Element fromCityElement = (Element) fromCityList.item(0);

                    NodeList textFromCityList = fromCityElement.getChildNodes();
                    String fromCity = (textFromCityList.item(0)).getNodeValue().trim();

                    NodeList toCityList = DistanceElement.getElementsByTagName("to_city");
                    Element toCityElement = (Element) toCityList.item(0);

                    NodeList textToCityList = toCityElement.getChildNodes();
                    String toCity = (textToCityList.item(0)).getNodeValue().trim();

                    NodeList distanceList = DistanceElement.getElementsByTagName("distance");
                    Element distanceElement = (Element) distanceList.item(0);

                    NodeList textDistanceList = distanceElement.getChildNodes();
                    String distance = (textDistanceList.item(0)).getNodeValue().trim();

                    statement.executeUpdate("insert into distance(from_city, to_city, distance) values('" + fromCity + "','" + toCity + "','" + distance + "')");
                }
            }
            return Response.status(Response.Status.OK).build();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Response.ok().entity("Error:" + e.getMessage()).build();
        }
    }
}
