package com.hzb.erp.security.service.impl;

import com.hzb.erp.common.entity.SysLog;
import com.hzb.erp.common.entity.rbac.SysPermissionDTO;
import com.hzb.erp.common.entity.rbac.SysRole;
import com.hzb.erp.common.mapper.rbac.SysPermissionMapper;
import com.hzb.erp.common.mapper.rbac.SysRoleMapper;
import com.hzb.erp.common.service.SysLogService;
import com.hzb.erp.security.service.SecurityDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p> </p>
 *
 * @author Ryan 541720500@qq.com
 */
@Service
public class SecurityDbServiceImpl implements SecurityDbService {

    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysLogService sysLogService;
    @Resource
    private SysPermissionMapper sysPermissionMapper;


    @Override
    public List<SysPermissionDTO> getAllPermission() {
        return sysPermissionMapper.getAllBaseInfo();
    }

    @Override
    public List<SysRole> selectRoleByPermission(Long permissionId) {
        return sysRoleMapper.selectRoleByPermission(permissionId);
    }

    @Override
    public void loginLog(SysLog olog) {
        sysLogService.addOne(olog);
    }


}
