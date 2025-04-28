package com.hzb.erp.security.service.impl;

import com.hzb.erp.common.entity.Staff;
import com.hzb.erp.common.mapper.StaffMapper;
import com.hzb.erp.common.service.StaffService;
import com.hzb.erp.security.Enums.LoginUserIdentity;
import com.hzb.erp.security.Util.JwtUserDetails;
import com.hzb.erp.security.mapper.SecurityUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * description:
 */
@Service
public class StaffUserDetailsService implements UserDetailsService {
    @Autowired
    private SecurityUserMapper securityUserMapper;
    @Resource
    private StaffMapper staffMapper;
    @Override
    public JwtUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        JwtUserDetails userDetails = securityUserMapper.loadStaffByUsername(username);
        if (userDetails != null) {
            userDetails.setIdentity(LoginUserIdentity.STAFF.getCode());
        }
        return userDetails;
    }

    public void storeWxAccessId(Long uid, Long wxAccessId) {
        Staff staff = staffMapper.selectById(uid);
        if (staff != null) {
            staff.setWxAccessId(wxAccessId);
            staffMapper.updateById(staff);
        }
    }

}