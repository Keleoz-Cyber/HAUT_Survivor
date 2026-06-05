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

-- ============================================================
-- 事件池：每个地点 3-4 个事件，按概率权重随机触发
-- ============================================================

INSERT INTO `event` (id, event_name, event_type, location_id, description, probability, min_week, max_week, status) VALUES
-- 教学楼 (4 events)
(1, '早八点名危机', '学习', 1, '你昨晚赶报告睡得很晚，醒来发现距离早八上课只剩 15 分钟。', 80, 1, 20, 1),
(2, '课堂突然提问', '学习', 1, '老师讲到关键知识点时突然看向你，似乎准备让你回答问题。', 60, 1, 20, 1),
(13, '课堂传纸条', '社交', 1, '上课时后排同学给你传了一张纸条，上面写着问答题的答案暗示。老师正在黑板前转身。', 40, 1, 20, 1),
(14, '期末突击复习', '学习', 1, '距离期末考试只剩三天，你翻开课本发现很多章节还是全新的。', 55, 12, 20, 1),
-- 图书馆 (3 events)
(3, '图书馆抢座', '学习', 2, '期末周的图书馆座位很紧张，你到达时只剩一个角落位置。', 70, 1, 20, 1),
(15, '图书馆低电量', '学习', 2, '你正在赶报告，笔记本电量突然跳到 8%，充电器在宿舍。', 50, 1, 20, 1),
(16, '偶遇学长学姐', '社交', 2, '你在书架间遇到一位看起来很厉害的学长，他正在翻阅考研资料。', 45, 1, 20, 1),
-- 宿舍 (4 events)
(4, '宿舍 DDL 突袭', '学习', 3, '你打开学习平台，发现课程作业截止时间比记忆中更早。', 75, 1, 20, 1),
(11, '生活费余额不足', '金钱', 3, '你看了一眼余额，发现本月生活费已经接近见底。', 50, 1, 20, 1),
(17, '室友开黑邀请', '社交', 3, '室友喊你一起打排位，说就打两把，但你今晚还有 DDL 要赶。', 60, 1, 20, 1),
(18, '断网危机', '生活', 3, '宿舍突然断网，你的课设代码还没推到远程仓库。', 35, 1, 20, 1),
-- 食堂 (3 events)
(5, '食堂夜宵诱惑', '健康', 4, '晚上十点，你路过食堂附近，闻到了夜宵的香味。', 65, 1, 20, 1),
(12, '粮食守护者挑战', '特色', 4, '食堂餐盘回收处还有不少剩饭，系统向你发起节粮挑战。', 45, 1, 20, 1),
(19, '新品试吃活动', '健康', 4, '食堂窗口贴出海报：新菜品免费试吃，限量 50 份。', 45, 1, 20, 1),
-- 操场 (3 events)
(6, '体测通知', '健康', 5, '班级群突然通知本周体测，你意识到自己已经很久没有运动。', 70, 1, 16, 1),
(20, '晨跑邀约', '健康', 5, '室友定好了六点半的闹钟，邀请你一起去操场跑步。', 50, 1, 20, 1),
(21, '运动会报名', '社交', 5, '院系运动会开始报名，班级群里大家都在讨论参加什么项目。', 40, 1, 8, 1),
-- 实验室 (4 events)
(7, 'Java 代码报错', '技能', 6, '你正在写 Java 课设，控制台突然出现一长串红色报错。', 80, 1, 20, 1),
(8, '实验数据异常', '技能', 6, '实验记录中有一组数据明显不合理，报告今晚就要提交。', 50, 1, 20, 1),
(22, 'Git 合并冲突', '技能', 6, '你 git pull 之后，五个文件同时出现冲突标记，控制台一片红。', 55, 1, 20, 1),
-- 社团活动区 (3 events)
(9, '社团招新', '社交', 7, '社团活动区正在招新，学长学姐热情邀请你加入。', 55, 1, 8, 1),
(23, '社团换届竞选', '社交', 7, '你所在的社团正在换届，有人推荐你竞选部长。', 40, 1, 20, 1),
(24, '志愿服务招募', '社交', 7, '校青协在招募周末志愿服务，时长可以换综测加分。', 45, 1, 20, 1),
-- 快递站 (3 events)
(10, '快递到了但下雨', '生活', 8, '你收到快递到站短信，但窗外正下着雨。', 60, 1, 20, 1),
(25, '快递被代拿', '生活', 8, '你的快递显示已签收，但你根本没有去取。签名不是你的。', 35, 1, 20, 1),
(26, '囤货决策', '金钱', 8, '购物节到了，购物车里塞满了东西，你需要决定买多少。', 50, 1, 20, 1);

-- ============================================================
-- 事件选项：每个事件 2-3 个选项，体现取舍
-- ============================================================

INSERT INTO event_option (id, event_id, option_text, result_text, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change) VALUES
-- 1: 早八点名危机
(1, 1, '立刻起床冲向教学楼', '你成功赶到教室，但整个人还没完全清醒。', 5, -4, 0, 0, 0, 3, 2, 20),
(2, 1, '请室友帮忙确认情况', '室友提醒你老师已经到教室，你赶紧出发。', 2, -1, 0, 2, 0, 1, 1, 12),
(3, 1, '继续睡觉', '你睡得很香，但点名记录不太友好。', -8, 5, 0, 0, 0, 4, -6, 5),
-- 2: 课堂突然提问
(4, 2, '根据课前预习回答', '你答得不算完美，但老师认可了你的思路。', 5, 0, 0, 0, 1, -1, 2, 18),
(5, 2, '诚实说自己还没想清楚', '老师让你课后再补充，你记下了这个知识点。', 1, 0, 0, 0, 0, 2, 1, 8),
(6, 2, '低头假装记笔记', '老师没有继续追问，但你错过了一次表现机会。', -2, 0, 0, 0, 0, 1, -2, 3),
-- 3: 图书馆抢座
(7, 3, '立刻坐下开始复习', '角落很安静，你进入了不错的学习状态。', 8, 0, 0, 0, 1, -2, 4, 24),
(8, 3, '去别处找更舒服的位置', '你花了些时间，但找到了更适合自己的学习环境。', 3, 2, 0, 0, 0, -1, 1, 14),
(9, 3, '放弃自习回宿舍', '你短暂放松了，但复习计划被迫后移。', -5, 2, 0, 0, 0, 4, -4, 5),
-- 4: 宿舍 DDL 突袭
(10, 4, '马上拆分任务开始写', '你把作业拆成小块，终于稳住了节奏。', 6, -2, 0, 0, 2, -2, 5, 26),
(11, 4, '先问同学确认要求', '你避免了理解偏差，但留给自己的时间更少。', 3, 0, 0, 3, 0, 1, 2, 16),
(12, 4, '先刷一会儿短视频缓缓', '压力暂时下降，但 DDL 没有因此变远。', -4, 0, 0, 0, 0, 5, -5, 3),
-- 5: 食堂夜宵诱惑
(13, 5, '买一份犒劳自己', '夜宵带来快乐，也带走了一点预算和健康。', 0, -3, -8, 0, 0, -5, -1, 10),
(14, 5, '忍住回宿舍喝水', '你守住了作息和预算，自律值上升。', 0, 2, 0, 0, 0, 1, 6, 18),
(15, 5, '顺便给室友带一份', '室友很开心，你的钱包不太开心。', 0, -2, -15, 5, 0, -3, 0, 14),
-- 6: 体测通知
(16, 6, '今天就开始跑步训练', '第一天很累，但身体状态开始回升。', 0, 8, 0, 0, 0, -2, 4, 24),
(17, 6, '制定一周训练计划', '计划让你更安心，关键还要看执行。', 0, 3, 0, 0, 0, -1, 5, 18),
(18, 6, '祈祷体测延期', '你暂时逃避了现实，但现实还在操场等你。', 0, -2, 0, 0, 0, 4, -4, 3),
-- 7: Java 代码报错
(19, 7, '冷静阅读报错信息', '你定位到了问题，技能值明显提升。', 0, 0, 0, 0, 8, -3, 5, 30),
(20, 7, '复制报错去搜索', '你找到了解法，但还没完全理解原因。', 0, 0, 0, 0, 4, -2, 1, 18),
(21, 7, '关掉电脑明天再说', '压力暂时下降，项目进度也一起下降。', -2, 0, 0, 0, -2, -5, -6, 5),
-- 8: 实验数据异常
(22, 8, '检查实验步骤和原始记录', '你发现了记录偏差，报告可信度提升。', 4, 0, 0, 0, 5, -2, 4, 24),
(23, 8, '向同学请教数据处理方式', '交流帮你打开思路，也补上了一个细节。', 2, 0, 0, 4, 3, -1, 1, 18),
(24, 8, '直接忽略异常值', '报告暂时能写下去，但风险被埋下了。', -2, 0, 0, 0, -3, 3, -3, 5),
-- 9: 社团招新
(25, 9, '加入一个感兴趣的社团', '你认识了新朋友，校园生活更丰富。', 0, 0, 0, 8, 0, -2, 1, 20),
(26, 9, '先了解活动时间再决定', '你避免了时间冲突，也保留了选择空间。', 0, 0, 0, 3, 0, -1, 2, 12),
(27, 9, '绕开人群回宿舍', '你保持了安静，但错过了一次社交机会。', 0, 0, 0, -4, 0, -1, 0, 4),
-- 10: 快递到了但下雨
(28, 10, '撑伞去取快递', '你成功取回快递，但鞋子湿了。', 0, -1, 0, 0, 0, 1, 2, 12),
(29, 10, '等雨小一点再去', '你做了更稳妥的安排，没有打乱节奏。', 0, 0, 0, 0, 0, -1, 2, 10),
(30, 10, '拜托同学顺路带回', '同学帮了你一次，人情账也记上了。', 0, 0, 0, 3, 0, -1, 0, 10),
-- 11: 生活费余额不足
(31, 11, '制定剩余预算计划', '你重新掌控了消费节奏。', 0, 0, 8, 0, 0, -2, 5, 22),
(32, 11, '减少非必要消费', '你开始精打细算，钱包稍微安全了一点。', 0, 0, 5, 0, 0, 1, 3, 16),
(33, 11, '假装没看到余额', '快乐没有持续太久，月底压力正在逼近。', 0, 0, -8, 0, 0, 5, -4, 3),
-- 12: 粮食守护者挑战
(34, 12, '按需取餐并完成节粮打卡', '你完成了节粮挑战，也更理解粮食来之不易。', 2, 0, 0, 0, 3, -1, 6, 24),
(35, 12, '发布节粮倡议', '你的倡议被同学看到，影响力小小扩散。', 1, 0, 0, 5, 2, 0, 4, 20),
(36, 12, '视而不见', '你没有额外行动，系统默默记下了这次选择。', 0, 0, 0, 0, 0, 0, -1, 2),
-- 13: 课堂传纸条
(37, 13, '偷偷回应纸条', '老师没发现，但你的注意力被分散了。', -2, 0, 0, 3, 0, 1, -2, 10),
(38, 13, '收起纸条继续听讲', '你专注于课堂，知识在脑子里更清晰了。', 5, 0, 0, -1, 0, -1, 4, 18),
(39, 13, '举手举报', '课堂恢复了秩序，但你的社交关系有点微妙。', 2, 0, 0, -5, 0, 3, 3, 6),
-- 14: 期末突击复习
(40, 14, '系统梳理重点章节', '你用最笨的办法，把核心知识点过了一遍。', 7, -3, 0, 0, 1, 2, 4, 28),
(41, 14, '找学霸借笔记', '学霸的笔记条理清晰，你补上了很多盲区。', 4, 0, 0, 4, 0, 1, 1, 20),
(42, 14, '直接背往年题', '往年题覆盖了一部分考点，但你的底气不太够。', 2, 0, 0, 0, 0, 5, -2, 12),
-- 15: 图书馆低电量
(43, 15, '找插座继续学', '你在墙角找到插座，报告总算赶出来了。', 4, 0, 0, 0, 0, 2, 2, 16),
(44, 15, '收拾东西回宿舍', '断了学习状态，但回去充好电再说。', -2, 2, 0, 0, 0, -1, -1, 8),
(45, 15, '借同学充电宝', '同学帮了忙，你的报告保住了。', 2, 0, 0, 3, 0, -1, 1, 14),
-- 16: 偶遇学长学姐
(46, 16, '请教学习和项目经验', '学长分享了实用建议，你收获很大。', 2, 0, 0, 4, 3, -1, 1, 22),
(47, 16, '打招呼后继续自习', '你保持了礼貌和学习节奏两不误。', 2, 0, 0, 1, 0, 0, 2, 12),
(48, 16, '装没看见', '你避免了社交，但错过了一条可能有用的信息。', 0, 0, 0, -2, 0, 1, -1, 3),
-- 17: 室友开黑邀请
(49, 17, '一起打两把', '确实放松了，但时间一晃就过去了。', -2, 0, 0, 5, 0, -3, -4, 12),
(50, 17, '约定打完再做任务', '你做了折中，室友也算满意。', 0, 0, 0, 3, 0, 2, 1, 14),
(51, 17, '拒绝，继续赶 DDL', 'DDL 保住了，但室友略有点失落。', 3, 0, 0, -2, 2, 3, 5, 20),
-- 18: 断网危机
(52, 18, '用手机热点续命', '流量花了不少，但代码推上去了。', 0, 0, -6, 0, 2, 1, 2, 14),
(53, 18, '去图书馆蹭网', '换个环境反而效率更高了。', 2, -1, 0, 0, 1, -1, 2, 16),
(54, 18, '干脆休息', '断网反而让你早睡了，明天再说。', 0, 4, 0, 0, 0, -3, 1, 10),
-- 19: 新品试吃活动
(55, 19, '去尝一尝', '新菜品味道不错，你和食堂阿姨多聊了两句。', 0, 2, -5, 3, 0, -1, 0, 12),
(56, 19, '还是吃熟悉的', '稳扎稳打，至少不会踩雷。', 0, 1, 0, 0, 0, 0, 1, 8),
(57, 19, '看了评价再决定', '你查了评价，发现好评居多，决定下次再试。', 0, 0, 0, 0, 0, -1, 3, 10),
-- 20: 晨跑邀约
(58, 20, '起来跑步', '清晨的空气和运动让你一整天状态都不错。', 0, 6, 0, 0, 0, -3, 5, 22),
(59, 20, '答应了但睡过头', '室友白等了你十分钟，你有点不好意思。', 0, 0, 0, -2, 0, 2, -3, 5),
(60, 20, '委婉拒绝', '你多睡了一会儿，但运动计划又推迟了。', 0, -1, 0, -1, 0, 1, -1, 4),
-- 21: 运动会报名
(61, 21, '报名参加比赛项目', '你在赛场上全力以赴，同学们都在加油。', 0, 4, 0, 5, 0, -1, 3, 24),
(62, 21, '报名当志愿者', '你帮忙组织检录，认识了几个隔壁班的同学。', 0, 1, 0, 6, 0, 0, 3, 20),
(63, 21, '不报名', '你选择在场外围观，也挺好的。', 0, 0, 0, -2, 0, 0, 0, 3),
-- 22: Git 合并冲突
(64, 22, '冷静分析冲突文件', '你逐个解决了冲突，对代码结构理解更深了。', 0, 0, 0, 0, 6, 2, 3, 26),
(65, 22, '问同学帮忙解决', '同学帮你梳理了冲突，你也学到了方法。', 0, 0, 0, 4, 3, -1, 1, 18),
(66, 22, '直接覆盖重来', '冲突没了，但你也丢了一些之前的改动。', -2, 0, 0, 0, -3, 5, -3, 5),
-- 23: 社团换届竞选
(67, 23, '参与竞选', '你上台演讲，虽然紧张但赢得了认可。', 0, -1, 0, 7, 0, 3, 3, 24),
(68, 23, '帮忙组织换届活动', '你在幕后做了很多工作，组织能力明显提升。', 0, 0, 0, 4, 2, 1, 2, 18),
(69, 23, '在一旁围观', '换届跟你关系不大，你继续忙自己的事。', 0, 0, 0, -1, 0, 0, 0, 4),
-- 24: 志愿服务招募
(70, 24, '报名参加志愿服务', '你度过了充实的一天，也拿到了综测加分。', 1, -1, 0, 5, 0, -1, 4, 22),
(71, 24, '先看看时间安排', '你理性地评估了一下，没有冲动报名。', 0, 0, 0, 1, 0, -1, 2, 10),
(72, 24, '不感兴趣', '你省下了时间，但也没什么额外收获。', 0, 0, 0, -1, 0, 0, -1, 3),
-- 25: 快递被代拿
(73, 25, '联系代拿人取回', '你找到了快递，但跑了一趟。', 0, -1, 0, 2, 0, 2, 1, 12),
(74, 25, '找快递站投诉', '快递站帮你查到了代拿记录。', 0, 0, 0, -1, 0, 3, 3, 10),
(75, 25, '算了重新买一个', '你省了麻烦，但钱包又瘦了一圈。', 0, 0, -10, 0, 0, 2, -2, 3),
-- 26: 囤货决策
(76, 26, '列清单按需购买', '你只买了确实需要的东西，钱包还算安全。', 0, 0, -5, 0, 0, -1, 4, 18),
(77, 26, '跟着满减活动走', '你买了一大堆，短期不缺东西但预算超标了。', 0, 0, -15, 2, 0, 1, -2, 10),
(78, 26, '不参与购物节', '你保持了理性消费，省下的钱够吃一周食堂。', 0, 0, 5, 0, 0, -2, 4, 14);

-- ============================================================
-- 地点视觉配置
-- ============================================================

UPDATE campus_location SET icon_key = 'building', background_image = 'scene-classroom', theme_color = '#2563eb' WHERE id = 1;
UPDATE campus_location SET icon_key = 'book-open', background_image = 'scene-library', theme_color = '#0f766e' WHERE id = 2;
UPDATE campus_location SET icon_key = 'bed', background_image = 'scene-dorm', theme_color = '#9333ea' WHERE id = 3;
UPDATE campus_location SET icon_key = 'utensils', background_image = 'scene-canteen', theme_color = '#dc2626' WHERE id = 4;
UPDATE campus_location SET icon_key = 'activity', background_image = 'scene-track', theme_color = '#16a34a' WHERE id = 5;
UPDATE campus_location SET icon_key = 'code', background_image = 'scene-lab', theme_color = '#7c3aed' WHERE id = 6;
UPDATE campus_location SET icon_key = 'users', background_image = 'scene-club', theme_color = '#ea580c' WHERE id = 7;
UPDATE campus_location SET icon_key = 'package', background_image = 'scene-package', theme_color = '#0891b2' WHERE id = 8;

-- ============================================================
-- 事件氛围配置
-- ============================================================

UPDATE `event` SET scene_image = 'scene-classroom', mood_tag = '危机' WHERE id IN (1, 2);
UPDATE `event` SET scene_image = 'scene-classroom', mood_tag = '课堂' WHERE id = 13;
UPDATE `event` SET scene_image = 'scene-classroom', mood_tag = '突击' WHERE id = 14;
UPDATE `event` SET scene_image = 'scene-library', mood_tag = '紧张' WHERE id = 3;
UPDATE `event` SET scene_image = 'scene-library', mood_tag = '紧迫' WHERE id = 15;
UPDATE `event` SET scene_image = 'scene-library', mood_tag = '偶遇' WHERE id = 16;
UPDATE `event` SET scene_image = 'scene-dorm', mood_tag = 'DDL' WHERE id IN (4, 11);
UPDATE `event` SET scene_image = 'scene-dorm', mood_tag = '社交' WHERE id = 17;
UPDATE `event` SET scene_image = 'scene-dorm', mood_tag = '断网' WHERE id = 18;
UPDATE `event` SET scene_image = 'scene-canteen', mood_tag = '诱惑' WHERE id IN (5, 12);
UPDATE `event` SET scene_image = 'scene-canteen', mood_tag = '新品' WHERE id = 19;
UPDATE `event` SET scene_image = 'scene-track', mood_tag = '挑战' WHERE id = 6;
UPDATE `event` SET scene_image = 'scene-track', mood_tag = '晨跑' WHERE id = 20;
UPDATE `event` SET scene_image = 'scene-track', mood_tag = '竞技' WHERE id = 21;
UPDATE `event` SET scene_image = 'scene-lab', mood_tag = '调试' WHERE id IN (7, 8);
UPDATE `event` SET scene_image = 'scene-lab', mood_tag = '冲突' WHERE id = 22;
UPDATE `event` SET scene_image = 'scene-club', mood_tag = '社交' WHERE id = 9;
UPDATE `event` SET scene_image = 'scene-club', mood_tag = '竞选' WHERE id = 23;
UPDATE `event` SET scene_image = 'scene-club', mood_tag = '公益' WHERE id = 24;
UPDATE `event` SET scene_image = 'scene-package', mood_tag = '生活支线' WHERE id = 10;
UPDATE `event` SET scene_image = 'scene-package', mood_tag = '意外' WHERE id = 25;
UPDATE `event` SET scene_image = 'scene-package', mood_tag = '消费' WHERE id = 26;

-- ============================================================
-- 选项风险等级和预览文案
-- ============================================================

UPDATE event_option SET preview_text = '高压冲刺，保住学业', risk_level = 'medium' WHERE id = 1;
UPDATE event_option SET preview_text = '依赖社交，风险较低', risk_level = 'low' WHERE id = 2;
UPDATE event_option SET preview_text = '短期舒服，长期吃亏', risk_level = 'high' WHERE id = 3;
UPDATE event_option SET preview_text = '稳妥定位，技能收益高', risk_level = 'low' WHERE id = 19;
UPDATE event_option SET preview_text = '快但理解有限', risk_level = 'medium' WHERE id = 20;
UPDATE event_option SET preview_text = '压力下降，进度坠落', risk_level = 'high' WHERE id = 21;
-- 13: 课堂传纸条
UPDATE event_option SET preview_text = '冒险分心', risk_level = 'medium' WHERE id = 37;
UPDATE event_option SET preview_text = '专注课堂，稳定收益', risk_level = 'low' WHERE id = 38;
UPDATE event_option SET preview_text = '正义但得罪人', risk_level = 'high' WHERE id = 39;
-- 14: 期末突击复习
UPDATE event_option SET preview_text = '笨办法但扎实', risk_level = 'low' WHERE id = 40;
UPDATE event_option SET preview_text = '借力社交', risk_level = 'medium' WHERE id = 41;
UPDATE event_option SET preview_text = '投机取巧', risk_level = 'high' WHERE id = 42;
-- 17: 室友开黑邀请
UPDATE event_option SET preview_text = '放松但时间没了', risk_level = 'high' WHERE id = 49;
UPDATE event_option SET preview_text = '折中方案', risk_level = 'medium' WHERE id = 50;
UPDATE event_option SET preview_text = 'DDL 保住，社交下降', risk_level = 'low' WHERE id = 51;
-- 22: Git 合并冲突
UPDATE event_option SET preview_text = '踏实但费脑', risk_level = 'low' WHERE id = 64;
UPDATE event_option SET preview_text = '求助社交', risk_level = 'medium' WHERE id = 65;
UPDATE event_option SET preview_text = '粗暴但损失代码', risk_level = 'high' WHERE id = 66;

-- ============================================================
-- 副本种子数据：Java 课设：DDL 前夜
-- ============================================================

INSERT INTO dungeon (id, dungeon_name, dungeon_type, description, cover_image, theme_style, estimated_minutes, difficulty_label, reward_exp, reward_title, status) VALUES
(1, 'Java 课设：DDL 前夜', '课设', '答辩前夜，系统还差关键设计、数据库关系和 Bug 修复。你需要在压力爆表前让项目活下来。', 'scene-lab', 'DDL', 8, '普通', 180, 'DDL 幸存者', 1);

INSERT INTO dungeon_task (id, dungeon_id, task_name, task_type, task_order, scene_text, target_text, background_image, minigame_type, minigame_config, timer_seconds, settlement_rule, random_enabled, attribute_check_rule, pass_condition, required, status) VALUES
(1, 1, '需求风暴', 'single_choice', 1, '老师突然强调：系统不能只是普通任务管理，要体现校园特色、数据库设计和选择后果。你的项目说明文档还停留在"功能列表"阶段。', '选择一个课设救场策略。', 'scene-lab', 'none', NULL, NULL, '根据策略影响后续 Bug 风险和技能收益。', 0, NULL, 'score>=40', 1, 1),
(2, 1, '数据库拼图', 'minigame', 2, '凌晨 1:17，实验室灯还亮着。用户、属性、事件、选项和副本任务之间的关系像被揉皱的草稿纸。', '用最少的混乱把核心表关系串起来。', 'scene-lab', 'db_link', 'user->player_attribute,event->event_option,dungeon->dungeon_task', 45, '根据选择的关系设计给出评分。技能高时收益更明显。', 0, 'skill>=50', 'score>=50', 1, 1),
(3, 1, 'Bug 暴走', 'minigame', 3, '控制台连续报错，DDL 还剩最后一晚。你需要定位每个 Bug 的真正原因，否则页面可能在答辩现场沉默。', '阅读 Bug 现象，选择最可能的原因。', 'scene-lab', 'bug_hunt', 'symptom->cause', 60, '每题答对加分，技能和前序标签影响最终得分。', 1, 'pressure<85', 'score>=50', 1, 1);

INSERT INTO dungeon_task_option (id, dungeon_task_id, option_type, option_text, is_correct, trigger_probability, result_text, evaluation, score, academic_change, health_change, money_change, social_change, skill_change, pressure_change, discipline_change, exp_change, next_task_id, status) VALUES
(1, 1, 'strategy', '先压缩范围，只保留能演示的校园生存闭环', 1, 100, '你砍掉了花哨但来不及的内容，把课设目标收回到"地图-事件-副本"这条主线。', '优秀完成', 85, 4, 0, 0, 0, 6, -4, 5, 35, 2, 1),
(2, 1, 'strategy', '继续堆功能，看看最后能不能都跑起来', 0, 100, '功能列表变长了，但每个模块都像半成品。你感觉答辩风险正在上升。', '勉强完成', 42, 2, -2, 0, 0, 2, 8, -2, 12, 2, 1),
(3, 1, 'strategy', '先写报告，代码明天再抢救', 0, 100, '报告目录变整齐了，项目本体却还没准备好面对老师的鼠标。', '普通完成', 55, 3, 0, 0, 0, 1, 4, 1, 18, 2, 1);

-- 组织种子数据
INSERT INTO organization (id, org_name, org_type, description, unlock_location_id, unlock_explore_level, recommended_attribute, weekly_ap_cost, theme_color) VALUES
(1, '学生会', '学生会', '校园活动的组织者和协调者。加入学生会能快速积累社交和声望，但活动会占用大量时间。', 7, 20, '社交', 1, '#f59e0b'),
(2, '实验室项目组', '实验室', '跟着老师做项目的硬核路线。需要一定的技能基础，但能快速提升技术和自律。', 6, 20, '技能', 1, '#8b5cf6');

-- ============================================================
-- 学期结局种子数据
-- ============================================================

INSERT INTO semester_ending (id, ending_name, ending_type, description, condition_rule, priority, theme_color, icon) VALUES
(1, '课设战神', '学业', '你的 Java 课设拿到了高分，老师点名表扬了你的数据库设计。你用技能和自律把课设做到了极致。', 'skill>=70 AND academic>=60 AND discipline>=50', 30, '#7c3aed', '💻'),
(2, '图书馆常驻民', '学业', '图书馆的每一个角落你都熟悉，你成了这里最稳定的自习者。学业和自律双高，但社交可能有点欠缺。', 'academic>=70 AND discipline>=60 AND social<50', 25, '#0f766e', '📚'),
(3, '社团风云人物', '社交', '你在社团活动区是出了名的活跃分子，声望和社交值都很高。不过学业可能需要补课。', 'social>=70 AND discipline>=40', 20, '#ea580c', '🎉'),
(4, '实验室编外研究员', '学业', '你虽然不是正式成员，但在实验室花的时间比大多数组员还多。技能和自律都很强，项目进度推进明显。', 'skill>=65 AND discipline>=55 AND academic>=50', 22, '#8b5cf6', '🔬'),
(5, 'DDL 幸存者', '生活', '你在高压中一路硬撑，所有关键任务勉强完成。压力值很高，但你活下来了。', 'pressure>=60 AND academic>=40 AND health>=40', 15, '#dc2626', '⏰'),
(6, '快乐摆烂人', '生活', '你选择了快乐路线。压力很低，自律也很低，学业只是还行。但谁说这不是一种活法呢？', 'pressure<30 AND discipline<40 AND academic<50', 18, '#f59e0b', '😎'),
(7, '六边形工大学子', '均衡', '你的各项属性都比较均衡，没有明显短板，也没有极端优势。你是一个合格的工大学子。', 'academic>=50 AND health>=50 AND social>=50 AND skill>=50 AND discipline>=50 AND pressure<60', 10, '#2563eb', '⚖️');
