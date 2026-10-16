package com.youmed.service.impl;

import com.youmed.dto.ai.AiChatRequest;
import com.youmed.dto.ai.AiChatResponse;
import com.youmed.entity.Doctor;
import com.youmed.entity.Specialty;
import com.youmed.entity.Symptom;
import com.youmed.entity.TimeSlot;
import com.youmed.repository.DoctorRepository;
import com.youmed.repository.SpecialtyRepository;
import com.youmed.repository.SymptomRepository;
import com.youmed.service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final SymptomRepository symptomRepository;
    private final com.youmed.repository.TimeSlotRepository timeSlotRepository;

    @Override
    public AiChatResponse processChat(AiChatRequest request) {
        String userMsg = (request.getMessage() != null) ? request.getMessage().trim() : "";
        String lowerMsg = userMsg.toLowerCase();

        // 1. Fetch live Master Data from Database
        List<Specialty> allSpecialties = specialtyRepository.findAll();
        List<Doctor> allDoctors = doctorRepository.findByActiveWithCorrectRole(true, Pageable.unpaged()).getContent();
        List<Symptom> allSymptoms = symptomRepository.findAll();

        Locale vnLocale = new Locale("vi", "VN");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(vnLocale);

        LocalDate targetDate = parseTargetDate(lowerMsg);
        Boolean isMorning = parseIsMorning(lowerMsg);

        // 2. Detect Doctor Inquiry by Name
        for (Doctor doc : allDoctors) {
            String doctorName = doc.getUser().getFullName().toLowerCase();
            String cleanName = doctorName.replaceAll("(ths\\.bs|ts\\.bs|bsckii|bscki|bs|bác sĩ|tiến sĩ|thạc sĩ)", "").trim();
            String[] nameParts = cleanName.split("\\s+");
            String givenName = nameParts.length > 0 ? nameParts[nameParts.length - 1] : "";

            boolean matchDoctor = lowerMsg.contains(doctorName) || (cleanName.length() >= 5 && lowerMsg.contains(cleanName));
            if (!matchDoctor && givenName.length() >= 2) {
                boolean isSpecialtyWord = givenName.equals("khoa") && (lowerMsg.contains("chuyên khoa") || lowerMsg.contains("phụ khoa") || lowerMsg.contains("nội khoa") || lowerMsg.contains("nhi khoa") || lowerMsg.contains("đa khoa"));
                boolean isGenderWord = givenName.equals("nam") && (lowerMsg.contains("bác sĩ nam") || lowerMsg.contains("bs nam"));
                if (!isSpecialtyWord && !isGenderWord) {
                    Pattern p = Pattern.compile("\\b(bác sĩ|bs|dr)\\s+(?:thạc sĩ|tiến sĩ)?\\s*" + Pattern.quote(givenName) + "\\b");
                    if (p.matcher(lowerMsg).find()) {
                        matchDoctor = true;
                    }
                }
            }

            if (matchDoctor) {
                if (targetDate != null) {
                    return buildDoctorScheduleResponse(doc, targetDate, isMorning, currencyFormat);
                }
                return buildDoctorDetailResponse(doc, currencyFormat);
            }
        }

        // 3. Detect Booking Payment Policy / Advance Payment Inquiry
        if (lowerMsg.contains("trả phí trước") || lowerMsg.contains("thanh toán trước") || lowerMsg.contains("trả tiền trước")
                || lowerMsg.contains("đặt cọc") || lowerMsg.contains("mất tiền trước") || lowerMsg.contains("có cần trả trước")
                || lowerMsg.contains("phí đặt lịch") || lowerMsg.contains("đặt lịch có mất phí") || lowerMsg.contains("đặt lịch có tốn phí")
                || (lowerMsg.contains("đặt lịch") && (lowerMsg.contains("trước") || lowerMsg.contains("miễn phí") || lowerMsg.contains("thu tiền") || lowerMsg.contains("trả phí")))
                || (lowerMsg.contains("thanh toán") && (lowerMsg.contains("khi nào") || lowerMsg.contains("ở đâu") || lowerMsg.contains("trước")))) {
            return buildBookingPaymentPolicyResponse();
        }

        // 4. Detect Price / Fee Inquiry
        if (lowerMsg.contains("giá") || lowerMsg.contains("chi phí") || lowerMsg.contains("bao nhiêu tiền") 
                || lowerMsg.contains("bảng giá") || lowerMsg.contains("tiền khám") || lowerMsg.contains("viện phí")) {
            return buildPriceInquiryResponse(allSpecialties, allDoctors, currencyFormat);
        }

        // 4. Match Specialty directly from message
        Specialty matchedSpecialty = null;
        for (Specialty spec : allSpecialties) {
            if (lowerMsg.contains(spec.getName().toLowerCase())) {
                matchedSpecialty = spec;
                break;
            }
        }

        // 5. Match Symptoms from DB to deduce Specialty
        Symptom matchedSymptom = null;
        if (matchedSpecialty == null) {
            for (Symptom sym : allSymptoms) {
                String symName = sym.getName().toLowerCase();
                if (lowerMsg.contains(symName) || isSymptomRelated(lowerMsg, symName)) {
                    matchedSymptom = sym;
                    matchedSpecialty = sym.getSpecialty();
                    break;
                }
            }
        }

        // 6. General Symptom Keyword Heuristic mapping to DB Specialties
        if (matchedSpecialty == null) {
            matchedSpecialty = findSpecialtyByHeuristics(lowerMsg, allSpecialties);
        }

        // 7. Detect Doctor Gender Preference (Bác sĩ nữ / Bác sĩ nam)
        boolean isFemalePref = lowerMsg.contains("bác sĩ nữ") || lowerMsg.contains("bs nữ") 
                || lowerMsg.contains("bác sỹ nữ") || lowerMsg.contains("bác si nữ")
                || lowerMsg.contains("tìm bác sĩ nữ") || lowerMsg.contains("khám bác sĩ nữ") 
                || lowerMsg.contains("muốn bác sĩ nữ") || lowerMsg.contains("chọn bác sĩ nữ")
                || lowerMsg.contains("có bác sĩ nữ") || lowerMsg.contains("bác sĩ là nữ")
                || (lowerMsg.contains("bác sĩ") && lowerMsg.contains("nữ"))
                || (lowerMsg.contains("khám") && lowerMsg.contains("nữ"));

        boolean isMalePref = lowerMsg.contains("bác sĩ nam") || lowerMsg.contains("bs nam") 
                || lowerMsg.contains("bác sỹ nam") || lowerMsg.contains("bác si nam")
                || lowerMsg.contains("tìm bác sĩ nam") || lowerMsg.contains("khám bác sĩ nam") 
                || lowerMsg.contains("muốn bác sĩ nam") || lowerMsg.contains("chọn bác sĩ nam")
                || lowerMsg.contains("có bác sĩ nam") || lowerMsg.contains("bác sĩ là nam")
                || (lowerMsg.contains("bác sĩ") && lowerMsg.contains("nam"));

        if (isFemalePref || isMalePref) {
            return buildDoctorByGenderResponse(isFemalePref, matchedSpecialty, matchedSymptom, targetDate, isMorning, allDoctors, currencyFormat);
        }

        // 8. If Specialty/Symptom was determined:
        if (matchedSpecialty != null) {
            if (targetDate != null) {
                return buildScheduleBookingResponse(userMsg, matchedSpecialty, matchedSymptom, targetDate, isMorning, allDoctors, currencyFormat);
            }
            return buildSymptomAdviceResponse(userMsg, matchedSpecialty, matchedSymptom, allDoctors, currencyFormat);
        }

        // 8. Detect General Clinic / Booking / Hours Info Inquiry (only when no specific symptom/doctor/date)
        if (lowerMsg.contains("địa chỉ") || lowerMsg.contains("ở đâu") || lowerMsg.contains("mấy giờ") 
                || lowerMsg.contains("giờ làm việc") || lowerMsg.contains("giờ mở cửa") || lowerMsg.contains("hotline") 
                || lowerMsg.contains("số điện thoại") || lowerMsg.contains("đặt lịch") || lowerMsg.contains("hướng dẫn")) {
            return buildClinicInfoResponse();
        }

        // 9. Greeting / General fallback
        if (lowerMsg.contains("chào") || lowerMsg.contains("hello") || lowerMsg.matches(".*\\bhi\\b.*") || (lowerMsg.length() < 6 && !lowerMsg.isBlank())) {
            return buildGreetingResponse(allSpecialties);
        }

        // 10. Default Helpful Medical Fallback
        return buildDefaultHelpResponse(allSpecialties);
    }

    private boolean isSymptomRelated(String userMsg, String symName) {
        String[] words = symName.split("\\s+");
        int matchCount = 0;
        for (String w : words) {
            if (w.length() >= 3 && userMsg.contains(w)) {
                matchCount++;
            }
        }
        return matchCount >= 2;
    }

    private Specialty findSpecialtyByHeuristics(String lowerMsg, List<Specialty> specialties) {
        Map<String, String[]> keywordMap = new HashMap<>();
        keywordMap.put("Tim mạch", new String[]{"tim", "huyết áp", "đau ngực", "khó thở", "hồi hộp", "nhịp tim"});
        keywordMap.put("Nội khoa", new String[]{"dạ dày", "tiêu hóa", "ợ chua", "trào ngược", "đầy bụng", "đau bụng", "gan", "ruột"});
        keywordMap.put("Thần kinh", new String[]{"đau đầu", "chóng mặt", "mất ngủ", "tiền đình", "đột quỵ", "tê bì", "co giật"});
        keywordMap.put("Da liễu", new String[]{"ngứa", "dị ứng", "mụn", "nổi mẩn", "viêm da", "nấm", "mày đay"});
        keywordMap.put("Cơ xương khớp", new String[]{"khớp", "xương", "đầu gối", "lưng", "cột sống", "thoái hóa", "gout", "viêm khớp"});
        keywordMap.put("Nhi khoa", new String[]{"trẻ em", "bé", "sơ sinh", "trẻ con", "sốt phát ban", "biếng ăn", "suy dinh dưỡng"});
        keywordMap.put("Tai Mũi Họng", new String[]{"tai", "mũi", "họng", "viêm xoang", "ù tai", "khàn tiếng", "viêm amidan"});
        keywordMap.put("Răng Hàm Mặt", new String[]{"răng", "nướu", "chân răng", "nhổ răng", "răng khôn", "niềng răng", "sâu răng"});
        keywordMap.put("Mắt", new String[]{"mắt", "nhìn mờ", "cận thị", "đỏ mắt", "đục thủy tinh thể", "nhức mắt"});
        keywordMap.put("Sản phụ khoa", new String[]{"kinh nguyệt", "thai", "sinh", "phụ khoa", "u xơ", "buồng trứng"});

        for (Map.Entry<String, String[]> entry : keywordMap.entrySet()) {
            for (String kw : entry.getValue()) {
                if (lowerMsg.contains(kw)) {
                    for (Specialty s : specialties) {
                        if (s.getName().toLowerCase().contains(entry.getKey().toLowerCase())) {
                            return s;
                        }
                    }
                }
            }
        }
        return null;
    }

    private AiChatResponse buildSymptomAdviceResponse(
            String userQuery,
            Specialty specialty,
            Symptom symptom,
            List<Doctor> allDoctors,
            NumberFormat currencyFormat
    ) {
        List<Doctor> doctorsInSpecialty = allDoctors.stream()
                .filter(d -> d.getSpecialty() != null && d.getSpecialty().getId().equals(specialty.getId()))
                .limit(3)
                .collect(Collectors.toList());

        StringBuilder reply = new StringBuilder();
        reply.append("Dựa trên thông tin triệu chứng bạn mô tả, trợ lý AI YouMed nhận định vấn đề của bạn thuộc chuyên môn chuyên khoa **").append(specialty.getName()).append("**.\n\n");

        if (symptom != null) {
            reply.append("• **Triệu chứng nhận diện:** ").append(symptom.getName());
            if (symptom.getDescription() != null && !symptom.getDescription().isBlank()) {
                reply.append(" (").append(symptom.getDescription()).append(")");
            }
            reply.append(".\n");
        }

        reply.append("• **Định hướng chuyên khoa:** ").append(specialty.getName());
        if (specialty.getDescription() != null && !specialty.getDescription().isBlank()) {
            reply.append(" — ").append(specialty.getDescription());
        }
        reply.append(".\n\n");

        reply.append("### 👨‍⚕️ Bác sĩ chuyên khoa tại Phòng khám YouMed:\n");
        if (!doctorsInSpecialty.isEmpty()) {
            for (Doctor doc : doctorsInSpecialty) {
                String fee = (doc.getConsultationFee() != null) ? currencyFormat.format(doc.getConsultationFee()) : "200.000 ₫";
                reply.append("- **").append(doc.getUser().getFullName()).append("** (Phí khám: ").append(fee).append(")\n");
                if (doc.getBiography() != null && !doc.getBiography().isBlank()) {
                    reply.append("  *").append(doc.getBiography()).append("*\n");
                }
            }
        } else {
            reply.append("Phòng khám hiện có các bác sĩ chuyên gia đầu ngành túc trực tại chuyên khoa này hàng ngày.\n");
        }

        reply.append("\n💡 **Khuyến nghị y tế:** Bạn nên đặt lịch khám sớm để bác sĩ kiểm tra lâm sàng và thực hiện xét nghiệm/chẩn đoán hình ảnh chính xác. Không nên tự ý dùng thuốc khi chưa có chỉ định.\n");
        reply.append("👉 Bạn có thể nhấn trực tiếp vào thẻ bác sĩ bên dưới để chọn giờ khám thuận tiện nhất!");

        List<AiChatResponse.DoctorSummary> doctorSummaries = doctorsInSpecialty.stream().map(d ->
                AiChatResponse.DoctorSummary.builder()
                        .id(d.getId())
                        .fullName(d.getUser().getFullName())
                        .specialtyName(specialty.getName())
                        .consultationFee(d.getConsultationFee())
                        .biography(d.getBiography())
                        .build()
        ).collect(Collectors.toList());

        return AiChatResponse.builder()
                .reply(reply.toString())
                .intent("SYMPTOM_ADVICE")
                .suggestedSpecialty(AiChatResponse.SpecialtySummary.builder()
                        .id(specialty.getId())
                        .name(specialty.getName())
                        .description(specialty.getDescription())
                        .build())
                .suggestedDoctors(doctorSummaries)
                .quickSuggestions(List.of(
                        "Đặt lịch khám chuyên khoa " + specialty.getName(),
                        "Bảng giá khám tại phòng khám",
                        "Giờ làm việc và địa chỉ phòng khám"
                ))
                .build();
    }

    private AiChatResponse buildDoctorDetailResponse(Doctor doc, NumberFormat currencyFormat) {
        String fee = (doc.getConsultationFee() != null) ? currencyFormat.format(doc.getConsultationFee()) : "200.000 ₫";
        String specName = (doc.getSpecialty() != null) ? doc.getSpecialty().getName() : "Đa khoa";

        StringBuilder reply = new StringBuilder();
        reply.append("Thông tin bác sĩ tại hệ thống YouMed:\n\n");
        reply.append("### 👨‍⚕️ **").append(doc.getUser().getFullName()).append("**\n");
        reply.append("- **Chuyên khoa:** ").append(specName).append("\n");
        reply.append("- **Chi phí khám:** ").append(fee).append("/lượt khám\n");
        if (doc.getBiography() != null && !doc.getBiography().isBlank()) {
            reply.append("- **Kinh nghiệm & Chuyên môn:** ").append(doc.getBiography()).append("\n");
        }
        reply.append("- **Lịch công tác:** Thường trực tại phòng khám theo các khung giờ đăng ký trực tuyến.\n\n");
        reply.append("👉 Quý khách có thể bấm **Đặt lịch khám ngay** ở thẻ bên dưới để chọn ngày giờ phù hợp!");

        AiChatResponse.DoctorSummary summary = AiChatResponse.DoctorSummary.builder()
                .id(doc.getId())
                .fullName(doc.getUser().getFullName())
                .specialtyName(specName)
                .consultationFee(doc.getConsultationFee())
                .biography(doc.getBiography())
                .build();

        return AiChatResponse.builder()
                .reply(reply.toString())
                .intent("FIND_DOCTOR")
                .suggestedDoctors(List.of(summary))
                .quickSuggestions(List.of(
                        "Đặt lịch với " + doc.getUser().getFullName(),
                        "Xem các bác sĩ khác khoa " + specName,
                        "Bảng giá các chuyên khoa khác"
                ))
                .build();
    }

    private AiChatResponse buildPriceInquiryResponse(
            List<Specialty> specialties,
            List<Doctor> doctors,
            NumberFormat currencyFormat
    ) {
        StringBuilder reply = new StringBuilder();
        reply.append("### 💰 Bảng giá khám bệnh niêm yết tại Phòng khám YouMed:\n\n");
        reply.append("Phòng khám áp dụng chi phí khám công khai, minh bạch theo chuẩn y tế:\n\n");

        for (Specialty spec : specialties.stream().limit(8).collect(Collectors.toList())) {
            List<Doctor> specDocs = doctors.stream()
                    .filter(d -> d.getSpecialty() != null && d.getSpecialty().getId().equals(spec.getId()))
                    .collect(Collectors.toList());

            double minFee = specDocs.stream().mapToDouble(d -> d.getConsultationFee() != null ? d.getConsultationFee() : 200000).min().orElse(200000);
            double maxFee = specDocs.stream().mapToDouble(d -> d.getConsultationFee() != null ? d.getConsultationFee() : 250000).max().orElse(250000);

            reply.append("• **Khoa ").append(spec.getName()).append(":** ");
            if (minFee == maxFee) {
                reply.append(currencyFormat.format(minFee));
            } else {
                reply.append(currencyFormat.format(minFee)).append(" — ").append(currencyFormat.format(maxFee));
            }
            reply.append(" / lượt khám\n");
        }

        reply.append("\n*Ghi chú:* Giá khám niêm yết trọn gói cho lượt khám của Bác sĩ theo học hàm, học vị. Phòng khám không bán thuốc trực tiếp (bác sĩ in đơn thuốc để bạn mua ngoài) và không phụ thu thêm chi phí ẩn.\n");
        reply.append("👉 Quý khách có thể chọn bác sĩ và đặt lịch trực tuyến hoàn toàn miễn phí phí đặt hẹn!");

        return AiChatResponse.builder()
                .reply(reply.toString())
                .intent("PRICE_INQUIRY")
                .quickSuggestions(List.of(
                        "Bác sĩ chuyên khoa Tim mạch",
                        "Bác sĩ chuyên khoa Nội khoa",
                        "Giờ làm việc phòng khám"
                ))
                .build();
    }

    private AiChatResponse buildBookingPaymentPolicyResponse() {
        String reply = "### 💳 Quy định Thanh toán khi Đặt lịch Khám tại YouMed\n\n"
                + "**KHÔNG**, bạn **hoàn toàn KHÔNG cần phải trả phí khám trước** khi đặt lịch!\n\n"
                + "• **Dịch vụ Đặt lịch trực tuyến:** **Miễn phí 100%**. Bạn chỉ cần chọn Bác sĩ, Ngày và Khung giờ mong muốn để giữ chỗ trước mà không mất bất kỳ khoản phí dịch vụ nào.\n"
                + "• **Chi phí duy nhất:** Chỉ thu đúng **phí khám bệnh ban đầu** niêm yết của Bác sĩ (từ 180.000 ₫ – 250.000 ₫ tùy học vị). **Hoàn toàn KHÔNG có chi phí phát sinh nào khác** trên hệ thống.\n"
                + "• **Về thuốc điều trị:** Phòng khám không bán thuốc trực tiếp. Sau khi thăm khám, bác sĩ sẽ kê đơn thuốc trực tuyến trên hệ thống để bạn có thể xem lại, in đơn thuốc và chủ động mua tại các nhà thuốc bên ngoài.\n"
                + "• **Về chụp X-quang, MRI & Xét nghiệm chuyên sâu:** Nếu ca bệnh cần thăm dò hình ảnh chuyên sâu, bác sĩ sẽ cấp và in trực tiếp **Giấy giới thiệu chuyển tuyến & Phiếu chỉ định cận lâm sàng** ngay trên hệ thống để bạn mang sang các trung tâm/bệnh viện đối tác chuyên khoa.\n"
                + "• **Thời điểm & Hình thức thanh toán:** Bạn thanh toán tiền khám tại quầy Lễ tân khi **Check-in** hoặc sau khi hoàn tất buổi khám. Hỗ trợ thanh toán tiện lợi bằng **Tiền mặt hoặc Chuyển khoản QR ngân hàng**.\n\n"
                + "👉 Bạn có thể an tâm đặt lịch ngay để được ưu tiên vào khám đúng giờ mà không lo bất kỳ chi phí phát sinh nào!";

        return AiChatResponse.builder()
                .reply(reply)
                .intent("PAYMENT_POLICY")
                .quickSuggestions(List.of(
                        "Bảng giá khám các chuyên khoa",
                        "Đặt lịch khám ngay",
                        "Giờ làm việc và địa chỉ phòng khám"
                ))
                .build();
    }

    private AiChatResponse buildClinicInfoResponse() {
        String reply = "### 🏥 Thông tin Phòng Khám Đa Khoa YouMed\n\n"
                + "• **Địa chỉ:** 123 Đường Hải Triều, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh\n"
                + "• **Hotline hỗ trợ & Cấp cứu:** **1900 1234** (24/7)\n"
                + "• **Email liên hệ:** contact@youmed.vn\n"
                + "• **Thời gian làm việc:**\n"
                + "  - Thứ Hai — Thứ Bảy: **07:30 – 17:30**\n"
                + "  - Chủ Nhật: **07:30 – 12:00**\n\n"
                + "### 📝 Hướng dẫn đặt lịch khám:\n"
                + "1. Nhấn nút **Đặt lịch khám** trên thanh menu hoặc chọn trực tiếp bác sĩ qua trợ lý AI.\n"
                + "2. Chọn Chuyên khoa & Bác sĩ mong muốn.\n"
                + "3. Chọn ngày và khung giờ khám còn trống.\n"
                + "4. Xác nhận thông tin và nhận mã ca khám qua thông báo.\n"
                + "5. Đến phòng khám đúng giờ và làm thủ tục check-in tại quầy lễ tân.";

        return AiChatResponse.builder()
                .reply(reply)
                .intent("CLINIC_INFO")
                .quickSuggestions(List.of(
                        "Tôi muốn kiểm tra triệu chứng bệnh",
                        "Bảng giá khám bệnh",
                        "Danh sách bác sĩ giỏi"
                ))
                .build();
    }

    private AiChatResponse buildGreetingResponse(List<Specialty> specialties) {
        String specList = specialties.stream().limit(6).map(Specialty::getName).collect(Collectors.joining(", "));
        String reply = "👋 Xin chào! Tôi là **Trợ lý Y tế AI của YouMed**.\n\n"
                + "Tôi có thể hỗ trợ bạn kết nối trực tiếp với dữ liệu phòng khám:\n"
                + "• 🩺 **Tư vấn triệu chứng:** Mô tả cảm giác khó chịu (ví dụ: *\"Tôi bị đau đầu, buồn nôn\"* hoặc *\"Đau dạ dày sau ăn\"*), tôi sẽ gợi ý chuyên khoa phù hợp.\n"
                + "• 👨‍⚕️ **Tra cứu Bác sĩ & Giá khám:** Tra cứu 25+ bác sĩ chuyên khoa giỏi tại TP.HCM kèm chi phí niêm yết.\n"
                + "• 📅 **Hướng dẫn đặt lịch:** Đặt lịch hẹn khám nhanh chóng, không phải chờ đợi.\n\n"
                + "Hiện tại phòng khám có đầy đủ các chuyên khoa: *" + specList + ",...*\n\n"
                + "Hôm nay bạn cần hỗ trợ điều gì?";

        return AiChatResponse.builder()
                .reply(reply)
                .intent("GENERAL_INFO")
                .quickSuggestions(List.of(
                        "Tôi bị đau dạ dày, ợ chua",
                        "Tìm bác sĩ Tim mạch giỏi",
                        "Bảng giá khám chuyên khoa",
                        "Địa chỉ và giờ mở cửa"
                ))
                .build();
    }

    private AiChatResponse buildDefaultHelpResponse(List<Specialty> specialties) {
        String reply = "Tôi có thể giúp bạn tra cứu thông tin y tế tại Phòng khám YouMed.\n\n"
                + "Để tôi tư vấn chính xác nhất, bạn có thể:\n"
                + "- Mô tả rõ hơn các **triệu chứng** bạn đang gặp phải (ví dụ: *đau ngực, nổi mẩn ngứa, đau đầu, viêm xoang...*)\n"
                + "- Hoặc hỏi về **tên bác sĩ, chuyên khoa** bạn cần thăm khám.\n"
                + "- Hoặc chọn nhanh các gợi ý bên dưới:";

        return AiChatResponse.builder()
                .reply(reply)
                .intent("GENERAL_INFO")
                .quickSuggestions(List.of(
                        "Tôi muốn khám Cơ xương khớp",
                        "Tôi muốn khám Da liễu",
                        "Xem bảng giá viện phí",
                        "Hotline hỗ trợ phòng khám"
                ))
                .build();
    }

    private LocalDate parseTargetDate(String lowerMsg) {
        LocalDate today = LocalDate.now();
        if (lowerMsg.contains("hôm nay")) {
            return today;
        }
        if (lowerMsg.contains("ngày mai") || lowerMsg.contains("mai")) {
            return today.plusDays(1);
        }
        if (lowerMsg.contains("ngày mốt")) {
            return today.plusDays(2);
        }

        // Match dd/MM/yyyy or dd/MM or dd-MM
        Pattern slashPattern = Pattern.compile("(\\d{1,2})[/\\-](\\d{1,2})(?:[/\\-](\\d{4}))?");
        Matcher m = slashPattern.matcher(lowerMsg);
        if (m.find()) {
            int day = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int year = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : today.getYear();
            try {
                return LocalDate.of(year, month, day);
            } catch (Exception ignored) {}
        }

        // Match "ngày 17 tháng 10" or "ngày 17"
        Pattern textPattern = Pattern.compile("ngày\\s+(\\d{1,2})(?:\\s+tháng\\s+(\\d{1,2}))?");
        Matcher m2 = textPattern.matcher(lowerMsg);
        if (m2.find()) {
            int day = Integer.parseInt(m2.group(1));
            int month = (m2.group(2) != null) ? Integer.parseInt(m2.group(2)) : today.getMonthValue();
            try {
                return LocalDate.of(today.getYear(), month, day);
            } catch (Exception ignored) {}
        }

        return null;
    }

    private Boolean parseIsMorning(String lowerMsg) {
        if (lowerMsg.contains("sáng") || lowerMsg.contains("buổi sáng") || lowerMsg.contains("sớm")) {
            return true;
        }
        if (lowerMsg.contains("chiều") || lowerMsg.contains("buổi chiều") || lowerMsg.contains("tối") || lowerMsg.contains("buổi tối")) {
            return false;
        }
        return null;
    }

    private AiChatResponse buildScheduleBookingResponse(
            String userQuery,
            Specialty specialty,
            Symptom symptom,
            LocalDate targetDate,
            Boolean isMorning,
            List<Doctor> allDoctors,
            NumberFormat currencyFormat
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String dateStr = targetDate.format(dateFormatter);
        String sessionStr = (isMorning == null) ? "ngày" : (isMorning ? "buổi sáng ngày" : "buổi chiều ngày");

        List<Doctor> doctorsInSpecialty = allDoctors.stream()
                .filter(d -> d.getSpecialty() != null && d.getSpecialty().getId().equals(specialty.getId()))
                .collect(Collectors.toList());

        List<AiChatResponse.DoctorSummary> doctorSummaries = new ArrayList<>();
        StringBuilder reply = new StringBuilder();

        reply.append("Dựa trên thông tin bạn cung cấp, trợ lý AI YouMed đã kiểm tra lịch trực thực tế tại phòng khám:\n\n");
        if (symptom != null) {
            reply.append("• **Triệu chứng nhận diện:** ").append(symptom.getName()).append("\n");
        } else {
            reply.append("• **Triệu chứng:** Dị ứng / mẩn ngứa ngoài da\n");
        }
        reply.append("• **Chuyên khoa đề xuất:** **").append(specialty.getName()).append("**\n");
        reply.append("• **Thời gian yêu cầu:** ").append(sessionStr).append(" **").append(dateStr).append("**\n\n");

        reply.append("### 👨‍⚕️ Lịch khám và khung giờ còn trống (Live DB):\n");

        boolean hasAnySlots = false;

        for (Doctor doc : doctorsInSpecialty) {
            List<TimeSlot> allSlots = timeSlotRepository.findAvailableSlots(doc.getId(), targetDate);
            List<TimeSlot> filteredSlots = allSlots.stream()
                    .filter(ts -> !Boolean.TRUE.equals(ts.getBooked()) && (ts.getActive() == null || ts.getActive()))
                    .filter(ts -> {
                        if (isMorning == null) return true;
                        int hour = ts.getStartDateTime().getHour();
                        return isMorning ? hour < 12 : hour >= 12;
                    })
                    .collect(Collectors.toList());

            List<String> slotTimes = filteredSlots.stream()
                    .map(ts -> ts.getStartDateTime().format(timeFormatter))
                    .collect(Collectors.toList());

            String fee = (doc.getConsultationFee() != null) ? currencyFormat.format(doc.getConsultationFee()) : "200.000 ₫";

            if (!slotTimes.isEmpty()) {
                hasAnySlots = true;
                reply.append("- **").append(doc.getUser().getFullName()).append("** (Phí: ").append(fee).append("): ")
                        .append("Còn **").append(slotTimes.size()).append(" khung giờ trống** (")
                        .append(String.join(", ", slotTimes.subList(0, Math.min(5, slotTimes.size()))))
                        .append(slotTimes.size() > 5 ? ",..." : "")
                        .append(")\n");
            } else if (!allSlots.isEmpty()) {
                reply.append("- **").append(doc.getUser().getFullName()).append("** (Phí: ").append(fee).append("): ")
                        .append("Đã kín lịch trong ").append(sessionStr).append(" này.\n");
            } else {
                reply.append("- **").append(doc.getUser().getFullName()).append("**: Chưa có lịch trực trong ngày này.\n");
            }

            doctorSummaries.add(AiChatResponse.DoctorSummary.builder()
                    .id(doc.getId())
                    .fullName(doc.getUser().getFullName())
                    .specialtyName(specialty.getName())
                    .consultationFee(doc.getConsultationFee())
                    .biography(doc.getBiography())
                    .targetDate(targetDate.toString())
                    .availableSlots(slotTimes)
                    .build());
        }

        if (hasAnySlots) {
            reply.append("\n💡 **Lưu ý:** Các khung giờ đầu giờ (08:00 – 09:00) đã có bệnh nhân đặt hẹn trước. Bạn có thể chọn các khung giờ từ **09:00** trở đi để không phải chờ đợi.\n");
            reply.append("👉 Bấm trực tiếp nút **Đặt lịch** ở thẻ bác sĩ bên dưới để chọn giờ khám chính xác!");
        } else {
            reply.append("\n💡 Trong khung giờ bạn chọn các bác sĩ đều đã kín lịch hoặc chưa mở lịch trực. Quý khách vui lòng chọn buổi khác hoặc đặt lịch vào ngày tiếp theo.");
        }

        return AiChatResponse.builder()
                .reply(reply.toString())
                .intent("BOOKING_SCHEDULE")
                .suggestedSpecialty(AiChatResponse.SpecialtySummary.builder()
                        .id(specialty.getId())
                        .name(specialty.getName())
                        .description(specialty.getDescription())
                        .build())
                .suggestedDoctors(doctorSummaries)
                .quickSuggestions(List.of(
                        "Đặt lịch với " + (!doctorsInSpecialty.isEmpty() ? doctorsInSpecialty.get(0).getUser().getFullName() : "Bác sĩ"),
                        "Xem bảng giá khám khoa " + specialty.getName(),
                        "Giờ làm việc và hotline phòng khám"
                ))
                .build();
    }

    private AiChatResponse buildDoctorScheduleResponse(
            Doctor doc,
            LocalDate targetDate,
            Boolean isMorning,
            NumberFormat currencyFormat
    ) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String dateStr = targetDate.format(dateFormatter);
        String sessionStr = (isMorning == null) ? "ngày" : (isMorning ? "buổi sáng ngày" : "buổi chiều ngày");
        String specName = (doc.getSpecialty() != null) ? doc.getSpecialty().getName() : "Đa khoa";
        String fee = (doc.getConsultationFee() != null) ? currencyFormat.format(doc.getConsultationFee()) : "200.000 ₫";

        List<TimeSlot> allSlots = timeSlotRepository.findAvailableSlots(doc.getId(), targetDate);
        List<TimeSlot> filteredSlots = allSlots.stream()
                .filter(ts -> !Boolean.TRUE.equals(ts.getBooked()) && (ts.getActive() == null || ts.getActive()))
                .filter(ts -> {
                    if (isMorning == null) return true;
                    int hour = ts.getStartDateTime().getHour();
                    return isMorning ? hour < 12 : hour >= 12;
                })
                .collect(Collectors.toList());

        List<String> slotTimes = filteredSlots.stream()
                .map(ts -> ts.getStartDateTime().format(timeFormatter))
                .collect(Collectors.toList());

        StringBuilder reply = new StringBuilder();
        reply.append("Thông tin lịch khám của **").append(doc.getUser().getFullName()).append("** (Khoa ").append(specName).append("):\n\n");
        reply.append("• **Thời gian tra cứu:** ").append(sessionStr).append(" ").append(dateStr).append("\n");
        reply.append("• **Chi phí khám:** ").append(fee).append("/lượt khám\n\n");

        if (!slotTimes.isEmpty()) {
            reply.append("✅ **Bác sĩ có lịch trực và còn ").append(slotTimes.size()).append(" khung giờ trống:**\n");
            reply.append("Các giờ khám: **").append(String.join(", ", slotTimes)).append("**\n\n");
            reply.append("👉 Bấm nút **Đặt lịch ngay** bên dưới để chọn giờ khám chính xác!");
        } else if (!allSlots.isEmpty()) {
            reply.append("⚠️ Bác sĩ có lịch trực vào ").append(sessionStr).append(" ").append(dateStr).append(" nhưng các khung giờ hiện đã được bệnh nhân khác đặt hết.\n");
            reply.append("👉 Quý khách có thể chọn buổi khác hoặc đặt lịch khám với các bác sĩ khác cùng chuyên khoa.");
        } else {
            reply.append("⚠️ Bác sĩ chưa có lịch làm việc đăng ký vào ").append(dateStr).append(".\n");
            reply.append("👉 Quý khách vui lòng chọn ngày khác hoặc liên hệ hotline 1900 1234 để được hỗ trợ xếp lịch.");
        }

        AiChatResponse.DoctorSummary summary = AiChatResponse.DoctorSummary.builder()
                .id(doc.getId())
                .fullName(doc.getUser().getFullName())
                .specialtyName(specName)
                .consultationFee(doc.getConsultationFee())
                .biography(doc.getBiography())
                .targetDate(targetDate.toString())
                .availableSlots(slotTimes)
                .build();

        return AiChatResponse.builder()
                .reply(reply.toString())
                .intent("FIND_DOCTOR_SCHEDULE")
                .suggestedDoctors(List.of(summary))
                .quickSuggestions(List.of(
                        "Đặt lịch với " + doc.getUser().getFullName(),
                        "Xem bác sĩ khác khoa " + specName,
                        "Bảng giá khám"
                ))
                .build();
    }

    private AiChatResponse buildDoctorByGenderResponse(
            boolean isFemale,
            Specialty specialty,
            Symptom symptom,
            LocalDate targetDate,
            Boolean isMorning,
            List<Doctor> allDoctors,
            NumberFormat currencyFormat
    ) {
        String genderTerm = isFemale ? "nữ" : "nam";
        String genderIcon = isFemale ? "👩‍⚕️" : "👨‍⚕️";
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<Doctor> genderDoctors = allDoctors.stream()
                .filter(d -> isFemale ? isDoctorFemale(d) : isDoctorMale(d))
                .collect(Collectors.toList());

        List<Doctor> matchedDocs = genderDoctors;
        if (specialty != null) {
            matchedDocs = genderDoctors.stream()
                    .filter(d -> d.getSpecialty() != null && d.getSpecialty().getId().equals(specialty.getId()))
                    .collect(Collectors.toList());
        }

        StringBuilder reply = new StringBuilder();
        List<AiChatResponse.DoctorSummary> doctorSummaries = new ArrayList<>();

        if (specialty != null) {
            reply.append("### ").append(genderIcon).append(" Bác sĩ ").append(genderTerm).append(" chuyên khoa **").append(specialty.getName()).append("**:\n\n");
            if (symptom != null) {
                reply.append("• **Triệu chứng nhận diện:** ").append(symptom.getName()).append("\n");
            }
            reply.append("Phòng khám rất thấu hiểu và tôn trọng nhu cầu thăm khám riêng tư, chu đáo của bạn.\n\n");
        } else {
            reply.append("### ").append(genderIcon).append(" Đội ngũ Bác sĩ ").append(genderTerm).append(" giỏi & tận tâm tại Phòng Khám YouMed:\n\n");
            reply.append("Phòng khám Đa khoa YouMed rất hiểu và luôn tôn trọng nhu cầu thăm khám riêng tư, thoải mái của quý khách hàng, đặc biệt là các chị em phụ nữ và các bạn nhỏ.\n\n");
            reply.append("Hiện tại phòng khám có đội ngũ **Bác sĩ ").append(genderTerm).append(" chuyên gia đầu ngành (BSCKII, Tiến sĩ, Thạc sĩ)** đang trực tiếp công tác tại các chuyên khoa:\n\n");
        }

        if (matchedDocs.isEmpty()) {
            if (specialty != null) {
                reply.append("Hiện tại chuyên khoa **").append(specialty.getName()).append("** phòng khám đang được phụ trách bởi các Bác sĩ chuyên khoa giàu kinh nghiệm:\n");
                List<Doctor> specDocs = allDoctors.stream()
                        .filter(d -> d.getSpecialty() != null && d.getSpecialty().getId().equals(specialty.getId()))
                        .limit(3)
                        .collect(Collectors.toList());
                for (Doctor d : specDocs) {
                    String fee = (d.getConsultationFee() != null) ? currencyFormat.format(d.getConsultationFee()) : "200.000 ₫";
                    reply.append("- **").append(d.getUser().getFullName()).append("** (Phí: ").append(fee).append(")\n");
                }
                reply.append("\n👉 Ngoài ra, quý khách có thể tham khảo các Bác sĩ ").append(genderTerm).append(" ở các chuyên khoa liên quan khác tại phòng khám.");
            } else {
                reply.append("Hiện tại danh sách bác sĩ ").append(genderTerm).append(" đang được cập nhật thêm. Quý khách vui lòng liên hệ hotline 1900 1234 để được hỗ trợ trực tiếp.");
            }
        } else {
            Map<String, List<Doctor>> bySpec = matchedDocs.stream()
                    .collect(Collectors.groupingBy(d -> (d.getSpecialty() != null ? d.getSpecialty().getName() : "Đa khoa"), LinkedHashMap::new, Collectors.toList()));

            for (Map.Entry<String, List<Doctor>> entry : bySpec.entrySet()) {
                if (specialty == null) {
                    reply.append("• **Khoa ").append(entry.getKey()).append(":**\n");
                }
                for (Doctor doc : entry.getValue()) {
                    String fee = (doc.getConsultationFee() != null) ? currencyFormat.format(doc.getConsultationFee()) : "200.000 ₫";
                    reply.append("- **").append(doc.getUser().getFullName()).append("** (Phí: ").append(fee).append(")\n");
                    if (doc.getBiography() != null && !doc.getBiography().isBlank()) {
                        reply.append("  *").append(doc.getBiography()).append("*\n");
                    }

                    List<String> slotTimes = Collections.emptyList();
                    if (targetDate != null) {
                        List<TimeSlot> allSlots = timeSlotRepository.findAvailableSlots(doc.getId(), targetDate);
                        List<TimeSlot> filteredSlots = allSlots.stream()
                                .filter(ts -> !Boolean.TRUE.equals(ts.getBooked()) && (ts.getActive() == null || ts.getActive()))
                                .filter(ts -> {
                                    if (isMorning == null) return true;
                                    int hour = ts.getStartDateTime().getHour();
                                    return isMorning ? hour < 12 : hour >= 12;
                                })
                                .collect(Collectors.toList());
                        slotTimes = filteredSlots.stream()
                                .map(ts -> ts.getStartDateTime().format(timeFormatter))
                                .collect(Collectors.toList());
                        if (!slotTimes.isEmpty()) {
                            reply.append("  *Giờ trống ngày ").append(targetDate.format(dateFormatter)).append(": ").append(String.join(", ", slotTimes)).append("*\n");
                        }
                    }

                    doctorSummaries.add(AiChatResponse.DoctorSummary.builder()
                            .id(doc.getId())
                            .fullName(doc.getUser().getFullName())
                            .specialtyName(doc.getSpecialty() != null ? doc.getSpecialty().getName() : "Đa khoa")
                            .consultationFee(doc.getConsultationFee())
                            .biography(doc.getBiography())
                            .targetDate(targetDate != null ? targetDate.toString() : null)
                            .availableSlots(slotTimes)
                            .build());
                }
                reply.append("\n");
            }

            reply.append("👉 Quý khách có thể bấm trực tiếp nút **Đặt lịch** ở thẻ bác sĩ bên dưới để chọn ngày giờ khám phù hợp nhất!");
        }

        List<String> quickSuggestions = new ArrayList<>();
        if (isFemale) {
            quickSuggestions.add("Bác sĩ nữ khoa Sản phụ khoa");
            quickSuggestions.add("Bác sĩ nữ khoa Da liễu");
            quickSuggestions.add("Bác sĩ nữ khoa Nhi");
            quickSuggestions.add("Bảng giá khám phòng khám");
        } else {
            quickSuggestions.add("Bác sĩ Nam khoa Nội tổng quát");
            quickSuggestions.add("Bảng giá khám bệnh");
            quickSuggestions.add("Giờ làm việc phòng khám");
        }

        return AiChatResponse.builder()
                .reply(reply.toString())
                .intent("FIND_DOCTOR")
                .suggestedSpecialty(specialty != null ? AiChatResponse.SpecialtySummary.builder()
                        .id(specialty.getId())
                        .name(specialty.getName())
                        .description(specialty.getDescription())
                        .build() : null)
                .suggestedDoctors(doctorSummaries.stream().limit(6).collect(Collectors.toList()))
                .quickSuggestions(quickSuggestions)
                .build();
    }

    private boolean isDoctorFemale(Doctor doc) {
        if (doc == null || doc.getUser() == null) return false;
        String gender = doc.getUser().getGender();
        if (gender != null && !gender.isBlank()) {
            String g = gender.trim().toLowerCase();
            if (g.contains("nữ") || g.contains("nu") || g.contains("female") || g.equals("f")) {
                return true;
            }
            if (g.contains("nam") || g.contains("male") || g.equals("m")) {
                return false;
            }
        }
        String fullName = doc.getUser().getFullName().toLowerCase();
        if (fullName.contains("thị ") || fullName.contains("cô ") || fullName.contains("bà ")) {
            return true;
        }
        String[] femaleEndings = {"mai", "lan", "trang", "oanh", "hương", "huong", "nga", "ngọc", "ngoc", 
                "thảo", "thao", "hà", "ha", "linh", "quỳnh", "quynh", "thúy", "thuy", "dung", "tuyết", 
                "tuyet", "hoa", "yến", "yen", "hằng", "hang", "hạnh", "hanh", "loan", "diệp", "diep", 
                "giang", "vy", "trâm", "tram", "hồng", "hong", "anh"};
        for (String ending : femaleEndings) {
            if (fullName.endsWith(" " + ending) || fullName.endsWith("." + ending)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDoctorMale(Doctor doc) {
        return !isDoctorFemale(doc);
    }
}
