package me.kelei.wa.rest;

import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.services.IWaService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public String data(@QueryParam("qdate") String queryDate){

        if(StringUtils.isEmpty(queryDate)){
            queryDate = DateFormatUtils.format(new Date(), "yyyy-MM");
        }
        List<WaRecord> recordList = ijyWaDataService.getRecordList(queryDate);

        ijyWaDataService.saveWaRecordList(null);

        return null;
    }
}
