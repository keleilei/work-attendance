package me.kelei.wa.oldversion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class WorkAttendance {
	
	private Map<String, Object> globalMap = new HashMap<String, Object>();
	private Map<String,String> leftMap = new HashMap<String, String>();
	private Map<String,String> rightMap = new HashMap<String, String>();
	private String fromDate;
	private String toDate;
	
	public Map<String, Object> httpClientLogin(String userName, String userPwd, String fromDate, String toDate, String[][] leftArry, String[][] rightArry) throws Exception{
		this.fromDate = fromDate;
		this.toDate = toDate;
		
		initMap(leftArry, rightArry);
		
		String url = "http://124.42.1.13:8000/iclock/accounts/login/";
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
					System.out.println(line);
				}
			}
		}
		
		//获取用户ID
		HttpGet get = new HttpGet("http://1124.42.1.13:8000/iclock/staff/");
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
						globalMap.put("userName", name);
					}
				}
			}
		}
		
		//获取cookie
		List<Cookie> cookies = ((AbstractHttpClient) client).getCookieStore()
				.getCookies();
		String cookieName = "";
		String cookieValue = "";
		for (Cookie c : cookies) {
			cookieName = c.getName();
			cookieValue = c.getValue();
		}
		
		//获取数据
		List<String[]> dataList = new ArrayList<String[]>();
		int pageNumber = this.getDataList(dataList, uid, cookieName, cookieValue, "1");
		if(pageNumber > 1){
			for(int i = 2; i <= pageNumber; i++){
				this.getDataList(dataList, uid, cookieName, cookieValue, String.valueOf(i));
			}
		}
		
		//处理数据
		List<String[]> list = processData(dataList);
		String[][] dataArr = new String[list.size()][5];
		for(int i = 0; i < list.size(); i++){
			String[] arr = list.get(i);
			dataArr[i] = arr;
		}
		
		globalMap.put("dataArry", dataArr);
		
		return globalMap;
	}
	
	private int getDataList(List<String[]> dataList, String uid, String cookieName, String cookieValue, String pn) throws IOException{
		
		String kgUrl = "http://124.42.1.13:8000/iclock/staff/transaction/?p="+pn+"&t=staff_transaction.html&UserID__id__exact=" + uid;
		kgUrl += "&fromTime=" + fromDate + "&toTime=" + toDate;
		
		Document document = Jsoup.connect(kgUrl).cookie(cookieName, cookieValue).get();
		Elements tbody = document.select("tr[class^=row]");
		String script = document.data();
		String tmpScrpit = script.substring(script.indexOf("page_number_emp"));
		String pageNumber = tmpScrpit.substring(0, tmpScrpit.indexOf(";")).replace("page_number_emp=", "");
		
		for(int i = 0; i < tbody.size(); i++){
			Elements tds = tbody.get(i).getElementsByTag("td");
			String[] dataArr = new String[5]; 
			for(int j = 1; j < tds.size(); j++){
				dataArr[j-1] = tds.get(j).text();
			}
			dataList.add(dataArr);
		}
		return Integer.valueOf(pageNumber);
	}
	
	private void initMap(String[][] leftArry, String[][] rightArry){
		for(int i = 0; i < leftArry.length; i++){
			if(!isEmpty(leftArry[i][0])){
				leftMap.put(leftArry[i][0], leftArry[i][0]);
			}
		}
		for(int i = 0; i < rightArry.length; i++){
			if(!isEmpty(rightArry[i][0])){
				rightMap.put(rightArry[i][0], rightArry[i][0]);
			}
		}
	}
	
	private List<String[]> processData(List<String[]> dataList) throws Exception{
		
		//数据分类
		List<List<String[]>> categoryDateList = this.classifyData(dataList);
		
		globalMap.put("recordDay", categoryDateList.size());
		
		//数据过滤
		this.filterData(categoryDateList);
		
		//考勤验证
		List<String[]> list = this.workState(categoryDateList);
		
		return list;
	}
	
	private List<List<String[]>> classifyData(List<String[]> dataList) throws Exception{
		List<List<String[]>> categoryDateList = new ArrayList<List<String[]>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if(dataList != null && !dataList.isEmpty()){
			String dateStr = dataList.get(0)[0].substring(0, 10);
			List<String[]> tmpList = new ArrayList<String[]>();
			for(int i = 0; i < dataList.size(); i++){
				String[] lineArr = dataList.get(i);
				String tmpDateStr = lineArr[0].substring(0, 10);
				if(dateStr.equals(tmpDateStr)){
					tmpList.add(lineArr);
				}else{
					String limitDateStr = tmpDateStr + " 03:00:00";
					if(sdf.parse(lineArr[0]).getTime() < sdf.parse(limitDateStr).getTime()){
						tmpList.add(lineArr);
					}else{
						categoryDateList.add(tmpList);
						tmpList = new ArrayList<String[]>();
						tmpList.add(lineArr);
					}
				}
				dateStr = tmpDateStr;
				if(i == (dataList.size() - 1)){
					categoryDateList.add(tmpList);
				}
			}
			
		}
		return categoryDateList;
	}
	
	private void filterData(List<List<String[]>> categoryDateList) throws Exception{
		for(int i = 0; i < categoryDateList.size(); i++){
			List<String[] > ls = categoryDateList.get(i);
			if(ls.size() > 2){
				ls = this.orderData(ls);
				categoryDateList.set(i, ls);
			}
		}
	}
	
	private List<String[]> orderData(List<String[]> dataList) throws Exception{
		List<String[]> newList = new ArrayList<String[]>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int minDateIndex = 0;
		int maxDateIndex = 0;
		Date minDate = null;
		Date maxDate = null;
		for(int i = 0; i < dataList.size(); i++){
			String[] data = dataList.get(i);
			if(data[0] != null && !"".equals(data[0])){
				Date tmpDate = sdf.parse(data[0]);
				//比较最小日期
				if(minDate == null){
					minDate = tmpDate;
				}
				if(minDate.getTime() > tmpDate.getTime()){
					minDate = tmpDate;
					minDateIndex = i;
				}
				
				//比较最大日期
				if(maxDate == null){
					maxDate = tmpDate;
				}
				if(minDate.getTime() < tmpDate.getTime()){
					maxDate = tmpDate;
					maxDateIndex = i;
				}
				
			}
		}
		newList.add(dataList.get(minDateIndex));
		newList.add(dataList.get(maxDateIndex));
		
		return newList;
	}
	
	private List<String[]> workState(List<List<String[]>> categoryDateList) throws Exception{
		List<String[]> list = new ArrayList<String[]>();
		List<String> workDateList = this.getWorkDate(fromDate, toDate);
		
		globalMap.put("workDay", workDateList.size());
		
		Date curDate = new Date();
		int lateCount = 0;//迟到次数
		int earlyCount = 0;//早退次数
		int absenteeismCount = 0;//旷工次数
		int forgetCount = 0; //忘打卡次数
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for(int k = 0; k < workDateList.size(); k++){
			String workDate = workDateList.get(k);
			Date am = sdf.parse(workDate + " 09:06:00");
			Date pm = sdf.parse(workDate + " 18:00:00");
			boolean isWork = false;
			for(int i = 0; i < categoryDateList.size(); i++){
				List<String[]> ls = categoryDateList.get(i);
				for(int j = 0; j < ls.size(); j++){
					String[] arr = ls.get(j);
					if(arr[0].substring(0, 10).equals(workDate)){
						if(j == 0){
							Date trueAm = sdf.parse(arr[0]);
							if(trueAm.getTime() < am.getTime()){
								arr[4] = "正常";
							}else{
								arr[4] = "迟到";
								lateCount++;
							}
						}else{
							Date truePm = sdf.parse(arr[0]);
							SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
							String dateStr = arr[0];
							arr[1] = "下班签退";
							if(datef.format(new Date()).equals(dateStr.substring(0, 10))){
								if(truePm.getTime() > pm.getTime()){
									arr[4] = "正常";
								}else{
									arr[0] = workDate + " 18:00:00";
									arr[2] = "无";
									arr[3] = "无";
									arr[4] = "未下班";
								}
							}else{
								if(truePm.getTime() > pm.getTime()){
									arr[4] = "正常";
								}else{
									arr[4] = "早退";
									earlyCount++;
								}
							}
						}
						isWork = true;
						if(ls.size() == 1){
							if(sdf.parse(arr[0]).getTime() > sdf.parse(workDate + " 12:00:00").getTime()){
								String[] tmp = new String[]{workDate + " 09:00:00","上班签到","无","无","忘打卡"};
								list.add(tmp);
								list.add(arr);
								forgetCount++;
							}else{
								list.add(arr);
								if(curDate.getTime() < pm.getTime()){
									String[] tmp = new String[]{workDate + " 18:00:00","下班签退","无","无","未下班"};
									list.add(tmp);
								}else{
									String[] tmp = new String[]{workDate + " 18:00:00","下班签退","无","无","忘打卡"};
									list.add(tmp);
									forgetCount++;
								}
							}
						}else{
							list.add(arr);
						}
					}
				}
			}
			if(!isWork){
				if(curDate.getTime() > pm.getTime()){
					String[] tmp = new String[]{workDate + " 09:00:00","无","无","无","旷工"};
					list.add(tmp);
					absenteeismCount++;
				}else{
					String[] tmp = new String[]{workDate + " 09:00:00","无","无","无","无"};
					list.add(tmp);
				}
			}
		}
		
		globalMap.put("lateCount", lateCount);
		globalMap.put("earlyCount", earlyCount);
		globalMap.put("absenteeismCount", absenteeismCount);
		globalMap.put("forgetCount", forgetCount);
		
		return list;
	}
	
	private List<String> getWorkDate(String fromDateStr, String toDateStr) throws Exception{
		
		List<String> workList = new ArrayList<String>();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date fromDate = sdf.parse(fromDateStr);
		Date toDate = sdf.parse(toDateStr);
		SimpleDateFormat dateFm = new SimpleDateFormat("EEEE");
		
		GregorianCalendar gc=new GregorianCalendar(); 
		gc.setTime(fromDate);
		while(gc.getTime().getTime() <= toDate.getTime()){
			String tmpDate = sdf.format(gc.getTime());
			String isWorkDate = rightMap.get(tmpDate);
			if(isEmpty(isWorkDate)){
				String weekStr = dateFm.format(gc.getTime());
				if(!("星期日".equals(weekStr) || "星期六".equals(weekStr))){
					String isNotWork = leftMap.get(tmpDate);
					if(isEmpty(isNotWork)){
						workList.add(tmpDate);
					}
				}
			}else{
				workList.add(tmpDate);
			}
			gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
		}
		
		return workList;
	}
	
	public boolean isEmpty(String s) {
		return s == null || s.trim().length() == 0;
	}
	
//	public static void main(String[] args) throws Exception {
//		new WorkAttendance().httpClientLogin("1098", "ketingjiang", "2014-06-01","2014-06-30");
//	}
}
