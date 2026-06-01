-- 基础知识图谱数据：万历朝核心人物与关系

INSERT INTO game_entities (name, type, description, properties, created_at) VALUES
('朱翊钧', 'PERSON', '明神宗，隆庆帝第三子，十岁登基', '{"birth": 1563, "reign": "1572-1620"}', NOW()),
('张居正', 'PERSON', '字叔大，号太岳，湖广江陵人，万历首辅', '{"birth": 1525, "death": 1582}', NOW()),
('高拱', 'PERSON', '字肃卿，号中玄，河南新郑人，隆庆首辅', '{"birth": 1513, "death": 1578}', NOW()),
('冯保', 'PERSON', '号双林，深州人，司礼监掌印太监', '{"birth": 1543}', NOW()),
('隆庆帝', 'PERSON', '明穆宗朱载坖，隆庆六年病逝', '{"birth": 1537, "reign": "1567-1572"}', NOW()),
('乾清宫', 'LOCATION', '皇帝寝宫，位于紫禁城内廷', '{}', NOW()),
('文渊阁', 'LOCATION', '内阁办公处', '{}', NOW()),
('考成法', 'INSTITUTION', '张居正推行的官员考核制度', '{}', NOW()),
('一条鞭法', 'INSTITUTION', '张居正推行的赋税制度改革', '{}', NOW());

-- 人物关系
INSERT INTO game_relations (source_id, target_id, type, properties, session_id, created_at)
SELECT e1.id, e2.id, '顾命大臣', '{"order": 1}', 'system', NOW()
FROM game_entities e1, game_entities e2
WHERE e1.name = '隆庆帝' AND e2.name = '高拱';

INSERT INTO game_relations (source_id, target_id, type, properties, session_id, created_at)
SELECT e1.id, e2.id, '顾命大臣', '{"order": 2}', 'system', NOW()
FROM game_entities e1, game_entities e2
WHERE e1.name = '隆庆帝' AND e2.name = '张居正';

INSERT INTO game_relations (source_id, target_id, type, properties, session_id, created_at)
SELECT e1.id, e2.id, '师生', '{}', 'system', NOW()
FROM game_entities e1, game_entities e2
WHERE e1.name = '张居正' AND e2.name = '朱翊钧';

INSERT INTO game_relations (source_id, target_id, type, properties, session_id, created_at)
SELECT e1.id, e2.id, '推行', '{}', 'system', NOW()
FROM game_entities e1, game_entities e2
WHERE e1.name = '张居正' AND e2.name IN ('考成法', '一条鞭法');
