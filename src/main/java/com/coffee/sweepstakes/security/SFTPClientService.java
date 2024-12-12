package com.coffee.sweepstakes.security;

import com.coffee.sweepstakes.config.ServiceConfig;
import com.coffee.sweepstakes.dao.SftpLogsDao;
import com.coffee.sweepstakes.entity.SftpLogs;
import com.coffee.sweepstakes.exceptions.CoffeeException;
import com.coffee.sweepstakes.util.DateUtils;
import com.coffee.sweepstakes.util.ErrorConstants;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class SFTPClientService {

    private final ServiceConfig serviceConfig;
    private final SftpLogsDao sftpLogsDao;
    Session session;
    ChannelSftp channelSftp;

    public void uploadFile(String fileName, byte[] fileBytes) {
        try {
            session = getSFTPSession();
            channelSftp = getSFTPChannel(session);
            log.info("Going to upload file {} to SFTP", fileName);
            channelSftp.put(new ByteArrayInputStream(fileBytes), fileName);
            log.info("File {} successfully uploaded to sftp", fileName);
            saveSFTPLogs(fileName, true, null);
        } catch (Exception e) {
            saveSFTPLogs(fileName, false, e.getMessage());
            log.error("Error while uploading report to sftp", e);
            throw new CoffeeException(ErrorConstants.SYSTEM_ERROR_CODE, ErrorConstants.SOMETHING_WENT_WRONG);
        } finally {
            channelSftp.disconnect();
            session.disconnect();
        }
    }

    public byte[] downloadFile(String fileName) {
        try {
            Session session = getSFTPSession();
            channelSftp = getSFTPChannel(session);
            log.info("Going to get file {} to SFTP", fileName);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channelSftp.get(fileName, outputStream);
            log.info("File {} successfully downloading from sftp", fileName);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error while downloading report to sftp", e);
            throw new CoffeeException(ErrorConstants.SYSTEM_ERROR_CODE, ErrorConstants.SOMETHING_WENT_WRONG);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public void deleteFile(String fileName) {
        try {
            Session session = getSFTPSession();
            ChannelSftp channelSftp = getSFTPChannel(session);
            log.info("Going to delete file {} from SFTP", fileName);
            channelSftp.rm(fileName);
            log.info("File {} successfully deleted from SFTP", fileName);
        } catch (Exception e) {
            log.error("Error while deleting file from SFTP", e);
            throw new CoffeeException(ErrorConstants.SYSTEM_ERROR_CODE, ErrorConstants.SOMETHING_WENT_WRONG);
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }


    private void saveSFTPLogs(String fileName, boolean isFileUploaded, String message) {
        SftpLogs sftpLogs = SftpLogs.builder().fileName(fileName).sentDate(DateUtils.getAppDate()).fileUploaded(isFileUploaded).errorDetails(message).build();
        sftpLogsDao.saveSftpLogs(sftpLogs);
        log.info("Details added successfully inside sftpLogs table");
    }

    private ChannelSftp getSFTPChannel(Session session) throws JSchException, SftpException {
        log.info("Creating ChannelSftp through Session");
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
        log.info("Establishing connection with channelSFTP!");
        channelSftp.connect();
        channelSftp.cd(serviceConfig.getSftpDirectory());
        return channelSftp;
    }

    private Session getSFTPSession() throws JSchException {
        JSch jsch = new JSch();
        log.info("Creating session!");
        Session session = jsch.getSession(serviceConfig.getSftpUserName(), serviceConfig.getSftpHost(), serviceConfig.getSftpPort());
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(serviceConfig.getSftpPassword());
        log.info("Establishing connection with session!");
        session.connect();
        return session;
    }
}
