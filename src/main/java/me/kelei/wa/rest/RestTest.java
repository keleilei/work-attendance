package me.kelei.wa.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by kelei on 2016/9/7.
 */
@Path("/test/")
public class RestTest {

    @GET
    @Path("get")
    @Produces(MediaType.TEXT_PLAIN)
    public String get(){
        return "Test success!!!";
    }

}
