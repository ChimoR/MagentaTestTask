package com.example.AnotherOne;


import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


@Path("/get")

public class GetResource {

    private DataSource ds;
    private Connection connection;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response get() {
        StringBuilder sb = new StringBuilder();
        try {
            //Datasource connection
            InitialContext ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/magenta/datasource/test-distance-calculator");
            if (ds != null) {
                connection = ds.getConnection();
            }

            //Query execution and printing data
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM city";
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()) {
                String toAdd = "ID: " + rs.getInt("ID") +
                        " Name: " + rs.getString("Name") + "\n";
                sb.append(toAdd);
            }
            rs.close();
            statement.close();
            connection.close();
        }
        catch (SQLException | NullPointerException | NamingException e) {
            Response.ok().entity("Error:" + e.getMessage()).build();
        }
        return Response.ok(sb).build();
    }
}