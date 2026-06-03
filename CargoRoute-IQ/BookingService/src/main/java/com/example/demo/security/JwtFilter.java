package com.example.demo.security;
 
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
 
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Collections;
 
@Component	
public class JwtFilter extends OncePerRequestFilter{
 
 
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {

	    String auth = request.getHeader("Authorization");

		if (auth != null && auth.startsWith("Bearer ")) {
			String token = auth.substring(7);
			try {
				JwtUtil util = new JwtUtil();

				String username = util.extractUsername(token);
				String role     = util.extractRole(token);

				System.out.println("[JwtFilter] Authorization header present. username=" + username + ", role=" + role);

				if (username != null && role != null) {
					UsernamePasswordAuthenticationToken authentication =
							new UsernamePasswordAuthenticationToken(
									username,
									token,
									Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
							);
					SecurityContextHolder.getContext().setAuthentication(authentication);
					System.out.println("[JwtFilter] Authentication set for user=" + username + " authorities=" + authentication.getAuthorities());
				}
			} catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
				// Log the problem but do NOT write a response here — allow Spring Security to handle authentication failures.
				System.out.println("[JwtFilter] Invalid/expired token: " + e.getMessage());
				SecurityContextHolder.clearContext();
			}
		} else {
			System.out.println("[JwtFilter] No Authorization header present on request to " + request.getRequestURI());
		}

		filterChain.doFilter(request, response);
	}
}