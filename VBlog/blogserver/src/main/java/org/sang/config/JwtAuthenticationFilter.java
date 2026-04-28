package org.sang.config;

import org.sang.bean.Role;
import org.sang.bean.User;
import org.sang.client.UserServiceClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final UserServiceClient userServiceClient;

    public JwtAuthenticationFilter(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                Map<String, Object> claims = userServiceClient.verify(authorization);
                User user = buildUser(claims);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private User buildUser(Map<String, Object> claims) {
        User user = new User();
        user.setId(((Number) claims.get("userId")).longValue());
        user.setUsername(String.valueOf(claims.get("username")));
        user.setNickname(String.valueOf(claims.get("username")));
        user.setEnabled(true);

        List<Role> roles = new ArrayList<>();
        Object roleValues = claims.get("roles");
        if (roleValues instanceof List) {
            for (Object item : (List<?>) roleValues) {
                String roleName = String.valueOf(item);
                if (roleName.startsWith("ROLE_")) {
                    roleName = roleName.substring(5);
                }
                roles.add(new Role(null, roleName));
            }
        }
        user.setRoles(roles);
        return user;
    }
}
