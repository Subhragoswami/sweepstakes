package com.coffee.sweepstakes.repository;

import com.coffee.sweepstakes.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.userEmail) = LOWER(:email) AND u.eventCode = :eventCode")
    boolean existsByUserEmail(@Param("email") String email, @Param("eventCode") String eventCode);

    @Query("SELECT u FROM User u WHERE LOWER(u.userEmail) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT(:search, '%'))")
    Page<User> getAllBySearch(Pageable pageable, @Param("search") String search);

    @Query("SELECT u FROM User u WHERE (LOWER(u.userEmail) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.firstName) LIKE LOWER(CONCAT(:search, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT(:search, '%'))) AND u.eventCode = :eventCode")
    Page<User> getAllUsersWithSearchAndEventCode(Pageable pageable, @Param("search") String search, @Param("eventCode") String eventCode);

    @Query("SELECT u FROM User u WHERE u.eventCode = :eventCode ORDER BY u.createdAt DESC")
    Page<User> getAllUsersByEventCode(Pageable pageable, @Param("eventCode") String eventCode);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id = :userId")
    void updateIsActiveById(@Param("userId") UUID id, @Param("isActive") boolean isActive);

    Optional<User> findByEventCodeAndUserEmailIgnoreCase(@Param("eventCode") String eventCode, @Param("email") String email);

    @Query(value = "SELECT " +
            "u.event_code AS eventCode, " +
            "COUNT(*) AS totalUsers, " +
            "SUM(CASE WHEN u.is_email_sent THEN 1 ELSE 0 END) AS emailSentCount, " +
            "SUM(CASE WHEN u.is_email_opened THEN 1 ELSE 0 END) AS emailOpenedCount, " +
            "SUM(CASE WHEN u.is_facebook_link_clicked THEN 1 ELSE 0 END) AS facebookClickCount, " +
            "SUM(CASE WHEN u.is_insta_link_clicked THEN 1 ELSE 0 END) AS instaClickCount, " +
            "SUM(CASE WHEN u.is_twitter_link_clicked THEN 1 ELSE 0 END) AS twitterClickCount, " +
            "SUM(CASE WHEN u.is_linked_in_link_clicked THEN 1 ELSE 0 END) AS linkedinClickCount, " +
            "SUM(CASE WHEN u.is_News_Room_clicked THEN 1 ELSE 0 END) AS newsRoomClickCount, " +
            "COUNT(DISTINCT CASE " +
            "WHEN u.is_facebook_link_clicked = TRUE " +
            "OR u.is_insta_link_clicked = TRUE " +
            "OR u.is_twitter_link_clicked = TRUE " +
            "OR u.is_linked_in_link_clicked = TRUE " +
            "OR u.is_News_Room_clicked = TRUE " +
            "THEN u.id " +
            "ELSE NULL END) AS uniqueClickCount " +
            "FROM Users u " +
            "WHERE u.is_active = TRUE " +
            "GROUP BY u.event_code",
            nativeQuery = true)
    List<Object[]> getEmailCampaignStatsForAllEvents();

    @Query("SELECT DATE(u.createdAt) as creationDate, " +
            "COUNT(u) as userCount, " +
            "SUM(CASE WHEN u.consentToContact = true THEN 1 ELSE 0 END) as contactOptInCount, " +
            "SUM(CASE WHEN u.consentMarketing = true THEN 1 ELSE 0 END) as marketingOptInCount " +
            "FROM User u " +
            "WHERE u.eventCode = :eventCode " +
            "AND u.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(u.createdAt)")
    List<Object[]> findUserCountByEventCode(
            @Param("eventCode") String eventCode,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


}
