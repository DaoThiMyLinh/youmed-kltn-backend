package com.youmed.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.youmed.entity.Doctor;
import com.youmed.entity.Specialty;
import com.youmed.service.GeminiAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GeminiAiServiceImpl implements GeminiAiService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String modelName;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.trim().isBlank();
    }

    @Override
    public String generateMedicalAdvice(String userMessage, Specialty specialty, List<Doctor> doctors) {
        if (!isAvailable()) {
            return null;
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" 
                    + modelName + ":generateContent?key=" + apiKey.trim();

            String docListStr = doctors.stream()
                    .map(d -> "- " + d.getUser().getFullName() + " (" + (d.getSpecialty() != null ? d.getSpecialty().getName() : "Đa khoa") + ")")
                    .collect(Collectors.joining("\n"));

            String specStr = (specialty != null) ? specialty.getName() : "Đa khoa";

            String systemInstructionText = "Bạn là Trợ lý Y tế AI của Phòng khám Đa khoa YouMed (123 Hải Triều, Q.1, TP.HCM - Hotline: 1900 1234).\n"
                    + "Nhiệm vụ của bạn:\n"
                    + "1. Trả lời câu hỏi người bệnh với văn phong ân cần, đồng cảm, chuẩn mực y khoa.\n"
                    + "2. Với các câu hỏi khẩn cấp, tai nạn hoặc vết cắn động vật:\n"
                    + "   - Bị chó cắn / mèo cào / súc vật cắn: CẢNH BÁO TUYỆT ĐỐI KHÔNG bôi dầu nóng, không đắp lá, không nặn trích máu hay băng kín vì gây tổn thương mô và nhiễm trùng. HƯỚNG DẪN SƠ CỨU: Rửa ngay dưới vòi nước chảy liên tục ít nhất 15 phút với xà phòng, sát trùng bằng cồn 70° hoặc Povidine 10%. ĐẾN NGAY cơ sở y tế / chuyên khoa Truyền nhiễm để tiêm phòng Vắc-xin Dại + Uốn ván (và huyết thanh SAR nếu vết cắn độ III) càng sớm càng tốt (trong 24-48 giờ).\n"
                    + "   - Bị rắn cắn / côn trùng độc / ngộ độc: CẢNH BÁO TUYỆT ĐỐI KHÔNG bôi dầu, không rạch hút nọc, không ga-rô chặt. Rửa sạch, bất động chi bị cắn và đưa đến phòng cấp cứu gần nhất ngay lập tức.\n"
                    + "   - Ý định tự dùng thuốc (ví dụ uống sủi hạ sốt nhiều ngày, tự mua kháng sinh): CẢNH BÁO nguy cơ quá liều, tổn thương gan thận hoặc kháng kháng sinh; khuyên đi khám để tìm nguyên nhân gốc rễ.\n"
                    + "3. Hướng dẫn người bệnh thăm khám tại Chuyên khoa " + specStr + " của YouMed với các bác sĩ đầu ngành.\n"
                    + "4. Trình bày rõ ràng bằng Markdown (dùng gạch đầu dòng, in đậm các ý quan trọng), súc tích trong 2-3 đoạn ngắn, đi thẳng vào hướng dẫn cần thiết.";

            String promptContent = "Câu hỏi của bệnh nhân: \"" + userMessage + "\"\n\n"
                    + "Chuyên khoa đề xuất tại phòng khám: " + specStr + "\n"
                    + "Bác sĩ phụ trách tại phòng khám:\n" + docListStr + "\n\n"
                    + "Hãy đưa ra câu trả lời tư vấn y tế súc tích, an tâm và chuyên nghiệp cho bệnh nhân.";

            // Construct Gemini JSON payload
            ObjectNode rootNode = objectMapper.createObjectNode();

            // systemInstruction
            ObjectNode sysInstructionNode = rootNode.putObject("systemInstruction");
            ArrayNode sysParts = sysInstructionNode.putArray("parts");
            sysParts.addObject().put("text", systemInstructionText);

            // contents
            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentObj = contentsArray.addObject();
            contentObj.put("role", "user");
            ArrayNode userParts = contentObj.putArray("parts");
            userParts.addObject().put("text", promptContent);

            // generationConfig
            ObjectNode genConfig = rootNode.putObject("generationConfig");
            genConfig.put("temperature", 0.3);
            genConfig.put("maxOutputTokens", 500);

            String requestBody = objectMapper.writeValueAsString(rootNode);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(25))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode resJson = objectMapper.readTree(response.body());
                JsonNode candidates = resJson.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textPart = candidates.get(0).path("content").path("parts").get(0).path("text");
                    if (!textPart.isMissingNode()) {
                        return textPart.asText();
                    }
                }
            } else {
                log.warn("Gemini API returned status code {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Error communicating with Gemini API: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public ConsultationResult consultMedicalQuery(
            String userMessage,
            Specialty explicitSpecialty,
            List<Specialty> allSpecialties,
            List<Doctor> allDoctors
    ) {
        if (!isAvailable()) {
            return null;
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" 
                    + modelName + ":generateContent?key=" + apiKey.trim();

            String specCatalog = allSpecialties.stream()
                    .map(s -> "- " + s.getName() + ": " + (s.getDescription() != null ? s.getDescription() : ""))
                    .collect(Collectors.joining("\n"));

            String explicitInstruction = "";
            if (explicitSpecialty != null) {
                explicitInstruction = "\nLƯU Ý ĐẶC BIỆT: Người bệnh đã chỉ định hoặc có nhu cầu khám chuyên khoa **" 
                        + explicitSpecialty.getName() + "**. Hãy tập trung tư vấn cho chuyên khoa này.\n";
            }

            String systemInstructionText = "Bạn là Trợ lý Y tế AI thông minh của Phòng khám Đa khoa YouMed (123 Hải Triều, Q.1, TP.HCM - Hotline: 1900 1234).\n\n"
                    + "Danh sách các chuyên khoa chính thức của phòng khám YouMed:\n"
                    + specCatalog + "\n"
                    + explicitInstruction + "\n"
                    + "NHIỆM VỤ CỦA BẠN:\n"
                    + "1. Phân tích triệu chứng, tình trạng bệnh nhân mô tả (kể cả bệnh lý thông thường, chuyên sâu hay dùng tiếng lóng, tiếng địa phương).\n"
                    + "2. Dựa trên tri thức y khoa lâm sàng, chọn CHÍNH XÁC MỘT chuyên khoa phù hợp nhất trong danh sách chuyên khoa trên để bệnh nhân đăng ký khám.\n"
                    + "3. Đưa ra hướng dẫn y tế ân cần, chuẩn mực, trấn an tâm lý người bệnh:\n"
                    + "   - Nếu là tai nạn súc vật cắn (chó, mèo, dơi...): CẢNH BÁO TUYỆT ĐỐI KHÔNG bôi dầu nóng, không đắp lá, không nặn máu hay băng kín; HƯỚNG DẪN rửa xà phòng dưới vòi nước chảy 15 phút, sát trùng cồn 70°/Povidine và ĐẾN NGAY chuyên khoa Truyền nhiễm tiêm phòng Vắc-xin Dại + Uốn ván (trong 24-48 giờ).\n"
                    + "   - Nếu là rắn cắn, ngộ độc, khó thở cấp tính, dấu hiệu đột quỵ (méo miệng, liệt mặt, yếu nửa người, nói đớ): CẢNH BÁO nguy hiểm và hướng dẫn cấp cứu ngay.\n"
                    + "   - Ý định tự dùng thuốc (uống sủi hạ sốt nhiều ngày, tự mua kháng sinh): CẢNH BÁO nguy cơ ngộ độc, suy hoại tử tế bào gan cấp, xuất huyết tiêu hóa nếu sốt xuất huyết hoặc kháng thuốc; khuyên đi khám tìm nguyên nhân gốc rễ.\n"
                    + "   - Giải thích ngắn gọn lý do vì sao triệu chứng này cần được thăm khám tại chuyên khoa đó.\n"
                    + "4. QUY ĐỊNH ĐỊNH DẠNG ĐẦU RA BẮT BUỘC:\n"
                    + "Dòng đầu tiên: [CHUYEN_KHOA: <Tên chính xác một chuyên khoa trong danh sách trên>]\n"
                    + "Các dòng tiếp theo: Nội dung tư vấn y tế súc tích (khoảng 2-3 đoạn ngắn), định dạng Markdown rõ ràng, dùng gạch đầu dòng và in đậm ý quan trọng.";

            String promptContent = "Bệnh nhân hỏi: \"" + userMessage + "\"\n\nHãy phân tích lâm sàng và đưa ra chỉ định chuyên khoa kèm lời khuyên y tế chuẩn xác.";

            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode sysInstructionNode = rootNode.putObject("systemInstruction");
            ArrayNode sysParts = sysInstructionNode.putArray("parts");
            sysParts.addObject().put("text", systemInstructionText);

            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentObj = contentsArray.addObject();
            contentObj.put("role", "user");
            ArrayNode userParts = contentObj.putArray("parts");
            userParts.addObject().put("text", promptContent);

            ObjectNode genConfig = rootNode.putObject("generationConfig");
            genConfig.put("temperature", 0.2);
            genConfig.put("maxOutputTokens", 650);

            String requestBody = objectMapper.writeValueAsString(rootNode);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode resJson = objectMapper.readTree(response.body());
                JsonNode candidates = resJson.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textPart = candidates.get(0).path("content").path("parts").get(0).path("text");
                    if (!textPart.isMissingNode()) {
                        String fullText = textPart.asText().trim();
                        java.util.regex.Pattern tagPattern = java.util.regex.Pattern.compile("\\[CHUYEN_KHOA:\\s*(.*?)\\]", java.util.regex.Pattern.CASE_INSENSITIVE);
                        java.util.regex.Matcher matcher = tagPattern.matcher(fullText);
                        String detectedSpecName = null;
                        String cleanAdvice = fullText;
                        if (matcher.find()) {
                            detectedSpecName = matcher.group(1).trim();
                            cleanAdvice = fullText.replace(matcher.group(0), "").trim();
                        }
                        return new ConsultationResult(detectedSpecName, cleanAdvice);
                    }
                }
            } else {
                log.warn("Gemini API returned status code {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Error communicating with Gemini API in consultMedicalQuery: {}", e.getMessage());
        }

        return null;
    }
}
