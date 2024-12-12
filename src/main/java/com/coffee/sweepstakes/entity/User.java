package com.coffee.sweepstakes.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "Users")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true)
    private UUID id;
    private String eventCode;
    private String firstName;
    private String lastName;
    private String userEmail;
    private String userPhoneNumber;
    private boolean isActive;
    private boolean consentToContact;
    private boolean consentMarketing;
    private boolean isEmailSent;
    private boolean isEmailOpened;
    private boolean isFacebookLinkClicked;
    private boolean isInstaLinkClicked;
    private boolean isTwitterLinkClicked;
    private boolean isLinkedInLinkClicked;
    private boolean isNewsRoomClicked;
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
}
