package com.master.invoicemanagementserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "batch_upload")
@Getter
@Setter
@NoArgsConstructor
public class BatchUpload {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String importFileName;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endTime;

}
