package com.lm.web.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lm.web.configuration.redis.cache.IdGlobalGenerator;
import com.lm.web.entity.po.Dept;
import com.lm.web.entity.po.DeptRole;
import com.lm.web.entity.po.MenuRole;
import com.lm.web.entity.po.Role;
import com.lm.web.entity.vo.RoleVO;
import com.lm.web.enums.IsDeleteEnum;
import com.lm.web.repository.dao.DeptRepositoryDao;
import com.lm.web.repository.dao.DeptRoleRepositoryDao;
import com.lm.web.repository.dao.MenuRoleRepositoryDao;
import com.lm.web.repository.dao.RoleRepositoryDao;
import com.lm.web.repository.queryFilter.RoleQueryFilter;
import com.lm.web.service.RoleService;


@Service("roleService")
public class RoleServiceImpl implements RoleService {

	@Autowired
	private IdGlobalGenerator idGlobalGenerator;
	@Autowired
	private RoleRepositoryDao roleRepositoryDao;
	@Autowired
	private DeptRepositoryDao deptRepositoryDao;
	@Autowired
	private DeptRoleRepositoryDao deptRoleRepositoryDao; // 部门 <-> 角色
	@Autowired
	private MenuRoleRepositoryDao menuRoleRepositoryDao; // 菜单 <-> 角色

	@Override
	public Page<Role> findPageRole(RoleVO vo, Integer pageNo, Integer pageSize, String sortField) {

		// 查询条件
		RoleQueryFilter query = new RoleQueryFilter();
		if (StringUtils.isNotEmpty(vo.getRoleName())) {
			query.setRoleName(vo.getRoleName());
		}

		// 过滤掉是否删除字段
		query.setIsDelete(IsDeleteEnum.UN_DELETE.getVal());
		// 排序
		Sort sort = new Sort(Direction.DESC, sortField);
		// 分页条件
		Pageable page = new PageRequest(pageNo, pageSize, sort);
		// 分页查询
		Page<Role> pageData = roleRepositoryDao.findAll(query, page);
		List<Role> roleList = pageData.getContent();

		// role实体和dept实体的dept_id映射
		Map<Long, Dept> roleDeptMap = new HashMap<>();
		for (Role role : roleList) {
			roleDeptMap.put(role.getDeptId(), deptRepositoryDao.findOne(role.getDeptId()));
		}

		// 数据拼装
		for (Role role : roleList) {
			role.setDeptName(roleDeptMap.get(role.getDeptId()).getName());
		}

		return pageData;
	}

	@Override
	public List<Role> getRoleList() {
		List<Role> roleList = roleRepositoryDao.findAll();

		// Role与Dept映射
		Map<Long, Dept> roleDeptMap = new HashMap<>();
		for (Role role : roleList) {
			roleDeptMap.put(role.getDeptId(), deptRepositoryDao.findOne(role.getDeptId()));
			role.setDeptName(roleDeptMap.get(role.getDeptId()).getName()); // 获取部门名称
		}

		return roleList;
	}

	@Override
	public Role queryByRoleId(Long roleId) {
		return roleRepositoryDao.findOne(roleId);
	}
	

	@Override
	@Transactional  //添加事务
	public void updateRole(Role role) {
		
		Role oriRole = roleRepositoryDao.findOne(role.getRoleId()); // 查询是否已经存在
		if (null != oriRole) {
			oriRole.setRoleName(role.getRoleName()); // 角色名称
			oriRole.setDeptId(role.getDeptId()); // 所属部门
			oriRole.setRemark(role.getRemark()); // 备注
			oriRole.setIsDelete(IsDeleteEnum.UN_DELETE.getVal()); // 是否删除
			oriRole.setUpdateTime(new Date()); //更新时间
			Role newBean = roleRepositoryDao.save(oriRole); // 保存

			/*
			 * 修改角色数据时，更新菜单与角色的关系
			 * 1.先删除原来的菜单与角色的对应关系，2.重新插入一遍
			 */
			menuRoleRepositoryDao.deleteMenuRole(role.getRoleId());
			List<Long> menuIdList = role.getMenuIdList();
			if (null != menuIdList && menuIdList.size() > 0) {
				for (Long menuId : menuIdList) {
					MenuRole newMenuRole = new MenuRole();
					newMenuRole.setId(idGlobalGenerator.getSeqId(MenuRole.class));
					newMenuRole.setMenuId(menuId); // 菜单ID
					newMenuRole.setRoleId(newBean.getRoleId()); // 角色ID
					menuRoleRepositoryDao.save(newMenuRole);
				}
			}
			
			/*
			 * 修改角色数据时，更新部门与角色的关系
			 * 1.先删除原来的部门与角色的对应关系，2.重新插入一遍
			 */
			deptRoleRepositoryDao.deleteDeptRole(role.getRoleId());
			List<Long> deptIdList = role.getDeptIdList();
			if (null != deptIdList && deptIdList.size() > 0) {
				for (Long deptId : deptIdList) {
					DeptRole newDeptRole = new DeptRole();
					newDeptRole.setId(idGlobalGenerator.getSeqId(DeptRole.class));
					newDeptRole.setDeptId(deptId); // 部门ID
					newDeptRole.setRoleId(newBean.getRoleId()); // 角色ID
					deptRoleRepositoryDao.save(newDeptRole);
				}
			}
		}
		
	}
	

	@Override
	@Transactional  //添加事务
	public void deleteRoleBatch(Long[] roleIds) {
		if (null != roleIds && roleIds.length > 0) {
			for (int i = 0; i < roleIds.length; i++) {
				Role role = roleRepositoryDao.findOne(roleIds[i]);
				role.setIsDelete(IsDeleteEnum.DELETE.getVal()); // 标记为删除
			}
		}
	}

	@Override
	@Transactional  //添加事务
	public void saveRole(Role role) {
		Role roleBean = new Role();
		roleBean.setRoleId(idGlobalGenerator.getSeqId(Role.class)); //角色ID
		roleBean.setRoleName(role.getRoleName()); // 角色名称
		roleBean.setDeptId(role.getDeptId()); // 所属部门
		roleBean.setRemark(role.getRemark()); // 备注
		roleBean.setIsDelete(IsDeleteEnum.UN_DELETE.getVal()); // 是否删除
		roleBean.setCreateTime(new Date()); // 创建时间
		Role newRole = roleRepositoryDao.save(roleBean);

		// 保存菜单与角色的关系
		List<Long> menuIdList = role.getMenuIdList();
		if (null != menuIdList && menuIdList.size() > 0) {
			for (Long menuId : menuIdList) {
				MenuRole newMenuRole = new MenuRole();
				newMenuRole.setId(idGlobalGenerator.getSeqId(MenuRole.class));
				newMenuRole.setMenuId(menuId); // 菜单ID
				newMenuRole.setRoleId(newRole.getRoleId()); // 角色ID
				menuRoleRepositoryDao.save(newMenuRole);
			}
		}

		// 保存部门与角色的关系
		List<Long> deptIdList = role.getDeptIdList();
		if (null != deptIdList && deptIdList.size() > 0) {
			for (Long deptId : deptIdList) {
				DeptRole newDeptRole = new DeptRole();
				newDeptRole.setId(idGlobalGenerator.getSeqId(DeptRole.class));
				newDeptRole.setDeptId(deptId); // 部门ID
				newDeptRole.setRoleId(newRole.getRoleId()); // 角色ID
				deptRoleRepositoryDao.save(newDeptRole);
			}
		}
	}

}
