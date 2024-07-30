package group8.skyweaver_inventory.models;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProfitRepository extends JpaRepository<Profit, Long> {
    Profit findByDate(LocalDateTime date);

    @Query("SELECT p.profit FROM Profit p")
    List<Double> findAllProfits();

    @Query("SELECT p.date, p.profit FROM Profit p")
    List<LocalDateTime> findAllDates();
}