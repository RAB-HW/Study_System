package com.hzb.erp.security.provider;

import com.hzb.erp.security.Util.SecurityUtils;
import com.hzb.erp.security.provider.staff.StaffAuthenticationToken;
import com.hzb.erp.security.service.SecurityDbService;
import com.hzb.erp.security.service.impl.StaffUserDetailsService;
import com.hzb.erp.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hzb.erp.constants.CommonConst.WX_ACCESS_COOKIE;

/**
 * description: 用户角色校验具体实现
 */
@Component
@Slf4j
public class StaffAuthenticationProvider implements AuthenticationProvider {

    @Resource
    private StaffUserDetailsService userDetailsService;

    /**
     * 鉴权具体逻辑 登录时路过
     *
     * @param authentication
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(authentication.getName());
        SecurityUtils.checkUserDetails(userDetails);

        //转换authentication 认证时会先调用support方法,受支持才会调用,所以能强转
        StaffAuthenticationToken authenticationToken = (StaffAuthenticationToken) authentication;
        // 用户名密码校验 具体逻辑
        SecurityUtils.additionalAuthenticationChecks(userDetails, authentication, passwordEncoder());

        //构造已认证的authentication
        StaffAuthenticationToken authenticatedToken = new StaffAuthenticationToken(userDetails, authenticationToken.getCredentials(), userDetails.getAuthorities());
        authenticatedToken.setDetails(authenticationToken.getDetails());
        return authenticatedToken;
    }

    /**
     * 是否支持当前类
     *
     * @param authentication
     * @return
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return (StaffAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

//    /**
//     * 检查密码是否正确
//     *
//     * @param userDetails
//     * @param authentication
//     * @throws AuthenticationException
//     */
//    private void additionalAuthenticationChecks(UserDetails userDetails, StaffAuthenticationToken authentication) throws AuthenticationException {
//        if (authentication.getCredentials() == null) {
//            throw new BadCredentialsException("Bad credentials");
//        }
//        String presentedPassword = authentication.getCredentials().toString();
//        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
//            throw new BadCredentialsException("Bad credentials");
//        }
//    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}