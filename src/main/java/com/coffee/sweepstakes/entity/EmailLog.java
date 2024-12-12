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
@Table(name = "Email_Logs")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmailLog {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true)
    private UUID id;
    private UUID userId;
    private Date emailSentAt;
    private String statusMessage;
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
}
