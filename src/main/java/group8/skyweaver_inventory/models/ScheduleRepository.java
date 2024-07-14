package group8.skyweaver_inventory.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserUsername(String username);
    Schedule getById(Long id);
}
