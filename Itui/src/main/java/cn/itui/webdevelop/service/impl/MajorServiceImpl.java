package cn.itui.webdevelop.service.impl;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.itui.webdevelop.dao.MajorDao;
import cn.itui.webdevelop.service.MajorService;
import cn.itui.webdevelop.utils.WordParticiple;

public class MajorServiceImpl implements MajorService {
	private MajorDao majorDao;
	private int limit;
	
	public String searchMajorsList(String condition, String category,
			String subject, String major_type, String college_type, String area) {

		String is985 = "";
		String is34 = "";
		String is211 = "";
		if (!(college_type.equalsIgnoreCase("") || college_type.startsWith("其他"))) {
			if (college_type.startsWith("985")) {
				// 985
				is985 = "1";
			} 
			if (college_type.startsWith("34")) {
				// 34
				is34 = "1";
			} 
			if (college_type.startsWith("211")) {
				// 211
				is211 = "1";
			}
		}else if (college_type.startsWith("其他")){
			is211=is34=is985="0";
		}
//		is211=is34=is985=WordParticiple.filterAll(college_type);
		is211 = WordParticiple.filterAll(is211);
		is34 = WordParticiple.filterAll(is34);
		is985 = WordParticiple.filterAll(is985);		
		
		String type="";
		if (major_type.startsWith("学")){
			type="1";
		}else if (major_type.startsWith("专")) {
			type="2";
		}else {
			type = WordParticiple.filterAll(major_type);
		}
		
		System.out.println(is211+is985+is34);

		condition = WordParticiple.participle(condition);
		System.out.println(condition);
		// 全部
		category = WordParticiple.filterAll(category);
		subject = WordParticiple.filterAll(subject);
		area = WordParticiple.filterAll(area);
		
		List<HashMap<String, Object>> majorList = majorDao.searchMajors(condition, category, subject, is985, is211, is34, type, area, limit);
		for (int i = 0; i < majorList.size(); i++){
			HashMap<String, Object> map = majorList.get(i);
			int rank = (Integer)map.get("rank");
			if (rank > 1000) 
				map.put("rank", rank%1000+"+");
		}
		String json = buildJson(majorList);
		return json;
	}

	private String buildJson(List<HashMap<String, Object>> majorList) {
		// TODO Auto-generated method stub
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("num", majorList.size());
		map.put("list", majorList);
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(map);
	}

	public MajorDao getMajorDao() {
		return majorDao;
	}

	public void setMajorDao(MajorDao majorDao) {
		this.majorDao = majorDao;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}