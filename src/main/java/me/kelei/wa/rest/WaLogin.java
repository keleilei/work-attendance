package me.kelei.wa.rest;

import me.kelei.wa.entities.WaUser;
import me.kelei.wa.services.IJYWaDataService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * 登录
 * Created by kelei on 2016/9/19.
 */
@Path("/login/")
public class WaLogin {

    @Autowired
    private IJYWaDataService ijyWaDataService;

    @Context
    private HttpServletRequest request;

    @POST
    @Path("validate")
    @Produces(MediaType.APPLICATION_JSON)
    public String validateUser(@FormParam("wapid") String pid, @FormParam("wapwd")String password,
                               @FormParam("rememberme") String rememberMe){
        WaUser waUser = ijyWaDataService.login(pid, password);
        boolean isValidate = false;
        if(waUser != null){
            isValidate = true;
            request.getSession().setAttribute("waUser", waUser);
        }
        return "{\"isValidate\" : "+isValidate+"}";
    }

}
