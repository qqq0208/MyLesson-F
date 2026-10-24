package com.mdkj.service;

import com.mdkj.dto.CourseInsertDTO;
import com.mdkj.dto.CoursePageDTO;
import com.mdkj.dto.CourseUpdateDTO;
import com.mdkj.es.CourseDoc;
import com.mdkj.vo.CourseSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.service.IService;
import com.mdkj.entity.Course;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 课程表 服务层。
 *
 * @author YouTao
 * @since v1.0.0
 */
public interface CourseService extends IService<Course> {

    //添加 - 单条
    boolean insert(CourseInsertDTO dto);

    //查询 - 单条
    Course select(Long id);

    //查询 - 简单列表
    List<CourseSimpleListVO> simpleList();

    //查询 - 条件分页
    PageVO<Course> page(CoursePageDTO dto);

    //修改 - 单条
    boolean update(CourseUpdateDTO dto);

    //删除 - 单条
    boolean delete(Long id);

    //删除 - 多条
    boolean deleteBatch(List<Long> ids);

    //上传 - 封面
    String uploadCover(MultipartFile newFile,Long id);

    //上传 - 摘要
    String uploadSummary(MultipartFile newFile, Long id);

    //搜索 - 课程
    PageVO<CourseDoc> search(CoursePageDTO dto);
}
