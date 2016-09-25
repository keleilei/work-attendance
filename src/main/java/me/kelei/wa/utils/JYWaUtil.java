package me.kelei.wa.utils;

import me.kelei.wa.entities.WaRecord;
import me.kelei.wa.entities.WaUser;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 从精友考勤外网获取数据工具类
 * Created by kelei on 2016/9/24.
 */
public class JYWaUtil {

    private static final Logger logger = LoggerFactory.getLogger(JYWaUtil.class);
    private static CookieStore httpCookieStore = new BasicCookieStore();

    /**
     * 获取精友考勤网站的用户信息
     * @param pid
     * @param password
     * @return
     */
    public static WaUser getJYWaUser(String pid, String password){
        WaUser waUser = null;
        HttpClient client = jyWaLogin(pid, password);
        try{
            HttpGet get = new HttpGet("http://124.42.1.13:8000/iclock/staff/");
            HttpResponse response = client.execute(get);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), "utf-8"));
                    String line = null;
                    String uid = "";
                    String name = "";
                    while ((line = br.readLine()) != null) {
                        if(line.contains("uid=\"")){
                            uid = line.replaceAll("\\s", "").replace("uid=\"", "").replace("\";", "");
                        }
                        if(line.contains("strong")){
                            int sindex = line.indexOf("strong");
                            line = line.substring(sindex);
                            name = line.substring(10, 12);
                        }
                    }
                    br.close();

                    if(!StringUtils.isEmpty(uid)){
                        waUser = new WaUser();
                        waUser.setWaUid(uid);
                        waUser.setWaPid(pid);
                        waUser.setWaUserName(name);
                        waUser.setWaUserPwd(password);
                    }
                }
            }
        }catch (Exception e){
            logger.error("获取精友考勤用户信息失败！", e);
        }
        return waUser;
    }

    /**
     * 获取考勤记录
     * @param waUser
     * @param startDate
     * @return
     */
    public static List<WaRecord> getJYWaRecordList(WaUser waUser, Date startDate){
        List<WaRecord> recordList = new ArrayList<>();
        HttpClient client = jyWaLogin(waUser.getWaPid(), waUser.getWaUserPwd());
        List<Cookie> cookies =httpCookieStore.getCookies();
        String cookieName = "";
        String cookieValue = "";
        for (Cookie c : cookies) {
            cookieName = c.getName();
            cookieValue = c.getValue();
        }
        try {
            int pageNum = 1;
            int pageCount;
            do{
                pageCount = getRecordByPage(waUser.getWaUid(), cookieName, cookieValue, pageNum, startDate, recordList);
                pageNum++;
            }while (pageNum <= pageCount);
        }catch (Exception e){
            logger.error("获取考勤记录失败！", e);
        }
        return recordList;
    }

    /**
     * 分页获取考勤记录
     * @param uid             用户UID
     * @param cookieName     验证登录必须参数
     * @param cookieValue    验证登录必须参数
     * @param pn              当前页数
     * @param startDate      查询起始日期
     * @param recordList     存放考勤记录
     * @return 总页数
     * @throws IOException
     */
    private static int getRecordByPage(String uid, String cookieName, String cookieValue, int pn, Date startDate,
                                       List<WaRecord> recordList) throws IOException {
        String fromDate = DateFormatUtils.format(startDate, "yyyy-MM-dd");
        String toDate = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
        String kgUrl = "http://124.42.1.13:8000/iclock/staff/transaction/?p=" + pn +
                "&t=staff_transaction.html&UserID__id__exact=" + uid +"&fromTime=" + fromDate + "&toTime=" + toDate;
        Document document = Jsoup.connect(kgUrl).cookie(cookieName, cookieValue).get();
        Elements rows = document.select("tr[class^=row]");
        String script = document.data();
        String tmpScrpit = script.substring(script.indexOf("page_number_emp"));
        String pageNumber = tmpScrpit.substring(0, tmpScrpit.indexOf(";")).replace("page_number_emp=", "");

        for(int i = 0; i < rows.size(); i++){
            Elements cols = rows.get(i).getElementsByTag("td");
            WaRecord record = new WaRecord();
            try {
                record.setWaDate(DateUtils.parseDate(cols.get(1).text(),"yyyy-MM-dd HH:mm:ss"));
            } catch (ParseException e) {
                logger.error("解析日期出错！", e);
            }
            record.setWaType(cols.get(2).text());
            record.setWaValidateWay(cols.get(3).text());
            record.setWaDevice(cols.get(4).text());
            recordList.add(record);
        }
        return Integer.valueOf(pageNumber);
    }

    /**
     * 登录精友考勤网站
     * @param pid         用户考勤号
     * @param password   用户密码
     * @return httpclient
     */
    private static HttpClient jyWaLogin(String pid, String password){
        String url = "http://124.42.1.13:8000/iclock/accounts/login/";
        HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).build();
        try {
            HttpPost login = new HttpPost(url);
            login.addHeader("X-Requested-With", "XMLHttpRequest");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("username", pid));
            nvps.add(new BasicNameValuePair("password", password));
            login.setEntity(new UrlEncodedFormEntity(nvps, HTTP.DEF_CONTENT_CHARSET));

            HttpResponse response = client.execute(login);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String responseText = EntityUtils.toString(entity);
                    logger.info("登录状态：" + responseText);
                }
            }
        }catch (Exception e){
            logger.error("登录精友考勤网站失败！", e);
        }
        return client;
    }

    public static void main(String[] args) throws ParseException {
        WaUser user = new WaUser();
        user.setWaPid("1098");
        user.setWaUid("291");
        user.setWaUserPwd("ketingjiang");
        JYWaUtil.getJYWaRecordList(user, DateUtils.parseDate("2016-08-01","yyyy-MM-dd"));
    }
}
