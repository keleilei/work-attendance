package me.kelei.wa.rest;

import com.alibaba.fastjson.JSON;
import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUser;
import me.kelei.wa.services.IWaService;
import me.kelei.wa.utils.JYWaUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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
            queryDate = DateFormatUtils.format(new Date(), "yyyy-MM");
        }

        //从精友考勤系统获取考勤记录
        List<WaRecord> jyRecordList = JYWaUtil.getJYWaRecordList(user, queryDate);

        //比对本地数据并返回处理过的记录
        List<WaRecord> recordList = null;
        if(!jyRecordList.isEmpty()){
            recordList = ijyWaDataService.saveWaRecordList(jyRecordList, queryDate);
        }

        return JSON.toJSONString(recordList);
    }
}
