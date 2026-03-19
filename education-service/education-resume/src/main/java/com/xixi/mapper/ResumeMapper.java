package com.xixi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xixi.entity.Resume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {

    /**
     * 按学生维度统计公开简历候选总数（每个学生最多计 1 条）。
     */
    @Select("""
            <script>
            SELECT COUNT(1)
            FROM (
                SELECT r.student_id
                FROM resume r
                WHERE r.visibility = 'PUBLIC'
                  AND r.status = 1
                  <if test="keyword != null and keyword != ''">
                    AND (
                        r.resume_title LIKE CONCAT('%', #{keyword}, '%')
                        OR EXISTS (
                            SELECT 1
                            FROM resume_skill rs
                            WHERE rs.resume_id = r.id
                              AND rs.skill_name LIKE CONCAT('%', #{keyword}, '%')
                        )
                    )
                  </if>
                  <if test="major != null and major != ''">
                    AND EXISTS (
                        SELECT 1
                        FROM resume_education re
                        WHERE re.resume_id = r.id
                          AND re.major LIKE CONCAT('%', #{major}, '%')
                    )
                  </if>
                  <if test="degree != null and degree != ''">
                    AND EXISTS (
                        SELECT 1
                        FROM resume_education re
                        WHERE re.resume_id = r.id
                          AND UPPER(re.degree) = UPPER(#{degree})
                    )
                  </if>
                GROUP BY r.student_id
            ) t
            </script>
            """)
    Long countPublicStudentTotal(
            @Param("keyword") String keyword,
            @Param("major") String major,
            @Param("degree") String degree
    );

    /**
     * 按学生维度分页查询公开简历，每个学生仅返回 1 份主展示简历。
     */
    @Select("""
            <script>
            SELECT
                ranked.id,
                ranked.student_id,
                ranked.resume_title,
                ranked.resume_template,
                ranked.avatar_url,
                ranked.contact_email,
                ranked.contact_phone,
                ranked.wechat,
                ranked.linkedin,
                ranked.github,
                ranked.self_introduction,
                ranked.career_objective,
                ranked.visibility,
                ranked.view_count,
                ranked.download_count,
                ranked.is_default,
                ranked.status,
                ranked.created_time,
                ranked.updated_time
            FROM (
                SELECT
                    r.*,
                    ROW_NUMBER() OVER (
                        PARTITION BY r.student_id
                        ORDER BY IFNULL(r.is_default, 0) DESC, r.updated_time DESC, r.created_time DESC, r.id DESC
                    ) AS rn
                FROM resume r
                WHERE r.visibility = 'PUBLIC'
                  AND r.status = 1
                  <if test="keyword != null and keyword != ''">
                    AND (
                        r.resume_title LIKE CONCAT('%', #{keyword}, '%')
                        OR EXISTS (
                            SELECT 1
                            FROM resume_skill rs
                            WHERE rs.resume_id = r.id
                              AND rs.skill_name LIKE CONCAT('%', #{keyword}, '%')
                        )
                    )
                  </if>
                  <if test="major != null and major != ''">
                    AND EXISTS (
                        SELECT 1
                        FROM resume_education re
                        WHERE re.resume_id = r.id
                          AND re.major LIKE CONCAT('%', #{major}, '%')
                    )
                  </if>
                  <if test="degree != null and degree != ''">
                    AND EXISTS (
                        SELECT 1
                        FROM resume_education re
                        WHERE re.resume_id = r.id
                          AND UPPER(re.degree) = UPPER(#{degree})
                    )
                  </if>
            ) ranked
            WHERE ranked.rn = 1
            ORDER BY ranked.view_count DESC, ranked.updated_time DESC, ranked.created_time DESC, ranked.id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<Resume> selectPublicStudentPage(
            @Param("offset") Long offset,
            @Param("limit") Long limit,
            @Param("keyword") String keyword,
            @Param("major") String major,
            @Param("degree") String degree
    );

    /**
     * 按学生查询主展示公开简历。
     */
    @Select("""
            SELECT *
            FROM resume
            WHERE student_id = #{studentId}
              AND visibility = 'PUBLIC'
              AND status = 1
            ORDER BY IFNULL(is_default, 0) DESC, updated_time DESC, created_time DESC, id DESC
            LIMIT 1
            """)
    Resume selectPrimaryPublicResumeByStudentId(@Param("studentId") Long studentId);

    /**
     * 按学生查询全部公开简历（用于详情页切换查看）。
     */
    @Select("""
            SELECT *
            FROM resume
            WHERE student_id = #{studentId}
              AND visibility = 'PUBLIC'
              AND status = 1
            ORDER BY IFNULL(is_default, 0) DESC, updated_time DESC, created_time DESC, id DESC
            """)
    List<Resume> selectPublicResumesByStudentId(@Param("studentId") Long studentId);

    /**
     * 按学生ID和简历ID查询指定公开简历。
     */
    @Select("""
            SELECT *
            FROM resume
            WHERE student_id = #{studentId}
              AND id = #{resumeId}
              AND visibility = 'PUBLIC'
              AND status = 1
            LIMIT 1
            """)
    Resume selectPublicResumeByStudentIdAndResumeId(
            @Param("studentId") Long studentId,
            @Param("resumeId") Long resumeId
    );
}
