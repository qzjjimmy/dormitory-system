package com.bishe.dormitory.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoomAssignmentService {
    private final JdbcTemplate jdbcTemplate;

    public RoomAssignmentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // -- Student DTO for assignment --
    public static class StudentProfile {
        public Long id;
        public String realName;
        public String gender;
        public String majorClass;
        public String sleepHabit;
        public String smoking;
        public List<String> hobbies = new ArrayList<>();
        public String cleanliness;
        public String gaming;
        public String snoring;
        public String returnTime;
        public String noiseTolerance;
        public String preferredRoomType;
        public String preferredBed;

        static StudentProfile fromMap(Map<String, Object> row) {
            StudentProfile s = new StudentProfile();
            s.id = ((Number) row.get("id")).longValue();
            s.realName = (String) row.get("realName");
            s.gender = (String) row.get("gender");
            s.majorClass = (String) row.get("majorClass");
            s.sleepHabit = (String) row.get("sleepHabit");
            s.smoking = (String) row.get("smoking");
            String h = (String) row.get("hobbies");
            if (h != null && !h.isEmpty()) {
                s.hobbies = Arrays.asList(h.split(","));
            }
            s.cleanliness = (String) row.get("cleanliness");
            s.gaming = (String) row.get("gaming");
            s.snoring = (String) row.get("snoring");
            s.returnTime = (String) row.get("returnTime");
            s.noiseTolerance = (String) row.get("noiseTolerance");
            s.preferredRoomType = (String) row.get("preferredRoomType");
            s.preferredBed = (String) row.get("preferredBed");
            return s;
        }
    }

    // -- Room DTO --
    public static class RoomSlot {
        public String roomNo;
        public List<StudentProfile> members = new ArrayList<>();
        public int capacity = 4;

        public int available() { return capacity - members.size(); }
    }

    // -- Compatibility score between two students (0-100) --
    public int compatibilityScore(StudentProfile a, StudentProfile b) {
        // Hard constraint: gender must match
        if (a.gender == null || b.gender == null || !a.gender.equals(b.gender)) {
            return 0;
        }
        int score = 0;

        // Sleep habit (weight 25)
        if (nullSafeEquals(a.sleepHabit, b.sleepHabit)) score += 25;
        // Smoking (weight 20)
        if (nullSafeEquals(a.smoking, b.smoking)) score += 20;
        // Cleanliness (weight 15) — 3 levels
        score += levelScore(a.cleanliness, b.cleanliness, 15, 7.5);
        // Gaming (weight 10)
        if (nullSafeEquals(a.gaming, b.gaming)) score += 10;
        // Snoring (weight 10)
        if (nullSafeEquals(a.snoring, b.snoring)) score += 10;
        // Return time (weight 10) — 3 levels
        score += levelScore(a.returnTime, b.returnTime, 10, 5);
        // Noise tolerance (weight 5) — 3 levels
        score += levelScore(a.noiseTolerance, b.noiseTolerance, 5, 2.5);
        // Hobbies (weight 5) — shared ratio
        score += hobbyScore(a.hobbies, b.hobbies);

        return score;
    }

    private boolean nullSafeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private int levelScore(String a, String b, int max, double step) {
        if (nullSafeEquals(a, b)) return max;
        if (a == null || b == null) return 0;
        List<List<String>> levels = Arrays.asList(
            Arrays.asList("整洁", "一般", "随意"),
            Arrays.asList("早归", "正常", "晚归"),
            Arrays.asList("安静", "正常", "热闹")
        );
        for (List<String> lvl : levels) {
            int ia = lvl.indexOf(a);
            int ib = lvl.indexOf(b);
            if (ia >= 0 && ib >= 0) {
                int diff = Math.abs(ia - ib);
                if (diff == 0) return max;
                if (diff == 1) return (int)(max - step);
                return 0;
            }
        }
        return nullSafeEquals(a, b) ? max : 0;
    }

    private int hobbyScore(List<String> a, List<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0;
        Set<String> setA = new HashSet<>(a);
        Set<String> setB = new HashSet<>(b);
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);
        if (union.isEmpty()) return 0;
        int shared = 0;
        for (String ha : setA) {
            if (setB.contains(ha)) shared++;
        }
        return (int) Math.round(5.0 * shared / union.size());
    }

    // -- Phase 1: Same-class assignment --
    public List<RoomSlot> assignByClass(List<StudentProfile> students) {
        List<RoomSlot> allRooms = new ArrayList<>();
        Map<String, List<StudentProfile>> groups = new LinkedHashMap<>();
        for (StudentProfile s : students) {
            String key = (s.majorClass != null ? s.majorClass : "未分班") + "|" +
                         (s.preferredRoomType != null ? s.preferredRoomType : "4人间");
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(s);
        }

        int roomSeq = 1;
        for (Map.Entry<String, List<StudentProfile>> entry : groups.entrySet()) {
            List<StudentProfile> group = new ArrayList<>(entry.getValue());
            String roomType = group.get(0).preferredRoomType != null ? group.get(0).preferredRoomType : "4人间";
            int capacity = "6人间".equals(roomType) ? 6 : 4;

            group.sort((a, b) -> {
                if (!nullSafeEquals(a.sleepHabit, b.sleepHabit))
                    return String.valueOf(a.sleepHabit).compareTo(String.valueOf(b.sleepHabit));
                return a.realName.compareTo(b.realName);
            });

            while (!group.isEmpty()) {
                RoomSlot room = new RoomSlot();
                room.roomNo = "芙蓉楼3 · " + (500 + roomSeq) + "室";
                room.capacity = capacity;
                roomSeq++;

                StudentProfile anchor = group.remove(0);
                room.members.add(anchor);

                for (int bed = 1; bed < capacity && !group.isEmpty(); bed++) {
                    int bestIdx = -1;
                    int bestScore = -1;
                    for (int i = 0; i < group.size(); i++) {
                        int totalScore = 0;
                        for (StudentProfile m : room.members) {
                            totalScore += compatibilityScore(group.get(i), m);
                        }
                        int avgScore = room.members.isEmpty() ? 0 : totalScore / room.members.size();
                        if (avgScore > bestScore) {
                            bestScore = avgScore;
                            bestIdx = i;
                        }
                    }
                    if (bestIdx >= 0) {
                        room.members.add(group.remove(bestIdx));
                    }
                }
                allRooms.add(room);
            }
        }
        return allRooms;
    }

    // -- Phase 2: Cross-class filling --
    public void fillRemaining(List<RoomSlot> rooms, List<StudentProfile> unassigned) {
        List<StudentProfile> remaining = new ArrayList<>(unassigned);
        for (RoomSlot room : rooms) {
            while (room.available() > 0 && !remaining.isEmpty()) {
                int bestIdx = -1;
                int bestScore = -1;
                for (int i = 0; i < remaining.size(); i++) {
                    int totalScore = 0;
                    for (StudentProfile m : room.members) {
                        totalScore += compatibilityScore(remaining.get(i), m);
                    }
                    int avgScore = room.members.isEmpty() ? 50 : totalScore / room.members.size();
                    if (avgScore > bestScore) {
                        bestScore = avgScore;
                        bestIdx = i;
                    }
                }
                if (bestIdx >= 0 && bestScore > 0) {
                    room.members.add(remaining.remove(bestIdx));
                } else {
                    break;
                }
            }
        }
    }

    // -- Get all existing assigned rooms with compatibility scores --
    public Map<String, Object> getAllRooms() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id,real_name AS realName,room_no AS roomNo,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance " +
            "FROM sys_user WHERE role='student' AND room_no IS NOT NULL");

        // Group by room prefix
        Map<String, List<StudentProfile>> groups = new LinkedHashMap<>();
        Map<Long, String> bedMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String roomNo = (String) row.get("roomNo");
            if (roomNo == null) continue;
            String prefix = roomNo.replaceAll(" · \\d+号床.*$", "");
            StudentProfile sp = StudentProfile.fromMap(row);
            groups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(sp);
            // Extract bed number
            java.util.regex.Matcher bedMatcher = java.util.regex.Pattern.compile("(\\d+号床)").matcher(roomNo);
            bedMap.put(sp.id, bedMatcher.find() ? bedMatcher.group(1) : "?号床");
        }

        List<Map<String, Object>> roomList = new ArrayList<>();
        for (Map.Entry<String, List<StudentProfile>> entry : groups.entrySet()) {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("roomNo", entry.getKey());
            int capacity = entry.getValue().size() > 4 ? 6 : 4;
            rm.put("capacity", capacity);

            // Sort members by bed number
            entry.getValue().sort((a, b) -> {
                String ba = bedMap.getOrDefault(a.id, "0");
                String bb = bedMap.getOrDefault(b.id, "0");
                int na = Integer.parseInt(ba.replaceAll("\\D", "0"));
                int nb = Integer.parseInt(bb.replaceAll("\\D", "0"));
                return na - nb;
            });

            List<Map<String, Object>> memberList = new ArrayList<>();
            int totalScore = 0, pairCount = 0;
            for (StudentProfile m : entry.getValue()) {
                Map<String, Object> mb = new LinkedHashMap<>();
                mb.put("id", m.id);
                mb.put("realName", m.realName);
                mb.put("majorClass", m.majorClass);
                mb.put("sleepHabit", m.sleepHabit);
                mb.put("smoking", m.smoking);
                mb.put("cleanliness", m.cleanliness);
                mb.put("gaming", m.gaming);
                mb.put("bed", bedMap.getOrDefault(m.id, "?号床"));
                memberList.add(mb);
                for (StudentProfile other : entry.getValue()) {
                    if (!m.id.equals(other.id)) {
                        totalScore += compatibilityScore(m, other);
                        pairCount++;
                    }
                }
            }
            rm.put("members", memberList);
            rm.put("avgCompatibility", pairCount > 0 ? totalScore / pairCount : 100);
            roomList.add(rm);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rooms", roomList);
        return result;
    }

    // -- Compute pairwise compatibility heatmap for all unassigned students --
    public Map<String, Object> computeHeatmap() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id,real_name AS realName,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance," +
            "preferred_room_type AS preferredRoomType " +
            "FROM sys_user WHERE role='student' AND room_no IS NULL");

        List<StudentProfile> students = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            StudentProfile sp = StudentProfile.fromMap(row);
            if (sp.gender != null) students.add(sp);
        }

        List<Map<String, Object>> labels = new ArrayList<>();
        for (StudentProfile s : students) {
            Map<String, Object> label = new LinkedHashMap<>();
            label.put("id", s.id);
            label.put("name", s.realName);
            label.put("majorClass", s.majorClass);
            labels.add(label);
        }

        List<List<Integer>> matrix = new ArrayList<>();
        for (int i = 0; i < students.size(); i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < students.size(); j++) {
                if (i == j) {
                    row.add(100);
                } else {
                    row.add(compatibilityScore(students.get(i), students.get(j)));
                }
            }
            matrix.add(row);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("matrix", matrix);
        return result;
    }

    // -- Full pipeline: execute both phases --
    public Map<String, Object> executeAssignment() {
        // Fetch all unassigned students (room_no is NULL) with complete profiles
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id,real_name AS realName,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance," +
            "preferred_room_type AS preferredRoomType,preferred_bed AS preferredBed " +
            "FROM sys_user WHERE role='student' AND room_no IS NULL");

        List<StudentProfile> students = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            StudentProfile sp = StudentProfile.fromMap(row);
            if (sp.gender != null) {
                students.add(sp);
            }
        }

        if (students.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("message", "没有待分配的学生");
            result.put("rooms", Collections.emptyList());
            result.put("unassignedCount", 0);
            return result;
        }

        // Phase 1: same-class assignment
        List<RoomSlot> rooms = assignByClass(new ArrayList<>(students));

        // Collect remaining unassigned from phase 1
        List<StudentProfile> stillUnassigned = new ArrayList<>();
        for (StudentProfile s : students) {
            boolean found = false;
            for (RoomSlot r : rooms) {
                for (StudentProfile m : r.members) {
                    if (m.id.equals(s.id)) { found = true; break; }
                }
                if (found) break;
            }
            if (!found) stillUnassigned.add(s);
        }

        // Phase 2: cross-class filling
        if (!stillUnassigned.isEmpty()) {
            fillRemaining(rooms, stillUnassigned);
        }

        // Collect final unassigned
        List<StudentProfile> finalUnassigned = new ArrayList<>();
        for (StudentProfile s : students) {
            boolean found = false;
            for (RoomSlot r : rooms) {
                for (StudentProfile m : r.members) {
                    if (m.id.equals(s.id)) { found = true; break; }
                }
                if (found) break;
            }
            if (!found) finalUnassigned.add(s);
        }

        // Build response
        List<Map<String, Object>> roomList = new ArrayList<>();
        for (RoomSlot r : rooms) {
            Map<String, Object> rm = new LinkedHashMap<>();
            rm.put("roomNo", r.roomNo);
            rm.put("capacity", r.capacity);
            List<Map<String, Object>> memberList = new ArrayList<>();
            int totalScore = 0;
            int pairCount = 0;
            int bedIdx = 0;
            for (StudentProfile m : r.members) {
                bedIdx++;
                Map<String, Object> mb = new LinkedHashMap<>();
                mb.put("id", m.id);
                mb.put("realName", m.realName);
                mb.put("majorClass", m.majorClass);
                mb.put("sleepHabit", m.sleepHabit);
                mb.put("smoking", m.smoking);
                mb.put("cleanliness", m.cleanliness);
                mb.put("gaming", m.gaming);
                mb.put("bed", bedIdx + "号床");
                memberList.add(mb);
                // Calculate pairwise scores for avg
                for (StudentProfile other : r.members) {
                    if (!m.id.equals(other.id)) {
                        totalScore += compatibilityScore(m, other);
                        pairCount++;
                    }
                }
            }
            rm.put("members", memberList);
            rm.put("avgCompatibility", pairCount > 0 ? totalScore / pairCount : 100);
            roomList.add(rm);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("rooms", roomList);
        result.put("unassignedCount", finalUnassigned.size());
        return result;
    }

    // -- Transfer recommendation: find top-3 rooms for a student --
    public List<Map<String, Object>> recommendTransfer(Long studentId) {
        Map<String, Object> studentRow = jdbcTemplate.queryForMap(
            "SELECT id,real_name AS realName,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance " +
            "FROM sys_user WHERE id=?", studentId);

        StudentProfile student = StudentProfile.fromMap(studentRow);

        // Find all students with rooms
        List<Map<String, Object>> allRooms = jdbcTemplate.queryForList(
            "SELECT id,real_name AS realName,room_no AS roomNo,gender,major_class AS majorClass," +
            "sleep_habit AS sleepHabit,smoking,hobbies,cleanliness,gaming,snoring," +
            "return_time AS returnTime,noise_tolerance AS noiseTolerance " +
            "FROM sys_user WHERE role='student' AND room_no IS NOT NULL");

        // Group by room prefix (extract building + room before bed info)
        Map<String, List<Map<String, Object>>> roomGroups = new LinkedHashMap<>();
        for (Map<String, Object> row : allRooms) {
            String roomNo = (String) row.get("roomNo");
            if (roomNo == null) continue;
            String prefix = roomNo.replaceAll(" · \\d+号床.*$", "");
            roomGroups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(row);
        }

        // Score each room (only those with < 4 members)
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : roomGroups.entrySet()) {
            if (entry.getValue().size() >= 4) continue; // full

            int totalScore = 0;
            int count = 0;
            boolean genderMismatch = false;
            for (Map<String, Object> row : entry.getValue()) {
                String theirGender = (String) row.get("gender");
                if (theirGender != null && !theirGender.equals(student.gender)) {
                    genderMismatch = true;
                    break;
                }
                StudentProfile other = StudentProfile.fromMap(row);
                totalScore += compatibilityScore(student, other);
                count++;
            }
            if (genderMismatch) continue;

            int avgScore = count > 0 ? totalScore / count : 50;

            Map<String, Object> rec = new LinkedHashMap<>();
            rec.put("roomNo", entry.getKey());
            rec.put("currentCount", entry.getValue().size());
            rec.put("compatibility", avgScore);

            // Build reason string
            StringBuilder reason = new StringBuilder();
            if (count == 0) {
                reason.append("空宿舍");
            } else {
                List<String> matches = new ArrayList<>();
                boolean allSameSleep = true, allSameSmoke = true;
                for (Map<String, Object> row : entry.getValue()) {
                    if (!nullSafeEquals(student.sleepHabit, (String) row.get("sleepHabit"))) allSameSleep = false;
                    if (!nullSafeEquals(student.smoking, (String) row.get("smoking"))) allSameSmoke = false;
                }
                if (allSameSleep) matches.add("作息一致");
                if (allSameSmoke) matches.add("同为不抽烟");
                reason.append(String.join("、", matches));
                if (reason.length() == 0) reason.append("部分兼容");
            }
            rec.put("reason", reason.toString());
            recommendations.add(rec);
        }

        recommendations.sort((a, b) -> ((Integer) b.get("compatibility")).compareTo((Integer) a.get("compatibility")));
        return recommendations.size() > 3 ? recommendations.subList(0, 3) : recommendations;
    }

    // -- Confirm assignment: update room_no for students --
    public void confirmAssignment(List<Map<String, Object>> assignments) {
        for (Map<String, Object> a : assignments) {
            Long studentId = ((Number) a.get("studentId")).longValue();
            String roomNo = (String) a.get("roomNo");
            String bedNo = (String) a.get("bedNo");
            jdbcTemplate.update("UPDATE sys_user SET room_no=? WHERE id=?",
                    roomNo + " · " + bedNo, studentId);
        }
    }
}
