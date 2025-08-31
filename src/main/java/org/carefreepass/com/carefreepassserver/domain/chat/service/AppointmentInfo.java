package org.carefreepass.com.carefreepassserver.domain.chat.service;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class AppointmentInfo {
    private static final String HOSPITAL_NAME = "서울대병원";
    
    private String department;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    
    public String getHospitalName() {
        return HOSPITAL_NAME;
    }
    
    public boolean isValid() {
        return department != null && appointmentDate != null && appointmentTime != null;
    }
}