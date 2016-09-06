package me.kelei.wa.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by kelei on 2016/9/7.
 */
@Path("/test/")
public class RestTest {

    @GET
    @Path("get")
    public void get(){
        System.out.println("Test success!!!");
    }

}
