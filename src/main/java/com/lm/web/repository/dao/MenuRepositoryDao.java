package com.lm.web.repository.dao;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;

import com.lm.web.configuration.query.core.BaseRepository;
import com.lm.web.entity.po.Menu;



/**
 * 
 * (菜单dao层接口) 
 * @ClassName MenuRepositoryDao 
 * @author ShenZiYang 
 * @date 2018年1月6日 上午10:09:07
 */
public interface MenuRepositoryDao extends BaseRepository<Menu, Long>{
	

	/**
	 * 使用这种jpa查询时不能写实体类名，字段与数据库名字一致
	 * 查询菜单实体集合(查询菜单实体集合) 
	 * @Title menuList 
	 * @return List<Menu>返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月6日上午11:39:31
	 * @throws 查询失败
	 */
//	@Query(value = "SELECT m.*,(SELECT p.menu_name FROM sys_menu p WHERE p.menu_id = m.parent_id) AS parentName FROM sys_menu m",nativeQuery = true)
//	List<Menu> menuList();
	
	
	/**
	 * 
	 * (根据父菜单查询子菜单) 
	 * @Title queryMenuByParentId 
	 * @param parentId == 当前菜单id(menuId)
	 * @return List<Menu>返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月6日下午1:57:39
	 * @throws 查询失败
	 */
	@Query("SELECT m FROM Menu m WHERE m.parentId = ?1 ORDER BY m.sort ")
	List<Menu> queryMenuByParentId(Long parentId);
	
	/**
	 * 
	 *【根据父ID获取菜单名称】 
	 * @param parentId
	 * @return String返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月19日下午5:48:00
	 * @throws 异常
	 */
	@Query("SELECT m.name FROM Menu m WHERE m.menuId = ?1")
	String getParentName(Long parentId);
	
	/**
	 * 
	 *【查询不含按钮的菜单列表】 
	 * @return Menu返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月25日上午10:43:48
	 * @throws 异常 
	 */
	@Query("SELECT m FROM Menu m WHERE type != 2 ORDER BY sort ASC")
	List<Menu> queryNotButtonList();
	
	
	
}
