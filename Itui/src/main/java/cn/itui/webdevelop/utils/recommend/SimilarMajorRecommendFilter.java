package cn.itui.webdevelop.utils.recommend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import cn.itui.webdevelop.model.MajorInfo;
import cn.itui.webdevelop.utils.EnDeCode;
import cn.itui.webdevelop.utils.exception.DatabaseException;

/**
 * 根据major的code进行刷选
 * @author jimmycai
 *
 */
public class SimilarMajorRecommendFilter implements MajorRecommendFilter{
	public static final int SAMECOLLEGE_MAJORCOUNT = 8;
	public static final int SAMEMAJOR_MAJORCOUNT = 4;
	private static final String DEGREE = "degree";
	private static final String MAJORID = "majorId";

	/**
	 * 对candidates根据code进行刷选
	 * @throws DatabaseException 
	 */
	public MajorRecommendResult recommendMajorFilter(List<HashMap<String, Object>> candidates, String code, int majorId) throws Exception {
		if(candidates == null || code == null)
			throw DatabaseException.getInstance();
		int simiCount = 0;
		int nearCount = 0;
		int corrCount = 0;
		int tranCount = 0;
		HashMap<String, ArrayList<HashMap<String, Object>>> codeDataMaps = new HashMap<String, ArrayList<HashMap<String,Object>>>();
		for(HashMap<String, Object> curData : candidates) {
			if((Integer)curData.get("id") != majorId){
				String curCode = (String)curData.get("code");
				if(codeDataMaps.containsKey(curCode)) {
					codeDataMaps.get(curCode).add(curData);
				}
				else {
					ArrayList<HashMap<String, Object>> tmpDatas = new ArrayList<HashMap<String,Object>>();
					tmpDatas.add(curData);
					codeDataMaps.put(curCode, tmpDatas);
				}
			}
		}
		ArrayList<HashMap<String, Object>> retMajors = new ArrayList<HashMap<String,Object>>();
		//增加code一样的major
		if(codeDataMaps.containsKey(code)) {
			addArrays(retMajors, codeDataMaps.get(code),0);
			simiCount = retMajors.size();
			codeDataMaps.remove(code);
		}
		
		if(retMajors.size() < SAMECOLLEGE_MAJORCOUNT) {
			//增加code前4位一样的
			HashMap<String, ArrayList<HashMap<String, Object>>> tmpCodeDataMaps = (HashMap<String, ArrayList<HashMap<String, Object>>>) codeDataMaps.clone();
			Set<Entry<String, ArrayList<HashMap<String, Object>>>> codeDataSets = tmpCodeDataMaps.entrySet();
			for(Entry<String, ArrayList<HashMap<String, Object>>> curEntry : codeDataSets) {
				if(curEntry.getKey().substring(0, 4).equals(code.substring(0, 4))) {
					addArrays(retMajors, curEntry.getValue(),1);
					codeDataMaps.remove(curEntry.getKey());
				}
				if(retMajors.size() >= SAMECOLLEGE_MAJORCOUNT) {
					nearCount = SAMECOLLEGE_MAJORCOUNT - simiCount;
					return new MajorRecommendResult(retMajors, simiCount, nearCount, corrCount, tranCount);
				}
			}
			//增加code前两位一样的
			if(retMajors.size() < SAMECOLLEGE_MAJORCOUNT) {
				nearCount = retMajors.size() - simiCount;
				tmpCodeDataMaps = (HashMap<String, ArrayList<HashMap<String, Object>>>) codeDataMaps.clone();
				codeDataSets = tmpCodeDataMaps.entrySet();
				for(Entry<String, ArrayList<HashMap<String, Object>>> curEntry : codeDataSets) {
					if(curEntry.getKey().substring(0, 2).equals(code.substring(0, 2))) {
						addArrays(retMajors, curEntry.getValue(),2);
						codeDataMaps.remove(curEntry.getKey());
					}
					if(retMajors.size() >= SAMECOLLEGE_MAJORCOUNT) {
						corrCount = SAMECOLLEGE_MAJORCOUNT - simiCount - nearCount;
						return new MajorRecommendResult(retMajors, simiCount, nearCount, corrCount, tranCount);
					}
				}
			}
		}
		//如果还不够
		if(retMajors.size() < SAMECOLLEGE_MAJORCOUNT)
			corrCount = retMajors.size() - simiCount - nearCount;
		return new MajorRecommendResult(retMajors, simiCount, nearCount, corrCount, tranCount);
	}
	
	private void addArrays(ArrayList<HashMap<String, Object>> retMajors, ArrayList<HashMap<String, Object>> toAdd, int color) {
		if(retMajors == null || toAdd == null)
			return;
		for(HashMap<String, Object> curMap : toAdd) {
			if(retMajors.size() >= SAMECOLLEGE_MAJORCOUNT)
				return;
			addOneElement(retMajors, curMap, color);
		}
	}
	
	private void addOneElement(List<HashMap<String, Object>> retMajors, HashMap<String, Object> toAdd, int color) {
		if(retMajors == null || toAdd == null)
			return;
		HashMap<String, Object> majorCurInfo = new HashMap<String, Object>();
		majorCurInfo.put("majorId", EnDeCode.encodePara((Integer)toAdd.get("id")));
		majorCurInfo.put("majorName", toAdd.get("name"));
		majorCurInfo.put("schoolName", toAdd.get("school"));
		majorCurInfo.put("color", color);
		double rate = (Double) toAdd.get("rate");
		majorCurInfo.put("value", MajorInfo.translateRate(rate, true));
		retMajors.add(majorCurInfo);
	}

	/**
	 * 在前两位一样的所有major都不够的时候增加该学校的别的major
	 * 先找code前两位相差小的，如果不够就依次增加code相差的大小
	 * @throws DatabaseException 
	 */
	public MajorRecommendResult recommendMajorFilter(
			MajorRecommendResult recommendMajors,
			List<HashMap<String, Object>> candidateMajors, int collegeId, int majorId,
			String code) throws Exception {
		if(recommendMajors == null || candidateMajors == null || code == null)
			throw DatabaseException.getInstance();
		if(candidateMajors.size() <= (SAMECOLLEGE_MAJORCOUNT - recommendMajors.getMajors().size())){
			addArrays((ArrayList<HashMap<String,Object>>)recommendMajors.getMajors(), (ArrayList<HashMap<String,Object>>)candidateMajors, 3);
			return recommendMajors;
		}
		int preCode = Integer.parseInt(code.substring(0, 2));
		int I = 2;
		while(recommendMajors.getMajors().size() <= SAMECOLLEGE_MAJORCOUNT) {
			for(HashMap<String, Object> curMap : candidateMajors) {
				if((Integer)curMap.get("id") != majorId) {
					try{
						String curCode = (String)curMap.get("code");
						int curPreCode = Integer.parseInt(curCode.substring(0, 2));
						if(Math.abs(curPreCode - preCode) < I) {
							addOneElement(recommendMajors.getMajors(), curMap, 3);
						}
						if(recommendMajors.getMajors().size() >= SAMECOLLEGE_MAJORCOUNT)
							return recommendMajors;
					}catch(Exception e) {
						continue;
					}
				}
			}
		I++;
		}
		return recommendMajors;
	}

	/**
	 * 根据degree进行major的刷选
	 */
	public List<HashMap<String, Object>> recommendMajorFilter(
			List<HashMap<String, Object>> candidates, double degree) throws Exception{
		if(candidates == null)
			throw DatabaseException.getInstance();
		List<HashMap<String, Object>> resultMaps = new ArrayList<HashMap<String,Object>>();
		double curDegree = 0;
		double lastDegree = 0;
		if(candidates.size() == 0)
			return new ArrayList<HashMap<String,Object>>();
		curDegree = (Double)candidates.get(0).get(DEGREE);
		int index = 0;
		int i = 1;
		for(; i < candidates.size(); i++) {
			lastDegree = curDegree;
			curDegree = (Double)candidates.get(i).get(DEGREE);
			if(degree < curDegree && degree > lastDegree) {
				index = i;
			}
		}
		if(i == candidates.size())
			index = candidates.size() - 1;
		HashMap<String, Object> tmpMap = candidates.get(index);
		double curAADegree = (Double)tmpMap.get(DEGREE);	
		int majorId = (Integer)tmpMap.get(MAJORID);
		tmpMap.put(DEGREE, MajorInfo.formatDegree(curAADegree));
		tmpMap.put(MAJORID, EnDeCode.encodePara(majorId));
		resultMaps.add(tmpMap);
		for(int length = 1; resultMaps.size() < candidates.size(); length++) {
			if(index - length >= 0) {
				tmpMap = candidates.get(index - length);
				curAADegree = (Double)tmpMap.get(DEGREE);	
				majorId = (Integer)tmpMap.get(MAJORID);
				tmpMap.put(DEGREE, MajorInfo.formatDegree(curAADegree));
				tmpMap.put(MAJORID, EnDeCode.encodePara(majorId));
				resultMaps.add(tmpMap);
				if(resultMaps.size() >= SAMEMAJOR_MAJORCOUNT)
					return resultMaps;
			}
			if(index + length < candidates.size()) {
				tmpMap = candidates.get(index + length);
				curAADegree = (Double)tmpMap.get(DEGREE);	
				majorId = (Integer)tmpMap.get(MAJORID);
				tmpMap.put(DEGREE, MajorInfo.formatDegree(curAADegree));
				tmpMap.put(MAJORID, EnDeCode.encodePara(majorId));
				resultMaps.add(tmpMap);
				if(resultMaps.size() >= SAMEMAJOR_MAJORCOUNT)
					return resultMaps;
			}
		}
		
		return resultMaps;
	}

}
