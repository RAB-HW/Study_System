package com.hzb.erp.security.service;

import com.hzb.erp.common.entity.SysLog;
import com.hzb.erp.common.entity.rbac.SysPermissionDTO;
import com.hzb.erp.common.entity.rbac.SysRole;

import java.util.List;

/**
 * <p>数据服务 </p>
 *
 * @author Ryan 541720500@qq.com
 */
public interface SecurityDbService {

    List<SysPermissionDTO> getAllPermission();

    List<SysRole> selectRoleByPermission(Long permissionId);

    void loginLog(SysLog olog);
}
