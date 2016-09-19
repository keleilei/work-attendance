package me.kelei.wa.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 登录
 * Created by kelei on 2016/9/19.
 */
@Path("/login/")
public class WaLogin {

    @POST
    @Path("validate")
    @Produces(MediaType.APPLICATION_JSON)
    public String validateUser(@FormParam("waname") String userName, @FormParam("wapwd")String password,
                               @FormParam("rememberme") String rememberMe){
        System.out.println(userName);
        System.out.println(password);
        System.out.println(rememberMe);
        return "{\"isValidate\" : true}";
    }

}
