package com.example.AnotherOne;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Path("/calc")

public class CalculateResource {

    private DataSource ds;
    private Connection connection;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response calculate(@FormParam("first") String first,
                              @FormParam("second") String second,
                              @FormParam("type") String type) {
        double firstLat = 0;
        double firstLong = 0;
        double secondLat = 0;
        double secondLong = 0;
        int distance = 0;
        String query;
        String result;

        try {
            //Datasource connection
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/magenta/datasource/test-distance-calculator");
            if (ds != null) {
                connection = ds.getConnection();
            }

            //Queries execution block
            ResultSet rs = null;
            Statement statement = connection.createStatement();
            if (type.equals("Crow Flight") || type.equals("All")) {
                query = "SELECT Latitude, Longitude FROM city WHERE Name = '" + first + "'";
                rs = statement.executeQuery(query);
                if (rs.next()) {
                    firstLat = Double.parseDouble(rs.getString("Latitude"));
                    firstLong = Double.parseDouble(rs.getString("Longitude"));
                }
                else {
                    throw new NotEnoughDataException("Exception");
                }

                query = "SELECT Latitude, Longitude FROM city WHERE Name = '" + second + "'";
                rs = statement.executeQuery(query);
                if (rs.next()) {
                    secondLat = Double.parseDouble(rs.getString("Latitude"));
                    secondLong = Double.parseDouble(rs.getString("Longitude"));
                }
                else {
                    throw new NotEnoughDataException("Not enough data in Database");
                }
            }
            if (type.equals("Distance Matrix") || type.equals("All")) {
                query = "SELECT Distance FROM distance WHERE From_city = '" + first + "' AND To_city = '" + second + "'";
                rs = statement.executeQuery(query);
                if (rs.next()) {
                    distance = rs.getInt("Distance");
                }
                else {
                    query = "SELECT Distance FROM distance WHERE From_city = '" + second + "' AND To_city = '" + first + "'";
                    rs = statement.executeQuery(query);
                    if (rs.next()) {
                        distance = rs.getInt("Distance");
                    }
                    else {
                        throw new NotEnoughDataException("Exception");
                    }
                }
            }
            rs.close();
            statement.close();
            connection.close();
        }
        catch (SQLException | NullPointerException | NamingException | NotEnoughDataException e) {
            return Response.ok().entity("Error:" + e.getMessage()).build();
        }

        //Results print block
        if (type.equals("Crow Flight")) {
            String value = crowFlight(firstLat, firstLong, secondLat, secondLong);
            result = "Distance between " + first + " and " + second + " by Crow Flight = " + value + " meters.";
            return Response.ok(result).build();
        }
        else if (type.equals("Distance Matrix")) {
            result = "Distance between " + first + " and " + second + " by Distance Matrix = " + distance + " meters.";
            return Response.ok(result).build();
        }
        else {
            StringBuilder sb = new StringBuilder();
            String value = crowFlight(firstLat, firstLong, secondLat, secondLong);
            sb.append("Distance between " + first + " and " + second + " by Crow Flight = " + value + " meters.\n");
            sb.append("Distance between " + first + " and " + second + " by Distance Matrix = " + distance + " meters.");
            result = sb.toString();
            return Response.ok(result).build();
        }
    }

    //Method for CrowFlight calculate
    public String crowFlight(double latitude1, double longitude1, double latitude2, double longitude2) {
        int RADIUS = 6372795;

        //Lats and Longs to Radians
        double lat1 = latitude1 * Math.PI / 180;
        double long1 = longitude1 * Math.PI / 180;
        double lat2 = latitude2 * Math.PI / 180;
        double long2 = longitude2 * Math.PI / 180;

        //Cos and Sin
        double cl1 = Math.cos(lat1);
        double cl2 = Math.cos(lat2);
        double sl1 = Math.sin(lat1);
        double sl2 = Math.sin(lat2);
        double delta = long2 - long1;
        double cdelta = Math.cos(delta);
        double sdelta = Math.sin(delta);

        //Great circle length
        double y = Math.sqrt((Math.pow(cl2 * sdelta, 2)) + Math.pow(cl1 * sl2 - sl1 * cl2 * cdelta, 2));
        double x = sl1 * sl2 + cl1 * cl2 * cdelta;
        double res = Math.atan2(y, x) * RADIUS;

        return String.format("%.0f", res);
    }
}
