package me.kelei.wa.services;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取考勤数据实现类
 * Created by kelei on 2016/9/20.
 */
@Service
public class JYWaDataServiceImpl implements IJYWaDataService{


    public boolean login(String userName, String password) {
        try{
            String url = "http://124.42.1.13:8000/iclock/accounts/login/";
            HttpClient client = HttpClientBuilder.create().build();

            HttpPost login = new HttpPost(url);
            login.addHeader("X-Requested-With", "XMLHttpRequest");
//            login.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:48.0) Gecko/20100101 Firefox/48.0");
//            login.addHeader("Host", "124.42.1.13:8000");
//            login.addHeader("Referer", "http://124.42.1.13:8000/iclock/accounts/login/");
//            login.addHeader("Cookie", "sessionid=9cfd75d3809427187e2b9131adf52aa9");
//            login.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("username", userName));
            nvps.add(new BasicNameValuePair("password", password));
            login.setEntity(new UrlEncodedFormEntity(nvps, HTTP.DEF_CONTENT_CHARSET));

            HttpResponse response = client.execute(login);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            entity.getContent(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                    }
                }
            }

            //获取用户ID
            HttpGet get = new HttpGet("http://124.42.1.13:8000/iclock/staff/");
            response = client.execute(get);
            String uid = "";
            String name = "";
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            entity.getContent(), "utf-8"));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        if(line.contains("uid=\"")){
                            uid = line.replaceAll("\\s", "").replace("uid=\"", "").replace("\";", "");
                        }
                        if(line.contains("员工")){
                            int sindex = line.indexOf("员工");
                            line = line.substring(sindex);
                            name = line.substring(0, line.indexOf("<")).replace("员工 ", "");
                        }
                        System.out.println(line);
                    }
                }
            }
            System.out.println(uid);
            System.out.println(name);
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
