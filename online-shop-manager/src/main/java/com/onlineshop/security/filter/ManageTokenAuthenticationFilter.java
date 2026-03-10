package com.onlineshop.security.filter;

import com.onlineshop.framework.models.auth.bo.ParsedToken;
import com.onlineshop.framework.models.auth.enums.AccountType;
import com.onlineshop.framework.models.auth.service.ITokenService;
import com.onlineshop.framework.models.system.role.IRoleService;
import com.onlineshop.framework.security.AuthUser;
import com.onlineshop.framework.utils.AuthUserUtils;
import com.onlineshop.framework.utils.ResponseWriteUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Token认证过滤器
 * 在token解析成功后，从token中提取角色并转换为资源代码权限
 *
 * @author : Tomatos
 * @date : 2026/2/3
 */
@Slf4j
@Component
public class ManageTokenAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private ITokenService tokenService;

    @Autowired
    private IRoleService roleService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        log.info("Method[{}], URI: {}", request.getMethod(), requestUri);
        try {
            String token;
            ParsedToken parsedToken;
            if (
                    (token = getTokenFromRequest(request)) != null
                            && (parsedToken = tokenService.parse(token)) != null
            ) {
                if (!AccountType.ADMIN.getCode().equals(parsedToken.getAccountType())) {
                    ResponseWriteUtil.writeUnauthorized(response, "未授权");
                    return;
                }

                AuthUser authUser = convertToAuthUser(parsedToken);

                // 从token中的roles转换为资源代码
                List<SimpleGrantedAuthority> resourceCodeAuthorities =
                        convertRolesToResourceCodes(parsedToken.getRoles());

                UsernamePasswordAuthenticationToken authenticatedToken =
                        new UsernamePasswordAuthenticationToken(
                                authUser,
                                null,
                                resourceCodeAuthorities
                        );
                AuthUserUtils.setAuthentication(authenticatedToken);

                log.debug("用户 {} 的权限已加载，资源代码数量: {}", authUser.getUsername(),
                          resourceCodeAuthorities.size());
            }
        } catch (Exception e) {
            log.error("解析Token异常: {}", e.getMessage());
            ResponseWriteUtil.writeUnauthorized(response, "Token过期");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private AuthUser convertToAuthUser(ParsedToken parsedToken) {
        AuthUser authUser = new AuthUser(
                parsedToken.getUsername(),
                "",
                Collections.emptyList()
        );
        authUser.setUserId(parsedToken.getUserId());
        authUser.setStoreId(parsedToken.getStoreId());
        authUser.setCurrentAccountType(parsedToken.getAccountType());
        return authUser;
    }

    /**
     * 从角色列表转换为去重的资源代码权限列表
     * 根据token中的角色名称直接查询对应的资源代码
     *
     * @param roles token中包含的角色列表
     * @return 资源代码对应的GrantedAuthority列表
     */
    private List<SimpleGrantedAuthority> convertRolesToResourceCodes(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            List<String> resourceCodes = roleService.getResourceCodesByRoleNames(roles);

            return resourceCodes.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("转换角色为资源代码异常: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
