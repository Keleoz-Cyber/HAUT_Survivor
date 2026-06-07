-- ============================================================
-- Content Pack 2: 考试周与 DDL 生存线
-- ============================================================

INSERT INTO event (id, event_name, event_type, location_id, description, scene_image, mood_tag, probability, min_week, max_week, min_explore_level, status) VALUES
(2001, '早八点名危机', 'academic_crisis', 1, '闹钟响了三次，你终于在距离上课还有 12 分钟时醒来。群里有人说老师今天可能点名。', 'scene-classroom', '早八预警', 70, 1, 1, 0, 1),
(2002, '选课群消息轰炸', 'academic_crisis', 1, '刚下课，课程群突然刷出几十条消息：调课、作业、实验分组同时出现。', 'scene-classroom', '消息爆炸', 55, 1, 1, 0, 1),
(2003, '寝室作息磨合', 'academic_crisis', 3, '宿舍灯还亮着，阿杰正在开黑，明天早八的你陷入沉思。', 'scene-dorm', '寝室夜谈', 55, 1, 1, 0, 1),
(2004, '图书馆座位初体验', 'academic_crisis', 2, '你第一次认真找自习座位，发现插座、空调、同桌状态都像隐藏属性。', 'scene-library', '自习试炼', 50, 1, 1, 0, 1),
(2005, '课代表发来复习范围', 'academic_crisis', 2, '课代表突然在群里发了 8 页 PDF，说老师说这些都可能考。', 'scene-library', '小测预警', 70, 2, 2, 0, 1),
(2006, '林然的复习提纲', 'academic_crisis', 2, '学霸林然把一份排版工整到离谱的复习提纲放在桌上，你忍不住多看了两眼。', 'scene-library', '学霸光环', 55, 2, 2, 0, 1),
(2007, '社团招新撞上小测', 'academic_crisis', 7, '社团摊位正热闹，手机却弹出明天小测的提醒。热闹和复习开始抢你的行动点。', 'scene-club', '时间冲突', 60, 2, 2, 0, 1),
(2008, '老郑提醒课设别拖', 'academic_crisis', 6, '实验室师兄老郑看了一眼你的进度，说：课设这种东西，最后一天才开始会变成恐怖片。', 'scene-lab', '师兄警告', 60, 2, 2, 0, 1),
(2009, '第一次课程小测', 'academic_crisis', 1, '老师把小测卷子发下来，你发现题目都很眼熟，只是你和它们不太熟。', 'scene-classroom', '随堂小测', 65, 2, 2, 0, 1),
(2010, '食堂复习搭子局', 'academic_crisis', 4, '饭点的食堂很吵，但周予说这里最适合交换情报：谁点名，谁划重点，谁作业查重。', 'scene-canteen', '饭桌情报', 45, 2, 2, 0, 1),
(2011, '组队分工失控', 'academic_crisis', 6, '课设小组群里沉默了十分钟，最后只有你发了句：那我先建个仓库？', 'scene-lab', '组队危机', 75, 3, 3, 0, 1),
(2012, 'Git 合并地狱', 'academic_crisis', 6, '你拉取队友代码后，项目出现 37 个冲突。控制台红得像期末成绩单。', 'scene-lab', '合并冲突', 85, 3, 3, 0, 1),
(2013, '数据库设计返工', 'academic_crisis', 6, '老师一句“这个表是不是太万能了”，让你意识到数据库设计可能要推倒重来。', 'scene-lab', '结构返工', 80, 3, 3, 0, 1),
(2014, '宿舍通宵赶工', 'academic_crisis', 3, '凌晨一点，寝室只剩键盘声和泡面味。你看着未完成的课设，感觉 DDL 正坐在床边。', 'scene-dorm', '通宵边缘', 75, 3, 3, 0, 1),
(2015, '图书馆抢座失败', 'academic_crisis', 2, '你到图书馆时，好位置已经被书包占领。复习计划还没开始，心态先被考验。', 'scene-library', '座位战争', 60, 3, 3, 0, 1),
(2016, 'Bug 暴走排序', 'academic_crisis', 6, 'Bug 列表越修越长，你决定先给它们排个优先级，不然今晚谁也别想睡。', 'scene-lab', 'Bug 暴走', 80, 3, 3, 0, 1),
(2017, '考前抱佛脚', 'academic_crisis', 2, '距离考试还有一晚，你终于翻开了那本像新买的一样的教材。', 'scene-library', '最后冲刺', 80, 4, 4, 0, 1),
(2018, '体测前的操场夜跑', 'academic_crisis', 5, '小马在操场挥手：来都来了，跑一圈再回去复习。你的腿和大脑同时沉默。', 'scene-track', '体测冲刺', 60, 4, 4, 0, 1),
(2019, '课程报告最后修改', 'academic_crisis', 6, '报告封面、目录、截图、参考文献同时出问题，你开始怀疑 Word 也是副本 Boss。', 'scene-lab', '报告收尾', 65, 4, 4, 0, 1),
(2020, '期末前最后一节课', 'academic_crisis', 1, '老师说“我再强调最后一次”，全班突然坐直。你知道重点来了。', 'scene-classroom', '重点捕捉', 70, 4, 4, 0, 1);

INSERT INTO event_option (id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
(5001, 2001, '直接冲刺去教室', '硬冲早八', 'medium', '你一路冲到教室，刚坐下老师就开始点名。心跳很快，但名字保住了。', 2, -1, 0, 0, 0, 3, 2, 18),
(5002, 2001, '让室友帮忙占座', '求助室友', 'low', '阿杰嘴上嫌麻烦，还是给你占了个后排。你赶到时还能喘口气。', 1, 0, 0, 1, 0, 1, 0, 12),
(5003, 2001, '继续睡，赌一手不点名', '摆烂续命', 'high', '这一觉很香，群里的点名消息也很刺眼。你获得了休息，也欠下了学业债。', -2, 2, 0, 0, 0, -1, -3, 6),
(5004, 2005, '立刻整理复习计划', '计划先行', 'medium', '你把 8 页 PDF 拆成今晚能完成的三段，恐惧变成了进度条。', 4, 0, 0, 0, 1, 2, 3, 28),
(5005, 2005, '找林然借资料', '学霸支援', 'low', '林然把重点圈给你，还提醒你第二章例题最容易变形。', 3, 0, 0, 2, 0, -1, 1, 24),
(5006, 2005, '先收藏，晚上再看', '经典收藏', 'high', '你点了收藏，心里获得一种虚假的完成感。', 1, 0, 0, 0, 0, -1, -2, 8),
(5007, 2012, '冷静逐个处理冲突', '手动解冲突', 'high', '你一个文件一个文件处理，最后项目重新启动。你像刚从火场里走出来。', 2, -1, 0, 0, 5, 4, 2, 36),
(5008, 2012, '找老郑救场', '请求支援', 'low', '老郑让你先看冲突标记，再看业务意图。十分钟后你知道该删哪半边了。', 1, 0, 0, 1, 3, -2, 1, 30),
(5009, 2012, '复制旧版本覆盖', '危险回滚', 'high', '冲突没了，新功能也没了。你获得了安静，也获得了技术债。', -2, 0, 0, 0, -2, -3, -2, 8),
(5010, 2017, '只看高频题型', '精准抱佛脚', 'medium', '你放弃全面复习，开始抓最可能出现的题。效率很高，心跳也很快。', 4, -1, 0, 0, 0, 3, 1, 28),
(5011, 2017, '跟林然一起过重点', '学霸带飞', 'low', '林然把重点讲得很清楚，你第一次觉得抱佛脚也能有章法。', 5, 0, 0, 2, 0, 1, 1, 32),
(5012, 2017, '放弃挣扎早点睡', '保存人类状态', 'medium', '你关上书，决定至少以清醒的大脑进入考场。', -2, 3, 0, 0, 0, -3, 0, 12);

INSERT INTO event_option (id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change)
SELECT 5100 + id, id, '先稳住节奏', '稳妥处理', 'low', CONCAT('你没有被', event_name, '带乱节奏，而是先抓住最重要的一件事。'), 2, 0, 0, 0, 1, 0, 1, 16
FROM event WHERE event_type = 'academic_crisis' AND id NOT IN (2001, 2005, 2012, 2017);

INSERT INTO event_option (id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change)
SELECT 5200 + id, id, '找人帮忙', '求助路线', 'medium', CONCAT('你把问题说出来后，发现', event_name, '并不一定要一个人硬扛。'), 1, 0, 0, 2, 1, -1, 0, 14
FROM event WHERE event_type = 'academic_crisis' AND id NOT IN (2001, 2005, 2012, 2017);

INSERT INTO event_option (id, event_id, option_text, preview_text, risk_level, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change)
SELECT 5300 + id, id, '先糊过去', '短期逃避', 'high', CONCAT('你暂时绕开了', event_name, '，但心里知道这件事还会回来找你。'), -1, 1, 0, 0, 0, 2, -2, 6
FROM event WHERE event_type = 'academic_crisis' AND id NOT IN (2001, 2005, 2012, 2017);

INSERT INTO dungeon (id, dungeon_name, dungeon_type, description, cover_image, theme_style, estimated_minutes, difficulty_label, reward_exp, reward_title, status) VALUES
(3, '数据库课设答辩夜', 'DDL', '答辩前夜，你要把需求、ER 图、SQL 报错和老师追问一起压进一个还能运行的系统里。', 'dungeon-db-defense', 'DDL', 10, '中等偏高', 90, '表结构守夜人', 1);

INSERT INTO dungeon_task (id, dungeon_id, task_name, task_type, task_order, scene_text, target_text, background_image, minigame_type, minigame_config, timer_seconds, settlement_rule, random_enabled, attribute_check_rule, pass_condition, required, status) VALUES
(3001, 3, '需求梳理', 'choice', 1, '老师要求系统必须有用户、任务、记录、统计四类核心数据。队友已经开始问能不能直接建表。', '先把实体和关系想清楚，避免后面返工。', 'scene-lab', 'none', NULL, NULL, 'option_score', 0, NULL, 'score>=50', 1, 1),
(3002, 3, 'ER 图连线', 'minigame', 2, '你需要把用户、任务、副本记录、属性变化连起来。每一条关系都可能决定后面 Bug 的数量。', '选择正确的数据关系。', 'scene-lab', 'db_link', 'user->player_attribute,event->event_option,dungeon->dungeon_task', 45, 'db_link_score', 0, NULL, 'score>=50', 1, 1),
(3003, 3, 'SQL 暴走', 'minigame', 3, '答辩前一晚，外键、字段名、时间类型同时报错。控制台像在开红色演唱会。', '定位最关键的 Bug，让项目重新跑起来。', 'scene-lab', 'bug_hunt', NULL, 60, 'bug_hunt_score', 1, 'skill>=50', 'score>=50', 1, 1),
(3004, 3, '答辩现场', 'choice', 4, '老师问：为什么这里要拆表？你看了一眼队友，发现大家都在看你。', '解释你的数据库设计，让答辩稳住。', 'scene-classroom', 'none', NULL, NULL, 'option_score', 0, 'academic>=55 OR skill>=55', 'score>=50', 1, 1);

INSERT INTO dungeon_task_option (id, dungeon_task_id, option_type, option_text, is_correct, trigger_probability, result_text, evaluation, score, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_task_id, status) VALUES
(7001, 3001, 'strategy', '先列实体和关系，再决定表结构', 1, 100, '你把用户、任务、记录、统计拆成清晰模块。后面的表结构终于有了骨架。', '需求控场', 90, 4, 0, 0, 0, 5, -2, 3, 35, 3002, 1),
(7002, 3001, 'strategy', '直接开始建表，边做边改', 0, 100, '你很快写出了第一版表，但字段越加越多，万能表的气息开始蔓延。', '边跑边补', 55, 1, 0, 0, 0, 2, 3, 0, 18, 3002, 1),
(7003, 3001, 'strategy', '先做页面，数据库最后再说', 0, 100, '页面看起来有了，数据从哪里来暂时没人知道。你获得了进度错觉。', '范围失控', 35, -1, 0, 0, 0, 1, 5, -2, 8, 3002, 1),
(7004, 3004, 'defense', '解释拆表是为了降低重复和方便扩展', 1, 100, '老师点了点头，又追问了一个细节。你接住了，队友终于敢呼吸。', '答辩稳定', 90, 5, 0, 0, 1, 4, -3, 2, 40, NULL, 1),
(7005, 3004, 'defense', '承认有些设计是为了 Demo 先跑起来', 1, 100, '你的回答不算完美，但足够真实。老师没有继续深挖最危险的地方。', '惊险通过', 65, 2, 0, 0, 0, 2, 1, 0, 24, NULL, 1),
(7006, 3004, 'defense', '把问题转给队友补充', 0, 100, '队友愣了一秒，你们在沉默中完成了一次无声的责任交接。', '现场摇晃', 40, 0, -1, 0, 1, -1, 5, -1, 10, NULL, 1);

INSERT INTO weekly_goal (id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, active) VALUES
(2001, 'study_twice', '复习两小时', '本周完成 2 次学业危机事件，把复习从口号变成行动。', 'academic_event', 2, 35, 'academic', 3, 1),
(2002, 'ddl_survivor', 'DDL 幸存', '本周完成 2 个副本阶段，在压力线里活下来。', 'dungeon_stage', 2, 45, 'skill', 4, 1),
(2003, 'keep_calm_exam', '稳住别炸', '本周结束时压力不超过 55。', 'pressure_keep', 55, 40, 'pressure', 6, 1),
(2004, 'ask_for_help', '会求助也是本事', '本周至少遇见 1 位 NPC，大学不是单机游戏。', 'npc_meet', 1, 30, 'social', 3, 1);

INSERT INTO achievement (id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, active) VALUES
(2001, 'early_class_warrior', '早八战士', '在早八危机里选择认真应对。', '⏰', 'academic_event', 1, '早八幸存者', 1),
(2002, 'ddl_survivor_plus', 'DDL 幸存者', '完成数据库课设答辩夜副本。', '🗄️', 'dungeon_completed_db', 1, '表结构守夜人', 1),
(2003, 'last_minute_master', '抱佛脚大师', '在第 4 周完成考前复习事件。', '📚', 'academic_event', 1, '考前冲刺型选手', 1),
(2004, 'calm_under_pressure', '高压稳定器', '高压周仍然控制住压力。', '🧘', 'pressure_keep', 1, '情绪稳定大师', 1),
(2005, 'help_seeker', '会求助的人', '通过 NPC 帮助解决学业危机。', '🤝', 'academic_event', 1, '不单打独斗', 1);

INSERT INTO rumor (id, week_number, location_id, rumor_title, rumor_text, effect_hint, rarity, active) VALUES
(2001, 1, 1, '点名雷达启动', '有人说开学第一周老师最喜欢突然点名，因为大家还没进入状态。', '教学楼事件更容易出现早八和点名压力。', 'common', 1),
(2002, 1, 3, '阿杰的开黑陷阱', '阿杰说只打一把，但熟人都知道这一把通常有复数含义。', '宿舍可能出现放松与自律的取舍。', 'common', 1),
(2003, 2, 2, '三楼靠窗复习位', '图书馆三楼靠窗位置据说复习效率特别高，前提是你抢得到。', '图书馆更适合复习类事件。', 'rare', 1),
(2004, 2, 7, '招新和小测撞车', '社团区很热闹，但课程群里的小测提醒也是真的。', '社团和学业开始争夺行动点。', 'common', 1),
(2005, 2, 6, '老郑的合并忠告', '老郑说课设不要最后一天才合并代码，他说这话时眼神很有故事。', '实验室事件可能提前提示课设风险。', 'rare', 1),
(2006, 3, 6, '控制台红色预警', '实验室今晚有人通宵改 Bug，控制台红得照亮了半张脸。', '第 3 周实验室 DDL 事件概率提高。', 'common', 1),
(2007, 3, 3, '泡面救不了所有 Bug', '宿舍里泡面味越浓，说明大家离 DDL 越近。', '宿舍可能出现通宵赶工事件。', 'common', 1),
(2008, 3, 2, '座位战争升级', '图书馆的书包占座行为进入期末前形态。', '图书馆可能触发抢座和复习冲突。', 'common', 1),
(2009, 4, 2, '林然的最后重点', '林然整理了一份考前重点，但据说只会发给认真问的人。', '第 4 周图书馆适合抱佛脚。', 'rare', 1),
(2010, 4, 5, '体测队伍会变短', '有人说下午晚一点操场排队会短，但那时候腿也更不想动。', '操场体测事件出现。', 'common', 1),
(2011, 4, 6, '答辩老师爱问拆表', '数据库老师最近很爱问：为什么这里要拆表？', '数据库课设答辩夜需要解释能力。', 'rare', 1),
(2012, 4, 4, '热汤回血传说', '食堂二楼的热汤能救一半熬夜灵魂，另一半要靠睡觉。', '食堂仍然是降压和回血地点。', 'common', 1);
