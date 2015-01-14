package cn.itui.webdevelop.service.impl;

import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import cn.itui.webdevelop.dao.CollegeDao;
import cn.itui.webdevelop.dao.MajorDao;
import cn.itui.webdevelop.dao.MajorInfoDao;
import cn.itui.webdevelop.dao.ScoreDao;
import cn.itui.webdevelop.model.College;
import cn.itui.webdevelop.model.Major;
import cn.itui.webdevelop.model.MajorInfo;
import cn.itui.webdevelop.service.MajorInfoService;
import cn.itui.webdevelop.utils.recommend.CollegeRecommendFilter;
import cn.itui.webdevelop.utils.recommend.MajorRecommendFilter;
import cn.itui.webdevelop.utils.recommend.MajorRecommendResult;
import cn.itui.webdevelop.utils.recommend.SimilarMajorRecommendFilter;

public class MajorInfoServiceImpl implements MajorInfoService{
	private static final int N = 4;
	private CollegeDao collegeDao;
	private MajorDao majorDao;
	private MajorInfoDao majorInfoDao;
	private ScoreDao scoreDao;
	private MajorRecommendFilter majorRecommendFilter;
	private CollegeRecommendFilter collegeRecommendFilter;

	public String getMajorInfo(int id) throws Exception {
		//get major main info
		MajorInfo majorMainInfo = majorInfoDao.getMajorInfoById(id);
		//get college logo and rank info
		HashMap<String, Object> collegeLogoAndRank = collegeDao.findLogoAndRankByMajorId(id);
		//get year-score infos
		List<HashMap<String, Object>> yearScores = scoreDao.getLastNYearsScoreByMajorId(id, N);
		//get major base info
		Major majorBaseInfo = majorDao.findMajorById(id);
		List<HashMap<String, Object>> candidateMajors = majorDao.findCodeLikeMajorByCollegeId(majorBaseInfo.getCode(), majorBaseInfo.getCollegeId());
		MajorRecommendResult recommendMajors = majorRecommendFilter.recommendMajorFilter(candidateMajors, majorBaseInfo.getCode(), id);
		if(recommendMajors.getMajors().size() < SimilarMajorRecommendFilter.SAMECOLLEGE_MAJORCOUNT) {
			recommendMajors = processTransdisciplinary(recommendMajors, candidateMajors, majorBaseInfo.getCollegeId(), id, majorBaseInfo.getCode());
		}
		//different college major recommend
		List<HashMap<String, Object>> candidateDiffCollMajors = majorDao.findAreaSameCodeMajorByCollegeIdAndMajorCode(majorBaseInfo.getCollegeId(), majorBaseInfo.getCode());
		List<HashMap<String, Object>> diffCollRecommendMajors = majorRecommendFilter.recommendMajorFilter(candidateDiffCollMajors, majorMainInfo.getRate());
		//recommend college
		System.out.println("college logo:" + collegeLogoAndRank.get("logo"));
		System.out.println("college rank:" + collegeLogoAndRank.get("rank"));
		System.out.println("college local_rank:" + collegeLogoAndRank.get("localRank"));
		int collegeRank = (Integer)collegeLogoAndRank.get("rank");
		int collegeId = (Integer)collegeLogoAndRank.get("id");
		List<College> candidateColleges = collegeDao.findCollegeInRank(collegeRank, collegeId);
		List<HashMap<String, Object>> recommendColleges = collegeRecommendFilter.recommendCollege(candidateColleges, collegeRank);
		//build json string
		String jsonResult = buildJson(majorMainInfo, collegeLogoAndRank, yearScores,recommendMajors, recommendColleges, diffCollRecommendMajors);
		return jsonResult;
	}
	
	private MajorRecommendResult processTransdisciplinary(MajorRecommendResult recommendMajors, List<HashMap<String, Object>> candidateMajors, int collegeId, int majorId, String code) {
		int needCount = SimilarMajorRecommendFilter.SAMECOLLEGE_MAJORCOUNT - recommendMajors.getMajors().size();
		List<HashMap<String, Object>> allMajors = majorDao.findMajorByCollegeIdAndNotInMajorIds(collegeId, candidateMajors);
		if(allMajors.size() >= needCount)
			recommendMajors.setTransdisciplinaryCount(needCount);
		recommendMajors = majorRecommendFilter.recommendMajorFilter(recommendMajors, allMajors, collegeId, majorId, code);
		return recommendMajors;
	}
	
	private String buildJson(MajorInfo majorMainInfo, HashMap<String, Object> logoAndRank, List<HashMap<String, Object>> yearScores, 
			MajorRecommendResult recommendMajors, List<HashMap<String, Object>> recommendColleges, List<HashMap<String, Object>> diffCollRecommendMajors) {
		HashMap<String, Object> jsonMap = new HashMap<String, Object>();
		//base info
		HashMap<String, Object> baseInfoMap = new HashMap<String, Object>();
		baseInfoMap.put("collegeIndexPage", logoAndRank.get("logo"));
		//grade info
		HashMap<String, String> gradeInfoMap = new HashMap<String, String>();
		gradeInfoMap.put("grade", majorMainInfo.getGrade());
		gradeInfoMap.put("rateGrade", majorMainInfo.getRateGrade());
		gradeInfoMap.put("scoreGrade", majorMainInfo.getScoreGrade());
		gradeInfoMap.put("collegeGrade", majorMainInfo.getCollegeGrade());
		gradeInfoMap.put("cityGrade", majorMainInfo.getCityGrade());
		//rank info
		HashMap<String, String> rankInfoMap = new HashMap<String, String>();
		rankInfoMap.put("majorRank", majorMainInfo.translateMajorRank());
		rankInfoMap.put("collegeRank", translaterank(logoAndRank.get("rank")));
		rankInfoMap.put("collegeLocalRank", translaterank(logoAndRank.get("localRank")));
		//score info
		HashMap<String, Object> scoreInfoMap = new HashMap<String, Object>();
		scoreInfoMap.put("yearScoreInfo", yearScores);
		scoreInfoMap.put("scoreAvg", majorMainInfo.getScoreAvg());
		scoreInfoMap.put("scoreLowYear", majorMainInfo.getScoreLowYear());
		scoreInfoMap.put("scoreLow", majorMainInfo.getScoreLow());
		scoreInfoMap.put("trend", majorMainInfo.getTrend());
		//competition info
		HashMap<String, String> competitionInfoMap = new HashMap<String, String>();
		competitionInfoMap.put("degree", majorMainInfo.getDegree().toString());
		competitionInfoMap.put("rateDegree", MajorInfo.translateDegree(majorMainInfo.getRateDegree()));
		competitionInfoMap.put("scoreDegree", MajorInfo.translateDegree(majorMainInfo.getScoreDegree()));
		competitionInfoMap.put("collegeDegree", MajorInfo.translateDegree(majorMainInfo.getCollegeDegree()));
		competitionInfoMap.put("cityDegree", MajorInfo.translateDegree(majorMainInfo.getCityDegree()));
		competitionInfoMap.put("degreeDescription", majorMainInfo.getDegreeDescription());
		//applyAdmit info
		HashMap<String, Object> applyAdmitInfoMap = new HashMap<String, Object>();
		applyAdmitInfoMap.put("rate", MajorInfo.translateRate(majorMainInfo.getRate()));
		applyAdmitInfoMap.put("applyDescription", majorMainInfo.getApplyDescription());
		applyAdmitInfoMap.put("admitDescription", majorMainInfo.getAdmitDescription());
		applyAdmitInfoMap.put("applyCount", majorMainInfo.getApplyNum()+"");
		applyAdmitInfoMap.put("admitCount", majorMainInfo.getAdmitNum()+"");
		applyAdmitInfoMap.put("exemptionCount", majorMainInfo.getExemption()+"");
		//major recommend info
		HashMap<String, Object> majorRecommendMap = new HashMap<String, Object>();
		majorRecommendMap.put("mainInfo", recommendMajors.getMajors());
		majorRecommendMap.put("similarCount", recommendMajors.getSimiliarCount());
		majorRecommendMap.put("nearCount", recommendMajors.getNearCount());
		majorRecommendMap.put("correlateCount", recommendMajors.getCorrelateCount());
		majorRecommendMap.put("transdisciplinaryCount", recommendMajors.getTransdisciplinaryCount());
		
		//college recommend info
		HashMap<String, Object> collegeRecommendMap = new HashMap<String, Object>();
		collegeRecommendMap.put("mainInfo", recommendColleges);
		
		//different college same major recommend info
		HashMap<String, Object> diffCollMajorRecommendMap = new HashMap<String, Object>();
		diffCollMajorRecommendMap.put("mainInfo", diffCollRecommendMajors);
		
		jsonMap.put("baseInfo", baseInfoMap);
		jsonMap.put("gradeInfo", gradeInfoMap);
		jsonMap.put("rankInfo", rankInfoMap);
		jsonMap.put("scoreInfo", scoreInfoMap);
		jsonMap.put("competitionInfo", competitionInfoMap);
		jsonMap.put("applyAdmitInfo", applyAdmitInfoMap);
		jsonMap.put("majorRecommendInfo", majorRecommendMap);
		jsonMap.put("interestedCollegeInfo", collegeRecommendMap);
		jsonMap.put("interestedMajorInfo", diffCollMajorRecommendMap);
		
		Gson gson = new Gson();
		String jsonStr = gson.toJson(jsonMap);
		return jsonStr;
	}
	
	private String translaterank(Object rank) {
		if(rank == null) {
			return null;
		}
		try {
			int rankI = Integer.parseInt((String)rank);
			if(rankI > 1000)
				return (rankI - 1000) + "";
			else
				return rankI + "";
		} catch (Exception e) {
			return null;
		}
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

}