package com.coffee.sweepstakes.service;

import com.coffee.sweepstakes.config.ServiceConfig;
import com.coffee.sweepstakes.dao.EmailLogDao;
import com.coffee.sweepstakes.dao.UserDao;
import com.coffee.sweepstakes.entity.EmailLog;
import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.util.AppConstants;
import com.coffee.sweepstakes.util.ErrorConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final UserDao userDao;
    private final ServiceConfig serviceConfig;
    private final EmailLogDao emailLogDao;

    public void sendEmail(User user) {
        String htmlContent = loadHtmlTemplate(user);
        log.info("Generated email content for user {}: \n{}", user.getUserEmail(), htmlContent);

        Email from = new Email(serviceConfig.getSendgridFromEmail());
        Email to = new Email(user.getUserEmail());
        String subject = "Your Sweepstakes Entry Confirmation";
        Content content = new Content("text/html", htmlContent);

        Mail mail = new Mail(from, subject, to, content);
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        mail.addPersonalization(personalization);

        SendGrid sendGrid = new SendGrid(serviceConfig.getSendgridKey());

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            log.info("Email sent successfully to: {}", user.getUserEmail());

            updateEmailSentStatus(user);
            saveEmailLog(user.getId(), "Email sent successfully", new Date());

        } catch (Exception e) {
            log.error("Error sending email to {}: {}", user.getUserEmail(), e.getMessage());
            saveEmailLog(user.getId(), e.getMessage(), null);
            throw new CoffeeException(ErrorConstants.MAIL_SEND_ERROR_CODE, ErrorConstants.MAIL_SEND_ERROR_MESSAGE);
        }
    }

    public void resendEmail(UUID userId) {
        User user = userDao.getUserById(userId)
                .orElseThrow(() -> new CoffeeException(ErrorConstants.NOT_FOUND_ERROR_CODE, MessageFormat.format(ErrorConstants.NOT_FOUND_ERROR_MESSAGE, "User")));
        log.info("Sending email to: {} with id: {}", user.getFirstName(), userId);
        sendEmail(user);
    }

    private void updateEmailSentStatus(User user) {
        user.setEmailSent(true);
        userDao.saveUser(user);
    }

    private void saveEmailLog(UUID userId, String statusMessage, Date emailSentAt) {
        EmailLog emailLog = EmailLog.builder()
                .userId(userId)
                .statusMessage(statusMessage)
                .emailSentAt(emailSentAt)
                .build();
        emailLogDao.saveEmailLog(emailLog);
    }

    private String loadHtmlTemplate(User user) {
        try {
            Path path = Paths.get(AppConstants.TEMPLATE_PATH);
            String template = Files.readString(path);

            template = replacePlaceholders(template, user);
            return template;
        } catch (IOException e) {
            log.error("Error loading HTML template: {}", e.getMessage());
            throw new CoffeeException(ErrorConstants.MAIL_SEND_ERROR_CODE, ErrorConstants.MAIL_SEND_ERROR_MESSAGE);
        }
    }

    private Map<String, String[]> initializeEventLogoMap() {
        Map<String, String[]> map = new HashMap<>();
        String baseUrl = serviceConfig.getBaseUrl();

        map.put("arizona", new String[]{baseUrl + "arizona.png", baseUrl + "arizonaLogo.png"});
        map.put("seattle", new String[]{baseUrl + "seattle.png", baseUrl + "seattleLogo.png"});
        map.put("longIsland", new String[]{baseUrl + "longIsland.png", baseUrl + "longIslandLogo.png"});
        map.put("pittsburgh", new String[]{baseUrl + "pittsburgh.png", baseUrl + "pittsburghLogo.png"});
        map.put("knoxville", new String[]{baseUrl + "knoxville.png", baseUrl + "knoxvilleLogo.png"});
        map.put("houston", new String[]{baseUrl + "houston.png", baseUrl + "houstonLogo.png"});
        map.put("orlando", new String[]{baseUrl + "orlando.png", baseUrl + "orlandoLogo.png"});
        map.put("colorado", new String[]{baseUrl + "colorado.png", baseUrl + "coloradoLogo.png"});

        return map;
    }

    private String replacePlaceholders(String template, User user) {
        Map<String, String[]> logoMap = initializeEventLogoMap();
        String[] logos = logoMap.getOrDefault(user.getEventCode(), new String[] {"https://example.com/default-logo.png", "https://example.com/default-logo.png"});
        String eventLogoUrl = logos[0];
        String logoUrl = logos[1];
        return template.replace("[First_Name]",
                        user.getFirstName().substring(0, 1).toUpperCase()
                                + user.getFirstName().substring(1).toLowerCase())
                .replace("{{facebookUrl}}", buildSocialMediaUrl("facebook", user))
                .replace("{{twitterUrl}}", buildSocialMediaUrl("twitter", user))
                .replace("{{instagramUrl}}", buildSocialMediaUrl("instagram", user))
                .replace("{{linkedInUrl}}", buildSocialMediaUrl("linkedin", user))
                .replace("{{newsRoomUrl}}", buildSocialMediaUrl("newsroom", user))
                .replace("{{trackingPixelUrl}}", buildTrackingPixelUrl(user))
                .replace("{{eventLogoUrl}}", eventLogoUrl)
                .replace("{{logoUrl}}", logoUrl);
    }

    private String buildSocialMediaUrl(String platform, User user) {
        return serviceConfig.getApiUrl() + "track-social-click/" + platform +
                "?eventCode=" + user.getEventCode() + "&email=" + user.getUserEmail();
    }

    private String buildTrackingPixelUrl(User user) {
        return serviceConfig.getApiUrl() + "tracking-image.png?eventCode=" +
                user.getEventCode() + "&email=" + user.getUserEmail();
    }
}
