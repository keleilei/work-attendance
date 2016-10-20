package me.kelei.wa.rest;

import com.alibaba.fastjson.JSON;
import me.kelei.wa.entities.Holiday;
import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.services.IWaService;
import me.kelei.wa.utils.JYWaUtil;
import me.kelei.wa.utils.WaDict;
import me.kelei.wa.utils.WaPage;
import me.kelei.wa.utils.WaUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.util.Date;
import java.util.List;

/**
 * 考勤记录
 * Created by kelei on 2016/9/26.
 */
@Path("/data/")
public class WaData {

    @Autowired
    private IWaService ijyWaDataService;

    @Context
    private HttpServletRequest request;

    @GET
    @Path("/month")
    @Produces(MediaType.APPLICATION_JSON)
    public String queryDataByMonth(@QueryParam("qdate") String queryDate){

        WaUser user = (WaUser) request.getSession().getAttribute("waUser");

        if(StringUtils.isEmpty(queryDate)){
            queryDate = DateFormatUtils.format(WaUtil.getCurrentDay(), "yyyy-MM");
        }
        WaPage page = new WaPage();
        page.setStatus(WaDict.REQUEST_STATE_SUCCESS);

        //查询考勤记录
        List<WaRecord> recordList = ijyWaDataService.getWaRecordList(user, queryDate);
        //查询节假日
        List<Holiday> holidayList = ijyWaDataService.getHolidayList(queryDate);

        if(recordList == null || recordList.isEmpty()){
            page.setStatus(WaDict.REQUEST_STATE_EMPTY);
        }

        page.setRecordList(recordList);
        page.setHolidayList(holidayList);
        return JSON.toJSONString(page);
    }

    @GET
    @Path("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateData(@QueryParam("qdate") String queryDate){
        WaUser user = (WaUser) request.getSession().getAttribute("waUser");
        if(StringUtils.isEmpty(queryDate)){
            queryDate = DateFormatUtils.format(WaUtil.getCurrentDay(), "yyyy-MM");
        }
        WaPage page = new WaPage();
        page.setStatus(WaDict.REQUEST_STATE_SUCCESS);
        try {
            ijyWaDataService.saveWaRecordList(user, queryDate);
        } catch (IOException e) {
            page.setStatus(WaDict.REQUEST_STATE_FAIL);
        }
        return JSON.toJSONString(page);
    }

}
