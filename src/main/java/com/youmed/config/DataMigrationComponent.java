package com.youmed.config;

import com.youmed.repository.SpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataMigrationComponent implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataMigrationComponent.class);
    private final JdbcTemplate jdbcTemplate;
    private final SpecialtyRepository specialtyRepository;

    public DataMigrationComponent(JdbcTemplate jdbcTemplate, SpecialtyRepository specialtyRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.specialtyRepository = specialtyRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inserted = 0;
        int updated = 0;

        try {
            // Check if migration is needed
            Integer specialtiesCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM specialties", Integer.class);
            Integer doctorsNeedingMigration = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM doctors WHERE specialty_id IS NULL AND specialization IS NOT NULL",
                    Integer.class
            );

            boolean needsMigration = (specialtiesCount != null && specialtiesCount == 0) ||
                                     (doctorsNeedingMigration != null && doctorsNeedingMigration > 0);

            if (!needsMigration) {
                return; // Run only when needed
            }

            // 1. Fetch distinct raw specializations
            List<String> rawSpecializations = jdbcTemplate.queryForList(
                    "SELECT DISTINCT specialization FROM doctors WHERE specialization IS NOT NULL",
                    String.class
            );

            // 2. Normalize and insert
            for (String rawSpec : rawSpecializations) {
                String normalizedSpec = normalizeSpecialty(rawSpec);
                
                if (normalizedSpec != null && !specialtyRepository.existsByNameIgnoreCase(normalizedSpec)) {
                    jdbcTemplate.update(
                            "INSERT INTO specialties (name, active, created_at, updated_at) VALUES (?, true, NOW(), NOW())",
                            normalizedSpec
                    );
                    inserted++;
                }
            }

            // 3. Update doctors.specialty_id by matching normalized names ignoring case
            updated = jdbcTemplate.update(
                    "UPDATE doctors d " +
                    "SET specialty_id = s.id " +
                    "FROM specialties s " +
                    "WHERE LOWER(TRIM(d.specialization)) = LOWER(s.name) AND d.specialty_id IS NULL AND d.specialization IS NOT NULL"
            );

        } catch (Exception e) {
            log.debug("Specialty migration skipped or encountered an issue: {}", e.getMessage());
        }

        // 4. Update consultation_fee to 0 where it is null
        try {
            jdbcTemplate.update("UPDATE doctors SET consultation_fee = 0 WHERE consultation_fee IS NULL");
        } catch (Exception e) {
            log.debug("Consultation fee update skipped: {}", e.getMessage());
        }

        if (inserted > 0 || updated > 0) {
            log.info("\n===== Specialty Migration =====\n" +
                     "Specialties inserted: {}\n" +
                     "Doctors updated: {}\n" +
                     "Migration completed.\n" +
                     "===============================", inserted, updated);
        }
    }

    private String normalizeSpecialty(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        String trimmed = raw.trim().replaceAll("\\s+", " "); // Replace multiple spaces with one
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
