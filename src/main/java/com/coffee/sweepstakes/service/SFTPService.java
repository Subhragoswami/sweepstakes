package com.coffee.sweepstakes.service;

import com.coffee.sweepstakes.dao.EventDao;
import com.coffee.sweepstakes.dao.UserDao;
import com.coffee.sweepstakes.dto.EmailStateDto;
import com.coffee.sweepstakes.entity.Event;
import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.model.response.EventResponse;
import com.coffee.sweepstakes.security.SFTPClientService;
import com.coffee.sweepstakes.util.ErrorConstants;
import com.coffee.sweepstakes.util.SFTPFileUtils;
import com.coffee.sweepstakes.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SFTPService {


    private final UserDao userDao;
    private final EventDao eventDao;
    private final SFTPClientService sftpClientService;
    private final EventService eventService;
    private final SFTPFileUtils sftpFileUtils;

    public void uploadFileToSFTP(String eventCode) {
        EventResponse eventResponse = eventService.getEventByEventCode(eventCode);
        String eventDate = DateUtils.getFormatDateForSFTP(eventResponse.getEventEndDate());
        String fileName = (eventResponse.getZone().replaceAll("\\s*\\(.*?\\)", "") + "_" + eventResponse.getGeneralOffice() + "_" + eventResponse.getEventName()).replaceAll(" ", "_") + "_" + eventDate;
        uploadUserDataFile(null, eventResponse, fileName);
    }

    public ResponseEntity downloadFile(String fileName) {
        byte[] fileBytes = sftpClientService.downloadFile(fileName);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData(fileName, fileName);
        headers.setContentLength(fileBytes.length);
        return ResponseEntity.ok().headers(headers).body(new InputStreamResource(inputStream));
    }

    public ResponseEntity<String> deleteFileFromSFTP(String fileName) {
        try {
            sftpClientService.deleteFile(fileName);
            return ResponseEntity.ok().body("File deleted successfully.");
        } catch (CoffeeException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            return ResponseEntity.ok().body("Failed to delete the file.");
        }
    }

    private void uploadUserDataFile(@PageableDefault Pageable pageable, EventResponse event, String fileName) {
        Page<User> users = userDao.getUsersByEventCode(pageable, event.getEventCode());
        byte[] fileData = SFTPFileUtils.createSFTPUserWorkSheet(users, event);
        sftpClientService.uploadFile(fileName + "_" + users.getTotalElements() + ".csv", fileData);
    }

    public void uploadEmailCampaignToSFTP() {
        List<EmailStateDto> emailStateDtoList = getEmailCampaignStats();
        List<Event> eventList = eventDao.findAllEvents();
        log.info("Fetched {} EmailStateDto entries for event list: {}", emailStateDtoList.size(), eventList.size());
        try {
            byte[] fileData = SFTPFileUtils.createUserEmailCampaignWorkSheet(emailStateDtoList, eventList);
            sftpClientService.uploadFile("Email_Campaign_Report" + ".csv", fileData);
            log.info("Email Campaign Report has be uploaded on SFTP successfully.");
        } catch (Exception ex) {
            log.error("An unexpected error occurred: {}", ex.getMessage());
            throw new CoffeeException(ErrorConstants.SYSTEM_ERROR_CODE, ex.getMessage());
        }
    }

    private List<EmailStateDto> getEmailCampaignStats() {
        log.info("Fetching email campaign stats");
        List<Object[]> results = userDao.getEmailCampaignStatsWithUniqueClicks();
        log.info("Query returned {} results", results.size());
        return results.stream().map(result -> new EmailStateDto(
                (String) result[0],
                ((Number) result[1]).longValue(),
                ((Number) result[2]).longValue(),
                ((Number) result[3]).longValue(),
                ((Number) result[4]).longValue(),
                ((Number) result[5]).longValue(),
                ((Number) result[6]).longValue(),
                ((Number) result[7]).longValue(),
                ((Number) result[8]).longValue(),
                ((Number) result[9]).longValue()
        )).collect(Collectors.toList());
    }

    public void uploadSummaryFileToSFTP(String eventCode) {
        log.info("Fetching event list from event dao");
        Event event = eventDao.findByEventCode(eventCode)
                .orElseThrow(() -> new CoffeeException(ErrorConstants.MANDATORY_ERROR_CODE, MessageFormat.format(ErrorConstants.MANDATORY_ERROR_MESSAGE, "Event Code")));
        try (Workbook workbook = new XSSFWorkbook()) {
            List<Object[]> userCountsByDate = getUserCounts(eventCode, event.getEventStartDate(), event.getEventEndDate());
            sftpFileUtils.createSummaryFileWithDailyCounts(workbook, event, userCountsByDate);
            log.info("Writing work book to byte array.");
            byte[] byteCode = sftpFileUtils.writeWorkbookToByteArray(workbook);
            String eventLastDate = DateUtils.getFormatDateForSFTP(event.getEventEndDate());
            String fileName = (event.getZone().replaceAll("\\s*\\(.*?\\)", "") + "_" + event.getGeneralOffice() + "_" + event.getEventName()).replaceAll(" ", "_") + "_" + "Summary_Report" + "_" + eventLastDate;
            sftpClientService.uploadFile(fileName + ".csv", byteCode);
            log.info("Summary file uploaded on SFTP successfully.");
        } catch (Exception e) {
            log.error("Error generating Excel file", e);
            throw new CoffeeException(ErrorConstants.SOMETHING_WENT_WRONG, e.getMessage());
        }
    }

    private List<Object[]> getUserCounts(String eventCode, Date startDate, Date endDate) {
        List<Date> dateList = getDatesBetween(startDate, endDate);

        List<Object[]> userCounts = userDao.findUserCountByEventCode(eventCode, startDate, endDate);

        Map<Date, Object[]> countMap = new HashMap<>();
        for (Object[] count : userCounts) {
            Date creationDate = (Date) count[0];
            countMap.put(creationDate, count);
        }
        List<Object[]> result = new ArrayList<>();
        for (Date date : dateList) {
            Object[] count = countMap.get(date);
            result.add(Objects.requireNonNullElseGet(count, () -> new Object[]{date, 0L, 0L, 0L}));
        }
        return result;
    }

    private List<Date> getDatesBetween(Date startDate, Date endDate) {
        List<Date> dates = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        while (!calendar.getTime().after(endDate)) {
            dates.add(calendar.getTime());
            calendar.add(Calendar.DATE, 1);
        }
        return dates;
    }
}
