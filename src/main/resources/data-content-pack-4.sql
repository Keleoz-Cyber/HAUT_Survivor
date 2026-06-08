-- ============================================================
-- Content Pack 4: 传闻、周主题与探索奇遇机制化
-- ============================================================

INSERT INTO rumor
(id, week_number, location_id, rumor_title, rumor_text, effect_hint, effect_type, effect_value, effect_target, rarity, active) VALUES
(4001, 1, 2, '图书馆二楼突然空了一排座', '听说图书馆二楼靠窗位置今天没人抢，适合悄悄开始学习。', '图书馆探索度更容易增加', 'explore_bonus', 2, 'explore', 'common', 1),
(4002, 1, 3, '宿舍今晚可能晚点断电', '宿舍群里有人说今晚断电会推迟，虽然消息来源是"隔壁说的"。', '宿舍压力风险降低', 'safe_zone', 1, 'pressure', 'rare', 1),
(4003, 1, 4, '食堂新窗口排队很短', '三楼新窗口刚开，人还不多，拼桌聊天机会不少。', '食堂更容易遇见 NPC', 'npc_boost', 10, 'npc', 'common', 1),
(4004, 1, 1, '教学楼有人问路', '新生还在到处找教室，主动帮忙能混个脸熟。', '教学楼社交收益提高', 'attr_bonus', 1, 'social', 'common', 1),

(4005, 2, 7, '社团区今晚有联合招新', '几个社团临时决定一起摆摊，路过的人都会被热情拦住。', '社团区社交收益提高', 'attr_bonus', 2, 'social', 'common', 1),
(4006, 2, 8, '篮球社夜训缺人', '篮球社今晚训练缺人，去操场可能被拉去凑队。', '操场健康收益提高', 'attr_bonus', 2, 'health', 'common', 1),
(4007, 2, 6, '实验室开放日', '老郑说今晚实验室门没锁，想看项目结构可以来。', '实验室技能收益提高', 'attr_bonus', 2, 'skill', 'rare', 1),
(4008, 2, 4, '食堂拼桌情报局', '有人在食堂聊社团报名和课设队友，消息很杂但可能有用。', '食堂传闻效果更明显', 'npc_boost', 10, 'npc', 'rare', 1),

(4009, 3, 6, '老郑的白板还没擦', '实验室白板上还有昨晚留下的报错分析，像一张救命地图。', '实验室技能收益提高', 'attr_bonus', 2, 'skill', 'rare', 1),
(4010, 3, 2, '图书馆今晚有人通宵', '有人说图书馆今晚气氛像战场，坐下就不好意思摸鱼。', '图书馆学业收益提高但压力上升', 'attr_bonus', 2, 'academic', 'common', 1),
(4011, 3, 3, '宿舍开黑局变赶工局', '阿杰说今晚不打了，大家都在救自己的 DDL。', '宿舍压力风险降低', 'safe_zone', 1, 'pressure', 'common', 1),
(4012, 3, 1, '教学楼小测风声', '课代表突然问大家复习没，空气里有小测的味道。', '教学楼事件更偏学业', 'event_hint', 1, 'academic', 'common', 1),

(4013, 4, 8, '操场今晚有人组体测冲刺', '小马说最后一周再不练就只能靠玄学。', '操场健康收益提高', 'attr_bonus', 3, 'health', 'rare', 1),
(4014, 4, 2, '考前资料在图书馆流动', '据说有人整理了考试重点，但只在图书馆附近传。', '图书馆学业收益提高', 'attr_bonus', 2, 'academic', 'common', 1),
(4015, 4, 4, '食堂补给窗口加量', '期末周食堂某窗口突然多给一点，像在给学生续命。', '食堂健康收益提高', 'attr_bonus', 1, 'health', 'common', 1),
(4016, 4, 3, '宿舍早睡联盟成立', '隔壁寝室决定一起早睡，虽然听起来很不现实。', '宿舍自律收益提高', 'attr_bonus', 2, 'discipline', 'rare', 1);

INSERT INTO exploration_story_chain
(id, chain_key, chain_name, location_id, week_number, required_explore_level, step_number, scenario_text, result_text,
 academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_step_number, active) VALUES
(4001, 'library_seat', '被占的自习座', 2, 0, 10, 1, '你发现图书馆靠窗有个座位总被书包占着，但人一直没出现。', '你记住了这个座位，也开始观察图书馆真正的座位生态。', 2, 0, 0, 0, 0, 1, 1, 18, 2, 1),
(4002, 'library_seat', '被占的自习座', 2, 0, 10, 2, '你终于遇见了座位主人，对方也在赶 DDL。', '你们短暂交流了一下复习范围，发现彼此都很惨。', 3, 0, 0, 1, 0, 1, 2, 24, 3, 1),
(4003, 'library_seat', '被占的自习座', 2, 0, 10, 3, '你们形成了"谁先到谁占座"的默契。', '图书馆突然多了一个固定学习角，学业节奏稳了一点。', 5, 0, 0, 2, 0, -1, 3, 35, NULL, 1),

(4004, 'lab_whiteboard', '白板上的神秘报错', 6, 0, 20, 1, '实验室白板上留下了一串报错和箭头。', '你看不全懂，但至少知道这不是普通报错。', 0, 0, 0, 0, 3, 2, 1, 20, 2, 1),
(4005, 'lab_whiteboard', '白板上的神秘报错', 6, 0, 20, 2, '你根据白板线索定位到一个项目结构问题。', '你第一次觉得错误日志像线索，不只是精神攻击。', 1, 0, 0, 0, 5, 2, 2, 30, 3, 1),
(4006, 'lab_whiteboard', '白板上的神秘报错', 6, 0, 20, 3, '老郑发现你看懂了白板，开始把你当半个自己人。', '师兄点了点头，这种认可比一杯咖啡还提神。', 2, 0, 0, 1, 6, 1, 2, 40, NULL, 1),

(4007, 'track_night_run', '夜跑打卡局', 8, 0, 10, 1, '你看到一群人在操场边喊"今天第三天"。', '你被气氛带着走了一圈，身体比脑子先醒了。', 0, 4, 0, 1, 0, -2, 1, 18, 2, 1),
(4008, 'track_night_run', '夜跑打卡局', 8, 0, 10, 2, '小马邀请你一起跑一圈，说跑完再焦虑也不迟。', '你跑得不快，但压力真的松了一点。', 0, 5, 0, 2, 0, -3, 2, 28, 3, 1),
(4009, 'track_night_run', '夜跑打卡局', 8, 0, 10, 3, '你开始把夜跑当成减压手段。', '操场从路过的地方变成了你的回血点。', 0, 7, 0, 2, 0, -4, 3, 38, NULL, 1),

(4010, 'canteen_gossip', '拼桌情报局', 4, 0, 0, 1, '你被迫和陌生同学拼桌。', '对方聊起社团和课设队友，消息杂但有用。', 0, 0, 0, 3, 0, -1, 0, 18, 2, 1),
(4011, 'canteen_gossip', '拼桌情报局', 4, 0, 0, 2, '你听到一个关于社团和课程安排的真实情报。', '你发现食堂不只是吃饭的地方，也是校园信息中转站。', 1, 0, 0, 5, 1, 0, 1, 30, NULL, 1),

(4012, 'dorm_lights_out', '熄灯后的寝室会议', 3, 0, 0, 1, '熄灯后大家开始聊最近谁最惨。', '你发现不是只有自己在硬撑，压力稍微散了一点。', 0, 1, 0, 3, 0, -4, -1, 18, 2, 1),
(4013, 'dorm_lights_out', '熄灯后的寝室会议', 3, 0, 0, 2, '阿杰提出一个离谱但有用的减压方案。', '方案不一定科学，但寝室笑成一团，至少今晚没那么窒息。', 0, 1, 0, 4, 0, -5, -1, 30, NULL, 1);

INSERT INTO weekly_goal
(id, goal_key, goal_name, description, goal_type, target_value, reward_exp, reward_attribute, reward_amount, active) VALUES
(4001, 'rumor_hunter', '情报猎人', '本周触发 2 次传闻效果。', 'rumor_effect_used', 2, 35, 'social', 2, 1),
(4002, 'story_chaser', '校园奇遇追踪者', '本周推进 2 次探索奇遇。', 'exploration_story_step', 2, 40, 'skill', 2, 1),
(4003, 'theme_survivor', '顺势而为', '本周触发 2 次周主题修正。', 'weekly_modifier_used', 2, 30, 'discipline', 2, 1),
(4004, 'buddy_rescue', '搭子救场', '本周触发 1 次搭子外溢加成。', 'buddy_assist', 1, 35, 'pressure', 3, 1);

INSERT INTO achievement
(id, achievement_key, achievement_name, description, icon, condition_type, condition_value, reward_title, active) VALUES
(4001, 'first_rumor_effect', '听劝一次', '第一次让校园传闻真正帮上忙。', '📰', 'rumor_effect_used', 1, '情报新生', 1),
(4002, 'story_first_step', '奇遇开端', '第一次触发探索奇遇链。', '🧩', 'exploration_story_step', 1, '校园目击者', 1),
(4003, 'story_completed', '有始有终', '完成 1 条探索奇遇链。', '✅', 'exploration_story_completed', 1, '支线清理大师', 1),
(4004, 'theme_master', '看懂周节奏', '第一次触发周主题修正。', '📆', 'weekly_modifier_used', 1, '节奏感选手', 1),
(4005, 'buddy_saved_me', '搭子救我', '触发 1 次本周搭子外溢加成。', '🤝', 'buddy_assist', 1, '有人罩着', 1);
