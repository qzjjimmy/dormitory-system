package com.bishe.dormitory.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sys_user (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "username VARCHAR(40) UNIQUE NOT NULL," +
                "password VARCHAR(80) NOT NULL," +
                "real_name VARCHAR(60) NOT NULL," +
                "role VARCHAR(20) NOT NULL," +
                "phone VARCHAR(30)," +
                "room_no VARCHAR(30)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS biz_record (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "category VARCHAR(40) NOT NULL," +
                "title VARCHAR(120) NOT NULL," +
                "owner VARCHAR(60)," +
                "location VARCHAR(80)," +
                "amount DECIMAL(10,2) DEFAULT 0," +
                "status VARCHAR(30) NOT NULL," +
                "content TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS chat_message (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "from_user_id BIGINT NOT NULL," +
                "to_user_id BIGINT NOT NULL," +
                "content TEXT NOT NULL," +
                "is_read TINYINT DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        // -- Migration: add feature columns for smart dorm assignment --
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN gender VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN major_class VARCHAR(80)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN sleep_habit VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN smoking VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN hobbies VARCHAR(200)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN cleanliness VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN gaming VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN snoring VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN return_time VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN noise_tolerance VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN preferred_room_type VARCHAR(10)");
        addColumnIfNotExists("ALTER TABLE sys_user ADD COLUMN preferred_bed VARCHAR(10)");

        seedUser("admin", "123456", "系统管理员", "admin", "13800000001", "总控中心");
        seedUser("student", "123456", "邱子健", "student", "13800000002", "芙蓉楼3 · 519室 · 2号床");
        seedUser("dormkeeper", "123456", "李宿管", "dormkeeper", "13800000003", "芙蓉楼3值班室");
        seedUser("linruhai", "123456", "林如海", "student", "13800000004", "芙蓉楼3 · 519室 · 1号床");
        seedUser("qinxingrui", "123456", "秦兴睿", "student", "13800000005", "芙蓉楼3 · 519室 · 3号床");
        seedUser("chenjiahe", "123456", "陈家和", "student", "13800000006", "芙蓉楼3 · 519室 · 4号床");
        seedUser("zhangkeeper", "123456", "张宿管", "dormkeeper", "13800000007", "芙蓉楼3值班室");

        seedRecord("building", "芙蓉楼1", "李宿管", "北区", "启用", "6层，女生宿舍，配套自习区与洗衣房");
        seedRecord("building", "芙蓉楼2", "王宿管", "南区", "启用", "8层，男生宿舍，近教学楼");
        seedRecord("building", "芙蓉楼3", "张宿管", "东区", "启用", "7层，男生宿舍，配电梯，近食堂");
        seedRecord("building", "芙蓉楼5", "陈宿管", "西区", "维护中", "4层，留学生宿舍，独立卫浴");
        seedRecord("room", "519宿舍", "邱子健", "芙蓉楼3-5层", "已住满", "4人间，空调，独立卫浴，朝南");
        seedRecord("room", "101宿舍", "李同学", "芙蓉楼1-1层", "已住满", "4人间，朝南，独立卫浴");
        seedRecord("room", "203宿舍", "王同学", "芙蓉楼2-2层", "空2床", "6人间，空调，公共卫浴");
        seedRecord("student", "邱子健入住登记", "邱子健", "芙蓉楼3-519-2床", "在住", "软件工程 2023 级，学号20230001");
        seedRecord("student", "林如海入住登记", "林如海", "芙蓉楼3-519-1床", "在住", "计算机科学 2023 级，学号20230002");
        seedRecord("student", "秦兴睿入住登记", "秦兴睿", "芙蓉楼3-519-3床", "在住", "电子信息 2023 级，学号20230003");
        seedRecord("student", "陈家和入住登记", "陈家和", "芙蓉楼3-519-4床", "在住", "数据科学 2023 级，学号20230004");
        seedRecord("student", "王同学入住登记", "王同学", "芙蓉楼2-203-3床", "在住", "电子信息 2022 级，学号20220042");
        seedRecord("dormkeeper", "李宿管值班配置", "李宿管", "芙蓉楼1", "在岗", "负责芙蓉楼1日常巡检与访客登记");
        seedRecord("dormkeeper", "王宿管值班配置", "王宿管", "芙蓉楼2", "在岗", "负责芙蓉楼2日常管理与卫生检查");
        seedRecord("dormkeeper", "张宿管值班配置", "张宿管", "芙蓉楼3", "在岗", "负责芙蓉楼3日常巡检，519宿舍责任宿管");
        seedRecord("repair", "519空调制冷异常", "邱子健", "芙蓉楼3-519", "待处理", "空调开启后不制冷，需要派单维修");
        seedRecord("repair", "101水龙头漏水", "李同学", "芙蓉楼1-101", "处理中", "卫生间水龙头关不紧，持续滴水");
        seedRecord("repair", "203灯管损坏", "王同学", "芙蓉楼2-203", "已完成", "宿舍中间灯管闪烁后熄灭，已更换");
        seedRecord("repair", "519网络端口故障", "邱子健", "芙蓉楼3-519", "已驳回", "经检查为用户路由器配置问题，非端口故障");
        seedRecord("fee", "2026年4月水电费", "李同学", "芙蓉楼1-101", "已缴费", "水费28.00元，电费72.00元，合计100.00元");
        jdbcTemplate.update("UPDATE biz_record SET amount=100.00 WHERE category='fee' AND title='2026年4月水电费'");
        seedRecord("fee", "2026年5月水电费-519", "邱子健", "芙蓉楼3-519", "待缴费", "水费32.00元，电费86.50元");
        jdbcTemplate.update("UPDATE biz_record SET amount=118.50 WHERE category='fee' AND title='2026年5月水电费-519'");
        seedRecord("fee", "2026年5月水电费-203", "王同学", "芙蓉楼2-203", "已缴费", "水费25.00元，电费95.00元，合计120.00元");
        jdbcTemplate.update("UPDATE biz_record SET amount=120.00 WHERE category='fee' AND title='2026年5月水电费-203'");
        seedRecord("fee", "2026年3月热水费", "邱子健", "芙蓉楼3-519", "待缴费", "热水使用超额费用45.00元");
        jdbcTemplate.update("UPDATE biz_record SET amount=45.00 WHERE category='fee' AND title='2026年3月热水费'");
        seedRecord("announcement", "2026年寒假放假通知", "管理员", "全校宿舍", "已发布", "寒假时间为2026年1月15日至2月25日，离校前请关闭水电门窗");
        seedRecord("announcement", "五一假期宿舍安全提醒", "管理员", "全校宿舍", "已发布", "离寝前关闭电源，贵重物品随身携带");
        seedRecord("announcement", "关于开展宿舍卫生大检查的通知", "管理员", "全校宿舍", "已发布", "5月28日下午2点开始，请各宿舍提前打扫卫生");
        seedRecord("announcement", "宿舍用电安全须知", "管理员", "全校宿舍", "已发布", "严禁使用电热毯、电饭锅等大功率电器，违者通报处分");
        seedRecord("announcement", "校园网络升级通知", "管理员", "全校宿舍", "已发布", "5月25日0:00-6:00网络设备升级，届时断网");
        seedRecord("visitor", "访客预约：邱子健家长", "邱子健", "芙蓉楼3大厅", "待审核", "预计周六 14:00 到访，来访人为父母二人");
        seedRecord("visitor", "访客预约：李同学朋友", "李同学", "芙蓉楼1大厅", "已通过", "周日 10:00 到访，1位同学，预计停留2小时");
        seedRecord("transfer", "调宿申请：519转620", "邱子健", "芙蓉楼3", "审核中", "因作息原因申请更换宿舍，从519调至620");
        seedRecord("transfer", "调宿申请：床位调换", "林如海", "芙蓉楼3-519", "待审核", "申请从1号床换至4号床，靠窗位置");
        seedRecord("hygiene", "519宿舍卫生检查-4月", "张宿管", "芙蓉楼3-519", "优秀", "地面整洁，桌面有轻微杂物，整体良好，得分98");
        seedRecord("hygiene", "519宿舍卫生检查-5月", "张宿管", "芙蓉楼3-519", "良好", "床铺整齐，阳台清洁，得分90");
        seedRecord("hygiene", "101宿舍卫生检查-4月", "李宿管", "芙蓉楼1-101", "良好", "床铺整齐，卫生间清洁，得分92");
        seedRecord("hygiene", "203宿舍卫生检查-4月", "王宿管", "芙蓉楼2-203", "合格", "地面有少量垃圾未清理，桌面较乱，得分78");
        seedRecord("lateReturn", "晚归登记：邱子健", "张宿管", "芙蓉楼3门禁", "已登记", "23:36返校，原因：实验室项目加班");
        seedRecord("lateReturn", "晚归登记：王同学", "王宿管", "芙蓉楼2门禁", "已登记", "00:15返校，原因：火车站接人延误");
        seedRecord("item", "大件物品出入：显示器", "邱子健", "芙蓉楼3门岗", "已放行", "24寸显示器带出维修，已登记型号和序列号");
        seedRecord("item", "大件物品出入：主机箱", "林如海", "芙蓉楼3门岗", "待审批", "台式电脑主机搬入，需确认是否为新购设备");
        seedRecord("feedback", "热水供应建议", "邱子健", "芙蓉楼3", "待回复", "建议延长晚间热水供应时间至24:00");
        seedRecord("feedback", "洗衣机数量不足", "秦兴睿", "芙蓉楼3", "已回复", "芙蓉楼3仅2台洗衣机，高峰期排队时间过长");
        seedRecord("message", "维修派单通知", "张宿管", "芙蓉楼3-519", "已发送", "维修师傅将在今天下午上门处理519空调问题");
        seedRecord("message", "卫生检查提醒", "管理员", "芙蓉楼3", "已发送", "5月28日将进行月度卫生大检查，请做好准备");
        seedRecord("message", "费用催缴通知", "管理员", "芙蓉楼3-519", "已发送", "2026年5月水电费118.50元尚未缴纳，请及时缴费");

        jdbcTemplate.update("UPDATE sys_user SET real_name='邱子健', room_no='芙蓉楼3 · 519室 · 2号床' WHERE username='student'");

        // -- Set realistic dates for seed records (otherwise all default to CURRENT_TIMESTAMP) --
        // Announcements
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-01-05 09:00:00' WHERE category='announcement' AND title='2026年寒假放假通知'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-04-25 08:30:00' WHERE category='announcement' AND title='五一假期宿舍安全提醒'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-25 14:00:00' WHERE category='announcement' AND title='关于开展宿舍卫生大检查的通知'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-03-10 10:00:00' WHERE category='announcement' AND title='宿舍用电安全须知'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-20 16:00:00' WHERE category='announcement' AND title='校园网络升级通知'");
        // Fees
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-03-01 12:00:00' WHERE category='fee' AND title='2026年3月热水费'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-04-01 12:00:00' WHERE category='fee' AND title='2026年4月水电费'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-01 12:00:00' WHERE category='fee' AND title='2026年5月水电费-519'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-01 12:00:00' WHERE category='fee' AND title='2026年5月水电费-203'");
        // Repairs
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-20 08:00:00' WHERE category='repair' AND title='519空调制冷异常'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-10 15:00:00' WHERE category='repair' AND title='101水龙头漏水'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-04-15 09:00:00' WHERE category='repair' AND title='203灯管损坏'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-18 11:00:00' WHERE category='repair' AND title='519网络端口故障'");
        // Hygiene
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-04-28 16:00:00' WHERE category='hygiene' AND title='519宿舍卫生检查-4月'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-27 16:00:00' WHERE category='hygiene' AND title='519宿舍卫生检查-5月'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-04-28 14:00:00' WHERE category='hygiene' AND title='101宿舍卫生检查-4月'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-04-28 15:30:00' WHERE category='hygiene' AND title='203宿舍卫生检查-4月'");
        // Visits & transfers
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-26 20:00:00' WHERE category='visitor' AND title='访客预约：邱子健家长'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-22 18:00:00' WHERE category='visitor' AND title='访客预约：李同学朋友'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-23 21:00:00' WHERE category='transfer' AND title='调宿申请：519转620'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-25 19:00:00' WHERE category='transfer' AND title='调宿申请：床位调换'");
        // Late returns
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-26 23:36:00' WHERE category='lateReturn' AND title='晚归登记：邱子健'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-25 00:15:00' WHERE category='lateReturn' AND title='晚归登记：王同学'");
        // Items
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-24 17:00:00' WHERE category='item' AND title='大件物品出入：显示器'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-28 10:00:00' WHERE category='item' AND title='大件物品出入：主机箱'");
        // Feedback
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-20 22:00:00' WHERE category='feedback' AND title='热水供应建议'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-18 20:00:00' WHERE category='feedback' AND title='洗衣机数量不足'");
        // Messages
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-20 09:00:00' WHERE category='message' AND title='维修派单通知'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-25 10:00:00' WHERE category='message' AND title='卫生检查提醒'");
        jdbcTemplate.update("UPDATE biz_record SET created_at='2026-05-02 08:00:00' WHERE category='message' AND title='费用催缴通知'");

        // Seed chat messages — demo conversations (user IDs: 1=admin, 2=邱子健, 3=李宿管, 4=林如海, 5=秦兴睿, 6=陈家和, 7=张宿管)
        seedChatMessage(2L, 7L, "张宿管你好，519的空调不制冷了，能帮忙看看吗？");
        seedChatMessage(7L, 2L, "收到，我安排维修师傅今天下午过去。大概几点你在宿舍？");
        seedChatMessage(2L, 7L, "下午3点以后我都在，谢谢张宿管！");
        seedChatMessage(7L, 2L, "好的，已下单。维修师傅会带工具过去，你们提前把空调周围东西挪一下。");
        seedChatMessage(4L, 2L, "子健，今晚一起吃饭不？食堂新开了个窗口");
        seedChatMessage(2L, 4L, "好啊，几点？叫上兴睿和家和一起");
        seedChatMessage(4L, 2L, "六点楼下集合吧");
        seedChatMessage(5L, 2L, "明天卫生检查，记得把阳台垃圾倒了");
        seedChatMessage(2L, 5L, "收到，我今天晚上收拾");

        jdbcTemplate.update("UPDATE sys_user SET real_name='邱子健', room_no='芙蓉楼3 · 519室 · 2号床' WHERE username='student'");

        // -- Seed user feature data for smart assignment demo --
        jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='晚睡',smoking='否',hobbies='编程,游戏,电影',cleanliness='整洁',gaming='是',snoring='否',return_time='晚归',noise_tolerance='正常',preferred_room_type='4人间' WHERE username='student'");
        jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='早睡',smoking='否',hobbies='篮球,音乐,阅读',cleanliness='整洁',gaming='是',snoring='否',return_time='早归',noise_tolerance='安静',preferred_room_type='4人间' WHERE username='linruhai'");
        jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='晚睡',smoking='否',hobbies='游戏,电影,健身',cleanliness='一般',gaming='是',snoring='是',return_time='晚归',noise_tolerance='热闹',preferred_room_type='4人间' WHERE username='qinxingrui'");
        jdbcTemplate.update("UPDATE sys_user SET gender='男',major_class='智能科学与技术2025级',sleep_habit='早睡',smoking='否',hobbies='阅读,编程,音乐',cleanliness='整洁',gaming='是',snoring='否',return_time='正常',noise_tolerance='安静',preferred_room_type='4人间' WHERE username='chenjiahe'");
    }

    private void seedUser(String username, String password, String realName, String role, String phone, String roomNo) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE username=?", Integer.class, username);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO sys_user(username,password,real_name,role,phone,room_no) VALUES(?,?,?,?,?,?)",
                    username, passwordEncoder.encode(password), realName, role, phone, roomNo);
        }
    }

    private void seedRecord(String category, String title, String owner, String location, String status, String content) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM biz_record WHERE category=? AND title=?", Integer.class, category, title);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO biz_record(category,title,owner,location,status,content) VALUES(?,?,?,?,?,?)",
                    category, title, owner, location, status, content);
        }
    }

    private void seedChatMessage(long fromUserId, long toUserId, String content) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chat_message WHERE from_user_id=? AND to_user_id=? AND content=?",
                Integer.class, fromUserId, toUserId, content);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO chat_message(from_user_id, to_user_id, content, is_read) VALUES(?,?,?,1)",
                    fromUserId, toUserId, content);
        }
    }

    private void addColumnIfNotExists(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            // Column already exists — safe to ignore
        }
    }
}
