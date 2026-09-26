package com.youmed.controller;

import com.youmed.entity.Diagnosis;
import com.youmed.entity.Medicine;
import com.youmed.entity.MedicineCategory;
import com.youmed.entity.Symptom;
import com.youmed.repository.DiagnosisRepository;
import com.youmed.repository.MedicineCategoryRepository;
import com.youmed.repository.MedicineRepository;
import com.youmed.repository.SymptomRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/master-data")
@RequiredArgsConstructor
@Tag(name = "Master Data", description = "Master data APIs for dropdowns (Medicines, Symptoms, Diagnoses...)")
public class MasterDataController {

    private final MedicineCategoryRepository medicineCategoryRepository;
    private final MedicineRepository medicineRepository;
    private final SymptomRepository symptomRepository;
    private final DiagnosisRepository diagnosisRepository;

    @GetMapping("/medicine-categories")
    @Operation(summary = "Get all medicine categories")
    public ResponseEntity<List<MedicineCategory>> getAllMedicineCategories() {
        return ResponseEntity.ok(medicineCategoryRepository.findAll());
    }

    @GetMapping("/medicines")
    @Operation(summary = "Get all medicines or filter by category")
    public ResponseEntity<List<Medicine>> getMedicines(
            @RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return ResponseEntity.ok(medicineRepository.findByCategoryId(categoryId));
        }
        return ResponseEntity.ok(medicineRepository.findAll());
    }

    @GetMapping("/symptoms")
    @Operation(summary = "Get all symptoms")
    public ResponseEntity<List<Symptom>> getAllSymptoms() {
        return ResponseEntity.ok(symptomRepository.findAll());
    }

    @GetMapping("/diagnoses")
    @Operation(summary = "Get all diagnoses")
    public ResponseEntity<List<Diagnosis>> getAllDiagnoses() {
        return ResponseEntity.ok(diagnosisRepository.findAll());
    }
}
