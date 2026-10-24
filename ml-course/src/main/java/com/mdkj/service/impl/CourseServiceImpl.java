package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mdkj.constant.ML;
import com.mdkj.dao.CourseRepository;
import com.mdkj.dto.CourseInsertDTO;
import com.mdkj.dto.CoursePageDTO;
import com.mdkj.dto.CourseUpdateDTO;
import com.mdkj.es.CourseDoc;
import com.mdkj.exception.ServiceException;
import com.mdkj.mapper.EpisodeMapper;
import com.mdkj.mapper.SeasonMapper;
import com.mdkj.result.ResultCode;
import com.mdkj.util.MinioUtil;
import com.mdkj.vo.CourseSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.entity.Course;
import com.mdkj.mapper.CourseMapper;
import com.mdkj.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static com.mdkj.entity.table.CourseTableDef.COURSE;
import static com.mdkj.entity.table.SeasonTableDef.SEASON;
import static com.mdkj.entity.table.EpisodeTableDef.EPISODE;

/**
 * 课程表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    @Autowired
    private SeasonMapper seasonMapper;
    @Autowired
    private EpisodeMapper episodeMapper;
    @Autowired
    private CourseRepository courseRepository;

    @Override
    public boolean insert(CourseInsertDTO dto) {
        String title = dto.getTitle();
        //标题查重
        if (QueryChain.of(mapper)
                .where(COURSE.TITLE.eq(title)).
                exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }
        //组装实体类
        Course course = BeanUtil.copyProperties(dto, Course.class);
        course.setInfo(StrUtil.isEmpty(course.getInfo()) ? "暂无描述" : dto.getInfo());
        course.setSummary(ML.Course.DEFAULT_SUMMARY);
        course.setCover(ML.Course.DEFAULT_COVER);
        course.setCreated(LocalDateTime.now());
        course.setUpdated(LocalDateTime.now());
        //DB添加
        if (mapper.insert(course) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        return true;
    }

    @Override
    public Course select(Long id) {
        //查询课程 关联 类别 季次
        Course course = mapper.selectOneWithRelationsById(id);
        if (ObjectUtil.isNull(course)) {
            throw new ServiceException(ResultCode.COURSE_NOT_FOUND, id + "号课程不存在");
        }
        return course;
    }

    @Override
    public List<CourseSimpleListVO> simpleList() {
        return QueryChain.of(mapper)
                .orderBy(COURSE.IDX.asc(), COURSE.ID.desc())
                .listAs(CourseSimpleListVO.class);
    }

    @Override
    public PageVO<Course> page(CoursePageDTO dto) {
        //创建基础的QueryChain
        QueryChain<Course> queryChain = QueryChain.of(mapper).orderBy(COURSE.FK_CATEGORY_ID.asc(), COURSE.IDX.asc(), COURSE.ID.desc());
        //title条件
        String title = dto.getTitle();
        if (ObjectUtil.isNotNull(title)) {
            queryChain.where(COURSE.TITLE.like(title));
        }
        //类别ID条件
        Long categoryId = dto.getFkCategoryId();
        if (ObjectUtil.isNotNull(categoryId)) {
            queryChain.where(COURSE.FK_CATEGORY_ID.eq(categoryId));
        }
        //执行分页查询 - >关联
        Page<Course> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Course> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(CourseUpdateDTO dto) {
        Long id = dto.getId();
        String title = dto.getTitle();
        //检查课程是否存在
        this.existsById(id);
        //标题查重
        if (QueryChain.of(mapper)
                .where(COURSE.TITLE.eq(title)).
                exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }
        //组装实体类
        Course course = mapper.selectOneById(id);
        BeanUtil.copyProperties(dto, course);
        course.setUpdated(LocalDateTime.now());
        //DB修改
        if (mapper.update(course) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        //检查课程是否存在
        this.existsById(id);
        //通过课程主键查询全部季次的ID列表
        List<Long> seasonIds = QueryChain.of(seasonMapper)
                .select(SEASON.ID)
                .where(SEASON.FK_COURSE_ID.eq(id))
                .objListAs(Long.class);
        //存在季次时 批量删除季次以及季次下面的集次
        this.clearSeasonAndEpisode(seasonIds);

        //删除课程
        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除课程失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean deleteBatch(List<Long> ids) {
        //检查课程是否存在
        if (QueryChain.of(mapper)
                .where(COURSE.ID.in(ids))
                .count() < ids.size()) {
            throw new ServiceException(ResultCode.COURSE_NOT_FOUND, "至少一个课程不存在");
        }
        //通过课程id列表找到每个id季次列表
        List<Long> seasonIds = QueryChain.of(seasonMapper)
                .select(SEASON.ID)
                .where(SEASON.FK_COURSE_ID.in(ids))
                .objListAs(Long.class);
        //存在季次的时候 删除
        this.clearSeasonAndEpisode(seasonIds);

        //批量删除课程
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除课程失败");
        }

        return false;
    }

    @Override
    public String uploadCover(MultipartFile newFile, Long id) {
        //按主键查询记录
        Course course = mapper.selectOneById(id);
        if (ObjectUtil.isNull(course)) {
            throw new ServiceException(ResultCode.COURSE_NOT_FOUND, id + "号的课程不存在");
        }
        //备份旧文件名
        String cover = course.getCover();
        //生成新文件名
        String newFileName = MinioUtil.randomFilename(newFile);
        //DB更新
        course.setCover(newFileName);
        course.setUpdated(LocalDateTime.now());
        if (mapper.update(course) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库更新封面图片失败");
        }
        //操作MinIo
        try {
            //如果课程原封面 不是默认封面 从MinIo中删除
            if (!ML.Course.DEFAULT_COVER.equals(cover)) {
                MinioUtil.delete(cover, ML.MinIO.COURSE_COVER_DIR, ML.MinIO.BUCKET_NAME);
            }
            //上传新的封面文件
            MinioUtil.upload(newFile, newFileName, ML.MinIO.COURSE_COVER_DIR, ML.MinIO.BUCKET_NAME);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.SERVER_ERROR, "MinIo操作失败" + e.getMessage());
        }

        //返回新文件名字
        return newFileName;
    }

    @Override
    public String uploadSummary(MultipartFile newFile, Long id) {
        //按主键查询记录
        Course course = mapper.selectOneById(id);
        if (ObjectUtil.isNull(course)) {
            throw new ServiceException(ResultCode.COURSE_NOT_FOUND, id + "号的课程不存在");
        }
        //备份旧文件名
        String cover = course.getCover();
        //生成新文件名
        String newFileName = MinioUtil.randomFilename(newFile);
        //DB更新
        course.setSummary(newFileName);
        course.setUpdated(LocalDateTime.now());
        if (mapper.update(course) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库更新封面图片失败");
        }
        //操作MinIo
        try {
            //如果课程原封面 不是默认封面 从MinIo中删除
            if (!ML.Course.DEFAULT_COVER.equals(cover)) {
                MinioUtil.delete(cover, ML.MinIO.COURSE_SUMMARY_DIR, ML.MinIO.BUCKET_NAME);
            }
            //上传新的封面文件
            MinioUtil.upload(newFile, newFileName, ML.MinIO.COURSE_SUMMARY_DIR, ML.MinIO.BUCKET_NAME);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.SERVER_ERROR, "MinIo操作失败" + e.getMessage());
        }

        //返回新文件名字
        return newFileName;
    }

    @Override
    public PageVO<CourseDoc> search(CoursePageDTO dto) {
        Integer pageNum = dto.getPageNum();
        Integer pageSize = dto.getPageSize();
        String keyword = dto.getKeyword();
        //处理默认值
        pageNum = ObjectUtil.isNotNull(pageNum) ? pageNum : 0;
        pageSize = ObjectUtil.isNotNull(pageSize) ? pageSize : 0;
        //参数处理 对pageNum进行最小边界保护 规避ES报错 因为ES的分页是从0开始的
        pageNum = pageNum - 1;
        if (pageNum < 0) {
            pageNum = 0;
        }
        if (pageSize < 0) {
            pageSize = 0;
        }
        //初始化es的分页对象
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        //声明分页结果对象
        org.springframework.data.domain.Page<CourseDoc> esPage;
        //如果keyword为空 默认全查
        if (StrUtil.isEmpty(keyword)) {
            esPage = courseRepository.findAll(pageable);
        } else {
            esPage = courseRepository.searchByTitleOrAuthorOrderByIdx(keyword, keyword, pageable);
        }
        //组装PageVO
        PageVO pageVO = new PageVO();
        pageVO.setPageNum(pageNum + 1L);
        pageVO.setPageSize(pageSize);
        pageVO.setTotalRow(esPage.getTotalElements());
        pageVO.setTotalPage(esPage.getTotalPages());
        pageVO.setRecords(esPage.getContent());
        return pageVO;
    }


    //检查课程是否存在
    private boolean existsById(Long id) {
        if (!QueryChain.of(mapper)
                .where(COURSE.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.COURSE_NOT_FOUND, id + "号的课程不存在");
        }
        return true;
    }

    //根据季次ID列表 删除全部季次 以及 每个季次下的集次
    private void clearSeasonAndEpisode(List<Long> seasonIds) {
        //存在季次的时候 批量删除
        if (!CollUtil.isEmpty(seasonIds)) {
            //通过季次的ID列表查询出全部集次的ID列表
            List<Long> episodeIds = QueryChain.of(episodeMapper)
                    .select(EPISODE.ID)
                    .where(EPISODE.FK_SEASON_ID.in(seasonIds))
                    .objListAs(Long.class);
            //批量删除集次
            if (CollUtil.isNotEmpty(episodeIds)) {
                if (episodeMapper.deleteBatchByIds(episodeIds) <= 0) {
                    throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除集次失败");
                }
            }
            //批量删除季次
            if (seasonMapper.deleteBatchByIds(seasonIds) <= 0) {
                throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除季次失败");
            }
        }
    }
}
