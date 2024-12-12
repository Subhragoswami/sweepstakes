package com.coffee.sweepstakes.service;

import com.coffee.sweepstakes.dao.UserDao;
import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.util.ErrorConstants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTrackingService {
    private final UserDao userDao;

    public void trackEmailOpen(String eventCode, String recipientEmail, HttpServletResponse response) {
        log.info("Email opened by: {}", recipientEmail);
        try {
            User user = userDao.findUserByEventCodeAndEmail(eventCode, recipientEmail)
                    .orElseThrow(() -> new CoffeeException(ErrorConstants.NOT_FOUND_ERROR_CODE,
                            MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "User")));
            user.setEmailOpened(true);
            userDao.saveUser(user);
            // Serve a 1x1 transparent PNG image
            byte[] imageBytes = Files.readAllBytes(Paths.get("src/main/resources/static/1x1-transparent.png"));
            response.setContentType("image/png");
            response.getOutputStream().write(imageBytes);
        } catch (Exception e) {
            throw new CoffeeException(ErrorConstants.MAIL_SEND_ERROR_CODE,
                    MessageFormat.format(ErrorConstants.MAIL_SEND_ERROR_MESSAGE, e.getMessage()));
        }
    }

    public void trackSocialClick(String platform, String eventCode, String recipientEmail, HttpServletResponse response) {
        log.info("Social media {} link clicked by: {}", platform, recipientEmail);
        try {
            User user = userDao.findUserByEventCodeAndEmail(eventCode, recipientEmail)
                    .orElseThrow(() -> new CoffeeException(ErrorConstants.NOT_FOUND_ERROR_CODE,
                            MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "User")));

            String redirectUrl = getRedirectUrlAndMarkLinkAsClicked(platform.toLowerCase(), user);
            if (redirectUrl == null) {
                log.error("Invalid platform: {}", platform);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            userDao.saveUser(user);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            throw new CoffeeException(ErrorConstants.MAIL_SEND_ERROR_CODE,
                    MessageFormat.format(ErrorConstants.MAIL_SEND_ERROR_MESSAGE, e.getMessage()));
        }
    }

    private String getRedirectUrlAndMarkLinkAsClicked(String platform, User user) {
        switch (platform) {
            case "facebook":
                user.setFacebookLinkClicked(true);
                return "https://www.facebook.com/newyorklife/";
            case "twitter":
                user.setTwitterLinkClicked(true);
                return "https://x.com/NewYorkLife";
            case "instagram":
                user.setInstaLinkClicked(true);
                return "https://www.instagram.com/newyorklife/";
            case "linkedin":
                user.setLinkedInLinkClicked(true);
                return "https://www.linkedin.com/company/newyorklife/";
            case "newsroom":
                user.setNewsRoomClicked(true);
                return "https://www.newyorklife.com?tid=1839&cmpid=afc_SP24_spon_Cffe_CffeConEml_na_na_prosp_lclcomm_HP_na_0_0_0";
            default:
                return null;
        }
    }

}
