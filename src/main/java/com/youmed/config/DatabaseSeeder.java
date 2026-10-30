package com.youmed.config;

import com.youmed.entity.*;
import com.youmed.enums.*;
import com.youmed.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class DatabaseSeeder implements ApplicationRunner {

    private final DataSource dataSource;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final DoctorReviewRepository doctorReviewRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAll(false);
    }

    @Transactional
    public void seedAll(boolean force) {
        log.info("Checking database seeding status (force={})...", force);

        // 1. Seed Master Data (Specialties, Medicines, Symptoms, Diagnoses)
        seedMasterDataSql();

        // 2. Always ensure users, managers, and doctors are up to date
        Map<String, User> userMap = seedUsers();
        updateSpecialtyManagers(userMap);
        List<Doctor> doctors = seedDoctors(userMap);
        Map<Long, List<TimeSlot>> doctorSlotsMap = seedDoctorSchedulesAndTimeSlots(doctors);

        // 3. Ensure completed appointments have realistic reviews
        ensureDoctorReviews();

        // 4. Check if appointments already populated
        if (!force && appointmentRepository.count() >= 100) {
            log.info("Appointments and medical records already populated (Doctors: {}, Appointments: {}). Master data and doctor rosters synced.",
                    doctorRepository.count(), appointmentRepository.count());
            return;
        }

        log.info("Starting MASSIVE Database Seeding for Appointments and Medical Records...");

        // 4. Seed Appointments with diverse statuses
        List<User> patients = extractPatients(userMap);
        List<Appointment> appointments = seedMassiveAppointments(patients, doctors, doctorSlotsMap);

        // 8. Seed Medical Records & Prescriptions for COMPLETED appointments
        seedMedicalRecordsAndPrescriptions(appointments);

        // 9. Seed Invoices for appointments
        seedInvoices(appointments);

        log.info("\n====================================================================\n" +
                "  MASSIVE DATABASE SEEDING COMPLETED SUCCESSFULLY!\n" +
                "  - Total Specialties: {}\n" +
                "  - Total Doctors: {}\n" +
                "  - Total Patients: {}\n" +
                "  - Total TimeSlots: {}\n" +
                "  - Total Appointments: {}\n" +
                "  - Total Medical Records: {}\n" +
                "  - Total Prescriptions: {}\n" +
                "  - Total Invoices: {}\n" +
                "  All passwords: 123456 (Admin: admin123)\n" +
                "====================================================================",
                specialtyRepository.count(),
                doctorRepository.count(),
                patients.size(),
                timeSlotRepository.count(),
                appointmentRepository.count(),
                medicalRecordRepository.count(),
                prescriptionRepository.count(),
                invoiceRepository.count()
        );
    }

    private void seedMasterDataSql() {
        try {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("migration_master_data.sql"));
            populator.setContinueOnError(true);
            populator.execute(dataSource);
            log.info("Master data SQL executed successfully.");
        } catch (Exception e) {
            log.warn("Master data SQL execution note: {}", e.getMessage());
        }
    }

    private Map<String, User> seedUsers() {
        Map<String, User> map = new HashMap<>();
        String defaultPw = passwordEncoder.encode("123456");

        // 1. Admin
        if (!userRepository.existsByEmail("admin@gmail.com")) {
            User admin = User.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Quản Trị Viên Hệ Thống")
                    .phone("0901000001")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            map.put("admin@gmail.com", userRepository.save(admin));
        } else {
            map.put("admin@gmail.com", userRepository.findByEmail("admin@gmail.com").get());
        }

        // 2. 5 Receptionists
        createOrGetUser(map, "letan1@youmed.com", defaultPw, "Nguyễn Thị Lễ Tân", "0902000001", "Nữ", LocalDate.of(1996, 5, 12), Role.RECEPTIONIST, "Quận 1, TP.HCM");
        createOrGetUser(map, "letan2@youmed.com", defaultPw, "Trần Văn Tiếp Đón", "0902000002", "Nam", LocalDate.of(1994, 8, 20), Role.RECEPTIONIST, "Quận Bình Thạnh, TP.HCM");
        createOrGetUser(map, "letan3@youmed.com", defaultPw, "Lê Thị Thu Thảo", "0902000003", "Nữ", LocalDate.of(1998, 11, 4), Role.RECEPTIONIST, "Quận 3, TP.HCM");
        createOrGetUser(map, "letan4@youmed.com", defaultPw, "Phạm Ngọc Anh", "0902000004", "Nữ", LocalDate.of(1997, 3, 19), Role.RECEPTIONIST, "Quận Phú Nhuận, TP.HCM");
        createOrGetUser(map, "letan5@youmed.com", defaultPw, "Vũ Hoàng Quân", "0902000005", "Nam", LocalDate.of(1995, 7, 28), Role.RECEPTIONIST, "Quận 10, TP.HCM");

        // 3. 13 Department Managers (1 for each specialty)
        createOrGetUser(map, "manager.noikhoa@youmed.com", defaultPw, "PGS.TS Lê Văn Trưởng", "0903000001", "Nam", LocalDate.of(1972, 3, 15), Role.DEPARTMENT_MANAGER, "Hà Nội");
        createOrGetUser(map, "manager.nhikhoa@youmed.com", defaultPw, "BSCKII Hoàng Văn Nhi", "0903000002", "Nam", LocalDate.of(1978, 11, 5), Role.DEPARTMENT_MANAGER, "Đà Nẵng");
        createOrGetUser(map, "manager.sanphukhoa@youmed.com", defaultPw, "TS.BS Đặng Thị Phụ Sản", "0903000003", "Nữ", LocalDate.of(1975, 4, 18), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.dalieu@youmed.com", defaultPw, "PGS.TS Nguyễn Văn Da", "0903000004", "Nam", LocalDate.of(1970, 9, 21), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.mat@youmed.com", defaultPw, "BSCKII Trần Quang Mắt", "0903000005", "Nam", LocalDate.of(1974, 1, 30), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.taimuihong@youmed.com", defaultPw, "TS.BS Vũ Thị Họng", "0903000006", "Nữ", LocalDate.of(1977, 8, 14), Role.DEPARTMENT_MANAGER, "Hà Nội");
        createOrGetUser(map, "manager.ranghammat@youmed.com", defaultPw, "BSCKII Bùi Văn Răng", "0903000007", "Nam", LocalDate.of(1973, 12, 10), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.timmach@youmed.com", defaultPw, "TS.BS Phạm Thị Trưởng", "0903000008", "Nữ", LocalDate.of(1976, 7, 22), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.thankinh@youmed.com", defaultPw, "PGS.TS Đỗ Văn Kinh", "0903000009", "Nam", LocalDate.of(1969, 6, 25), Role.DEPARTMENT_MANAGER, "Hà Nội");
        createOrGetUser(map, "manager.coxuongkhop@youmed.com", defaultPw, "BSCKII Hồ Thị Khớp", "0903000010", "Nữ", LocalDate.of(1971, 10, 8), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.truyennhiem@youmed.com", defaultPw, "BSCKII Đặng Thị Thúy", "0903000011", "Nữ", LocalDate.of(1976, 5, 12), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.hohap@youmed.com", defaultPw, "PGS.TS Trần Văn Hô", "0903000012", "Nam", LocalDate.of(1973, 2, 10), Role.DEPARTMENT_MANAGER, "TP.HCM");
        createOrGetUser(map, "manager.tieuhoa@youmed.com", defaultPw, "BSCKII Nguyễn Văn Tiêu", "0903000013", "Nam", LocalDate.of(1971, 8, 20), Role.DEPARTMENT_MANAGER, "TP.HCM");

        // 4. 29 Doctors
        String[][] doctorData = {
                {"bs.tuan@youmed.com", "ThS.BS Nguyễn Anh Tuấn", "0904000001", "Nam", "1982-04-18", "Quận 3, TP.HCM"},
                {"bs.binh@youmed.com", "TS.BS Trần Văn Bình", "0904000002", "Nam", "1980-08-11", "Quận 1, TP.HCM"},
                {"bs.mai@youmed.com", "BSCKII Hoàng Thị Mai", "0904000003", "Nữ", "1985-02-14", "Quận 5, TP.HCM"},
                {"bs.lan@youmed.com", "BSCKII Trần Mai Lan", "0904000004", "Nữ", "1985-09-25", "Quận 7, TP.HCM"},
                {"bs.son@youmed.com", "ThS.BS Lê Hồng Sơn", "0904000005", "Nam", "1988-12-05", "Quận 2, TP.HCM"},
                {"bs.trang@youmed.com", "BSCKI Nguyễn Thùy Trang", "0904000006", "Nữ", "1991-03-22", "Quận Tân Bình, TP.HCM"},
                {"bs.oanh@youmed.com", "BSCKII Đỗ Thị Kim Oanh", "0904000007", "Nữ", "1981-06-19", "Quận 10, TP.HCM"},
                {"bs.ngocanh@youmed.com", "TS.BS Vũ Thị Ngọc Anh", "0904000008", "Nữ", "1984-10-15", "Quận Phú Nhuận, TP.HCM"},
                {"bs.huong@youmed.com", "TS.BS Phạm Thu Hương", "0904000009", "Nữ", "1988-06-30", "Quận Tân Bình, TP.HCM"},
                {"bs.huy@youmed.com", "ThS.BS Đặng Quốc Huy", "0904000010", "Nam", "1987-05-12", "Quận Bình Thạnh, TP.HCM"},
                {"bs.linh@youmed.com", "BSCKI Lê Khánh Linh", "0904000011", "Nữ", "1992-09-08", "Quận 4, TP.HCM"},
                {"bs.minh@youmed.com", "BSCKI Hoàng Quang Minh", "0904000012", "Nam", "1986-12-14", "TP. Thủ Đức, TP.HCM"},
                {"bs.nam@youmed.com", "ThS.BS Phan Văn Nam", "0904000013", "Nam", "1983-01-20", "Quận 6, TP.HCM"},
                {"bs.thao@youmed.com", "ThS.BS Đỗ Phương Thảo", "0904000014", "Nữ", "1990-08-08", "Quận Phú Nhuận, TP.HCM"},
                {"bs.dang@youmed.com", "BSCKII Nguyễn Hải Đăng", "0904000015", "Nam", "1979-11-17", "Quận 11, TP.HCM"},
                {"bs.hung@youmed.com", "BSCKII Vũ Mạnh Hùng", "0904000016", "Nam", "1980-02-28", "Quận 5, TP.HCM"},
                {"bs.ha@youmed.com", "ThS.BS Trịnh Thu Hà", "0904000017", "Nữ", "1989-07-04", "Quận 8, TP.HCM"},
                {"bs.dung@youmed.com", "PGS.TS Lê Quốc Dũng", "0904000018", "Nam", "1975-01-10", "Quận 10, TP.HCM"},
                {"bs.cuong@youmed.com", "TS.BS Nguyễn Khắc Cường", "0904000019", "Nam", "1982-04-29", "Quận 3, TP.HCM"},
                {"bs.minhtuan@youmed.com", "BSCKII Phạm Minh Tuấn", "0904000020", "Nam", "1978-09-14", "Quận 1, TP.HCM"},
                {"bs.hungtk@youmed.com", "PGS.TS Đinh Trọng Hưng", "0904000021", "Nam", "1974-03-08", "Quận Cầu Giấy, Hà Nội"},
                {"bs.hang@youmed.com", "BSCKII Lương Thị Thu Hằng", "0904000022", "Nữ", "1986-12-23", "Quận Đống Đa, Hà Nội"},
                {"bs.khoa@youmed.com", "TS.BS Bùi Đăng Khoa", "0904000023", "Nam", "1983-10-19", "Quận 11, TP.HCM"},
                {"bs.trong@youmed.com", "ThS.BS Ngô Văn Trọng", "0904000024", "Nam", "1987-06-15", "Quận Gò Vấp, TP.HCM"},
                {"bs.hanh@youmed.com", "BSCKII Đỗ Thị Mỹ Hạnh", "0904000025", "Nữ", "1982-08-30", "Quận 12, TP.HCM"},
                {"bs.chaunhietdoi@youmed.com", "TS.BS Nguyễn Văn Vĩnh Châu", "0904000026", "Nam", "1966-10-15", "Quận 5, TP.HCM"},
                {"bs.dungtruyennhiem@youmed.com", "BSCKII Lê Thị Mỹ Dung", "0904000027", "Nữ", "1979-04-12", "Quận 10, TP.HCM"},
                {"bs.thanhtuyenhohap@youmed.com", "ThS.BS Nguyễn Thanh Tuyền", "0904000028", "Nữ", "1984-07-20", "Quận 1, TP.HCM"},
                {"bs.minhtieuhoa@youmed.com", "BSCKII Trần Quang Minh", "0904000029", "Nam", "1980-03-15", "Quận 3, TP.HCM"}
        };

        for (String[] doc : doctorData) {
            createOrGetUser(map, doc[0], defaultPw, doc[1], doc[2], doc[3], LocalDate.parse(doc[4]), Role.DOCTOR, doc[5]);
        }

        // 5. 60 Patients (Diverse ages, genders, locations)
        String[] firstNames = {"Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ", "Hồ", "Ngô", "Dương"};
        String[] middleMale = {"Văn", "Hữu", "Đức", "Minh", "Quốc", "Gia", "Thành", "Công"};
        String[] middleFemale = {"Thị", "Ngọc", "Thu", "Mai", "Phương", "Khánh", "Thanh"};
        String[] lastNamesMale = {"Nam", "Hải", "Tuấn", "Long", "Phúc", "Khang", "Tùng", "Bách", "Kiên", "Duy", "Thắng", "Bình", "Sơn", "Đạt"};
        String[] lastNamesFemale = {"Hoa", "Mai", "Lan", "Hương", "Trang", "Linh", "Thảo", "Hà", "Vy", "Huyền", "Yến", "Ngân", "Anh", "Nhi"};
        String[] cities = {"Hà Nội", "TP.HCM", "Đà Nẵng", "Cần Thơ", "Hải Phòng", "Huế", "Nha Trang", "Bình Dương", "Đồng Nai", "Quảng Ninh"};

        Random rand = new Random(42); // deterministic seed for consistency
        for (int i = 1; i <= 60; i++) {
            boolean isMale = (i % 2 == 1);
            String lastName = firstNames[rand.nextInt(firstNames.length)];
            String middle = isMale ? middleMale[rand.nextInt(middleMale.length)] : middleFemale[rand.nextInt(middleFemale.length)];
            String firstName = isMale ? lastNamesMale[rand.nextInt(lastNamesMale.length)] : lastNamesFemale[rand.nextInt(lastNamesFemale.length)];
            String fullName = lastName + " " + middle + " " + firstName;
            String email = "benhnhan" + i + "@gmail.com";
            String phone = String.format("09%08d", 50000000 + i);
            String gender = isMale ? "Nam" : "Nữ";
            int year = 1950 + rand.nextInt(55); // 1950 - 2005
            int month = 1 + rand.nextInt(12);
            int day = 1 + rand.nextInt(28);
            String address = (rand.nextInt(900) + 1) + " Đường số " + (rand.nextInt(20) + 1) + ", " + cities[rand.nextInt(cities.length)];

            createOrGetUser(map, email, defaultPw, fullName, phone, gender, LocalDate.of(year, month, day), Role.PATIENT, address);
        }

        return map;
    }

    private void createOrGetUser(Map<String, User> map, String email, String password, String name, String phone, String gender, LocalDate dob, Role role, String address) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .password(password)
                    .fullName(name)
                    .phone(phone)
                    .gender(gender)
                    .dateOfBirth(dob)
                    .role(role)
                    .address(address)
                    .enabled(true)
                    .build();
            user = userRepository.save(user);
        }
        map.put(email, user);
    }

    private void updateSpecialtyManagers(Map<String, User> userMap) {
        String[][] mappings = {
                {"Nội khoa", "manager.noikhoa@youmed.com"},
                {"Nhi khoa", "manager.nhikhoa@youmed.com"},
                {"Sản phụ khoa", "manager.sanphukhoa@youmed.com"},
                {"Da liễu", "manager.dalieu@youmed.com"},
                {"Mắt", "manager.mat@youmed.com"},
                {"Tai Mũi Họng", "manager.taimuihong@youmed.com"},
                {"Răng Hàm Mặt", "manager.ranghammat@youmed.com"},
                {"Tim mạch", "manager.timmach@youmed.com"},
                {"Thần kinh", "manager.thankinh@youmed.com"},
                {"Cơ xương khớp", "manager.coxuongkhop@youmed.com"},
                {"Truyền nhiễm", "manager.truyennhiem@youmed.com"},
                {"Hô hấp", "manager.hohap@youmed.com"},
                {"Tiêu hóa - Gan mật", "manager.tieuhoa@youmed.com"}
        };

        for (String[] mapping : mappings) {
            Specialty specialty = specialtyRepository.findByName(mapping[0]).orElse(null);
            User manager = userMap.get(mapping[1]);
            if (specialty != null && manager != null) {
                specialty.setManager(manager);
                specialtyRepository.save(specialty);
            }
        }
    }

    private List<Doctor> seedDoctors(Map<String, User> userMap) {
        List<Doctor> doctors = new ArrayList<>();

        Object[][] doctorConfig = {
                // Nội khoa
                {"bs.tuan@youmed.com", "Nội khoa", 200000.0, "Thạc sĩ Bác sĩ chuyên khoa Nội tổng quát hơn 15 năm kinh nghiệm điều trị tiêu hóa, dạ dày, gan mật.", "/images/doctors/bs-tuan.jpg"},
                {"bs.binh@youmed.com", "Nội khoa", 250000.0, "Tiến sĩ Bác sĩ chuyên khoa Nội tiết - Đái tháo đường, Bệnh viện Chợ Rẫy.", "/images/doctors/bs-binh.jpg"},
                {"bs.mai@youmed.com", "Nội khoa", 220000.0, "Bác sĩ Chuyên khoa II Nội tổng hợp, tận tâm, nhiều năm công tác tại BV Đại học Y Dược.", "/images/doctors/bs-mai.jpg"},
                // Nhi khoa
                {"bs.lan@youmed.com", "Nhi khoa", 250000.0, "Bác sĩ Chuyên khoa II Nhi, nguyên Trưởng khoa Nhi BV Nhi Đồng 2, chuyên sâu hô hấp và dinh dưỡng.", "/images/doctors/bs-lan.jpg"},
                {"bs.son@youmed.com", "Nhi khoa", 180000.0, "Thạc sĩ Bác sĩ Nhi khoa, chuyên khám và tư vấn các bệnh truyền nhiễm và miễn dịch ở trẻ.", "/images/doctors/bs-son.jpg"},
                {"bs.trang@youmed.com", "Nhi khoa", 180000.0, "Bác sĩ Chuyên khoa I Nhi, nhẹ nhàng, chuyên khám các bệnh tiêu hóa và sơ sinh.", "/images/doctors/bs-trang.jpg"},
                // Sản phụ khoa
                {"bs.oanh@youmed.com", "Sản phụ khoa", 300000.0, "Bác sĩ Chuyên khoa II Sản Phụ khoa, BV Từ Dũ, chuyên theo dõi thai kỳ nguy cơ cao và hiếm muộn.", "/images/doctors/bs-oanh.jpg"},
                {"bs.ngocanh@youmed.com", "Sản phụ khoa", 250000.0, "Tiến sĩ Bác sĩ Sản khoa, hơn 12 năm kinh nghiệm phẫu thuật nội soi phụ khoa.", "/images/doctors/bs-ngocanh.jpg"},
                // Da liễu
                {"bs.huong@youmed.com", "Da liễu", 200000.0, "Tiến sĩ Bác sĩ Da liễu, chuyên gia đầu ngành về điều trị mụn, viêm da cơ địa và thẩm mỹ da liễu.", "/images/doctors/bs-huong.jpg"},
                {"bs.huy@youmed.com", "Da liễu", 220000.0, "Thạc sĩ Bác sĩ Da liễu, BV Da Liễu TP.HCM, chuyên laser thẩm mỹ và bệnh tự miễn.", "/images/doctors/bs-huy.jpg"},
                {"bs.linh@youmed.com", "Da liễu", 180000.0, "Bác sĩ Chuyên khoa I Da liễu, giàu kinh nghiệm chăm sóc và phục hồi da tổn thương.", "/images/doctors/bs-linh.jpg"},
                // Mắt
                {"bs.minh@youmed.com", "Mắt", 180000.0, "Bác sĩ Chuyên khoa I Mắt, chuyên khám tật khúc xạ, đục thủy tinh thể và các bệnh lý giác mạc.", "/images/doctors/bs-minh.jpg"},
                {"bs.nam@youmed.com", "Mắt", 200000.0, "Thạc sĩ Bác sĩ Nhãn khoa, chuyên phẫu thuật Lasik và điều trị Glaucoma.", "/images/doctors/bs-nam.jpg"},
                // Tai Mũi Họng
                {"bs.thao@youmed.com", "Tai Mũi Họng", 200000.0, "Thạc sĩ Bác sĩ Tai Mũi Họng, BV Tai Mũi Họng Trung ương, chuyên trị viêm xoang, viêm tai giữa.", "/images/doctors/bs-thao.jpg"},
                {"bs.dang@youmed.com", "Tai Mũi Họng", 250000.0, "Bác sĩ Chuyên khoa II Tai Mũi Họng, chuyên phẫu thuật nội soi xoang và thanh quản.", "/images/doctors/bs-dang.jpg"},
                // Răng Hàm Mặt
                {"bs.hung@youmed.com", "Răng Hàm Mặt", 220000.0, "Bác sĩ Chuyên khoa II Răng Hàm Mặt, hơn 18 năm kinh nghiệm phục hình răng sứ và nha chu.", "/images/doctors/bs-hung.jpg"},
                {"bs.ha@youmed.com", "Răng Hàm Mặt", 200000.0, "Thạc sĩ Bác sĩ Nha khoa thẩm mỹ, niềng răng trong suốt và cấy ghép Implant.", "/images/doctors/bs-ha.jpg"},
                // Tim mạch
                {"bs.dung@youmed.com", "Tim mạch", 350000.0, "Phó Giáo sư, Tiến sĩ Tim mạch can thiệp, Viện Tim TP.HCM, chuyên gia tăng huyết áp và mạch vành.", "/images/doctors/bs-dung-tm.jpg"},
                {"bs.cuong@youmed.com", "Tim mạch", 280000.0, "Tiến sĩ Bác sĩ Tim mạch, chuyên gia siêu âm tim và rối loạn nhịp tim.", "/images/doctors/bs-cuong.jpg"},
                {"bs.minhtuan@youmed.com", "Tim mạch", 250000.0, "Bác sĩ Chuyên khoa II Tim mạch, chuyên điều trị suy tim và xơ vữa động mạch.", "/images/doctors/bs-minhtuan.jpg"},
                // Thần kinh
                {"bs.hungtk@youmed.com", "Thần kinh", 350000.0, "Phó Giáo sư, Tiến sĩ Thần kinh học, BV Bạch Mai, chuyên gia điều trị tai biến mạch máu não và Parkinson.", "/images/doctors/bs-hungtk.jpg"},
                {"bs.hang@youmed.com", "Thần kinh", 250000.0, "Bác sĩ Chuyên khoa II Thần kinh, chuyên trị mất ngủ kéo dài, đau nửa đầu Migraine và đau dây thần kinh.", "/images/doctors/bs-hang.jpg"},
                // Cơ xương khớp
                {"bs.khoa@youmed.com", "Cơ xương khớp", 250000.0, "Tiến sĩ Bác sĩ Cơ Xương Khớp, chuyên điều trị thoái hóa khớp, thoát vị đĩa đệm và bệnh gút.", "/images/doctors/bs-khoa.jpg"},
                {"bs.trong@youmed.com", "Cơ xương khớp", 200000.0, "Thạc sĩ Bác sĩ Chấn thương Chỉnh hình và Cơ xương khớp, BV Chấn thương Chỉnh hình.", "/images/doctors/bs-trong.jpg"},
                {"bs.hanh@youmed.com", "Cơ xương khớp", 220000.0, "Bác sĩ Chuyên khoa II Cơ Xương Khớp, chuyên tiêm khớp vi điểm và phục hồi chức năng.", "/images/doctors/bs-hanh.jpg"},
                // Truyền nhiễm & Tiêm chủng vắc xin
                {"bs.chaunhietdoi@youmed.com", "Truyền nhiễm", 300000.0, "Tiến sĩ Bác sĩ chuyên khoa Bệnh Nhiệt đới và Truyền nhiễm, nguyên Giám đốc BV Bệnh Nhiệt Đới, chuyên gia hàng đầu về phơi nhiễm dại, uốn ván, sốt xuất huyết, viêm gan.", "/images/doctors/bs-chau.jpg"},
                {"bs.dungtruyennhiem@youmed.com", "Truyền nhiễm", 220000.0, "Bác sĩ Chuyên khoa II Truyền nhiễm & Tiêm chủng, chuyên xử lý khẩn cấp vết cắn động vật nghi dại, tư vấn vắc-xin và kháng huyết thanh.", "/images/doctors/bs-dung.jpg"},
                // Hô hấp
                {"bs.thanhtuyenhohap@youmed.com", "Hô hấp", 220000.0, "Thạc sĩ Bác sĩ Hô hấp, chuyên điều trị hen phế quản, COPD, viêm phổi và bệnh đường thở mạn tính.", "/images/doctors/bs-tuyen.jpg"},
                // Tiêu hóa - Gan mật
                {"bs.minhtieuhoa@youmed.com", "Tiêu hóa - Gan mật", 250000.0, "Bác sĩ Chuyên khoa II Tiêu hóa - Gan mật, chuyên nội soi tiêu hóa, điều trị loét dạ dày HP và viêm gan virus.", "/images/doctors/bs-minh-th.jpg"}
        };

        for (Object[] cfg : doctorConfig) {
            String email = (String) cfg[0];
            String specName = (String) cfg[1];
            Double fee = (Double) cfg[2];
            String bio = (String) cfg[3];
            String imgUrl = cfg.length > 4 ? (String) cfg[4] : null;

            User user = userMap.get(email);
            if (user != null) {
                Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
                if (doctor == null) {
                    Specialty specialty = specialtyRepository.findByName(specName).orElse(null);
                    doctor = Doctor.builder()
                            .user(user)
                            .specialty(specialty)
                            .consultationFee(fee)
                            .biography(bio)
                            .imageUrl(imgUrl)
                            .active(true)
                            .build();
                    doctor = doctorRepository.save(doctor);
                } else if (doctor.getImageUrl() == null && imgUrl != null) {
                    doctor.setImageUrl(imgUrl);
                    doctor = doctorRepository.save(doctor);
                }
                doctors.add(doctor);
            }
        }

        return doctors;
    }

    private Map<Long, List<TimeSlot>> seedDoctorSchedulesAndTimeSlots(List<Doctor> doctors) {
        Map<Long, List<TimeSlot>> slotsMap = new HashMap<>();

        LocalDate today = LocalDate.now();
        List<LocalDate> dates = new ArrayList<>();
        // 5 days past, today, and 8 days future
        for (int i = -5; i <= 8; i++) {
            dates.add(today.plusDays(i));
        }

        for (Doctor doctor : doctors) {
            List<TimeSlot> doctorSlots = new ArrayList<>();

            for (LocalDate date : dates) {
                DoctorSchedule schedule = doctorScheduleRepository.findByDoctorIdAndWorkingDate(doctor.getId(), date).orElse(null);
                if (schedule == null) {
                    schedule = DoctorSchedule.builder()
                            .doctor(doctor)
                            .workingDate(date)
                            .startTime(LocalTime.of(8, 0))
                            .endTime(LocalTime.of(17, 0))
                            .status(ScheduleStatus.APPROVED)
                            .active(true)
                            .build();
                    schedule = doctorScheduleRepository.save(schedule);

                    // Morning slots: 08:00, 08:30, 09:00, 09:30, 10:00, 10:30, 11:00
                    for (int h = 8; h <= 10; h++) {
                        doctorSlots.add(createSlot(schedule, date, LocalTime.of(h, 0), LocalTime.of(h, 30)));
                        doctorSlots.add(createSlot(schedule, date, LocalTime.of(h, 30), LocalTime.of(h + 1, 0)));
                    }
                    doctorSlots.add(createSlot(schedule, date, LocalTime.of(11, 0), LocalTime.of(11, 30)));

                    // Afternoon slots: 13:30, 14:00, 14:30, 15:00, 15:30, 16:00
                    doctorSlots.add(createSlot(schedule, date, LocalTime.of(13, 30), LocalTime.of(14, 0)));
                    for (int h = 14; h <= 15; h++) {
                        doctorSlots.add(createSlot(schedule, date, LocalTime.of(h, 0), LocalTime.of(h, 30)));
                        doctorSlots.add(createSlot(schedule, date, LocalTime.of(h, 30), LocalTime.of(h + 1, 0)));
                    }
                    doctorSlots.add(createSlot(schedule, date, LocalTime.of(16, 0), LocalTime.of(16, 30)));
                } else {
                    doctorSlots.addAll(timeSlotRepository.findByDoctorScheduleId(schedule.getId()));
                }
            }
            slotsMap.put(doctor.getId(), doctorSlots);
        }

        return slotsMap;
    }

    private TimeSlot createSlot(DoctorSchedule schedule, LocalDate date, LocalTime start, LocalTime end) {
        TimeSlot slot = TimeSlot.builder()
                .doctorSchedule(schedule)
                .startDateTime(LocalDateTime.of(date, start))
                .endDateTime(LocalDateTime.of(date, end))
                .booked(false)
                .active(true)
                .build();
        return timeSlotRepository.save(slot);
    }

    private List<User> extractPatients(Map<String, User> userMap) {
        List<User> patients = new ArrayList<>();
        for (User u : userMap.values()) {
            if (u.getRole() == Role.PATIENT) {
                patients.add(u);
            }
        }
        return patients;
    }

    private List<Appointment> seedMassiveAppointments(List<User> patients, List<Doctor> doctors, Map<Long, List<TimeSlot>> doctorSlotsMap) {
        List<Appointment> createdAppointments = new ArrayList<>();
        LocalDate today = LocalDate.now();

        String[] sampleReasons = {
                "Đau dạ dày âm ỉ, ợ chua nhiều lần sau bữa ăn",
                "Khám sức khỏe tổng quát định kỳ",
                "Ho khan kéo dài, rát cổ họng kèm sốt nhẹ",
                "Tái khám huyết áp cao, thường xuyên chóng mặt",
                "Nổi mẩn ngứa dị ứng vùng mặt và cánh tay",
                "Đau nhức khớp gối khi vận động lên cầu thang",
                "Bé biếng ăn, sốt cao 39 độ C quấy khóc",
                "Tái khám thai sản định kỳ 24 tuần",
                "Đau nửa đầu Migraine, mất ngủ kéo dài",
                "Mỏi mắt, nhìn mờ khi làm việc máy tính",
                "Viêm mũi dị ứng, nghẹt mũi khó thở về đêm",
                "Đau răng hàm dưới, buốt khi uống nước lạnh",
                "Đau tức ngực trái khi gắng sức",
                "Khám da liễu mụn bọc sưng viêm",
                "Đau lưng vùng thắt lưng, lan xuống hông"
        };

        Random rand = new Random(100);

        // 1. Generate COMPLETED appointments in the past (days -5 to -1) -> Target: ~70 - 80 appointments
        for (int dayOffset = -5; dayOffset <= -1; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            for (Doctor doctor : doctors) {
                List<TimeSlot> slots = findAvailableSlotsForDate(doctorSlotsMap.get(doctor.getId()), date);
                // Take 1 - 2 slots per doctor per day
                int slotsToBook = 1 + rand.nextInt(2);
                for (int s = 0; s < slotsToBook && s < slots.size(); s++) {
                    TimeSlot slot = slots.get(s);
                    User patient = patients.get(rand.nextInt(patients.size()));
                    String reason = sampleReasons[rand.nextInt(sampleReasons.length)];
                    createdAppointments.add(createAppointment(patient, doctor, slot, reason, AppointmentStatus.COMPLETED));
                }
            }
        }

        // 2. Generate TODAY appointments -> Statuses: CHECKED_IN, IN_PROGRESS, COMPLETED
        for (Doctor doctor : doctors) {
            List<TimeSlot> todaySlots = findAvailableSlotsForDate(doctorSlotsMap.get(doctor.getId()), today);
            if (todaySlots.size() >= 3) {
                // 1 CHECKED_IN
                TimeSlot slot1 = todaySlots.get(0);
                User pat1 = patients.get(rand.nextInt(patients.size()));
                createdAppointments.add(createAppointment(pat1, doctor, slot1, sampleReasons[rand.nextInt(sampleReasons.length)], AppointmentStatus.CHECKED_IN));

                // 1 IN_PROGRESS for every second doctor
                if (doctor.getId() % 2 == 0 && todaySlots.size() >= 2) {
                    TimeSlot slot2 = todaySlots.get(1);
                    User pat2 = patients.get(rand.nextInt(patients.size()));
                    createdAppointments.add(createAppointment(pat2, doctor, slot2, sampleReasons[rand.nextInt(sampleReasons.length)], AppointmentStatus.IN_PROGRESS));
                }

                // 1 COMPLETED (examined earlier today)
                if (todaySlots.size() >= 3) {
                    TimeSlot slot3 = todaySlots.get(2);
                    User pat3 = patients.get(rand.nextInt(patients.size()));
                    createdAppointments.add(createAppointment(pat3, doctor, slot3, sampleReasons[rand.nextInt(sampleReasons.length)], AppointmentStatus.COMPLETED));
                }
            }
        }

        // 3. Generate TOMORROW & FUTURE appointments -> Statuses: CONFIRMED, PENDING, CANCELLED
        for (int dayOffset = 1; dayOffset <= 4; dayOffset++) {
            LocalDate date = today.plusDays(dayOffset);
            for (Doctor doctor : doctors) {
                List<TimeSlot> futureSlots = findAvailableSlotsForDate(doctorSlotsMap.get(doctor.getId()), date);
                if (futureSlots.size() >= 2) {
                    // 1 CONFIRMED (approved, waiting for tomorrow check-in)
                    TimeSlot slot1 = futureSlots.get(0);
                    User pat1 = patients.get(rand.nextInt(patients.size()));
                    createdAppointments.add(createAppointment(pat1, doctor, slot1, sampleReasons[rand.nextInt(sampleReasons.length)], AppointmentStatus.CONFIRMED));

                    // 1 PENDING (needs receptionist approval)
                    TimeSlot slot2 = futureSlots.get(1);
                    User pat2 = patients.get(rand.nextInt(patients.size()));
                    createdAppointments.add(createAppointment(pat2, doctor, slot2, sampleReasons[rand.nextInt(sampleReasons.length)], AppointmentStatus.PENDING));

                    // Occasional CANCELLED
                    if (rand.nextInt(5) == 0 && futureSlots.size() >= 3) {
                        TimeSlot slot3 = futureSlots.get(2);
                        User pat3 = patients.get(rand.nextInt(patients.size()));
                        createdAppointments.add(createAppointment(pat3, doctor, slot3, "Bệnh nhân có việc bận xin hủy lịch", AppointmentStatus.CANCELLED));
                    }
                }
            }
        }

        return createdAppointments;
    }

    private List<TimeSlot> findAvailableSlotsForDate(List<TimeSlot> slots, LocalDate date) {
        List<TimeSlot> result = new ArrayList<>();
        if (slots == null) return result;
        for (TimeSlot slot : slots) {
            if (slot.getStartDateTime().toLocalDate().isEqual(date) && Boolean.FALSE.equals(slot.getBooked())) {
                result.add(slot);
            }
        }
        return result;
    }

    private Appointment createAppointment(User patient, Doctor doctor, TimeSlot slot, String reason, AppointmentStatus status) {
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .timeSlot(slot)
                .appointmentTime(slot.getStartDateTime())
                .reason(reason)
                .status(status)
                .build();
        appointment = appointmentRepository.save(appointment);

        if (status != AppointmentStatus.CANCELLED) {
            slot.setBooked(true);
            slot.setAppointment(appointment);
            timeSlotRepository.save(slot);
        }

        return appointment;
    }

    private void seedMedicalRecordsAndPrescriptions(List<Appointment> appointments) {
        Map<String, String[]> medicalInfoMap = new HashMap<>();
        medicalInfoMap.put("Nội khoa", new String[]{
                "Viêm loét dạ dày tá tràng, trào ngược GERD độ A",
                "Đau tức vùng thượng vị, ợ chua nhiều sau ăn, đầy bụng khó tiêu",
                "Kiêng đồ chua cay, tránh bia rượu và cà phê, ngủ kê cao đầu 15 độ, tái khám sau 2 tuần."
        });
        medicalInfoMap.put("Nhi khoa", new String[]{
                "Viêm phế quản cấp, sốt siêu vi",
                "Sốt cao 38.8 độ C, ho có đờm trắng đục, khò khè nhẹ về đêm",
                "Uống nhiều nước ấm, chườm ấm trán nách bẹn khi sốt > 38.5, nếu thở nhanh co lõm ngực cần tái khám ngay."
        });
        medicalInfoMap.put("Sản phụ khoa", new String[]{
                "Theo dõi thai 24 tuần bình thường, thiếu máu nhẹ thai kỳ",
                "Mệt mỏi nhẹ, không đau bụng, không ra huyết",
                "Bổ sung viên sắt và acid folic mỗi ngày sau ăn, ăn nhiều rau xanh đậm, tái khám theo lịch hẹn 4 tuần."
        });
        medicalInfoMap.put("Da liễu", new String[]{
                "Viêm da tiếp xúc dị ứng, mụn trứng cá viêm mức độ vừa",
                "Ngứa rát nhiều, nổi sẩn đỏ vùng hai má và cánh tay",
                "Tránh nắng gắt, không cào gãi làm xước da, tạm ngưng mỹ phẩm có cồn, bôi thuốc mỡ đúng theo chỉ dẫn."
        });
        medicalInfoMap.put("Mắt", new String[]{
                "Viêm kết mạc cấp (đau mắt đỏ), tật khúc xạ cận thị",
                "Cộm xốn mắt, chảy nước mắt nhiều, đỏ củng mạc hai mắt",
                "Đeo kính râm bảo vệ mắt khi ra đường, rửa mắt bằng nước muối sinh lý 3 lần/ngày, không dụi mắt."
        });
        medicalInfoMap.put("Tai Mũi Họng", new String[]{
                "Viêm xoang hàm cấp tính, viêm họng hạt mạn",
                "Chảy mũi vàng xanh, nghẹt mũi nghẹt hai bên, đau tức vùng xoang trán",
                "Xịt rửa mũi bằng nước muối biển sâu hàng ngày, giữ ấm cổ họng khi ngủ máy lạnh."
        });
        medicalInfoMap.put("Răng Hàm Mặt", new String[]{
                "Viêm nướu răng quanh cuống răng 36, sâu men răng",
                "Đau nhức ê buốt khi ăn đồ nóng lạnh, chảy máu chân răng khi đánh răng",
                "Vệ sinh răng miệng bằng chỉ nha khoa, súc miệng nước sát khuẩn sau ăn, hẹn trám răng sau 1 tuần."
        });
        medicalInfoMap.put("Tim mạch", new String[]{
                "Tăng huyết áp nguyên phát độ 2, rối loạn lipid máu",
                "Huyết áp đo tại phòng khám 150/95 mmHg, đau đầu vùng gáy, hồi hộp",
                "Chế độ ăn nhạt (dưới 5g muối/ngày), tập thể dục nhẹ nhàng 30p mỗi ngày, uống thuốc hạ áp đều đặn."
        });
        medicalInfoMap.put("Thần kinh", new String[]{
                "Đau đầu căng thẳng (Tension headache), suy nhược thần kinh",
                "Đau âm ỉ hai bên thái dương, khó vào giấc ngủ, ngủ chập chờn",
                "Nghỉ ngơi điều độ, tránh thức khuya quá 23h, hạn chế sử dụng thiết bị điện tử trước khi ngủ."
        });
        medicalInfoMap.put("Cơ xương khớp", new String[]{
                "Thoái hóa khớp gối hai bên độ 2, đau dây chằng",
                "Đau cứng khớp gối buổi sáng khi thức dậy, phát tiếng lạo xạo khi co duỗi",
                "Tránh mang vác vật nặng, hạn chế leo cầu thang, tập bơi hoặc đạp xe tại chỗ để tăng cường cơ đùi."
        });

        for (Appointment app : appointments) {
            if (app.getStatus() == AppointmentStatus.COMPLETED && !medicalRecordRepository.existsByAppointmentId(app.getId())) {
                String specName = (app.getDoctor().getSpecialty() != null) ? app.getDoctor().getSpecialty().getName() : "Nội khoa";
                String[] info = medicalInfoMap.getOrDefault(specName, medicalInfoMap.get("Nội khoa"));

                LocalDate followUpDate = (app.getId() % 5 != 0) ? app.getAppointmentTime().toLocalDate().plusDays((app.getId() % 3 == 0) ? 7 : ((app.getId() % 3 == 1) ? 14 : 30)) : null;

                MedicalRecord record = MedicalRecord.builder()
                        .appointment(app)
                        .diagnosis(info[0])
                        .symptoms(info[1])
                        .notes(info[2])
                        .followUpDate(followUpDate)
                        .build();
                record = medicalRecordRepository.save(record);

                // Create Prescription with 2-3 items
                Prescription prescription = Prescription.builder()
                        .medicalRecord(record)
                        .prescribedDate(app.getAppointmentTime().toLocalDate())
                        .note("Uống thuốc đầy đủ liều, đúng giờ, không tự ý ngừng thuốc khi chưa hết liệu trình.")
                        .items(new ArrayList<>())
                        .build();

                addPrescriptionItemsForSpecialty(prescription, specName);
                prescriptionRepository.save(prescription);
            }
        }
    }

    private void addPrescriptionItemsForSpecialty(Prescription prescription, String specName) {
        if (specName.contains("Nhi") || specName.contains("Họng")) {
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Augmentin 1g")
                    .dosage("1g (1 viên)")
                    .frequency("2 lần/ngày")
                    .duration("7 ngày")
                    .instruction("Uống ngay trước bữa ăn sáng và tối")
                    .build());
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Paracetamol 500mg")
                    .dosage("500mg (1 viên)")
                    .frequency("3 lần/ngày")
                    .duration("3 ngày")
                    .instruction("Uống khi sốt > 38.5 độ C, cách nhau tối thiểu 4-6 tiếng")
                    .build());
        } else if (specName.contains("Tim")) {
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Amlodipine 5mg")
                    .dosage("5mg (1 viên)")
                    .frequency("1 lần/ngày")
                    .duration("30 ngày")
                    .instruction("Uống 1 viên vào mỗi buổi sáng sau khi thức dậy")
                    .build());
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Losartan 50mg")
                    .dosage("50mg (1 viên)")
                    .frequency("1 lần/ngày")
                    .duration("30 ngày")
                    .instruction("Uống vào buổi chiều sau ăn")
                    .build());
        } else if (specName.contains("Nội") || specName.contains("Tiêu hóa")) {
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Omeprazole 20mg")
                    .dosage("20mg (1 viên)")
                    .frequency("1 lần/ngày")
                    .duration("14 ngày")
                    .instruction("Uống trước bữa ăn sáng 30 phút")
                    .build());
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Phosphalugel (Chữ P)")
                    .dosage("1 gói")
                    .frequency("3 lần/ngày")
                    .duration("7 ngày")
                    .instruction("Uống khi có cơn đau dạ dày hoặc sau ăn 1-2 tiếng")
                    .build());
        } else {
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Vitamin C 1000mg")
                    .dosage("1000mg (1 viên sủi)")
                    .frequency("1 lần/ngày")
                    .duration("10 ngày")
                    .instruction("Hòa tan với 200ml nước, uống sau bữa ăn sáng")
                    .build());
            prescription.addItem(PrescriptionItem.builder()
                    .medicineName("Paracetamol 500mg")
                    .dosage("500mg (1 viên)")
                    .frequency("2 lần/ngày")
                    .duration("5 ngày")
                    .instruction("Uống sau khi ăn khi thấy đau hoặc mệt")
                    .build());
        }
    }

    private void seedInvoices(List<Appointment> appointments) {
        for (Appointment app : appointments) {
            if (invoiceRepository.findByAppointmentId(app.getId()).isEmpty()) {
                Double fee = (app.getDoctor().getConsultationFee() != null) ? app.getDoctor().getConsultationFee() : 200000.0;
                InvoiceStatus invStatus = InvoiceStatus.PENDING;
                String method = null;
                LocalDateTime paidAt = null;

                if (app.getStatus() == AppointmentStatus.COMPLETED) {
                    invStatus = InvoiceStatus.PAID;
                    method = (app.getId() % 2 == 0) ? "VNPAY" : "TIỀN MẶT";
                    paidAt = app.getAppointmentTime().minusMinutes(15);
                } else if (app.getStatus() == AppointmentStatus.CHECKED_IN || app.getStatus() == AppointmentStatus.IN_PROGRESS) {
                    invStatus = InvoiceStatus.PAID;
                    method = "TIỀN MẶT";
                    paidAt = app.getAppointmentTime().minusMinutes(10);
                }

                Invoice invoice = Invoice.builder()
                        .patient(app.getPatient())
                        .appointment(app)
                        .amount(BigDecimal.valueOf(fee))
                        .type(InvoiceType.CONSULTATION)
                        .status(invStatus)
                        .description("Phí khám bệnh chuyên khoa " + (app.getDoctor().getSpecialty() != null ? app.getDoctor().getSpecialty().getName() : ""))
                        .paymentMethod(method)
                        .paymentDate(paidAt)
                        .build();
                invoiceRepository.save(invoice);
            }
        }

        // Align timestamps realistically across days
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("UPDATE appointments SET created_at = (appointment_time - INTERVAL '1 day' + INTERVAL '2 hours') WHERE created_at > appointment_time;");
            stmt.execute("UPDATE invoices inv SET created_at = app.created_at FROM appointments app WHERE inv.appointment_id = app.id;");
            stmt.execute("UPDATE invoices inv SET payment_date = (app.appointment_time - INTERVAL '15 minutes') FROM appointments app WHERE inv.appointment_id = app.id AND inv.status = 'PAID';");
            stmt.execute("UPDATE medical_records mr SET created_at = (app.appointment_time + INTERVAL '25 minutes') FROM appointments app WHERE mr.appointment_id = app.id;");
            stmt.execute("UPDATE prescriptions p SET created_at = mr.created_at FROM medical_records mr WHERE p.medical_record_id = mr.id;");
        } catch (Exception e) {
            log.warn("Could not align entity timestamps: {}", e.getMessage());
        }
    }

    private void ensureDoctorReviews() {
        if (doctorReviewRepository == null || doctorReviewRepository.count() >= 25) {
            return;
        }
        log.info("Checking and seeding realistic doctor reviews for completed appointments...");
        List<Appointment> completedAppointments = appointmentRepository.findByStatus(
                AppointmentStatus.COMPLETED, org.springframework.data.domain.Pageable.unpaged()
        ).getContent();

        String[] comments = {
                "Bác sĩ khám rất tận tâm, tư vấn cặn kẽ và giải thích rõ ràng.",
                "Bác sĩ chuyên môn cao, thái độ nhẹ nhàng và chu đáo.",
                "Phòng khám sạch đẹp, bác sĩ nhiệt tình dặn dò kỹ lưỡng.",
                "Điều trị rất hiệu quả, tôi cảm thấy đỡ nhiều sau khi dùng thuốc.",
                "Rất hài lòng về thái độ phục vụ và chuyên môn của bác sĩ YouMed.",
                "Bác sĩ tận tình, giải thích cặn kẽ từng loại thuốc và cách sinh hoạt.",
                "Thủ tục nhanh gọn, bác sĩ giỏi và ân cần."
        };

        for (Appointment app : completedAppointments) {
            if (!doctorReviewRepository.existsByAppointmentId(app.getId())) {
                int rating = (app.getId() % 7 == 0 || app.getId() % 11 == 0) ? 4 : 5;
                String comment = comments[(int) (app.getId() % comments.length)];

                DoctorReview review = DoctorReview.builder()
                        .appointment(app)
                        .doctor(app.getDoctor())
                        .patient(app.getPatient())
                        .rating(rating)
                        .comment(comment)
                        .build();
                doctorReviewRepository.save(review);
            }
        }
        log.info("Completed seeding doctor reviews. Total reviews now in DB: {}", doctorReviewRepository.count());
    }
}
