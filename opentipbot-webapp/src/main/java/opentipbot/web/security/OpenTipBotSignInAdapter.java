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

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.context.request.NativeWebRequest;
import opentipbot.persistence.repository.OpenTipBotUserRepository;

import javax.servlet.http.HttpServletResponse;

/**
 * Signs the user in by setting the currentUser property on the {@link SecurityContext}.
 * Remembers the sign-in after the current request completes by storing the user's id in a cookie.
 * This is cookie is read in {@link UserInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Object)} on subsequent requests.
 * @author Gilles Cadignan
 * @see UserInterceptor
 */
public class OpenTipBotSignInAdapter implements SignInAdapter {

	private final UserCookieGenerator userCookieGenerator = new UserCookieGenerator();

    private final OpenTipBotUserRepository opentipbotUserRepository;

    public OpenTipBotSignInAdapter(OpenTipBotUserRepository opentipbotUserRepository) {
        this.opentipbotUserRepository = opentipbotUserRepository;
    }


    public String signIn(String userId, Connection<?> connection, NativeWebRequest request) {
        SecurityContext.setCurrentUser(opentipbotUserRepository.findOne(Long.parseLong(userId)));
		userCookieGenerator.addCookie(userId, request.getNativeResponse(HttpServletResponse.class));
		return null;
	}

}
