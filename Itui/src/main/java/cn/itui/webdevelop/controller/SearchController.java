/**
 * this controller is for searching
 */
package cn.itui.webdevelop.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cn.itui.webdevelop.service.CollegeService;
import cn.itui.webdevelop.service.MajorService;

@Controller
public class SearchController{
	
	private MajorService majorService;
	private CollegeService collegeService;
	
	@RequestMapping(value=URLConstants.SEARCH, method=RequestMethod.POST)
	public String search(HttpServletRequest request, HttpServletResponse response) throws Exception{

//		System.out.println(request.get);
		int type=Integer.parseInt(request.getParameter("t"));
		String condition = request.getParameter("c");
		
		System.out.println(condition);
		
//		Map<String, String[]> postMap = request.getParameterMap();
//		String category = postMap.get("cg")[0];
//		String subject = postMap.get("sj")[0];
//		String college_type = postMap.get("ct")[0];
//		String major_type = postMap.get("mt")[0];
//		String area = postMap.get("a")[0];
		
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
		String json = "";
		if (br != null){
			json = br.readLine();
		}
		
//		Gson gson = new Gson();
//		Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
//		System.out.println(map.get("sj"));
		String category = request.getParameter("c");
		System.out.println(category);
		String subject = request.getParameter("sj");
		System.out.println(subject);
		String area = request.getParameter("a");
		String college_type = request.getParameter("ct");
		String major_type = request.getParameter("mt");

//		System.out.println(postMap.get("a"));
		
		String result;
		if (type==1){
			result = majorService.searchMajorsList(condition, category, subject, major_type, college_type, area);
		}else if (type==2){
			System.out.println("college");
			result = collegeService.searchCollegeList(condition);
		}else {
			//error
			result="";
		}
		return result;
	}

	public MajorService getMajorService() {
		return majorService;
	}

	public void setMajorService(MajorService majorService) {
		this.majorService = majorService;
	}

	public CollegeService getCollegeService() {
		return collegeService;
	}

	public void setCollegeService(CollegeService collegeService) {
		this.collegeService = collegeService;
	}
	
}
