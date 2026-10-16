-- ====================================================================
-- SEED DATA CHO HỆ THỐNG YOUMED (POSTGRESQL)
-- File này có thể chạy trực tiếp trong pgAdmin / DBeaver / psql
-- Mật khẩu mặc định cho các tài khoản: 123456 (Admin: admin123)
-- ====================================================================

-- 1. Master Data: Specialties
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

-- 2. Master Data: Medicine Categories
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

-- 3. Master Data: Medicines
INSERT INTO medicines (name, description, unit, category_id, created_at, updated_at) VALUES 
('Paracetamol 500mg', 'Thuốc giảm đau, hạ sốt thông dụng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Ibuprofen 400mg', 'Thuốc kháng viêm không steroid (NSAID)', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Panadol Extra', 'Giảm đau mạnh, có chứa Caffeine', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Amoxicillin 500mg', 'Kháng sinh nhóm penicillin, phổ rộng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Augmentin 1g', 'Kháng sinh điều trị nhiễm khuẩn nặng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Vitamin C 1000mg', 'Bổ sung vitamin C, tăng đề kháng', 'Viên sủi', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Decolgen', 'Thuốc trị cảm cúm, sổ mũi', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Prospan', 'Siro trị ho chiết xuất lá thường xuân', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Omeprazole 20mg', 'Thuốc giảm tiết axit dạ dày (PPI)', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Phosphalugel (Chữ P)', 'Thuốc trung hòa axit dạ dày', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Amlodipine 5mg', 'Thuốc điều trị tăng huyết áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Losartan 50mg', 'Thuốc điều trị tăng huyết áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Loratadine 10mg', 'Thuốc chống dị ứng, không gây buồn ngủ', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 4. Master Data: Symptoms
INSERT INTO symptoms (name, description, created_at, updated_at) VALUES 
('Ho khan', 'Ho không có đờm', NOW(), NOW()),
('Ho có đờm', 'Ho khạc ra đờm', NOW(), NOW()),
('Sốt cao', 'Nhiệt độ cơ thể trên 38.5 độ C', NOW(), NOW()),
('Đau đầu', 'Cảm giác đau nhức vùng đầu', NOW(), NOW()),
('Đau họng', 'Đau rát vùng họng', NOW(), NOW()),
('Sổ mũi', 'Chảy dịch từ mũi', NOW(), NOW()),
('Đau bụng', 'Đau vùng bụng', NOW(), NOW()),
('Tiêu chảy', 'Đi ngoài phân lỏng nhiều lần', NOW(), NOW()),
('Mệt mỏi', 'Cảm giác thiếu năng lượng, uể oải', NOW(), NOW()),
('Ợ chua', 'Dịch dạ dày trào ngược lên thực quản', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 5. Master Data: Diagnoses
INSERT INTO diagnoses (name, description, created_at, updated_at) VALUES 
('Viêm họng cấp', 'Tình trạng viêm nhiễm ở họng', NOW(), NOW()),
('Cảm cúm', 'Bệnh lý hô hấp do virus', NOW(), NOW()),
('Trào ngược dạ dày thực quản (GERD)', 'Dịch dạ dày trào ngược lên thực quản', NOW(), NOW()),
('Tăng huyết áp', 'Huyết áp cao', NOW(), NOW()),
('Viêm loét dạ dày', 'Tổn thương niêm mạc dạ dày', NOW(), NOW()),
('Viêm da cơ địa', 'Bệnh lý viêm da dị ứng', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
