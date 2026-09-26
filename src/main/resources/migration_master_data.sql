-- Insert Specialties
INSERT INTO specialties (name, description, active, created_at, updated_at) VALUES 
('Nội khoa', 'Khám và điều trị các bệnh nội khoa chung', true, NOW(), NOW()),
('Nhi khoa', 'Khám và điều trị các bệnh lý ở trẻ em', true, NOW(), NOW()),
('Sản phụ khoa', 'Khám và điều trị các bệnh lý phụ khoa, thai sản', true, NOW(), NOW()),
('Da liễu', 'Khám và điều trị các bệnh lý về da', true, NOW(), NOW()),
('Mắt', 'Khám và điều trị các bệnh lý về mắt', true, NOW(), NOW()),
('Tai Mũi Họng', 'Khám và điều trị các bệnh lý tai mũi họng', true, NOW(), NOW()),
('Răng Hàm Mặt', 'Khám và điều trị các bệnh lý răng hàm mặt', true, NOW(), NOW()),
('Tim mạch', 'Khám và điều trị các bệnh lý tim mạch', true, NOW(), NOW()),
('Thần kinh', 'Khám và điều trị các bệnh lý thần kinh', true, NOW(), NOW()),
('Cơ xương khớp', 'Khám và điều trị các bệnh lý cơ xương khớp', true, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert Medicine Categories
INSERT INTO medicine_categories (name, description, created_at, updated_at) VALUES 
('Thuốc giảm đau, hạ sốt', 'Các loại thuốc giảm đau, hạ sốt thông thường', NOW(), NOW()),
('Kháng sinh', 'Thuốc kháng sinh điều trị nhiễm khuẩn', NOW(), NOW()),
('Vitamin và khoáng chất', 'Các loại vitamin và khoáng chất bổ sung', NOW(), NOW()),
('Thuốc ho, cảm, sổ mũi', 'Thuốc điều trị triệu chứng hô hấp trên', NOW(), NOW()),
('Thuốc tiêu hóa, dạ dày', 'Thuốc điều trị các bệnh lý dạ dày, ruột, tiêu chảy', NOW(), NOW()),
('Thuốc tim mạch, huyết áp', 'Thuốc điều trị tăng huyết áp, suy tim', NOW(), NOW()),
('Thuốc tiểu đường', 'Thuốc điều trị đái tháo đường', NOW(), NOW()),
('Thuốc dị ứng, kháng histamin', 'Thuốc trị mẩn ngứa, dị ứng', NOW(), NOW()),
('Thuốc bôi ngoài da', 'Các loại kem, thuốc mỡ bôi da', NOW(), NOW()),
('Dung dịch sát khuẩn, nhỏ mắt', 'Nước muối sinh lý, cồn, thuốc nhỏ mắt', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert Medicines
INSERT INTO medicines (name, description, unit, category_id, created_at, updated_at) VALUES 
-- Giảm đau, hạ sốt
('Paracetamol 500mg', 'Thuốc giảm đau, hạ sốt thông dụng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Ibuprofen 400mg', 'Thuốc kháng viêm không steroid (NSAID)', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Panadol Extra', 'Giảm đau mạnh, có chứa Caffeine', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Aspirin 81mg', 'Thuốc giảm đau, ngừa huyết khối', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),

-- Kháng sinh
('Amoxicillin 500mg', 'Kháng sinh nhóm penicillin, phổ rộng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Augmentin 1g', 'Kháng sinh điều trị nhiễm khuẩn nặng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Azithromycin 250mg', 'Kháng sinh nhóm macrolid', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Cefuroxime 500mg', 'Kháng sinh nhóm cephalosporin', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),

-- Vitamin
('Vitamin C 1000mg', 'Bổ sung vitamin C, tăng đề kháng', 'Viên sủi', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Vitamin 3B (B1, B6, B12)', 'Bổ sung vitamin nhóm B, hỗ trợ thần kinh', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Canxi D3', 'Bổ sung canxi và vitamin D3 cho xương', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Sắt Folic', 'Bổ sung sắt cho người thiếu máu', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),

-- Ho, cảm
('Decolgen', 'Thuốc trị cảm cúm, sổ mũi', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Tiffy', 'Thuốc trị cảm, hạ sốt', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Prospan', 'Siro trị ho chiết xuất lá thường xuân', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Acemuc 200mg', 'Thuốc long đờm', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),

-- Tiêu hóa
('Oresol', 'Bù nước và điện giải', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Loperamide 2mg', 'Thuốc cầm tiêu chảy', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Omeprazole 20mg', 'Thuốc giảm tiết axit dạ dày (PPI)', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Phosphalugel (Chữ P)', 'Thuốc trung hòa axit dạ dày', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Smecta', 'Thuốc điều trị tiêu chảy, bảo vệ niêm mạc', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Men tiêu hóa Enterogermina', 'Bổ sung lợi khuẩn đường ruột', 'Ống', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),

-- Tim mạch
('Amlodipine 5mg', 'Thuốc điều trị tăng huyết áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Losartan 50mg', 'Thuốc điều trị tăng huyết áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Bisoprolol 5mg', 'Thuốc điều trị suy tim, tăng huyết áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Rosuvastatin 10mg', 'Thuốc hạ mỡ máu', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),

-- Tiểu đường
('Metformin 500mg', 'Thuốc kiểm soát đường huyết type 2', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiểu đường'), NOW(), NOW()),
('Gliclazide 30mg', 'Thuốc kích thích tiết insulin', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiểu đường'), NOW(), NOW()),
('Diamicron MR 60mg', 'Thuốc hạ đường huyết', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiểu đường'), NOW(), NOW()),

-- Dị ứng
('Loratadine 10mg', 'Thuốc chống dị ứng, không gây buồn ngủ', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW()),
('Cetirizine 10mg', 'Thuốc kháng histamin trị mẩn ngứa, sổ mũi', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW()),
('Telfast 180mg', 'Thuốc trị viêm mũi dị ứng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW()),

-- Thuốc bôi da
('Gentrisone', 'Kem bôi trị viêm da, nấm', 'Tuýp', (SELECT id FROM medicine_categories WHERE name = 'Thuốc bôi ngoài da'), NOW(), NOW()),
('Fucidin', 'Kem bôi trị nhiễm trùng da', 'Tuýp', (SELECT id FROM medicine_categories WHERE name = 'Thuốc bôi ngoài da'), NOW(), NOW()),
('Bepanthen', 'Kem bôi hăm tã, làm dịu da', 'Tuýp', (SELECT id FROM medicine_categories WHERE name = 'Thuốc bôi ngoài da'), NOW(), NOW()),

-- Sát khuẩn, nhỏ mắt
('V.Rohto', 'Thuốc nhỏ mắt giảm mỏi, ngứa', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Dung dịch sát khuẩn, nhỏ mắt'), NOW(), NOW()),
('Natri Clorid 0.9%', 'Nước muối sinh lý nhỏ mắt, mũi', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Dung dịch sát khuẩn, nhỏ mắt'), NOW(), NOW()),
('Povidine 10%', 'Dung dịch sát khuẩn vết thương', 'Chai', (SELECT id FROM medicine_categories thực tế thuộc loại: 'Dung dịch sát khuẩn, nhỏ mắt'), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert Symptoms
INSERT INTO symptoms (name, description, created_at, updated_at) VALUES 
('Ho khan', 'Ho không có đờm', NOW(), NOW()),
('Ho có đờm', 'Ho khạc ra đờm', NOW(), NOW()),
('Sốt cao', 'Nhiệt độ cơ thể trên 38.5 độ C', NOW(), NOW()),
('Sốt nhẹ', 'Nhiệt độ cơ thể từ 37.5 đến 38.5 độ C', NOW(), NOW()),
('Đau đầu', 'Cảm giác đau nhức vùng đầu', NOW(), NOW()),
('Chóng mặt', 'Cảm giác mất thăng bằng, hoa mắt', NOW(), NOW()),
('Đau họng', 'Đau rát vùng họng', NOW(), NOW()),
('Sổ mũi', 'Chảy dịch từ mũi', NOW(), NOW()),
('Nghẹt mũi', 'Khó thở bằng mũi', NOW(), NOW()),
('Buồn nôn', 'Cảm giác muốn nôn', NOW(), NOW()),
('Đau bụng', 'Đau vùng bụng', NOW(), NOW()),
('Tiêu chảy', 'Đi ngoài phân lỏng nhiều lần', NOW(), NOW()),
('Táo bón', 'Khó đi đại tiện', NOW(), NOW()),
('Mệt mỏi', 'Cảm giác thiếu năng lượng, uể oải', NOW(), NOW()),
('Đau nhức cơ khớp', 'Đau ở các cơ hoặc khớp', NOW(), NOW()),
('Khó thở', 'Cảm giác tức ngực, thiếu oxy', NOW(), NOW()),
('Phát ban', 'Nổi mẩn đỏ trên da', NOW(), NOW()),
('Mất ngủ', 'Khó vào giấc hoặc ngủ không sâu', NOW(), NOW()),
('Ợ chua', 'Dịch dạ dày trào ngược lên thực quản', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Insert Diagnoses
INSERT INTO diagnoses (name, description, created_at, updated_at) VALUES 
('Viêm họng cấp', 'Tình trạng viêm nhiễm ở họng', NOW(), NOW()),
('Viêm amidan', 'Viêm nhiễm tại amidan', NOW(), NOW()),
('Cảm cúm', 'Bệnh lý hô hấp do virus', NOW(), NOW()),
('Sốt xuất huyết', 'Bệnh truyền nhiễm do virus Dengue', NOW(), NOW()),
('Rối loạn tiêu hóa', 'Các vấn đề về hệ tiêu hóa', NOW(), NOW()),
('Viêm loét dạ dày', 'Tổn thương niêm mạc dạ dày', NOW(), NOW()),
('Trào ngược dạ dày thực quản (GERD)', 'Dịch dạ dày trào ngược lên thực quản', NOW(), NOW()),
('Viêm phế quản', 'Viêm đường thở dẫn vào phổi', NOW(), NOW()),
('Hen suyễn', 'Bệnh viêm mạn tính đường thở', NOW(), NOW()),
('Covid-19', 'Bệnh hô hấp do virus SARS-CoV-2', NOW(), NOW()),
('Tăng huyết áp', 'Huyết áp cao', NOW(), NOW()),
('Tiểu đường type 2', 'Bệnh lý chuyển hóa', NOW(), NOW()),
('Rối loạn lipid máu', 'Mỡ máu cao', NOW(), NOW()),
('Viêm khớp dạng thấp', 'Bệnh tự miễn gây viêm khớp', NOW(), NOW()),
('Thoái hóa cột sống', 'Tình trạng thoái hóa xương khớp', NOW(), NOW()),
('Viêm da cơ địa', 'Bệnh lý viêm da dị ứng', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
