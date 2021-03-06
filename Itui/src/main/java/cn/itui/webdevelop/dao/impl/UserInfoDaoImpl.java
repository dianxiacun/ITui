package cn.itui.webdevelop.dao.impl;

import java.sql.Timestamp;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import cn.itui.webdevelop.dao.UserInfoDao;

public class UserInfoDaoImpl implements UserInfoDao {
	private SqlSession sqlSession;

	public String getUserLogo(int id) {		
		return sqlSession.selectOne("cn.itui.webdevelop.dao.UserInfoDao.getUserLogo",id);
	}

	public SqlSession getSqlSession() {
		return sqlSession;
	}

	public void setSqlSession(SqlSession sqlSession) {
		this.sqlSession = sqlSession;
	}

	public int insertUserInfo_logo(int id, String string, long regTime) {
		// TODO Auto-generated method stub
		Timestamp time = new Timestamp(regTime);
		HashMap<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("id", id);
		parameter.put("logo", string);
		parameter.put("regTime", time);
		return sqlSession.insert("cn.itui.webdevelop.dao.UserInfoDao.insertUserLogo",parameter);
	}
}
