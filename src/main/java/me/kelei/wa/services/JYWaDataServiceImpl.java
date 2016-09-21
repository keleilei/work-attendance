package me.kelei.wa.services;

import me.kelei.wa.entities.WaUser;
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
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by kelei on 2016/9/20.
 */
@Service
public class JYWaDataServiceImpl implements IJYWaDataService{


    public WaUser login(String pid, String password) {
        WaUser waUser = null;
        try{
            String url = "http://124.42.1.13:8000/iclock/accounts/login/";
            HttpClient client = HttpClientBuilder.create().build();

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
                    System.out.println(responseText);
                }
            }

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
                        if(line.contains("strong")){
                            int sindex = line.indexOf("strong");
                            line = line.substring(sindex);
                            name = line.substring(10, 12);
                        }
                    }
                    br.close();

                    if(!StringUtils.isEmpty(uid)){
                        waUser = new WaUser();
                        waUser.setUid(uid);
                        waUser.setPid(pid);
                        waUser.setUserName(name);
                        waUser.setUserPwd(password);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return waUser;
    }
}
