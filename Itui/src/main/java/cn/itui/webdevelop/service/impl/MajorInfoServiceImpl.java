package cn.itui.webdevelop.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import cn.itui.webdevelop.dao.CollegeDao;
import cn.itui.webdevelop.dao.MajorDao;
import cn.itui.webdevelop.dao.MajorInfoDao;
import cn.itui.webdevelop.dao.RetestDao;
import cn.itui.webdevelop.dao.ScoreDao;
import cn.itui.webdevelop.model.College;
import cn.itui.webdevelop.model.MajorInfo;
import cn.itui.webdevelop.model.Retest;
import cn.itui.webdevelop.service.MajorInfoService;
import cn.itui.webdevelop.utils.EnDeCode;
import cn.itui.webdevelop.utils.ResponseUtil;
import cn.itui.webdevelop.utils.exception.DatabaseException;
import cn.itui.webdevelop.utils.recommend.CollegeRecommendFilter;
import cn.itui.webdevelop.utils.recommend.MajorRecommendFilter;
import cn.itui.webdevelop.utils.recommend.MajorRecommendResult;
import cn.itui.webdevelop.utils.recommend.SimilarMajorRecommendFilter;

public class MajorInfoServiceImpl implements MajorInfoService{
	public static long majorAllInfoTime = 0;
	public static long yearScoreInfoTime = 0;
	public static long majorRecommendTime = 0;
	public static long majorRecommendDiffCTime = 0;
	public static long collegeRecommendTime = 0;
	private static final int N = 4;
	private CollegeDao collegeDao;
	private MajorDao majorDao;
	private MajorInfoDao majorInfoDao;
	private ScoreDao scoreDao;
	private RetestDao retestDao;
	private MajorRecommendFilter majorRecommendFilter;//对数据库查询得到的major数据进行过滤
	private CollegeRecommendFilter collegeRecommendFilter;//对数据库查询得到的college数据进行过滤

	public String getMajorInfo(HttpServletRequest request, int majorId) throws Exception {
		//get major main info, base info, college logo and rank info
		long ST = System.currentTimeMillis();
		HashMap<String, Object> majorAllInfos = majorInfoDao.findMajorAllInfoByMajorId(majorId);
		long ET = System.currentTimeMillis();
		majorAllInfoTime = ET - ST;
		if(majorAllInfos == null)
			throw DatabaseException.getInstance();
		String code = (String)majorAllInfos.get("code");
		int collegeId = (Integer)majorAllInfos.get("collegeId");
		
		//get year-score infos
		ST = System.currentTimeMillis();
		List<HashMap<String, Object>> yearScores = scoreDao.getLastNYearsScoreByMajorId(majorId, N);
		ET = System.currentTimeMillis();
		yearScoreInfoTime = ET - ST;

		//recommend majors
		ST = System.currentTimeMillis();
		List<HashMap<String, Object>> candidateMajors = majorDao.findCodeLikeMajorByCollegeId(code, collegeId);
		ET = System.currentTimeMillis();
		majorRecommendTime = ET - ST;
		MajorRecommendResult recommendMajors = majorRecommendFilter.recommendMajorFilter(candidateMajors, code, majorId);
		if(recommendMajors.getMajors().size() < SimilarMajorRecommendFilter.SAMECOLLEGE_MAJORCOUNT) {
			recommendMajors = processTransdisciplinary(recommendMajors, candidateMajors, collegeId, majorId, code);
		}
		//different college major recommend
		ST = System.currentTimeMillis();
		List<HashMap<String, Object>> candidateDiffCollMajors = majorDao.findAreaSameCodeMajorByCollegeIdAndMajorCode(collegeId, code);
		ET = System.currentTimeMillis();
		majorRecommendDiffCTime = ET - ST;
		List<HashMap<String, Object>> diffCollRecommendMajors = majorRecommendFilter.recommendMajorFilter(candidateDiffCollMajors, (Double)majorAllInfos.get("rate"));
		//recommend college
		int collegeRank = (Integer)majorAllInfos.get("rank");
		ST = System.currentTimeMillis();
		List<College> candidateColleges = collegeDao.findCollegeInRank(collegeRank, collegeId);
		ET = System.currentTimeMillis();
		collegeRecommendTime = ET - ST;
		List<HashMap<String, Object>> recommendColleges = collegeRecommendFilter.recommendCollege(candidateColleges, collegeRank);
		//build json string
		String jsonResult = buildMajorInfoJson(majorAllInfos, yearScores,recommendMajors, recommendColleges, diffCollRecommendMajors, request);
		return jsonResult;
	}
	
	public String getRetestInfo(int majorId) throws Exception {
		try{
			Retest retest = retestDao.findRetestByMajorId(majorId);
			String jsonResult = ResponseUtil.wrapNormalReturn(retest);
			return jsonResult;
		}catch(Exception e) {
			e.printStackTrace();
			throw DatabaseException.getInstance();
		}
	}
	
	private MajorRecommendResult processTransdisciplinary(MajorRecommendResult recommendMajors, List<HashMap<String, Object>> candidateMajors, int collegeId, int majorId, String code) throws Exception{
		int needCount = SimilarMajorRecommendFilter.SAMECOLLEGE_MAJORCOUNT - recommendMajors.getMajors().size();
		long ST = System.currentTimeMillis();
		List<HashMap<String, Object>> allMajors = majorDao.findMajorByCollegeIdAndNotInMajorIds(collegeId, candidateMajors);
		long ET = System.currentTimeMillis();
		majorRecommendTime += ET - ST;
		if(allMajors.size() >= needCount)
			recommendMajors.setTransdisciplinaryCount(needCount);
		recommendMajors = majorRecommendFilter.recommendMajorFilter(recommendMajors, allMajors, collegeId, majorId, code);
		
		return recommendMajors;
	}
	
	private String buildMajorInfoJson(HashMap<String, Object> majorAllInfos, List<HashMap<String, Object>> yearScores, 
			MajorRecommendResult recommendMajors, List<HashMap<String, Object>> recommendColleges, List<HashMap<String, Object>> diffCollRecommendMajors, HttpServletRequest request) throws Exception {
		LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<String, Object>();
		//base info
		LinkedHashMap<String, Object> baseInfoMap = new LinkedHashMap<String, Object>();
		baseInfoMap.put("collegeIndexPage", College.COLLEGE_URL + EnDeCode.encodePara((Integer)majorAllInfos.get("collegeId")));
		//grade info
		LinkedHashMap<String, Object> gradeInfoMap = new LinkedHashMap<String, Object>();
		gradeInfoMap.put("grade", majorAllInfos.get("grade"));
		gradeInfoMap.put("rateGrade", majorAllInfos.get("rateGrade"));
		gradeInfoMap.put("scoreGrade", majorAllInfos.get("scoreGrade"));
		gradeInfoMap.put("collegeGrade", majorAllInfos.get("collegeGrade"));
		gradeInfoMap.put("cityGrade", majorAllInfos.get("cityGrade"));
		//rank info
		LinkedHashMap<String, String> rankInfoMap = new LinkedHashMap<String, String>();
		rankInfoMap.put("majorRank", MajorInfo.translateRank(majorAllInfos.get("majorRank")));
		rankInfoMap.put("collegeRank", MajorInfo.translateRank(majorAllInfos.get("rank")));
		rankInfoMap.put("collegeLocalRank", MajorInfo.translateRank(majorAllInfos.get("localRank")));
		//score info
		LinkedHashMap<String, Object> scoreInfoMap = new LinkedHashMap<String, Object>();
		scoreInfoMap.put("yearScoreInfo", yearScores);
		scoreInfoMap.put("scoreAvg", majorAllInfos.get("scoreAvg"));
		scoreInfoMap.put("scoreLowYear", majorAllInfos.get("scoreLowYear"));
		scoreInfoMap.put("scoreLow", majorAllInfos.get("scoreLow"));
		scoreInfoMap.put("scoreHigh", majorAllInfos.get("scoreHigh"));
		scoreInfoMap.put("trend", majorAllInfos.get("trend"));
		//competition info
		LinkedHashMap<String, String> competitionInfoMap = new LinkedHashMap<String, String>();
		competitionInfoMap.put("degree", MajorInfo.formatDegree((Double)majorAllInfos.get("degree")));
		competitionInfoMap.put("rateDegree", MajorInfo.translateDegree((String)majorAllInfos.get("rateDegree")));
		competitionInfoMap.put("scoreDegree", MajorInfo.translateDegree((String)majorAllInfos.get("scoreDegree")));
		competitionInfoMap.put("collegeDegree", MajorInfo.translateDegree((String)majorAllInfos.get("collegeDegree")));
		competitionInfoMap.put("cityDegree", MajorInfo.translateDegree((String)majorAllInfos.get("cityDegree")));
		competitionInfoMap.put("degreeDescription", (String)majorAllInfos.get("degreeDescription"));
		//applyAdmit info
		LinkedHashMap<String, Object> applyAdmitInfoMap = new LinkedHashMap<String, Object>();
		applyAdmitInfoMap.put("rate", MajorInfo.translateRate((Double)majorAllInfos.get("rate")));
		applyAdmitInfoMap.put("applyDescription", majorAllInfos.get("applyDescription"));
		applyAdmitInfoMap.put("admitDescription", majorAllInfos.get("admitDescription"));
		applyAdmitInfoMap.put("applyCount", majorAllInfos.get("applyNum"));
		applyAdmitInfoMap.put("admitCount", majorAllInfos.get("admitNum"));
		applyAdmitInfoMap.put("exemptionCount", majorAllInfos.get("exemption"));
		//major recommend info
		LinkedHashMap<String, Object> majorRecommendMap = new LinkedHashMap<String, Object>();
		majorRecommendMap.put("mainInfo", recommendMajors.getMajors());
		majorRecommendMap.put("similarCount", recommendMajors.getSimiliarCount());
		majorRecommendMap.put("nearCount", recommendMajors.getNearCount());
		majorRecommendMap.put("correlateCount", recommendMajors.getCorrelateCount());
		majorRecommendMap.put("transdisciplinaryCount", recommendMajors.getTransdisciplinaryCount());
		
		//college recommend info
		LinkedHashMap<String, Object> collegeRecommendMap = new LinkedHashMap<String, Object>();
		collegeRecommendMap.put("mainInfo", recommendColleges);
		
		//different college same major recommend info
		LinkedHashMap<String, Object> diffCollMajorRecommendMap = new LinkedHashMap<String, Object>();
		diffCollMajorRecommendMap.put("mainInfo", diffCollRecommendMajors);
		
		jsonMap.put("baseInfo", baseInfoMap);
		jsonMap.put("gradeInfo", gradeInfoMap);
		jsonMap.put("rankInfo", rankInfoMap);
		jsonMap.put("scoreInfo", scoreInfoMap);
		jsonMap.put("competitionInfo", competitionInfoMap);
		jsonMap.put("applyAdmitInfo", applyAdmitInfoMap);
		jsonMap.put("majorRecommendInfo", majorRecommendMap);
		jsonMap.put("interestedMajorInfo", diffCollMajorRecommendMap);
		jsonMap.put("interestedCollegeInfo", collegeRecommendMap);

		String jsonStr = ResponseUtil.wrapNormalReturn(jsonMap);;
		return jsonStr;
	}

	public CollegeDao getCollegeDao() {
		return collegeDao;
	}

	public void setCollegeDao(CollegeDao collegeDao) {
		this.collegeDao = collegeDao;
	}

	public MajorDao getMajorDao() {
		return majorDao;
	}

	public void setMajorDao(MajorDao majorDao) {
		this.majorDao = majorDao;
	}

	public MajorInfoDao getMajorInfoDao() {
		return majorInfoDao;
	}

	public void setMajorInfoDao(MajorInfoDao majorInfoDao) {
		this.majorInfoDao = majorInfoDao;
	}

	public ScoreDao getScoreDao() {
		return scoreDao;
	}

	public void setScoreDao(ScoreDao scoreDao) {
		this.scoreDao = scoreDao;
	}

	public MajorRecommendFilter getMajorRecommendFilter() {
		return majorRecommendFilter;
	}

	public void setMajorRecommendFilter(MajorRecommendFilter majorRecommendFilter) {
		this.majorRecommendFilter = majorRecommendFilter;
	}

	public CollegeRecommendFilter getCollegeRecommendFilter() {
		return collegeRecommendFilter;
	}

	public void setCollegeRecommendFilter(
			CollegeRecommendFilter collegeRecommendFilter) {
		this.collegeRecommendFilter = collegeRecommendFilter;
	}

	public RetestDao getRetestDao() {
		return retestDao;
	}

	public void setRetestDao(RetestDao retestDao) {
		this.retestDao = retestDao;
	}

}
