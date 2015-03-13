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

import opentipbot.persistence.model.OpenTipBotUser;
import opentipbot.service.BitcoinService;
import opentipbot.service.OpenTipBotUserService;
import opentipbot.service.exception.OpenTipBotServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionSignUp;
import org.springframework.social.connect.UserProfile;

/**
 * @author Gilles Cadignan
 */
public class OpenTipBotConnectionSignUp implements ConnectionSignUp {

    private static final Logger logger = LoggerFactory.getLogger(OpenTipBotConnectionSignUp.class);

    private OpenTipBotUserService opentipbotUserService;
    private BitcoinService bitcoinService;

    public OpenTipBotConnectionSignUp(OpenTipBotUserService opentipbotUserService, BitcoinService bitcoinService) {
        this.opentipbotUserService = opentipbotUserService;
        this.bitcoinService = bitcoinService;
    }

    public String execute(Connection<?> connection) {
        OpenTipBotUser opentipbotUser = opentipbotUserService.findByTwitterIdentifier(connection.getKey().getProviderUserId());
        if (opentipbotUser == null){
            UserProfile socialMediaProfile = connection.fetchUserProfile();
            opentipbotUser = new OpenTipBotUser();
            opentipbotUser.setDisplayName(socialMediaProfile.getName());
            opentipbotUser.setTwitterIdentifier(connection.getKey().getProviderUserId());
            opentipbotUser.setUserName(socialMediaProfile.getUsername());
            opentipbotUser.setProfileImageUrl(connection.getImageUrl());
            opentipbotUser.setProfileUrl(connection.getProfileUrl());
            try {
                opentipbotUserService.createNewOpenTipBotUser(opentipbotUser);
            } catch (OpenTipBotServiceException e) {
                logger.error("Error while creating new OpenTipBotUser", e);
                return null;
            }

        } else {
            //update twitter info if they changed
            UserProfile socialMediaProfile = connection.fetchUserProfile();
            if (!opentipbotUser.getProfileImageUrl().equals(connection.getImageUrl()) ||
                    !opentipbotUser.getDisplayName().equals(socialMediaProfile.getName())){
                opentipbotUser.setDisplayName(socialMediaProfile.getName());
                opentipbotUser.setProfileImageUrl(connection.getImageUrl());
                opentipbotUserService.updateOpenTipBotUser(opentipbotUser);
            }

        }
        return Long.toString(opentipbotUser.getId());
    }

}
