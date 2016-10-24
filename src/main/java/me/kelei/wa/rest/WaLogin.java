package me.kelei.wa.rest;

import com.alibaba.fastjson.JSON;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.services.IWaService;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * 登录
 * Created by kelei on 2016/9/19.
 */
@Path("/login/")
public class WaLogin {
    private static final Logger logger = LoggerFactory.getLogger(WaLogin.class);

    @Autowired
    private IWaService ijyWaDataService;

    @Context
    private HttpServletRequest request;

    @Context
    private HttpServletResponse response;

    @POST
    @Path("validate")
    @Produces(MediaType.APPLICATION_JSON)
    public String validateUser(@FormParam("wapid") String pid, @FormParam("wapwd")String password){

        WaUser waUser = ijyWaDataService.getWaUser(pid);
        boolean isValidate = false;

        if(waUser == null){
            //保存用户
            waUser = ijyWaDataService.saveUser(pid, password);
            if(waUser != null){
                //保存用户更新信息
                ijyWaDataService.saveWaUpdate(pid);
            }
        }else{
            //验证用户密码
            if(!waUser.getWaUserPwd().equals(password)){
                waUser = ijyWaDataService.saveUser(pid, password);
            }
        }

        if(waUser != null){
            isValidate = true;
            request.getSession().setAttribute("waUser", waUser);
        }else{
            request.getSession().setAttribute("waUser", null);
        }
        return "{\"isValidate\" : "+isValidate+"}";
    }

    @GET
    @Path("rememberMe")
    public String rememberMeDispatcher(){
        String pid = (String) request.getAttribute("wapid");
        WaUser user = ijyWaDataService.getWaUser(pid);
        try {
            if(user == null){
                request.getRequestDispatcher("/page/login.html").forward(request, response);
            }else{
                request.getSession().setAttribute("waUser", user);
                request.getRequestDispatcher("/").forward(request, response);
            }
        }catch (Exception e){
            logger.error("跳转出错！", e);
        }
        return null;
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public String queryLoginUser() {
        WaUser user = (WaUser) request.getSession().getAttribute("waUser");
        WaUser clone = new WaUser();
        if(user != null){
            try {
                BeanUtils.copyProperties(clone, user);
                clone.setWaUserPwd("");
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return JSON.toJSONString(clone);
    }

    @GET
    @Path("/logout")
    public String logout(){
        request.getSession().setAttribute("waUser", null);
        request.getSession().setAttribute("logout", "1");
        return null;
    }

}
