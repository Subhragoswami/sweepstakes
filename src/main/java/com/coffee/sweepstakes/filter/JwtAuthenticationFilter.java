package com.coffee.sweepstakes.filter;

import com.coffee.sweepstakes.config.ServiceConfig;
import com.coffee.sweepstakes.model.request.LoginRequest;
import com.coffee.sweepstakes.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private List<String> whitelistURLs;
    private final ServiceConfig serviceConfig;
    @Value("${whitelisted.endpoints}")
    public void setWhitelistURLs( List<String> whitelistURLs) {
        this.whitelistURLs = whitelistURLs;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {
        try {
            final String authHeader = request.getHeader("Authorization");
            String requestUri = request.getRequestURI();

            if (whiteListingCheck(request, response, filterChain, authHeader, requestUri)) return;

            mandatoryCheck(authHeader, requestUri);

            if (loginAPICall(request, response, filterChain, authHeader)) return;

            authentication(request, authHeader);

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

    private void authentication(HttpServletRequest request, String authHeader) {
        final String jwt = authHeader.substring(7);
        final String userName = jwtService.extractUserName(jwt);
        if (StringUtils.isNotEmpty(userName) && SecurityContextHolder.getContext().getAuthentication() == null) {
            LoginRequest userDetails = new LoginRequest(serviceConfig.getUserName(), serviceConfig.getPassword());
            if ( jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails.getUserName(), userDetails.getPassword());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
        }
    }

    private static boolean loginAPICall(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String authHeader) throws IOException, ServletException {
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }

    private static void mandatoryCheck(String authHeader, String requestUri) {
        if ((StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) && !StringUtils.endsWith(requestUri, "/login")){
            throw new JwtException("Token is required.");
        }
    }

    private boolean whiteListingCheck(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String authHeader, String requestUri) throws IOException, ServletException {
        if ((StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer "))){
            String withoutContext = requestUri.replaceFirst(request.getContextPath(),"");
            for(String whitelistURL : whitelistURLs){
                 if(withoutContext.startsWith(whitelistURL)){
                     filterChain.doFilter(request, response);
                     return true;
                 }
            }
        }
        if ((StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) && request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            filterChain.doFilter(request, response);
            return true;
        }
        return false;
    }
}
