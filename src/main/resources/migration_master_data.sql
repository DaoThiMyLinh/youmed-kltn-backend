-- ====================================================================
-- MASTER DATA MIGRATION CHO PHÒNG KHÁM ĐA KHOA YOUMED
-- Cập nhật mở rộng: Chuyên khoa Truyền nhiễm, Hô hấp, Tiêu hóa - Gan mật
-- Thêm Vắc-xin phòng Dại, Uốn ván, Kháng sinh, Triệu chứng & Chẩn đoán lâm sàng
-- ====================================================================

-- 1. Insert Specialties
INSERT INTO specialties (name, description, image_url, active, created_at, updated_at) VALUES 
('Nội khoa', 'Khám và điều trị các bệnh nội khoa chung', '/images/specialties/noi-khoa.jpg', true, NOW(), NOW()),
('Nhi khoa', 'Khám và điều trị các bệnh lý ở trẻ em', '/images/specialties/nhi-khoa.jpg', true, NOW(), NOW()),
('Sản phụ khoa', 'Khám và điều trị các bệnh lý phụ khoa, thai sản', '/images/specialties/san-phu-khoa.jpg', true, NOW(), NOW()),
('Da liễu', 'Khám và điều trị các bệnh lý về da', '/images/specialties/da-lieu.jpg', true, NOW(), NOW()),
('Mắt', 'Khám và điều trị các bệnh lý về mắt', '/images/specialties/mat.jpg', true, NOW(), NOW()),
('Tai Mũi Họng', 'Khám và điều trị các bệnh lý tai mũi họng', '/images/specialties/tai-mui-hong.jpg', true, NOW(), NOW()),
('Răng Hàm Mặt', 'Khám và điều trị các bệnh lý răng hàm mặt', '/images/specialties/rang-ham-mat.jpg', true, NOW(), NOW()),
('Tim mạch', 'Khám và điều trị các bệnh lý tim mạch', '/images/specialties/tim-mach.jpg', true, NOW(), NOW()),
('Thần kinh', 'Khám và điều trị các bệnh lý thần kinh', '/images/specialties/than-kinh.jpg', true, NOW(), NOW()),
('Cơ xương khớp', 'Khám và điều trị các bệnh lý cơ xương khớp', '/images/specialties/co-xuong-khop.jpg', true, NOW(), NOW()),
('Truyền nhiễm', 'Khám, điều trị bệnh truyền nhiễm, phơi nhiễm Dại, Uốn ván, Sốt xuất huyết, Viêm gan', '/images/specialties/truyen-nhiem.jpg', true, NOW(), NOW()),
('Hô hấp', 'Khám và điều trị các bệnh lý đường hô hấp, phổi, hen suyễn, COPD', '/images/specialties/ho-hap.jpg', true, NOW(), NOW()),
('Tiêu hóa - Gan mật', 'Khám và điều trị chuyên sâu bệnh lý dạ dày, đại tràng, gan mật tụy', '/images/specialties/tieu-hoa-gan-mat.jpg', true, NOW(), NOW())
ON CONFLICT (name) DO UPDATE SET 
    image_url = EXCLUDED.image_url,
    description = EXCLUDED.description,
    active = EXCLUDED.active;

-- 2. Insert Medicine Categories
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
('Dung dịch sát khuẩn, nhỏ mắt', 'Nước muối sinh lý, cồn, thuốc nhỏ mắt', NOW(), NOW()),
('Vắc-xin & Huyết thanh sinh học', 'Vắc-xin phòng dại, uốn ván, cúm, huyết thanh kháng độc', NOW(), NOW()),
('Thuốc kháng virus', 'Thuốc điều trị nhiễm virus, cúm, zona, viêm gan', NOW(), NOW()),
('Thuốc hạ mỡ máu & Huyết khối', 'Thuốc điều trị rối loạn lipid máu, chống kết tập tiểu cầu', NOW(), NOW()),
('Thuốc an thần, thần kinh', 'Thuốc điều trị đau thần kinh, rối loạn giấc ngủ, tuần hoàn não', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 3. Insert Medicines
INSERT INTO medicines (name, description, unit, category_id, created_at, updated_at) VALUES 
-- Vắc-xin & Huyết thanh sinh học (Truyền nhiễm / Tiêm chủng)
('Vắc-xin Verorab 0.5ml', 'Vắc-xin phòng bệnh Dại tế bào Vero bất hoạt (Pháp)', 'Lọ', (SELECT id FROM medicine_categories WHERE name = 'Vắc-xin & Huyết thanh sinh học'), NOW(), NOW()),
('Vắc-xin Abhayrab 0.5ml', 'Vắc-xin phòng bệnh Dại tinh chế (Ấn Độ)', 'Lọ', (SELECT id FROM medicine_categories WHERE name = 'Vắc-xin & Huyết thanh sinh học'), NOW(), NOW()),
('Huyết thanh kháng Dại SAR (1000 IU)', 'Huyết thanh kháng dại từ ngựa tiêm tại vết thương độ III', 'Lọ', (SELECT id FROM medicine_categories WHERE name = 'Vắc-xin & Huyết thanh sinh học'), NOW(), NOW()),
('Vắc-xin Uốn ván VAT', 'Vắc-xin phòng uốn ván hấp phụ cho vết thương bẩn', 'Lọ', (SELECT id FROM medicine_categories WHERE name = 'Vắc-xin & Huyết thanh sinh học'), NOW(), NOW()),
('Huyết thanh kháng uốn ván SAT (1500 IU)', 'Huyết thanh trung hòa độc tố uốn ván khẩn cấp', 'Ống', (SELECT id FROM medicine_categories WHERE name = 'Vắc-xin & Huyết thanh sinh học'), NOW(), NOW()),
('Vắc-xin Cúm Influvac Tetra', 'Vắc-xin phòng 4 chủng cúm mùa cho trẻ em và người lớn', 'Bơm tiêm', (SELECT id FROM medicine_categories WHERE name = 'Vắc-xin & Huyết thanh sinh học'), NOW(), NOW()),
('Vắc-xin Viêm gan B Engerix-B 20mcg', 'Vắc-xin tái tổ hợp phòng virus viêm gan B', 'Lọ', (SELECT id FROM medicine_categories WHERE name = 'Vắc-xin & Huyết thanh sinh học'), NOW(), NOW()),

-- Giảm đau, hạ sốt, kháng viêm
('Paracetamol 500mg', 'Thuốc giảm đau, hạ sốt thông dụng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Ibuprofen 400mg', 'Thuốc kháng viêm không steroid (NSAID)', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Panadol Extra', 'Giảm đau mạnh, có chứa Caffeine', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Efferalgan 500mg', 'Viên sủi giảm đau, hạ sốt tác dụng nhanh', 'Viên sủi', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Hapacol 650mg', 'Giảm đau hạ sốt liều cao cho người lớn', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Meloxicam 7.5mg', 'Thuốc kháng viêm giảm đau khớp chuyên sâu', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Celecoxib 200mg', 'Kháng viêm chọn lọc COX-2 êm dịu dạ dày', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),
('Aspirin 81mg', 'Thuốc giảm đau, ngừa huyết khối tim mạch', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc giảm đau, hạ sốt'), NOW(), NOW()),

-- Kháng sinh
('Amoxicillin 500mg', 'Kháng sinh nhóm penicillin, phổ rộng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Augmentin 1g', 'Kháng sinh Amoxicillin + Clavulanic điều trị nhiễm khuẩn nặng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Azithromycin 250mg', 'Kháng sinh nhóm macrolid điều trị hô hấp, da', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Cefuroxime 500mg (Zinnat)', 'Kháng sinh Cephalosporin thế hệ 2 đường hô hấp, tiết niệu', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Ciprofloxacin 500mg', 'Kháng sinh Quinolone điều trị nhiễm khuẩn tiêu hóa, tiết niệu', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Clarithromycin 500mg', 'Kháng sinh macrolide phác đồ diệt vi khuẩn HP dạ dày', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Metronidazole 250mg (Klion)', 'Kháng sinh trị nhiễm khuẩn kỵ khí và amip đường ruột', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),
('Doxycycline 100mg', 'Kháng sinh Tetracycline trị mụn trứng cá và sốt rét', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Kháng sinh'), NOW(), NOW()),

-- Kháng virus
('Acyclovir 800mg', 'Thuốc kháng virus điều trị Zona thần kinh, Thủy đậu, Herpes', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc kháng virus'), NOW(), NOW()),
('Tamiflu 75mg (Oseltamivir)', 'Thuốc kháng virus đặc hiệu điều trị Cúm A, Cúm B', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc kháng virus'), NOW(), NOW()),

-- Vitamin & Khoáng chất
('Vitamin C 1000mg', 'Bổ sung vitamin C, tăng đề kháng cơ thể', 'Viên sủi', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Vitamin 3B (B1, B6, B12)', 'Bổ sung vitamin nhóm B, hỗ trợ đau dây thần kinh', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Canxi D3', 'Bổ sung canxi và vitamin D3 cho xương chắc khỏe', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Sắt Folic (Ferrovit)', 'Bổ sung sắt hữu cơ cho phụ nữ có thai và thiếu máu', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),
('Kẽm Gluconate 50mg', 'Bổ sung kẽm tăng miễn dịch và phục hồi niêm mạc ruột', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Vitamin và khoáng chất'), NOW(), NOW()),

-- Hô hấp, Ho, Cảm
('Decolgen', 'Thuốc trị cảm cúm, sổ mũi, nghẹt mũi', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Tiffy Dey', 'Thuốc trị cảm sốt, nhức đầu, sổ mũi', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Prospan', 'Siro trị ho thảo dược chiết xuất lá thường xuân', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Acemuc 200mg', 'Thuốc tiêu chất nhầy, long đờm đường hô hấp', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Singulair 10mg (Montelukast)', 'Thuốc phòng ngừa và kiểm soát cơn hen suyễn', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),
('Xịt mũi Otrivin 0.1%', 'Thuốc co mạch giảm sung huyết mũi nhanh chóng', 'Lọ', (SELECT id FROM medicine_categories WHERE name = 'Thuốc ho, cảm, sổ mũi'), NOW(), NOW()),

-- Tiêu hóa & Dạ dày
('Oresol', 'Gói bù nước và chất điện giải khi sốt cao, tiêu chảy', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Loperamide 2mg', 'Thuốc làm giảm nhu động ruột, cầm tiêu chảy cấp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Omeprazole 20mg', 'Thuốc ức chế bơm proton (PPI) giảm tiết axit dạ dày', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Nexium 40mg (Esomeprazole)', 'Thuốc điều trị loét dạ dày và trào ngược GERD cao cấp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Phosphalugel (Chữ P)', 'Gel nhôm phosphate trung hòa axit dạ dày, giảm ợ chua', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Smecta', 'Thuốc bảo vệ niêm mạc tiêu hóa, trị tiêu chảy', 'Gói', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Men tiêu hóa Enterogermina', 'Bào tử lợi khuẩn Bacillus clausii phục hồi đường ruột', 'Ống', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Spasmaverine 40mg', 'Thuốc chống co thắt cơ trơn đường tiêu hóa, giảm đau bụng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),
('Motilium-M 10mg (Domperidone)', 'Thuốc chống nôn, đầy hơi, khó tiêu', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiêu hóa, dạ dày'), NOW(), NOW()),

-- Tim mạch, Huyết áp, Hạ mỡ máu
('Amlodipine 5mg', 'Thuốc chẹn kênh canxi điều trị tăng huyết áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Losartan 50mg', 'Thuốc ức chế thụ thể angiotensin II trị huyết áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Coversyl 5mg (Perindopril)', 'Thuốc ức chế men chuyển điều trị tăng huyết áp, suy tim', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Concor 2.5mg (Bisoprolol)', 'Thuốc chẹn beta giao cảm ổn định nhịp tim, hạ áp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tim mạch, huyết áp'), NOW(), NOW()),
('Rosuvastatin 10mg (Crestor)', 'Thuốc hạ cholesterol và mỡ máu cao cấp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc hạ mỡ máu & Huyết khối'), NOW(), NOW()),
('Atorvastatin 20mg (Lipitor)', 'Thuốc hạ triglycerid và LDL-cholesterol phòng ngừa đột quỵ', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc hạ mỡ máu & Huyết khối'), NOW(), NOW()),
('Plavix 75mg (Clopidogrel)', 'Thuốc chống kết tập tiểu cầu ngừa nhồi máu cơ tim', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc hạ mỡ máu & Huyết khối'), NOW(), NOW()),

-- Tiểu đường
('Metformin 500mg (Glucophage)', 'Thuốc kiểm soát đường huyết đầu tay cho tiểu đường type 2', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiểu đường'), NOW(), NOW()),
('Diamicron MR 60mg (Gliclazide)', 'Thuốc phóng thích kéo dài kích thích tụy tiết insulin', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc tiểu đường'), NOW(), NOW()),

-- Dị ứng & Kháng viêm
('Loratadine 10mg', 'Thuốc chống dị ứng, kháng histamin không gây buồn ngủ', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW()),
('Cetirizine 10mg', 'Thuốc kháng histamin trị mẩn ngứa, mề đay, dị ứng', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW()),
('Telfast HD 180mg (Fexofenadine)', 'Thuốc trị viêm mũi dị ứng và mày đay mạn tính', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW()),
('Medrol 16mg (Methylprednisolone)', 'Thuốc kháng viêm corticoid mạnh trị dị ứng nặng, viêm khớp', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc dị ứng, kháng histamin'), NOW(), NOW()),

-- Thuốc an thần & Thần kinh
('Tanakan 40mg (Ginkgo Biloba)', 'Chiết xuất bạch quả tăng cường tuần hoàn não, giảm chóng mặt', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc an thần, thần kinh'), NOW(), NOW()),
('Gabapentin 300mg', 'Thuốc điều trị đau dây thần kinh tọa, đau sau Zona', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc an thần, thần kinh'), NOW(), NOW()),
('Magne B6', 'Hỗ trợ giảm lo âu, căng thẳng, rối loạn giấc ngủ', 'Viên', (SELECT id FROM medicine_categories WHERE name = 'Thuốc an thần, thần kinh'), NOW(), NOW()),

-- Thuốc bôi ngoài da & Sát trùng
('Gentrisone', 'Kem bôi trị nấm da, viêm da dị ứng, chàm', 'Tuýp', (SELECT id FROM medicine_categories WHERE name = 'Thuốc bôi ngoài da'), NOW(), NOW()),
('Fucidin', 'Kem bôi kháng sinh ngoài da trị chốc lở, nhiễm trùng', 'Tuýp', (SELECT id FROM medicine_categories WHERE name = 'Thuốc bôi ngoài da'), NOW(), NOW()),
('Bepanthen Balm', 'Kem bôi làm dịu da tổn thương, phục hồi vết nứt nẻ', 'Tuýp', (SELECT id FROM medicine_categories WHERE name = 'Thuốc bôi ngoài da'), NOW(), NOW()),
('Acyclovir bôi 5g', 'Kem bôi kháng virus mụn rộp Herpes môi, Zona', 'Tuýp', (SELECT id FROM medicine_categories WHERE name = 'Thuốc bôi ngoài da'), NOW(), NOW()),
('Povidine 10%', 'Dung dịch Povidone Iodine sát khuẩn vết thương hở, vết cắn', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Dung dịch sát khuẩn, nhỏ mắt'), NOW(), NOW()),
('Cồn đỏ sát trùng 70 độ', 'Cồn y tế sát khuẩn vết thương khẩn cấp', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Dung dịch sát khuẩn, nhỏ mắt'), NOW(), NOW()),
('Natri Clorid 0.9%', 'Nước muối sinh lý rửa sạch vết thương, nhỏ mắt mũi', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Dung dịch sát khuẩn, nhỏ mắt'), NOW(), NOW()),
('V.Rohto New', 'Thuốc nhỏ mắt kháng viêm, giảm ngứa mỏi mắt', 'Chai', (SELECT id FROM medicine_categories WHERE name = 'Dung dịch sát khuẩn, nhỏ mắt'), NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 4. Insert Symptoms
INSERT INTO symptoms (name, description, created_at, updated_at) VALUES 
-- Truyền nhiễm & Cấp cứu vết cắn
('Vết chó cắn', 'Vết thương do chó cắn, rách da, nguy cơ phơi nhiễm virus Dại và Uốn ván', NOW(), NOW()),
('Vết mèo cào', 'Vết cào xước do mèo, nguy cơ phơi nhiễm Dại và vi khuẩn Bartonella', NOW(), NOW()),
('Nghi ngờ nhiễm Dại', 'Sợ nước, sợ gió, kích động sau khi bị động vật cắn', NOW(), NOW()),
('Vết thương hở nhiễm bẩn', 'Vết rách da tiếp xúc đất cát, nguy cơ trực khuẩn uốn ván xâm nhập', NOW(), NOW()),
('Sốt phát ban', 'Sốt cao kèm nổi nốt đỏ li ti toàn thân, nghi sởi hoặc rubella', NOW(), NOW()),
('Vàng da vàng mắt', 'Sắc tố da và tròng trắng mắt chuyển vàng, nước tiểu sẫm màu', NOW(), NOW()),

-- Hô hấp & Tai Mũi Họng
('Ho khan', 'Ho kéo dài không có đờm, rát cổ họng', NOW(), NOW()),
('Ho có đờm', 'Ho khạc đờm đặc màu trắng, vàng hoặc xanh', NOW(), NOW()),
('Đau họng', 'Đau rát, nuốt vướng vùng hầu họng', NOW(), NOW()),
('Sổ mũi', 'Chảy dịch nhầy từ mũi, hắt hơi liên tục', NOW(), NOW()),
('Nghẹt mũi', 'Tắc nghẽn khoang mũi, khó thở bằng mũi', NOW(), NOW()),
('Khó thở kịch phát', 'Cảm giác thiếu dưỡng khí, thở khò khè, co kéo lồng ngực', NOW(), NOW()),
('Khàn tiếng', 'Mất giọng, khàn giọng kéo dài trên 1 tuần', NOW(), NOW()),
('Ù tai', 'Tiếng ve kêu hoặc tiếng gió rít trong tai', NOW(), NOW()),
('Chảy máu cam', 'Chảy máu từ niêm mạc mũi tự nhiên', NOW(), NOW()),

-- Tiêu hóa & Nội tổng quát
('Sốt cao', 'Nhiệt độ cơ thể đo được trên 38.5 độ C kéo dài', NOW(), NOW()),
('Sốt nhẹ', 'Nhiệt độ cơ thể từ 37.5 đến 38.5 độ C ớn lạnh về chiều', NOW(), NOW()),
('Đau bụng thượng vị', 'Đau quặn hoặc cồn cào vùng trên rốn sau khi ăn', NOW(), NOW()),
('Ợ chua', 'Ợ hơi, ợ chua nóng rát vùng xương ức', NOW(), NOW()),
('Buồn nôn', 'Cảm giác nôn nao, muốn nôn ói', NOW(), NOW()),
('Tiêu chảy', 'Đi ngoài phân lỏng nhiều hơn 3 lần trong ngày', NOW(), NOW()),
('Táo bón', 'Khó đi đại tiện, phân khô cứng kéo dài', NOW(), NOW()),
('Đầy bụng khó tiêu', 'Chướng bụng, cảm giác ậm ạch sau khi ăn', NOW(), NOW()),
('Mệt mỏi uể oải', 'Thiếu năng lượng, suy nhược cơ thể', NOW(), NOW()),

-- Tim mạch & Thần kinh
('Đau ngực dữ dội', 'Đau thắt ngực đè nặng lan ra vai trái và cánh tay', NOW(), NOW()),
('Hồi hộp tim đập nhanh', 'Đánh trống ngực, nhịp tim không đều', NOW(), NOW()),
('Đau đầu', 'Cảm giác đau nhức vùng đầu hoặc trán', NOW(), NOW()),
('Đau nửa đầu', 'Đau đầu theo nhịp mạch đập kèm buồn nôn, sợ ánh sáng', NOW(), NOW()),
('Chóng mặt', 'Cảm giác trời đất quay cuồng, mất thăng bằng khi đổi tư thế', NOW(), NOW()),
('Mất ngủ', 'Khó vào giấc, trằn trọc, ngủ chập chờn hay thức giấc', NOW(), NOW()),
('Co giật', 'Cơn co giật toàn thân hoặc co giật cơ cục bộ', NOW(), NOW()),
('Tê bì chân tay', 'Cảm giác châm chích, kiến bò hoặc tê cóng ở đầu ngón tay chân', NOW(), NOW()),

-- Da liễu, Mắt, Cơ xương khớp, Răng
('Phát ban mẩn ngứa', 'Nổi mảng mề đay đỏ, ngứa ngáy toàn thân', NOW(), NOW()),
('Mụn mủ ngoài da', 'Mụn sưng viêm, có dịch mủ trên mặt hoặc cơ thể', NOW(), NOW()),
('Đau nhức cơ khớp', 'Đau mỏi các khớp ngón tay, đầu gối, cột sống', NOW(), NOW()),
('Sưng đau khớp gối', 'Khớp gối sưng nóng đỏ đau, hạn chế co duỗi', NOW(), NOW()),
('Đau mỏi thắt lưng', 'Đau lưng lan xuống mông hoặc chân khi cúi gập', NOW(), NOW()),
('Đau nhức răng', 'Đau buốt chân răng khi ăn đồ nóng lạnh', NOW(), NOW()),
('Chảy mủ mắt đỏ mắt', 'Cộm rát mắt, chảy nước mắt, ghèn vàng dính mi', NOW(), NOW()),
('Tiểu buốt tiểu rắt', 'Đi tiểu đau buốt, cảm giác đi tiểu liên tục ngắt quãng', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- 5. Insert Diagnoses
INSERT INTO diagnoses (name, description, created_at, updated_at) VALUES 
-- Truyền nhiễm & Cấp cứu
('Vết thương do súc vật cắn (Nghi dại)', 'Vết cắn hoặc cào do chó, mèo, động vật hoang dã có nguy cơ lây truyền virus Dại', NOW(), NOW()),
('Phơi nhiễm virus Dại', 'Tình trạng tiếp xúc với virus dại qua vết cắn, vết rách da cần tiêm vắc-xin và huyết thanh khẩn cấp', NOW(), NOW()),
('Nguy cơ nhiễm trùng Uốn ván', 'Vết thương nhiễm bẩn nghi nhiễm trực khuẩn Clostridium tetani', NOW(), NOW()),
('Sốt xuất huyết Dengue', 'Bệnh truyền nhiễm cấp tính do muỗi Aedes truyền virus Dengue', NOW(), NOW()),
('Bệnh Tay Chân Miệng', 'Nhiễm virus Coxsackie hoặc Enterovirus gây phát ban lòng bàn tay, chân và loét miệng', NOW(), NOW()),
('Thủy đậu (Varicella)', 'Bệnh truyền nhiễm do virus Varicella-Zoster gây mụn nước ngứa toàn thân', NOW(), NOW()),
('Zona thần kinh (Herpes Zoster)', 'Tái hoạt virus Varicella Zoster dọc theo dây thần kinh cảm giác', NOW(), NOW()),
('Cảm cúm mùa (Cúm A/B)', 'Nhiễm virus Influenza đường hô hấp gây sốt cao, đau mình mẩy', NOW(), NOW()),
('Covid-19', 'Nhiễm virus SARS-CoV-2 gây viêm đường hô hấp', NOW(), NOW()),
('Viêm gan siêu vi B mạn', 'Nhiễm virus HBV gây tổn thương tế bào gan kéo dài', NOW(), NOW()),

-- Hô hấp & Tai Mũi Họng
('Viêm họng cấp', 'Tình trạng viêm nhiễm niêm mạc họng do virus hoặc vi khuẩn', NOW(), NOW()),
('Viêm amidan hốc mủ', 'Viêm nhiễm mủ tại hốc amidan khẩu cái', NOW(), NOW()),
('Viêm phế quản cấp', 'Viêm đường thở phế quản dẫn vào phổi gây ho đờm', NOW(), NOW()),
('Viêm phổi cộng đồng', 'Nhiễm trùng nhu mô phổi gây sốt cao, khó thở', NOW(), NOW()),
('Hen phế quản (Suyễn)', 'Bệnh viêm mạn tính phế quản gây co thắt và khó thở khò khè', NOW(), NOW()),
('Bệnh phổi tắc nghẽn mạn tính (COPD)', 'Tắc nghẽn đường thở không hồi phục do hút thuốc lá lâu năm', NOW(), NOW()),
('Viêm xoang cấp / mạn tính', 'Viêm ứ đọng dịch nhầy tại các hốc xoang hàm, xoang trán', NOW(), NOW()),
('Viêm mũi dị ứng', 'Phản ứng quá mẫn niêm mạc mũi với thời tiết, phấn hoa, bụi bặm', NOW(), NOW()),
('Viêm tai giữa cấp', 'Nhiễm trùng tai giữa gây đau tai, chảy dịch và giảm thính lực', NOW(), NOW()),

-- Tiêu hóa & Nội tiết
('Trào ngược dạ dày thực quản (GERD)', 'Dịch axit dạ dày trào ngược lên thực quản gây nóng rát', NOW(), NOW()),
('Viêm loét dạ dày tá tràng (có HP)', 'Tổn thương niêm mạc dạ dày do vi khuẩn Helicobacter Pylori', NOW(), NOW()),
('Rối loạn tiêu hóa cấp', 'Mất cân bằng hệ vi sinh đường ruột gây đau bụng tiêu chảy', NOW(), NOW()),
('Hội chứng ruột kích thích (IBS)', 'Rối loạn chức năng đại tràng gây co thắt đau bụng thất thường', NOW(), NOW()),
('Nhiễm khuẩn đường tiết niệu (UTI)', 'Nhiễm khuẩn bàng quang hoặc niệu đạo gây tiểu buốt, tiểu đục', NOW(), NOW()),
('Sỏi đường tiết niệu / Sỏi thận', 'Sự lắng cặn khoáng chất tạo sỏi trong hệ tiết niệu', NOW(), NOW()),
('Tiểu đường type 2 (Đái tháo đường)', 'Rối loạn chuyển hóa đường huyết do kháng insulin', NOW(), NOW()),
('Rối loạn lipid máu (Mỡ máu cao)', 'Tăng cholesterol và triglycerid trong máu', NOW(), NOW()),

-- Tim mạch & Thần kinh
('Tăng huyết áp nguyên phát', 'Huyết áp tâm thu >= 140 mmHg hoặc tâm trương >= 90 mmHg', NOW(), NOW()),
('Thiếu máu cơ tim cục bộ', 'Hẹp động mạch vành làm giảm lưu lượng máu nuôi tim', NOW(), NOW()),
('Rối loạn nhịp tim', 'Nhịp tim đập quá nhanh, quá chậm hoặc không đều', NOW(), NOW()),
('Đau đầu căng cơ', 'Đau đầu âm ỉ do stress hoặc căng thẳng cơ vùng cổ gáy', NOW(), NOW()),
('Đau nửa đầu Migraine', 'Cơn đau đầu mạch máu tái diễn từng đợt', NOW(), NOW()),
('Rối loạn tiền đình ngoại biên', 'Tổn thương mê đạo tai trong gây hoa mắt, chóng mặt quay cuồng', NOW(), NOW()),
('Thiểu năng tuần hoàn não', 'Lưu lượng máu lên não suy giảm gây đau đầu mất ngủ', NOW(), NOW()),
('Đau dây thần kinh tọa', 'Chèn ép rễ thần kinh thắt lưng lan xuống chân', NOW(), NOW()),

-- Cơ xương khớp, Da liễu, Mắt, Răng
('Thoái hóa khớp gối', 'Bào mòn sụn khớp gối gây đau và lục cục khi vận động', NOW(), NOW()),
('Thoát vị đĩa đệm cột sống thắt lưng', 'Nhân nhầy đĩa đệm thoát ra ngoài chèn ép tủy sống', NOW(), NOW()),
('Viêm khớp dạng thấp', 'Bệnh tự miễn mạn tính gây biến dạng các khớp nhỏ bàn tay', NOW(), NOW()),
('Cơn Gout (Gút) cấp tính', 'Lắng đọng tinh thể urat tại khớp ngón chân cái gây sưng nóng đỏ đau', NOW(), NOW()),
('Viêm da cơ địa (Eczema)', 'Bệnh lý viêm da dị ứng ngứa ngáy mạn tính', NOW(), NOW()),
('Mày đay mạn tính', 'Phản ứng dị ứng da nổi mảng phù nề ngứa tái diễn', NOW(), NOW()),
('Viêm kết mạc cấp (Đau mắt đỏ)', 'Viêm màng kết do Adenovirus hoặc vi khuẩn gây đỏ mắt, đổ ghèn', NOW(), NOW()),
('Viêm nha chu', 'Nhiễm trùng mô nâng đỡ quanh răng gây lung lay răng', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
