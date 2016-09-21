package me.kelei.wa.rest;

import me.kelei.wa.services.IJYWaDataService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * 登录
 * Created by kelei on 2016/9/19.
 */
@Path("/login/")
public class WaLogin {

    @Autowired
    private IJYWaDataService ijyWaDataService;

    @POST
    @Path("validate")
    @Produces(MediaType.APPLICATION_JSON)
    public String validateUser(@FormParam("waname") String userName, @FormParam("wapwd")String password,
                               @FormParam("rememberme") String rememberMe){
        boolean isSuccess = ijyWaDataService.login(userName, password);
        return "{\"isValidate\" : false}";
    }

}
