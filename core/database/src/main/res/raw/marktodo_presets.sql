-- Categories
INSERT OR IGNORE INTO marktodo_category (id, title, icon_key, sort_order, created_at, updated_at) VALUES
('work_growth', 'Work & Growth', 'Work', 0, 1735689600000, 1735689600000),
('life_chores', 'Life Chores', 'ShoppingCart', 1, 1735689600000, 1735689600000),
('inspiration', 'Inspiration & Ideas', 'StarBorder', 2, 1735689600000, 1735689600000);

-- Tasks: Work & Growth
INSERT OR IGNORE INTO marktodo_task (id, category_id, title, note, start_date, due_date, tags, is_completed, is_starred, sort_order, created_at, updated_at) VALUES
('work_1', 'work_growth', 'Finish important project', NULL, 1735689600000, 1738281600000, '', 0, 1, 0, 1735689600000, 1735689600000),
('work_2', 'work_growth', 'Weekly review', NULL, 1735689600000, 1736294400000, '', 0, 0, 1, 1735689600000, 1735689600000),
('work_3', 'work_growth', 'Get certified', NULL, 1735689600000, 1740873600000, '', 0, 0, 2, 1735689600000, 1735689600000),
('work_4', 'work_growth', 'Optimize workflow', NULL, 1735689600000, 1736899200000, '', 0, 0, 3, 1735689600000, 1735689600000),
('work_5', 'work_growth', 'Expand network', NULL, 1735689600000, 1737417600000, '', 0, 0, 4, 1735689600000, 1735689600000);

-- Tasks: Life Chores
INSERT OR IGNORE INTO marktodo_task (id, category_id, title, note, start_date, due_date, tags, is_completed, is_starred, sort_order, created_at, updated_at) VALUES
('chore_1', 'life_chores', 'Pay bills', NULL, 1735689600000, 1735862400000, '', 0, 0, 0, 1735689600000, 1735689600000),
('chore_2', 'life_chores', 'Dentist appointment', NULL, 1735689600000, 1736985600000, '', 0, 0, 1, 1735689600000, 1735689600000),
('chore_3', 'life_chores', 'Water the plants', NULL, 1735689600000, 1735689600000, '', 0, 0, 2, 1735689600000, 1735689600000),
('chore_4', 'life_chores', 'Do laundry', NULL, 1735689600000, 1735776000000, '', 0, 0, 3, 1735689600000, 1735689600000),
('chore_5', 'life_chores', 'Buy groceries', NULL, 1735689600000, 1735948800000, '', 0, 0, 4, 1735689600000, 1735689600000);

-- Tasks: Inspiration
INSERT OR IGNORE INTO marktodo_task (id, category_id, title, note, start_date, due_date, tags, is_completed, is_starred, sort_order, created_at, updated_at) VALUES
('idea_1', 'inspiration', 'Crazy ideas', NULL, 1735689600000, NULL, '', 0, 0, 0, 1735689600000, 1735689600000),
('idea_2', 'inspiration', 'Design inspiration', NULL, 1735689600000, NULL, '', 0, 0, 1, 1735689600000, 1735689600000),
('idea_3', 'inspiration', 'Write a poem', NULL, 1735689600000, NULL, '', 0, 0, 2, 1735689600000, 1735689600000),
('idea_4', 'inspiration', 'Video ideas', NULL, 1735689600000, NULL, '', 0, 0, 3, 1735689600000, 1735689600000);
