INSERT INTO `user` (id, username, password, nickname, role, status, create_time) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '管理员', 'ADMIN', 1, NOW()),
(2, 'student', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', '演示学生', 'USER', 1, NOW());

INSERT INTO campus_location (id, location_name, campus, description, status) VALUES
(1, '教学楼', '莲花街校区', '上课、点名、小测和课堂提问集中发生的区域。', 1),
(2, '图书馆', '莲花街校区', '自习、查资料、抢座和期末复习的重要地点。', 1),
(3, '宿舍', '莲花街校区', '休息、赶 DDL、室友互动和生活事件的中心。', 1),
(4, '食堂', '莲花街校区', '补充能量、控制预算和触发夜宵诱惑的地方。', 1),
(5, '操场', '莲花街校区', '运动、体测、跑步和健康恢复的区域。', 1),
(6, '实验室', '莲花街校区', '写代码、做实验、调试项目和处理报错的地方。', 1),
(7, '社团活动区', '莲花街校区', '社交、招新、活动冲突和展示自我的场景。', 1),
(8, '快递站', '莲花街校区', '取快递、排队、下雨和生活小插曲频发的地点。', 1);

INSERT INTO `event` (id, event_name, event_type, location_id, description, probability, min_week, max_week, status) VALUES
(1, '早八点名危机', '学习', 1, '你昨晚赶报告睡得很晚，醒来发现距离早八上课只剩 15 分钟。', 80, 1, 20, 1),
(2, '课堂突然提问', '学习', 1, '老师讲到关键知识点时突然看向你，似乎准备让你回答问题。', 60, 1, 20, 1),
(3, '图书馆抢座', '学习', 2, '期末周的图书馆座位很紧张，你到达时只剩一个角落位置。', 70, 8, 20, 1),
(4, '宿舍 DDL 突袭', '学习', 3, '你打开学习平台，发现课程作业截止时间比记忆中更早。', 75, 1, 20, 1),
(5, '食堂夜宵诱惑', '健康', 4, '晚上十点，你路过食堂附近，闻到了夜宵的香味。', 65, 1, 20, 1),
(6, '体测通知', '健康', 5, '班级群突然通知本周体测，你意识到自己已经很久没有运动。', 70, 4, 16, 1),
(7, 'Java 代码报错', '技能', 6, '你正在写 Java 课设，控制台突然出现一长串红色报错。', 80, 1, 20, 1),
(8, '实验数据异常', '技能', 6, '实验记录中有一组数据明显不合理，报告今晚就要提交。', 50, 1, 20, 1),
(9, '社团招新', '社交', 7, '社团活动区正在招新，学长学姐热情邀请你加入。', 55, 1, 8, 1),
(10, '快递到了但下雨', '生活', 8, '你收到快递到站短信，但窗外正下着雨。', 60, 1, 20, 1),
(11, '生活费余额不足', '金钱', 3, '你看了一眼余额，发现本月生活费已经接近见底。', 50, 1, 20, 1),
(12, '粮食守护者挑战', '特色', 4, '食堂餐盘回收处还有不少剩饭，系统向你发起节粮挑战。', 45, 1, 20, 1);

INSERT INTO event_option (id, event_id, option_text, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
(1, 1, '立刻起床冲向教学楼', '你成功赶到教室，但整个人还没完全清醒。', 5, -4, 0, 0, 0, 3, 2, 20),
(2, 1, '请室友帮忙确认情况', '室友提醒你老师已经到教室，你赶紧出发。', 2, -1, 0, 2, 0, 1, 1, 12),
(3, 1, '继续睡觉', '你睡得很香，但点名记录不太友好。', -8, 5, 0, 0, 0, 4, -6, 5),
(4, 2, '根据课前预习回答', '你答得不算完美，但老师认可了你的思路。', 5, 0, 0, 0, 1, -1, 2, 18),
(5, 2, '诚实说自己还没想清楚', '老师让你课后再补充，你记下了这个知识点。', 1, 0, 0, 0, 0, 2, 1, 8),
(6, 2, '低头假装记笔记', '老师没有继续追问，但你错过了一次表现机会。', -2, 0, 0, 0, 0, 1, -2, 3),
(7, 3, '立刻坐下开始复习', '角落很安静，你进入了不错的学习状态。', 8, 0, 0, 0, 1, -2, 4, 24),
(8, 3, '去别处找更舒服的位置', '你花了些时间，但找到了更适合自己的学习环境。', 3, 2, 0, 0, 0, -1, 1, 14),
(9, 3, '放弃自习回宿舍', '你短暂放松了，但复习计划被迫后移。', -5, 2, 0, 0, 0, 4, -4, 5),
(10, 4, '马上拆分任务开始写', '你把作业拆成小块，终于稳住了节奏。', 6, -2, 0, 0, 2, -2, 5, 26),
(11, 4, '先问同学确认要求', '你避免了理解偏差，但留给自己的时间更少。', 3, 0, 0, 3, 0, 1, 2, 16),
(12, 4, '先刷一会儿短视频缓缓', '压力暂时下降，但 DDL 没有因此变远。', -4, 0, 0, 0, 0, 5, -5, 3),
(13, 5, '买一份犒劳自己', '夜宵带来快乐，也带走了一点预算和健康。', 0, -3, -8, 0, 0, -5, -1, 10),
(14, 5, '忍住回宿舍喝水', '你守住了作息和预算，自律值上升。', 0, 2, 0, 0, 0, 1, 6, 18),
(15, 5, '顺便给室友带一份', '室友很开心，你的钱包不太开心。', 0, -2, -15, 5, 0, -3, 0, 14),
(16, 6, '今天就开始跑步训练', '第一天很累，但身体状态开始回升。', 0, 8, 0, 0, 0, -2, 4, 24),
(17, 6, '制定一周训练计划', '计划让你更安心，关键还要看执行。', 0, 3, 0, 0, 0, -1, 5, 18),
(18, 6, '祈祷体测延期', '你暂时逃避了现实，但现实还在操场等你。', 0, -2, 0, 0, 0, 4, -4, 3),
(19, 7, '冷静阅读报错信息', '你定位到了问题，技能值明显提升。', 0, 0, 0, 0, 8, -3, 5, 30),
(20, 7, '复制报错去搜索', '你找到了解法，但还没完全理解原因。', 0, 0, 0, 0, 4, -2, 1, 18),
(21, 7, '关掉电脑明天再说', '压力暂时下降，项目进度也一起下降。', -2, 0, 0, 0, -2, -5, -6, 5),
(22, 8, '检查实验步骤和原始记录', '你发现了记录偏差，报告可信度提升。', 4, 0, 0, 0, 5, -2, 4, 24),
(23, 8, '向同学请教数据处理方式', '交流帮你打开思路，也补上了一个细节。', 2, 0, 0, 4, 3, -1, 1, 18),
(24, 8, '直接忽略异常值', '报告暂时能写下去，但风险被埋下了。', -2, 0, 0, 0, -3, 3, -3, 5),
(25, 9, '加入一个感兴趣的社团', '你认识了新朋友，校园生活更丰富。', 0, 0, 0, 8, 0, -2, 1, 20),
(26, 9, '先了解活动时间再决定', '你避免了时间冲突，也保留了选择空间。', 0, 0, 0, 3, 0, -1, 2, 12),
(27, 9, '绕开人群回宿舍', '你保持了安静，但错过了一次社交机会。', 0, 0, 0, -4, 0, -1, 0, 4),
(28, 10, '撑伞去取快递', '你成功取回快递，但鞋子湿了。', 0, -1, 0, 0, 0, 1, 2, 12),
(29, 10, '等雨小一点再去', '你做了更稳妥的安排，没有打乱节奏。', 0, 0, 0, 0, 0, -1, 2, 10),
(30, 10, '拜托同学顺路带回', '同学帮了你一次，人情账也记上了。', 0, 0, 0, 3, 0, -1, 0, 10),
(31, 11, '制定剩余预算计划', '你重新掌控了消费节奏。', 0, 0, 8, 0, 0, -2, 5, 22),
(32, 11, '减少非必要消费', '你开始精打细算，钱包稍微安全了一点。', 0, 0, 5, 0, 0, 1, 3, 16),
(33, 11, '假装没看到余额', '快乐没有持续太久，月底压力正在逼近。', 0, 0, -8, 0, 0, 5, -4, 3),
(34, 12, '按需取餐并完成节粮打卡', '你完成了节粮挑战，也更理解粮食来之不易。', 2, 0, 0, 0, 3, -1, 6, 24),
(35, 12, '发布节粮倡议', '你的倡议被同学看到，影响力小小扩散。', 1, 0, 0, 5, 2, 0, 4, 20),
(36, 12, '视而不见', '你没有额外行动，系统默默记下了这次选择。', 0, 0, 0, 0, 0, 0, -1, 2);

UPDATE campus_location SET icon_key = 'building', background_image = 'scene-classroom', theme_color = '#2563eb' WHERE id = 1;
UPDATE campus_location SET icon_key = 'book-open', background_image = 'scene-library', theme_color = '#0f766e' WHERE id = 2;
UPDATE campus_location SET icon_key = 'bed', background_image = 'scene-dorm', theme_color = '#9333ea' WHERE id = 3;
UPDATE campus_location SET icon_key = 'utensils', background_image = 'scene-canteen', theme_color = '#dc2626' WHERE id = 4;
UPDATE campus_location SET icon_key = 'activity', background_image = 'scene-track', theme_color = '#16a34a' WHERE id = 5;
UPDATE campus_location SET icon_key = 'code', background_image = 'scene-lab', theme_color = '#7c3aed' WHERE id = 6;
UPDATE campus_location SET icon_key = 'users', background_image = 'scene-club', theme_color = '#ea580c' WHERE id = 7;
UPDATE campus_location SET icon_key = 'package', background_image = 'scene-package', theme_color = '#0891b2' WHERE id = 8;

UPDATE `event` SET scene_image = 'scene-classroom', mood_tag = '危机' WHERE id IN (1, 2);
UPDATE `event` SET scene_image = 'scene-library', mood_tag = '紧张' WHERE id = 3;
UPDATE `event` SET scene_image = 'scene-dorm', mood_tag = 'DDL' WHERE id IN (4, 11);
UPDATE `event` SET scene_image = 'scene-canteen', mood_tag = '诱惑' WHERE id IN (5, 12);
UPDATE `event` SET scene_image = 'scene-track', mood_tag = '挑战' WHERE id = 6;
UPDATE `event` SET scene_image = 'scene-lab', mood_tag = '调试' WHERE id IN (7, 8);
UPDATE `event` SET scene_image = 'scene-club', mood_tag = '社交' WHERE id = 9;
UPDATE `event` SET scene_image = 'scene-package', mood_tag = '生活支线' WHERE id = 10;

UPDATE event_option SET preview_text = '高压冲刺，保住学业', risk_level = 'medium' WHERE id = 1;
UPDATE event_option SET preview_text = '依赖社交，风险较低', risk_level = 'low' WHERE id = 2;
UPDATE event_option SET preview_text = '短期舒服，长期吃亏', risk_level = 'high' WHERE id = 3;
UPDATE event_option SET preview_text = '稳妥定位，技能收益高', risk_level = 'low' WHERE id = 19;
UPDATE event_option SET preview_text = '快但理解有限', risk_level = 'medium' WHERE id = 20;
UPDATE event_option SET preview_text = '压力下降，进度坠落', risk_level = 'high' WHERE id = 21;

INSERT INTO dungeon (id, dungeon_name, dungeon_type, description, cover_image, theme_style, estimated_minutes, difficulty_label, reward_exp, reward_title, status) VALUES
(1, 'Java 课设：DDL 前夜', '课设', '答辩前夜，系统还差关键设计、数据库关系和 Bug 修复。你需要在压力爆表前让项目活下来。', 'scene-lab', 'DDL', 8, '普通', 180, 'DDL 幸存者', 1);

INSERT INTO dungeon_task (id, dungeon_id, task_name, task_type, task_order, scene_text, target_text, background_image, minigame_type, minigame_config, timer_seconds, settlement_rule, random_enabled, attribute_check_rule, pass_condition, required, status) VALUES
(1, 1, '需求风暴', 'single_choice', 1, '老师突然强调：系统不能只是普通任务管理，要体现校园特色、数据库设计和选择后果。你的项目说明文档还停留在“功能列表”阶段。', '选择一个课设救场策略。', 'scene-lab', 'none', NULL, NULL, '根据策略影响后续 Bug 风险和技能收益。', 0, NULL, 'score>=40', 1, 1),
(2, 1, '数据库拼图', 'minigame', 2, '凌晨 1:17，实验室灯还亮着。用户、属性、事件、选项和副本任务之间的关系像被揉皱的草稿纸。', '用最少的混乱把核心表关系串起来。', 'scene-lab', 'db_link', 'user->player_attribute,event->event_option,dungeon->dungeon_task', 45, '根据选择的关系设计给出评分。技能高时收益更明显。', 0, 'skill>=50', 'score>=50', 1, 1),
(3, 1, 'Bug 暴走', 'key_task', 3, '控制台连续报错，DDL 还剩最后一晚。你需要判断先修什么，否则页面可能在答辩现场沉默。', '在压力和技能之间做一次关键判断。', 'scene-lab', 'bug_hunt', 'mapper,template,sql', 60, '根据修复策略结算最终评价。', 1, 'pressure<85', 'score>=50', 1, 1);

INSERT INTO dungeon_task_option (id, dungeon_task_id, option_type, option_text, is_correct, trigger_probability, result_text, evaluation, score, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_task_id, status) VALUES
(1, 1, 'strategy', '先压缩范围，只保留能演示的校园生存闭环', 1, 100, '你砍掉了花哨但来不及的内容，把课设目标收回到“地图-事件-副本”这条主线。', '优秀完成', 85, 4, 0, 0, 0, 6, -4, 5, 35, 2, 1),
(2, 1, 'strategy', '继续堆功能，看看最后能不能都跑起来', 0, 100, '功能列表变长了，但每个模块都像半成品。你感觉答辩风险正在上升。', '勉强完成', 42, 2, -2, 0, 0, 2, 8, -2, 12, 2, 1),
(3, 1, 'strategy', '先写报告，代码明天再抢救', 0, 100, '报告目录变整齐了，项目本体却还没准备好面对老师的鼠标。', '普通完成', 55, 3, 0, 0, 0, 1, 4, 1, 18, 2, 1),
(4, 2, 'minigame_choice', 'user 连接 player_attribute，event 连接 event_option，dungeon 连接 dungeon_task', 1, 100, '核心关系终于顺了，后续页面和结算都有了落点。', '结构清晰', 90, 5, 0, 0, 0, 10, -5, 6, 45, 3, 1),
(5, 2, 'minigame_choice', '所有表都先连 user，方便以后查询', 0, 100, '你得到了一个巨大中心表宇宙，查询好像方便了，但关系解释开始变得吃力。', '勉强完成', 45, 1, 0, 0, 0, 3, 6, -2, 15, 3, 1),
(6, 2, 'minigame_choice', '先不管关系，页面能显示再说', 0, 100, '页面暂时能糊出来，但后续每个功能都在问你“我的数据从哪来”。', '表关系迷雾', 30, 0, -2, 0, 0, 1, 10, -4, 8, 3, 1),
(7, 3, 'key_choice', '先看 SQL 和 Mapper，再看页面', 1, 100, '你定位到字段映射问题，控制台安静了很多。课设终于像一个项目了。', '课设战神预备役', 95, 6, -1, 0, 0, 12, -7, 5, 55, NULL, 1),
(8, 3, 'key_choice', '先改页面样式，让它看起来像能跑', 0, 100, '页面变好看了，但控制台还在提醒你不要只装修入口。', '外观抢救成功', 62, 2, -1, 0, 0, 4, 2, 1, 28, NULL, 1),
(9, 3, 'key_choice', '重启电脑，寄希望于玄学修复', 0, 100, '电脑重启了，Bug 也醒了。你获得了短暂的平静和更长的沉默。', '答辩沉默风险', 20, -3, 0, 0, 0, -2, 12, -5, 5, NULL, 1);
