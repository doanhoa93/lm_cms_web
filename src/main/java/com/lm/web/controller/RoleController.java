package com.lm.web.controller;

import java.util.List;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.lm.web.configuration.log.GwsLogger;
import com.lm.web.entity.po.Role;
import com.lm.web.entity.vo.RoleVO;
import com.lm.web.service.DeptRoleService;
import com.lm.web.service.MenuRoleService;
import com.lm.web.service.RoleService;
import com.lm.web.tools.base.BaseController;
import com.lm.web.tools.constant.CommConstant;
import com.lm.web.tools.result.Ret;
import com.lm.web.tools.validator.ValidatorUtils;


/**
 * 
 *【角色管理】
 * @ClassName RoleController 
 * @author ShenZiYang 
 * @date 2018年1月27日 下午11:19:15
 */
@RestController
@RequestMapping("/sys/role")
public class RoleController extends BaseController{
	
	@Autowired
	private RoleService roleService;
	@Autowired
	private MenuRoleService menuRoleService;
	@Autowired
	private DeptRoleService deptRoleService;

	/**
	 * 
	 * (分页查询角色列表) 
	 * @Title roleList 
	 * @param vo
	 * @return Ret返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月11日下午5:57:09
	 * @throws
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@RequiresPermissions("sys:role:list")
	public Ret roleList(RoleVO vo) {
		String code = CommConstant.GWSCOD0000;
		String message = CommConstant.GWSMSG0000;
		Long startTime = System.currentTimeMillis();
		GwsLogger.info("分页查询角色信息开始:code={},message={},startTime={}", code, message, startTime);
		
		// 如果不是管理员，则只查询自己创建的角色列表
		// if (getUserId() != Constant.ADMIN) {
		//
		// }
		
		Page<Role> pageData = null;
		try {
			pageData = roleService.findPageRole(vo, vo.getPageNo() - 1, vo.getPageSize(), "roleId");
		} catch (Exception e) {
			code = CommConstant.GWSCOD0001;
			message = CommConstant.GWSMSG0001;
			GwsLogger.error("分页查询角色信息异常:code={},message={},e={}", code, message, e);
		}

		Long endTime = System.currentTimeMillis() - startTime;
		GwsLogger.info("分页查询角色信息结束:code={},message={},endTime={}", code, message, endTime);
		return Ret.ok().put("page", pageData);
	}
	
	
	/**
	 * 
	 *【获取所有角色列表集合】
	 * @Title selectRoleList 
	 * @return Ret返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月27日下午11:21:55
	 * @throws  异常
	 */
	@RequestMapping("/select")
	@RequiresPermissions("sys:role:select")
	public Ret selectRoleList() {
		String code = CommConstant.GWSCOD0000;
		String message = CommConstant.GWSMSG0000;
		Long startTime = System.currentTimeMillis();
		GwsLogger.info("获取所有角色列表信息开始:code={},message={},startTime={}", code, message, startTime);
		
		List<Role> roleList = null;
		try {
			 roleList = roleService.getRoleList();
		} catch (Exception e) {
			code = CommConstant.GWSCOD0001;
			message = CommConstant.GWSMSG0001;
			GwsLogger.error("获取所有角色列表信息异常:code={},message={},e={}", code, message, e);
		}

		Long endTime = System.currentTimeMillis() - startTime;
		GwsLogger.info("获取所有角色列表信息结束:code={},message={},endTime={}", code, message, endTime);
		return Ret.ok().put("list", roleList);
	}
	
	/**
	 * 
	 * (角色信息,用于修改页面回显) 
	 * @Title info 
	 * @param roleId
	 * @return Ret返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月13日下午4:22:36
	 * @throws 异常
	 */
	@RequestMapping("/info/{roleId}")
	@RequiresPermissions("sys:role:info")
	public Ret roleInfo(@PathVariable("roleId") Long roleId){
		//根据角色ID获取角色实体
		Role role = roleService.queryByRoleId(roleId);
		//查询角色对应的菜单
		List<Long> menuIdList = menuRoleService.queryMenuByRoleId(roleId);
		role.setMenuIdList(menuIdList);
		//查询角色对应的部门
		List<Long> deptIdList = deptRoleService.queryDeptByRoleId(roleId);
		role.setDeptIdList(deptIdList);
		
		return Ret.ok().put("role", role);
	}
	

	/**
	 * 
	 *【修改角色】
	 * @Title update 
	 * @param role
	 * @return Ret返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月19日下午9:42:07
	 * @throws  异常
	 */
	@RequestMapping("/update")
	@RequiresPermissions("sys:role:update")
	public Ret update(@RequestBody Role role){
		ValidatorUtils.validateEntity(role);
		roleService.updateRole(role);
		return Ret.ok();
	}
	

	/**
	 * 
	 *【新增角色】
	 * @Title save 
	 * @param role
	 * @return R返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月20日下午10:30:15
	 * @throws  异常
	 */
	@RequestMapping("/save")
	@RequiresPermissions("sys:role:save")
	public Ret save(@RequestBody Role role) {
		String code = CommConstant.GWSCOD0000;
		String message = CommConstant.GWSMSG0000;
		Long startTime = System.currentTimeMillis();
		GwsLogger.info("新增角色操作开始:code={},message={},startTime={}", code, message, startTime);
		
		//功能权限为空判断
		if(null == role.getMenuIdList() || role.getMenuIdList().size() == 0){
			GwsLogger.error("功能权限菜单ID为空:menuIdList={}",role.getMenuIdList().size());
			return Ret.error("功能权限未选择!");
		}
		
		//数据权限为空判断
		if(null == role.getDeptIdList() || role.getDeptIdList().size() == 0){
			GwsLogger.error("数据权限部门ID为空:deptIdList={}",role.getMenuIdList().size());
			return Ret.error("数据权限未选择!");
		}
		
		try {
			ValidatorUtils.validateEntity(role);
			roleService.saveRole(role);
		} catch (Exception e) {
			code = CommConstant.GWSCOD0001;
			message = CommConstant.GWSMSG0001;
			GwsLogger.error("新增角色操作异常:code={},message={},e={}", code, message, e);
			return Ret.error(e.getMessage());
		}

		Long endTime = System.currentTimeMillis() - startTime;
		GwsLogger.info("新增角色操作结束:code={},message={},endTime={}", code, message, endTime);
		return Ret.ok();
	}
	
	
	/**
	 * 
	 *【删除角色--逻辑删除】
	 * @Title delete 
	 * @param roleIds
	 * @return R返回类型   
	 * @author ShenZiYang
	 * @date 2018年1月19日下午9:41:07
	 * @throws  异常
	 */
	@RequestMapping("/delete")
	@RequiresPermissions("sys:role:delete")
	public Ret delete(@RequestBody Long[] roleIds){
		roleService.deleteRoleBatch(roleIds);
		return Ret.ok();
	}
	
	
}
