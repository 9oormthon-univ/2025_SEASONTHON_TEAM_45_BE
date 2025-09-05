package org.carefreepass.com.carefreepassserver.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AppointmentInfo {
    private Long hospitalId;
    private String hospitalName;
    private String department;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    
    public boolean isValid() {
        return department != null && appointmentDate != null && appointmentTime != null;
    }
}