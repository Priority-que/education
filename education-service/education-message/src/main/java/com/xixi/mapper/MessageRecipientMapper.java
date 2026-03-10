package com.xixi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 投递接收人查询Mapper（跨库查询education_user_db.users）。
 */
@Mapper
public interface MessageRecipientMapper {

    @Select("""
            SELECT id
            FROM education_user_db.users
            WHERE status = 1
            """)
    List<Long> selectAllEnabledUserIds();

    @Select("""
            <script>
            SELECT id
            FROM education_user_db.users
            WHERE status = 1
              AND role IN
              <foreach collection="roles" item="role" open="(" separator="," close=")">
                #{role}
              </foreach>
            </script>
            """)
    List<Long> selectEnabledUserIdsByRoles(@Param("roles") List<Integer> roles);

    @Select("""
            <script>
            SELECT id
            FROM education_user_db.users
            WHERE status = 1
              AND id IN
              <foreach collection="userIds" item="userId" open="(" separator="," close=")">
                #{userId}
              </foreach>
            </script>
            """)
    List<Long> selectEnabledUserIdsByUserIds(@Param("userIds") List<Long> userIds);

    @Select("""
            SELECT id
            FROM education_course_db.course
            WHERE teacher_id = #{teacherId}
            """)
    List<Long> selectManagedCourseIdsByTeacherId(@Param("teacherId") Long teacherId);

    @Select("""
            <script>
            SELECT DISTINCT u.id
            FROM education_study_db.student_course sc
            INNER JOIN education_user_db.users u ON u.id = sc.student_id
            WHERE sc.teacher_id = #{teacherId}
              AND sc.learning_status != 'DROPPED'
              AND u.status = 1
              <if test="courseIds != null and courseIds.size() > 0">
                AND sc.course_id IN
                <foreach collection="courseIds" item="courseId" open="(" separator="," close=")">
                  #{courseId}
                </foreach>
              </if>
              <if test="roleCodes != null and roleCodes.size() > 0">
                AND u.role IN
                <foreach collection="roleCodes" item="roleCode" open="(" separator="," close=")">
                  #{roleCode}
                </foreach>
              </if>
              <if test="userIds != null and userIds.size() > 0">
                AND u.id IN
                <foreach collection="userIds" item="userId" open="(" separator="," close=")">
                  #{userId}
                </foreach>
              </if>
            ORDER BY u.id
            </script>
            """)
    List<Long> selectManagedUserIds(
            @Param("teacherId") Long teacherId,
            @Param("courseIds") List<Long> courseIds,
            @Param("roleCodes") List<Integer> roleCodes,
            @Param("userIds") List<Long> userIds
    );

    @Select("""
            SELECT role
            FROM education_user_db.users
            WHERE id = #{userId}
            LIMIT 1
            """)
    Integer selectUserRoleById(@Param("userId") Long userId);

    @Select("""
            <script>
            SELECT
              u.id AS userId,
              COALESCE(NULLIF(TRIM(u.real_name), ''), COALESCE(NULLIF(TRIM(u.nickname), ''), u.username)) AS displayName,
              s.student_number AS studentNo
            FROM education_user_db.users u
            LEFT JOIN education_user_db.students s ON s.user_id = u.id
            WHERE u.id IN
            <foreach collection="userIds" item="userId" open="(" separator="," close=")">
              #{userId}
            </foreach>
            ORDER BY u.id
            </script>
            """)
    List<Map<String, Object>> selectReceiverSamplesByUserIds(@Param("userIds") List<Long> userIds);

    @Select("""
            <script>
            SELECT COUNT(DISTINCT u.id)
            FROM education_study_db.student_course sc
            INNER JOIN education_user_db.users u ON u.id = sc.student_id
            LEFT JOIN education_user_db.students s ON s.user_id = u.id
            WHERE sc.teacher_id = #{teacherId}
              AND sc.learning_status != 'DROPPED'
              AND u.status = 1
              <if test="courseId != null">
                AND sc.course_id = #{courseId}
              </if>
              <if test="classId != null">
                AND sc.course_id = #{classId}
              </if>
              <if test="keyword != null and keyword != ''">
                AND (
                    u.real_name LIKE CONCAT('%', #{keyword}, '%')
                    OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
                    OR u.username LIKE CONCAT('%', #{keyword}, '%')
                    OR s.student_number LIKE CONCAT('%', #{keyword}, '%')
                )
              </if>
            </script>
            """)
    Long countTeacherReceivers(
            @Param("teacherId") Long teacherId,
            @Param("keyword") String keyword,
            @Param("courseId") Long courseId,
            @Param("classId") Long classId
    );

    @Select("""
            <script>
            SELECT
              u.id AS userId,
              COALESCE(NULLIF(TRIM(u.real_name), ''), COALESCE(NULLIF(TRIM(u.nickname), ''), u.username)) AS displayName,
              s.student_number AS studentNo,
              GROUP_CONCAT(DISTINCT sc.course_name ORDER BY sc.course_name SEPARATOR '/') AS courseNames
            FROM education_study_db.student_course sc
            INNER JOIN education_user_db.users u ON u.id = sc.student_id
            LEFT JOIN education_user_db.students s ON s.user_id = u.id
            WHERE sc.teacher_id = #{teacherId}
              AND sc.learning_status != 'DROPPED'
              AND u.status = 1
              <if test="courseId != null">
                AND sc.course_id = #{courseId}
              </if>
              <if test="classId != null">
                AND sc.course_id = #{classId}
              </if>
              <if test="keyword != null and keyword != ''">
                AND (
                    u.real_name LIKE CONCAT('%', #{keyword}, '%')
                    OR u.nickname LIKE CONCAT('%', #{keyword}, '%')
                    OR u.username LIKE CONCAT('%', #{keyword}, '%')
                    OR s.student_number LIKE CONCAT('%', #{keyword}, '%')
                )
              </if>
            GROUP BY u.id, u.real_name, u.nickname, u.username, s.student_number
            ORDER BY u.id DESC
            LIMIT #{offset}, #{pageSize}
            </script>
            """)
    List<Map<String, Object>> searchTeacherReceivers(
            @Param("teacherId") Long teacherId,
            @Param("keyword") String keyword,
            @Param("courseId") Long courseId,
            @Param("classId") Long classId,
            @Param("offset") Long offset,
            @Param("pageSize") Integer pageSize
    );
}
