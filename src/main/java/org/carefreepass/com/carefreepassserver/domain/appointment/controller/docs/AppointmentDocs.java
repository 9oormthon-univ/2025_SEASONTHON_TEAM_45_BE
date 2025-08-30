package org.carefreepass.com.carefreepassserver.domain.appointment.controller.docs;

import jakarta.validation.Valid;
import java.util.List;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCheckinRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentCreateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.request.AppointmentUpdateRequest;
import org.carefreepass.com.carefreepassserver.domain.appointment.dto.response.AppointmentResponse;
import org.carefreepass.com.carefreepassserver.golbal.response.ApiResponseTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface AppointmentDocs {
    
    ApiResponseTemplate<Long> createAppointment(@Valid @RequestBody AppointmentCreateRequest request);
    
    ApiResponseTemplate<String> checkinAppointment(@Valid @RequestBody AppointmentCheckinRequest request);
    
    ApiResponseTemplate<List<AppointmentResponse>> getTodayWaitingPatients();
    
    ApiResponseTemplate<List<AppointmentResponse>> getAllTodayAppointments();
    
    ApiResponseTemplate<String> deleteAppointment(@PathVariable Long appointmentId);
    
    ApiResponseTemplate<String> updateAppointmentStatus(@PathVariable Long appointmentId, @PathVariable String status);
    
    ApiResponseTemplate<String> updateAppointment(@PathVariable Long appointmentId, @Valid @RequestBody AppointmentUpdateRequest request);
}