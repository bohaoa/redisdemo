package cn.javass.spring.chapter8.dao.mybatis;


import org.mybatis.spring.support.SqlSessionDaoSupport;

import cn.javass.spring.chapter7.UserModel;
import cn.javass.spring.chapter7.dao.IUserDao;

public class UserMybatisDaoImpl  implements IUserDao {

	@Override
	public void save(UserModel model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int countAll() {
		// TODO Auto-generated method stub
		return 0;
	}

//    @Override
//    public void save(UserModel model) {
//        getSqlSession().insert("UserSQL.insert", model);
//    }
//
//    @Override
//    public int countAll() {
//        return (Integer) getSqlSession().selectOne("UserSQL.countAll");
//    }

}
