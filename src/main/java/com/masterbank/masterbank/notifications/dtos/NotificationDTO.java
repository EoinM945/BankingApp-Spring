package com.masterbank.masterbank.notifications.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.masterbank.masterbank.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {

    private Long id;
    private String subject;

    @NotBlank(message = "Recipient cannot be blank")
    private String recipient;

    private String body;
    private NotificationType type;
    private LocalDateTime createdDate;

    private String templateName;
    private Map<String, Object> templateVariables;
}
