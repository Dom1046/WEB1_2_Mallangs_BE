package com.mallangs.domain.jwt.filter;

import com.mallangs.domain.jwt.entity.CustomMemberDetails;
import com.mallangs.domain.jwt.entity.TokenCategory;
import com.mallangs.domain.jwt.service.RefreshTokenService;
import com.mallangs.domain.jwt.util.JWTUtil;
import com.mallangs.domain.member.entity.Member;
import com.mallangs.domain.member.entity.MemberRole;
import com.mallangs.domain.member.entity.embadded.Email;
import com.mallangs.domain.member.entity.embadded.UserId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final Long accessTokenValidity;
    private final Long accessRefreshTokenValidity;
    private final AccessTokenBlackList accessTokenBlackList;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/join")) {
                filterChain.doFilter(request, response);
                return;
            }
            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String accessToken = authorizationHeader.substring(7);
                Map<String, Object> claims = jwtUtil.validateToken(accessToken);

                if (accessTokenBlackList.checkBlackList(accessToken)){
                    handleException(response, new Exception("ACCESS TOKEN IS BLOCKED"));
                    return;
                }

                if (jwtUtil.isExpired(accessToken)) {
                    String refreshTokenFromCookies = getRefreshTokenFromCookies(request);

                    if (refreshTokenFromCookies != null) {
                        try {
                            Map<String, Object> payload = jwtUtil.validateToken(refreshTokenFromCookies);
                            String refreshTokenInRedis = refreshTokenService.readRefreshTokenInRedis(payload);

                            if (refreshTokenFromCookies.equals(refreshTokenInRedis)) {
                                String userId = (String)payload.get("userId");
                                String email = (String)payload.get("email");
                                String role = (String)payload.get("role");

                                Map<String, Object> payloadMap = new HashMap<>();
                                payloadMap.put("userId", userId);
                                payloadMap.put("email", email);
                                payloadMap.put("role", role);
                                payloadMap.put("category", TokenCategory.ACCESS_TOKEN.name());

                                if (!jwtUtil.isExpired(refreshTokenFromCookies)) {
                                    String newAccessToken = jwtUtil.createAccessToken(payloadMap, accessTokenValidity);
                                    payloadMap.put("category", TokenCategory.REFRESH_TOKEN.name());
                                    String newRefreshToken = jwtUtil.createRefreshToken(payloadMap, accessRefreshTokenValidity);
                                    refreshTokenService.insertInRedis(payloadMap, newRefreshToken);

                                    response.addHeader("Authorization", "Bearer " + newAccessToken);
                                    response.setStatus(HttpStatus.OK.value());
                                    Cookie refreshTokenCookie = new Cookie("RefreshToken", newRefreshToken);
                                    refreshTokenCookie.setPath("/");
                                    refreshTokenCookie.setHttpOnly(true);
                                    refreshTokenCookie.setMaxAge(3 * 24 * 60 * 60);
                                    response.addCookie(refreshTokenCookie);

                                    userId = claims.get("userId").toString();
                                    email = claims.get("email").toString();
                                    role = claims.get("role").toString();
                                    Member member = Member.builder()
                                            .userId(new UserId(userId))
                                            .email(new Email(email))
                                            .memberRole(MemberRole.valueOf(role))
                                            .build();

                                    log.info("필터, 리프레시 새로만듬: {}, refresh: {}", newAccessToken,newRefreshToken);
                                    CustomMemberDetails customUserDetails = new CustomMemberDetails(member);
                                    Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                                    SecurityContextHolder.getContext().setAuthentication(authToken);
                                } else {
                                    handleException(response, new Exception("EXPIRED REFRESH TOKEN"));
                                }
                            } else {
                                handleException(response, new Exception("INVALID REFRESH TOKEN"));
                            }
                        } catch (Exception e) {
                            handleException(response, new Exception("REFRESH TOKEN VALIDATION FAILED"));
                        }
                    } else {
                        handleException(response, new Exception("REFRESH TOKEN NOT FOUND"));
                    }
                    filterChain.doFilter(request, response);
                } else {

                    if (claims.get("category") == null || !((String) claims.get("category")).equals(TokenCategory.ACCESS_TOKEN.name())) {
                        handleException(response, new Exception("INVALID TOKEN CATEGORY"));
                        return;
                    }
                    if (claims.get("userId") == null || claims.get("email") == null || claims.get("role") == null) {
                        handleException(response, new Exception("INVALID TOKEN PAYLOAD"));
                        return;
                    }
                    String userId = claims.get("userId").toString();
                    String email = claims.get("email").toString();
                    String role = claims.get("role").toString();
                    Member member = Member.builder()
                            .userId(new UserId(userId))
                            .email(new Email(email))
                            .memberRole(MemberRole.valueOf(role))
                            .build();

                    log.info("필터, 무난히 통과 member: {}", member.toString());
                    CustomMemberDetails customUserDetails = new CustomMemberDetails(member);
                    Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    filterChain.doFilter(request, response);
                }
            } else {
                filterChain.doFilter(request, response);
            }
        } catch (Exception e) {
            log.error("fail to check Tokens: {}", e.getMessage());
            throw e;
        }
    }

    public void handleException(HttpServletResponse response, Exception e)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().println("{\"error\": \"" + e.getMessage() + "\"}");
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("RefreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
