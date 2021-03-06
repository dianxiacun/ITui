package cn.itui.webdevelop.service;

import cn.itui.webdevelop.utils.exception.DatabaseException;

public interface CollegeService {
	public String searchCollegeList(String condition, String area, String college_type, int from);
	public String findSchoolsById(String code, int CollegeId) throws DatabaseException;
	public String findMajorsBySchool(int collegeId, String school);
}
