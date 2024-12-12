package com.coffee.sweepstakes.util;


import com.coffee.sweepstakes.dto.EmailStateDto;
import com.coffee.sweepstakes.entity.Event;
import com.coffee.sweepstakes.entity.User;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.model.response.EventResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;


@Component
@Slf4j
public class SFTPFileUtils {

    private static void createUserCsv(Page<User> users, EventResponse event, Workbook workbook) {
        Sheet sheet = createExcelWorkbookSheet(workbook, "User", new String[]{"Zone", "General_Office", "Event_Name", "Event_Date", "First_Name", "Last_Name", "Email_Address", "Phone_Number", "Contact_Opt_In", "Email_Marketing_Opt_In", "Submission_Date"});
        int rowNum = 1;
        for (User user : users) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(event.getZone());
            row.createCell(1).setCellValue(event.getGeneralOffice());
            row.createCell(2).setCellValue(event.getEventName());
            row.createCell(3).setCellValue(DateUtils.getFormatDateForSFTP(event.getEventStartDate()));
            row.createCell(4).setCellValue(user.getFirstName());
            row.createCell(5).setCellValue(user.getLastName());
            row.createCell(6).setCellValue(user.getUserEmail());
            row.createCell(7).setCellValue(user.getUserPhoneNumber());
            row.createCell(8).setCellValue(user.isConsentToContact() ? "Yes" : "No");
            row.createCell(9).setCellValue(user.isConsentMarketing() ? "Yes" : "No");
            row.createCell(10).setCellValue(DateUtils.convertToEst(user.getCreatedAt()));
        }
    }

    private static void createUserEmailCampaignCsv(List<EmailStateDto> emailStateDtoList, List<Event> events, Workbook workbook) {
        Sheet sheet = createExcelWorkbookSheet(workbook, "EmailCampaign", new String[]{
                "Zone",
                "General_Office",
                "Event_Name",
                "Email_Sent",
                "Open_Email",
                "Open_Email_Rate",
                "NYL_Link_Click",
                "Social_Media_Icons_Click",
                "Total_Clicks",
                "Unique_Clicks",
                "Open_Click_Rate",
                "Click_Rate",
                "Total_Link_Click_Rate"
        });

        int rowNum = 1;
        long totalEmailSent = 0;
        long totalEmailOpened = 0;
        long totalNewsRoomClicks = 0;
        long totalSocialMediaClicks = 0;
        long totalUniqueClicks = 0;
        long totalTotalClicks = 0;

        Map<String, EmailStateDto> emailStateMap = emailStateDtoList.stream()
                .collect(Collectors.toMap(EmailStateDto::getEventCode, emailState -> emailState));

        for (Event event : events) {
            EmailStateDto emailState = emailStateMap.get(event.getEventCode());

            if (emailState != null) {
                long emailSent = emailState.getEmailSentCount();
                long emailOpened = emailState.getEmailOpenedCount();
                long newsRoomClicks = emailState.getNewsRoomClickCount();
                long socialMediaClicks = emailState.getInstaClickCount() + emailState.getFacebookClickCount() +
                        emailState.getTwitterClickCount() + emailState.getLinkedinClickCount();
                long uniqueClicks = emailState.getUniqueClickCount();
                long totalClicks = newsRoomClicks + socialMediaClicks;

                totalEmailSent += emailSent;
                totalEmailOpened += emailOpened;
                totalNewsRoomClicks += newsRoomClicks;
                totalSocialMediaClicks += socialMediaClicks;
                totalUniqueClicks += uniqueClicks;
                totalTotalClicks += totalClicks;

                double openEmailRate = (emailSent > 0) ? (double) emailOpened / emailSent : 0;
                double openClickRate = (emailOpened > 0) ? (double) uniqueClicks / emailOpened : 0;
                double clickRate = (emailSent > 0) ? (double) uniqueClicks / emailSent : 0;
                double linkClickRate = (emailSent > 0) ? (double) totalClicks / emailSent : 0;

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(event.getZone());
                row.createCell(1).setCellValue(event.getGeneralOffice());
                row.createCell(2).setCellValue(event.getEventName());
                row.createCell(3).setCellValue(emailSent);
                row.createCell(4).setCellValue(emailOpened);
                row.createCell(5).setCellValue(roundToThreeDecimalPlaces(openEmailRate));
                row.createCell(6).setCellValue(newsRoomClicks);
                row.createCell(7).setCellValue(socialMediaClicks);
                row.createCell(8).setCellValue(totalClicks);
                row.createCell(9).setCellValue(uniqueClicks);
                row.createCell(10).setCellValue(roundToThreeDecimalPlaces(openClickRate));
                row.createCell(11).setCellValue(roundToThreeDecimalPlaces(clickRate));
                row.createCell(12).setCellValue(roundToThreeDecimalPlaces(linkClickRate));
            }
        }

        double totalOpenEmailRate = (totalEmailSent > 0) ? (double) totalEmailOpened / totalEmailSent : 0;
        double totalOpenClickRate = (totalEmailOpened > 0) ? (double) totalUniqueClicks / totalEmailOpened : 0;
        double totalClickRate = (totalEmailSent > 0) ? (double) totalUniqueClicks / totalEmailSent : 0;
        double totalLinkClickRate = (totalEmailSent > 0) ? (double) totalTotalClicks / totalEmailSent : 0;

        Row summaryRow = sheet.createRow(rowNum);
        summaryRow.createCell(2).setCellValue("Total");
        summaryRow.createCell(3).setCellValue(totalEmailSent);
        summaryRow.createCell(4).setCellValue(totalEmailOpened);
        summaryRow.createCell(5).setCellValue(roundToThreeDecimalPlaces(totalOpenEmailRate));
        summaryRow.createCell(6).setCellValue(totalNewsRoomClicks);
        summaryRow.createCell(7).setCellValue(totalSocialMediaClicks);
        summaryRow.createCell(8).setCellValue(totalTotalClicks);
        summaryRow.createCell(9).setCellValue(totalUniqueClicks);
        summaryRow.createCell(10).setCellValue(roundToThreeDecimalPlaces(totalOpenClickRate));
        summaryRow.createCell(11).setCellValue(roundToThreeDecimalPlaces(totalClickRate));
        summaryRow.createCell(12).setCellValue(roundToThreeDecimalPlaces(totalLinkClickRate));
    }

    private static double roundToThreeDecimalPlaces(double value) {
        BigDecimal bd = new BigDecimal(value).setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static void descriptionSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Description");

        CellStyle centeredStyle = workbook.createCellStyle();
        centeredStyle.setAlignment(HorizontalAlignment.CENTER);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Metrics from Email campaign");

        titleCell.setCellStyle(centeredStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));
        String[][] descriptionData = {
                {"Heading", "Description"},
                {"Zone", "Time zone of the event"},
                {"General Office", "City of the event"},
                {"Event Name", "Name of the event"},
                {"Emails Sent", "Total email sent to the user."},
                {"Opened Emails", "Total email opened by the user."},
                {"Open Email Rate", "Total email sent to the user divided by Total email opened by the user (A6/A7)."},
                {"NYL Link Click", "Total click on NYL Newsroom link."},
                {"Social Media Icons Click", "Total click on any social media icon"},
                {"Total Clicks", "Total click on social media icon and NYL newsroom link (A9+A10)"},
                {"Unique Clicks", "Unique users who clicked on anything – so if 1 person clicks on 3 things, the unique click count is 1"},
                {"Open Click Rate", "Unique clicks/opens email (A12/A7)"},
                {"Click Rate", "Unique clicks/emails sent (A12/A6)"},
                {"Total Link Click Rate", "Total clicks/emails sent ((A9+A10)/A6)"}
        };

        for (int i = 0; i < descriptionData.length; i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(descriptionData[i][0]);
            row.createCell(1).setCellValue(descriptionData[i][1]);
        }
        sheet.setColumnWidth(0, 5000);
        sheet.setColumnWidth(1, 15000);
    }


    public static byte[] createUserEmailCampaignWorkSheet(List<EmailStateDto> emailStateDto, List<Event> event) {
        Workbook workbook = new XSSFWorkbook();
        descriptionSheet(workbook);
        getUserEmailCampaignWorkBook(emailStateDto, event, workbook);

        return createExcelInMemory(workbook);
    }


    private static Sheet createExcelWorkbookSheet(Workbook workbook, String sheetName, String[] headers) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }
        return sheet;
    }

    public static byte[] createSFTPUserWorkSheet(Page<User> users, EventResponse event) {
        Workbook workbook = getUserWorkBook(users, event);
        return createExcelInMemory(workbook);
    }

    private static void getUserEmailCampaignWorkBook(List<EmailStateDto> emailStateDto, List<Event> event, Workbook workbook) {
        createUserEmailCampaignCsv(emailStateDto, event, workbook);
    }

    private static Workbook getUserWorkBook(Page<User> users, EventResponse event) {
        Workbook workbook = new XSSFWorkbook();
        createUserCsv(users, event, workbook);
        return workbook;
    }

    public static byte[] createExcelInMemory(Workbook workbook) {
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("An error occurred while createExcelWorkBook the Excel file: {}", e.getMessage());
            throw new CoffeeException(ErrorConstants.SYSTEM_ERROR_CODE, ErrorConstants.SOMETHING_WENT_WRONG);
        } finally {
            try {
                workbook.close();
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error("An error occurred while closing connection in createExcelWorkBook method: {}", e.getMessage());
                throw new CoffeeException(ErrorConstants.SYSTEM_ERROR_CODE, ErrorConstants.SOMETHING_WENT_WRONG);
            }
        }
    }

    public void createSummaryFileWithDailyCounts(Workbook workbook, Event event, List<Object[]> userCountsByDate) {
        log.info("Creating summary file with daily count.");
        Sheet sheet = workbook.createSheet(event.getEventCode());
        createSummaryFileHeaderRow(sheet);

        int rowIndex = 1;
        long totalUsers = 0;
        long totalContactOptInCount = 0;
        long totalMarketingOptInCount = 0;

        for (Object[] userCountData : userCountsByDate) {
            Date creationDate = (Date) userCountData[0];
            long userCount = (long) userCountData[1];
            long contactOptInCount = (long) userCountData[2];
            long marketingOptInCount = (long) userCountData[3];

            createSummaryFileDataRow(sheet, rowIndex++, event, creationDate, userCount, contactOptInCount, marketingOptInCount);
            totalUsers += userCount;
            totalContactOptInCount += contactOptInCount;
            totalMarketingOptInCount += marketingOptInCount;
        }

        createSummaryFileTotalRow(sheet, rowIndex, totalUsers, totalContactOptInCount, totalMarketingOptInCount);
        log.info("Writing summary file completed.");
    }

    public byte[] writeWorkbookToByteArray(Workbook workbook) throws IOException {
        log.info("Converting work book to byte array.");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }

    private void createSummaryFileHeaderRow(Sheet sheet) {
        log.info("Creating summary file header row.");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Zone");
        headerRow.createCell(1).setCellValue("General_Office");
        headerRow.createCell(2).setCellValue("Event_Name");
        headerRow.createCell(3).setCellValue("Event_Date");
        headerRow.createCell(4).setCellValue("User_Count");
        headerRow.createCell(5).setCellValue("Contact_Opt_In");
        headerRow.createCell(6).setCellValue("Email_Marketing_Opt_In");
    }

    private void createSummaryFileDataRow(Sheet sheet, int rowIndex, Event event, Date creationDate, long userCount, long contactOptInCount, long marketingOptInCount) {
        log.info("Creating summary file data rows.");
        Row dataRow = sheet.createRow(rowIndex);
        dataRow.createCell(0).setCellValue(event.getZone());
        dataRow.createCell(1).setCellValue(event.getGeneralOffice());
        dataRow.createCell(2).setCellValue(event.getEventName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(creationDate);
        dataRow.createCell(3).setCellValue(formattedDate);
        dataRow.createCell(4).setCellValue(userCount);
        dataRow.createCell(5).setCellValue(contactOptInCount);
        dataRow.createCell(6).setCellValue(marketingOptInCount);
    }

    private void createSummaryFileTotalRow(Sheet sheet, int rowIndex, long totalUsers, long totalContactOptInCount, long totalMarketingOptInCount) {
        log.info("Creating summary file total row for each event.");
        Row totalRow = sheet.createRow(rowIndex);
        totalRow.createCell(3).setCellValue("TOTAL");
        totalRow.createCell(4).setCellValue(totalUsers);
        totalRow.createCell(5).setCellValue(totalContactOptInCount);
        totalRow.createCell(6).setCellValue(totalMarketingOptInCount);
    }
}