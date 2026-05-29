package com.liang.bbs.rest.interceptor;

import com.alibaba.fastjson.JSON;
import com.liang.bbs.rest.config.login.NoNeedLogin;
import com.liang.manage.auth.facade.server.UrlAccessRightService;
import com.liang.nansheng.common.auth.UserContextUtils;
import com.liang.nansheng.common.auth.UserSsoDTO;
import com.liang.nansheng.common.enums.ResponseCode;
import com.liang.nansheng.common.enums.RoleGradeEnum;
import com.liang.nansheng.common.web.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

//后端路径级别的权限控制
@Slf4j
@Component
public class UrlAccessCheckInterceptor implements HandlerInterceptor {
    private static final Set<String> SUPER_ADMIN_URIS = new HashSet<>(Arrays.asList(
            "/api/bbs/article/rebuildSearchIndex",
            "/api/bbs/article/rebuildStaticHtml"
    ));

    @DubboReference
    UrlAccessRightService urlAccessRightService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isNoNeedLogin(handler)) {
            return true;
        }

        UserSsoDTO currentUser = UserContextUtils.currentUser();
        if (currentUser != null) {
            String uri = request.getRequestURI();
            if (isDeleteEndpoint(uri)) {
                return true;
            }
            if (isSuperAdminBypass(uri, currentUser)) {
                return true;
            }

            Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            Boolean allowed = urlAccessRightService.checkUrlAccess(currentUser, uri, JSON.toJSONString(attribute));
            if (!allowed) {
                log.info("访问无权限的接口，uri={}, user={}", uri, currentUser);
                throw BusinessException.build(ResponseCode.URL_ACCESS_REFUSED);
            }
        }
        return true;
    }

    private boolean isNoNeedLogin(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        NoNeedLogin methodAnnotation = handlerMethod.getMethodAnnotation(NoNeedLogin.class);
        if (Objects.nonNull(methodAnnotation)) {
            return true;
        }

        Class<?> clazz = handlerMethod.getBeanType();
        return Objects.nonNull(AnnotationUtils.findAnnotation(clazz, NoNeedLogin.class));
    }

    private boolean isSuperAdminBypass(String uri, UserSsoDTO currentUser) {
        if (!SUPER_ADMIN_URIS.contains(uri) || CollectionUtils.isEmpty(currentUser.getRoles())) {
            return false;
        }

        List<String> grades = currentUser.getRoles().stream()
                .map(role -> role.getGrade())
                .distinct()
                .collect(Collectors.toList());
        return grades.contains(RoleGradeEnum.NS_SUPER_ADMIN_ROLE.name());
    }

    private boolean isDeleteEndpoint(String uri) {
        return uri != null
                && (uri.startsWith("/api/bbs/article/delete/")
                || uri.startsWith("/api/bbs/label/delete/"));
    }
}
