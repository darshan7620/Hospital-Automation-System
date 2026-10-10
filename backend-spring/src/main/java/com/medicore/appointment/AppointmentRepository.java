package com.medicore.appointment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
            select a from Appointment a
            where (:from is null or a.startTime >= :from)
              and (:to is null or a.startTime < :to)
              and (:doctorId is null or a.doctor.id = :doctorId)
              and (:patientId is null or a.patient.id = :patientId)
              and (:status is null or a.status = :status)
            """)
    Page<Appointment> search(@Param("from") Instant from,
                             @Param("to") Instant to,
                             @Param("doctorId") Long doctorId,
                             @Param("patientId") Long patientId,
                             @Param("status") AppointmentStatus status,
                             Pageable pageable);

    @Query("""
            select count(a) from Appointment a
            where a.doctor.id = :doctorId
              and a.status in :statuses
              and a.startTime < :end
              and a.endTime > :start
              and (:excludeId is null or a.id <> :excludeId)
            """)
    long countConflicts(@Param("doctorId") Long doctorId,
                        @Param("statuses") Collection<AppointmentStatus> statuses,
                        @Param("start") Instant start,
                        @Param("end") Instant end,
                        @Param("excludeId") Long excludeId);

    @Modifying
    @Query("delete from Appointment a where a.doctor.id = :doctorId")
    int deleteByDoctorId(@Param("doctorId") Long doctorId);

    long countByStartTimeGreaterThanEqualAndStartTimeLessThan(Instant from, Instant to);

    long countByStartTimeGreaterThanEqualAndStatusIn(Instant from, Collection<AppointmentStatus> statuses);

    @Query("""
            select a.status as status, count(a) as total
            from Appointment a
            group by a.status
            """)
    List<AppointmentStatusCount> countGroupedByStatus();

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.department"})
    @Query(value = """
            select a from Appointment a
            where a.startTime >= :from and a.startTime < :to
            order by a.startTime asc, a.id asc
            """,
            countQuery = """
            select count(a) from Appointment a
            where a.startTime >= :from and a.startTime < :to
            """)
    Page<Appointment> findTodayWithDetails(@Param("from") Instant from,
                                           @Param("to") Instant to,
                                           Pageable pageable);

    @EntityGraph(attributePaths = {"patient", "doctor", "doctor.department"})
    @Query("""
            select a from Appointment a
            where a.startTime >= :from and a.status in :statuses
            order by a.startTime asc, a.id asc
            """)
    List<Appointment> findUpcomingWithDetails(@Param("from") Instant from,
                                              @Param("statuses") Collection<AppointmentStatus> statuses,
                                              Pageable pageable);
}
