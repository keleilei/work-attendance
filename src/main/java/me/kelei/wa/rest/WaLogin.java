package me.kelei.wa.rest;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    public String validateUser(@FormParam("wausername") String userName, @FormParam("wxpwd")String password,
                               @FormParam("rememberme") String rememberMe){
        System.out.println(userName);
        System.out.println(password);
        System.out.println(rememberMe);
        return "{'isValidate' : true}";
    }

}
