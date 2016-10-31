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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try{
            client = jyWaLogin(pid, password);
            HttpGet get = new HttpGet("http://124.42.1.13:8000/iclock/staff/");
            response = client.execute(get);
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
                        if(line.contains("员工")){
                            int sindex = line.indexOf("员工");
                            line = line.substring(sindex);
                            name = line.substring(0, line.indexOf("<")).replace("员工 ", "");
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
        }finally {
            try {
                if(client != null)
                    client.close();
                if(response != null)
                    response.close();;
            } catch (IOException e) {
                logger.error("关闭httpclient连接失败！", e);
            }
        }
        return waUser;
    }

    /**
     * 获取考勤记录
     * @param waUser 用户
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return
     */
    public static List<WaRecord> getJYWaRecordList(WaUser waUser, Date startDate, Date endDate) throws IOException {
        List<WaRecord> recordList = new ArrayList<>();
        try {
            jyWaLogin(waUser.getWaPid(), waUser.getWaUserPwd()).close();
        }catch (IOException e){
            logger.error("登录网站失败！", e);
            throw new IOException(e);
        }
        List<Cookie> cookies =httpCookieStore.getCookies();
        String cookieName = "";
        String cookieValue = "";
        for (Cookie c : cookies) {
            cookieName = c.getName();
            cookieValue = c.getValue();
        }
        int pageNum = 1;
        int pageCount = 0;
        do{
            boolean retry = true;
            int retryCount = 0;
            while (retry){
                try {
                    if(retryCount == 10){
                        break;
                    }
                    pageCount = getRecordByPage(waUser, cookieName, cookieValue, pageNum, startDate, endDate, recordList);
                    retry = false;
                } catch (Exception e) {
                    retryCount++;
                    retry = true;
                }
            }
            if(retry){
                throw new ConnectException("接连精友考勤网站失败！");
            }
            pageNum++;
        }while (pageNum <= pageCount);
        //清Cookie
        httpCookieStore.clear();
        return recordList;
    }

    /**
     * 分页获取考勤记录
     * @param waUser          用户
     * @param cookieName     验证登录必须参数
     * @param cookieValue    验证登录必须参数
     * @param pn              当前页数
     * @param startDate      查询起始日期
     * @param endDate        查询结束日期
     * @param recordList     存放考勤记录
     * @return 总页数
     * @throws IOException
     */
    private static int getRecordByPage(WaUser waUser, String cookieName, String cookieValue, int pn, Date startDate,
                                       Date endDate, List<WaRecord> recordList) throws IOException {
        String fromDate = DateFormatUtils.format(startDate, "yyyy-MM-dd");
        String toDate = DateFormatUtils.format(endDate, "yyyy-MM-dd");
        String kgUrl = "http://124.42.1.13:8000/iclock/staff/transaction/?p=" + pn +
                "&t=staff_transaction.html&UserID__id__exact=" + waUser.getWaUid() +"&fromTime=" + fromDate + "&toTime=" + toDate;
        Document document = Jsoup.connect(kgUrl).timeout(3000).cookie(cookieName, cookieValue).get();
        Elements rows = document.select("tr[class^=row]");
        String script = document.data();
        String tmpScrpit = script.substring(script.indexOf("page_number_emp"));
        String pageNumber = tmpScrpit.substring(0, tmpScrpit.indexOf(";")).replace("page_number_emp=", "");

        for (Element row : rows) {
            Elements cols = row.getElementsByTag("td");
            WaRecord record = new WaRecord();
            record.setWaPid(waUser.getWaPid());
            record.setWaState(WaDict.RECORD_STATE_INVALID);//默认无效记录
            try {
                record.setWaDate(DateUtils.parseDate(cols.get(1).text(), "yyyy-MM-dd HH:mm:ss"));
                record.setWaWeek(DateFormatUtils.format(record.getWaDate(), "EEEE", Locale.CHINA));
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
    private static CloseableHttpClient jyWaLogin(String pid, String password) throws IOException {
        String url = "http://124.42.1.13:8000/iclock/accounts/login/";
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).build();
        HttpPost login = new HttpPost(url);
        login.addHeader("X-Requested-With", "XMLHttpRequest");
        List<NameValuePair> nvps = new ArrayList<>();
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
        return client;
    }
}
