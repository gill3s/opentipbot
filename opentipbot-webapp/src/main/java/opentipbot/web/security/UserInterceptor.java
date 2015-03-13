/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package opentipbot.web.security;

import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;
import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.persistence.repository.OpenTipBotUserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Before a request is handled:
 * 1. sets the current User in the {@link SecurityContext} from a cookie, if present and the user is still connected to Facebook.
 * 2. requires that the user sign-in if he or she hasn't already.
 * @author Gilles Cadignan
 */
public final class UserInterceptor extends HandlerInterceptorAdapter {

	private final UsersConnectionRepository connectionRepository;

    private final OpenTipBotUserRepository opentipbotUserRepository;
	
	private final UserCookieGenerator userCookieGenerator = new UserCookieGenerator();

	public UserInterceptor(UsersConnectionRepository connectionRepository, OpenTipBotUserRepository opentipbotUserRepository) {
		this.connectionRepository = connectionRepository;
        this.opentipbotUserRepository = opentipbotUserRepository;
	}
	
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		rememberUser(request, response);
		handleSignOut(request, response);			
		if (SecurityContext.userSignedIn() || requestForNotProtected(request)) {
			return true;
		} else {
			return requireSignIn(request, response);
		}
	}
	
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		SecurityContext.remove();
	}

	// internal helpers

	private void rememberUser(HttpServletRequest request, HttpServletResponse response) {
		String userId = userCookieGenerator.readCookieValue(request);
		if (userId == null) {
			return;
		}
		if (!userNotFound(userId)) {
			userCookieGenerator.removeCookie(response);
			return;
		}
        OpenTipBotUser opentipbotUser= opentipbotUserRepository.findOne(Long.parseLong(userId));
        if (opentipbotUser == null)
            return;
		SecurityContext.setCurrentUser(opentipbotUser);
	}

	private void handleSignOut(HttpServletRequest request, HttpServletResponse response) {
		if (SecurityContext.userSignedIn() && request.getServletPath().startsWith("/signout")) {
			connectionRepository.createConnectionRepository(SecurityContext.getCurrentUser().getId().toString()).removeConnections("twitter");
			userCookieGenerator.removeCookie(response);
			SecurityContext.remove();			
		}
	}
		
	private boolean requestForNotProtected(HttpServletRequest request) {
		return request.getServletPath().startsWith("/signin") || request.getServletPath().startsWith("/documentation")
                || request.getServletPath().startsWith("/about");
	}
	
	private boolean requireSignIn(HttpServletRequest request, HttpServletResponse response) throws Exception {
		new RedirectView("/signin", true).render(null, request, response);
		return false;
	}

	private boolean userNotFound(String userId) {
		// doesn't bother checking a local user database: simply checks if the userId is connected to Twitter
		return connectionRepository.createConnectionRepository(userId).findPrimaryConnection(Twitter.class) != null;
	}
	
}
