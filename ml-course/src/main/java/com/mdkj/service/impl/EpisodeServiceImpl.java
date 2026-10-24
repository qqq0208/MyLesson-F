package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mdkj.constant.ML;
import com.mdkj.dto.EpisodeExcelDTO;
import com.mdkj.dto.EpisodeInsertDTO;
import com.mdkj.dto.EpisodePageDTO;
import com.mdkj.dto.EpisodeUpdateDTO;
import com.mdkj.entity.Category;
import com.mdkj.entity.Course;
import com.mdkj.entity.Season;
import com.mdkj.exception.ServiceException;
import com.mdkj.result.ResultCode;
import com.mdkj.util.MinioUtil;
import com.mdkj.vo.EpisodeSimpleListVO;
import com.mdkj.vo.PageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.entity.Episode;
import com.mdkj.mapper.EpisodeMapper;
import com.mdkj.service.EpisodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.mdkj.entity.table.EpisodeTableDef.EPISODE;
/**
 * 集次表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class EpisodeServiceImpl extends ServiceImpl<EpisodeMapper, Episode>  implements EpisodeService{
    @Override
    public boolean insert(EpisodeInsertDTO dto) {
        String title = dto.getTitle();

        // 标题查重
        // select count(1) from episode where title = ?
        if (QueryChain.of(mapper)
                .where(EPISODE.TITLE.eq(title))
                .exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }

        // 组装实体类
        Episode episode = BeanUtil.copyProperties(dto, Episode.class);
        episode.setVideo(ML.Episode.DEFAULT_VIDEO);
        episode.setCover(ML.Episode.DEFAULT_VIDEO_COVER);
        episode.setInfo(StrUtil.isEmpty(dto.getInfo()) ? "暂无描述。" : dto.getInfo());
        episode.setCreated(LocalDateTime.now());
        episode.setUpdated(LocalDateTime.now());

        // insert into episode (title, info, video, cover, fk_season_id, idx, created, updated) values (?, ?, ?, ?, ?, ?, ?, ?)
        if (mapper.insert(episode) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        return true;
    }

    @Override
    public Episode select(Long id) {
        // 指定联查字段
        RelationManager.addQueryRelations("season");
        // select * from episode where id = ?
        Episode episode = mapper.selectOneWithRelationsById(id);
        if (ObjectUtil.isNull(episode)) {
            throw new ServiceException(ResultCode.EPISODE_NOT_FOUND, id + "号集次记录不存在");
        }
        return episode;
    }

    @Override
    public List<EpisodeSimpleListVO> simpleList() {
        // select id, title from episode order by idx asc, id desc
        return QueryChain.of(mapper)
                .orderBy(EPISODE.IDX.asc(), EPISODE.ID.desc())
                .withRelations()
                .listAs(EpisodeSimpleListVO.class);
    }

    @Override
    public PageVO<Episode> page(EpisodePageDTO dto) {
        // 指定联查字段
        RelationManager.addQueryRelations("season");

        // select * from episode order by idx asc, id desc
        QueryChain<Episode> queryChain = QueryChain.of(mapper)
                .orderBy(EPISODE.IDX.asc(), EPISODE.ID.desc());

        // title条件
        if (ObjectUtil.isNotEmpty(dto.getTitle())) {
            queryChain.where(EPISODE.TITLE.like(dto.getTitle()));
        }

        // fkSeasonId条件
        if (ObjectUtil.isNotEmpty(dto.getFkSeasonId())) {
            queryChain.where(EPISODE.FK_SEASON_ID.eq(dto.getFkSeasonId()));
        }

        // DB分页并转为VO
        Page<Episode> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Episode> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(EpisodeUpdateDTO dto) {

        String title = dto.getTitle();
        Long id = dto.getId();

        // 检查集次是否存在
        this.existsById(id);

        // 标题查重
        // select count(1) from episode where title = ? and id <> ?
        if (QueryChain.of(mapper)
                .where(EPISODE.TITLE.eq(title))
                .and(EPISODE.ID.ne(id))
                .exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }

        // 组装实体类
        Episode episode = mapper.selectOneById(id);
        BeanUtil.copyProperties(dto, episode);
        episode.setUpdated(LocalDateTime.now());

        // update episode set title = ?, info = ?, idx = ?, fk_season_id = ?, updated = ? where id = ?
        if (!UpdateChain.of(episode)
                .where(EPISODE.ID.eq(episode.getId()))
                .update()) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {

        // 检查集次是否存在
        this.existsById(id);

        // delete from episode where id =?
        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean deleteBatch(List<Long> ids) {

        // 检查集次是否存在
        // select count(*) from episode where id in (?, ?, ?)
        if (QueryChain.of(mapper)
                .where(EPISODE.ID.in(ids))
                .count() < ids.size()) {
            throw new ServiceException(ResultCode.EPISODE_NOT_FOUND, "至少一个集次数据不存在");
        }

        // delete from episode where id in (ids)
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除失败");
        }
        return true;
    }

    @Override
    public String uploadVideoCover(MultipartFile newFile, Long id) {
        // 按主键查询记录
        Episode episode = mapper.selectOneById(id);
        if (ObjectUtil.isNull(episode)) {
            throw new ServiceException(ResultCode.EPISODE_NOT_FOUND, id + "号集次数据不存在");
        }

        // 备份旧文件名
        String oldFileName = episode.getCover();

        // 生成新文件名
        String newFileName = MinioUtil.randomFilename(newFile);

        // DB更新文件名
        episode.setCover(newFileName);
        episode.setUpdated(LocalDateTime.now());
        if (mapper.update(episode) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库更新视频封面图片名失败");
        }

        try {
            // MinIO中删除旧文件：默认文件不删除
            if (!ML.Episode.DEFAULT_VIDEO_COVER.equals(oldFileName)) {
                MinioUtil.delete(oldFileName, ML.MinIO.EPISODE_VIDEO_COVER_DIR, ML.MinIO.BUCKET_NAME);
            }

            // MinIO上传新文件
            MinioUtil.upload(newFile, newFileName, ML.MinIO.EPISODE_VIDEO_COVER_DIR, ML.MinIO.BUCKET_NAME);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.SERVER_ERROR, "MinIO操作失败：" + e.getMessage());
        }

        // 返回新文件名
        return newFileName;
    }

    @Override
    public String uploadVideo(MultipartFile newFile, Long id) {
        // 按主键查询记录
        // select * from episode where id = ?
        Episode episode = mapper.selectOneById(id);
        if (ObjectUtil.isNull(episode)) {
            throw new ServiceException(ResultCode.EPISODE_NOT_FOUND, id + "号集次数据不存在");
        }

        // 备份旧文件名
        String oldFileName = episode.getVideo();

        // 生成新文件名
        String newFileName = MinioUtil.randomFilename(newFile);

        // DB更新文件名
        episode.setVideo(newFileName);
        episode.setUpdated(LocalDateTime.now());
        if (mapper.update(episode) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库更新视频文件名失败");
        }

        try {
            // MinIO中删除旧文件：默认文件不删除
            if (!ML.Episode.DEFAULT_VIDEO.equals(oldFileName)) {
                MinioUtil.delete(oldFileName, ML.MinIO.EPISODE_VIDEO_DIR, ML.MinIO.BUCKET_NAME);
            }

            // MinIO上传新文件
            MinioUtil.upload(newFile, newFileName, ML.MinIO.EPISODE_VIDEO_DIR, ML.MinIO.BUCKET_NAME);
        } catch (Exception e) {
            throw new ServiceException(ResultCode.SERVER_ERROR, "MinIO操作失败：" + e.getMessage());
        }

        // 返回新文件名
        return newFileName;
    }

    @Override
    public List<EpisodeExcelDTO> getExcelData() {
        //设置查询的层级递归深度  集次 -> 季次 -> 课程 -> 类别
        RelationManager.setMaxDepth(4);
        //使用关联查询 查询全部集次记录
        List<Episode> episodes = mapper.selectAllWithRelations();
        //数据转换
        List<EpisodeExcelDTO> results = episodes.stream().map(episode -> {
            //属性复制
            EpisodeExcelDTO dto = BeanUtil.copyProperties(episode, EpisodeExcelDTO.class);
            //获取集次的季次对象
            Season season = episode.getSeason();
            if(ObjectUtil.isNotNull(season)){
                //设置季次相关信息
                dto.setSeasonTitle(season.getTitle());
                dto.setSeasonInfo(season.getInfo());
                //获取季次的课程对象
                Course course = season.getCourse();
                if(ObjectUtil.isNotNull(course)){
                    //设置课程相关信息
                    dto.setCourseTitle(course.getTitle());
                    dto.setCourseInfo(course.getInfo());
                    dto.setPrice(course.getPrice());
                    dto.setAuthor(course.getAuthor());
                    //获取类别对象
                    Category category = course.getCategory();
                    if(ObjectUtil.isNotNull(category)){
                        dto.setCategoryTitle(category.getTitle());
                    }
                }
            }
            //返回dto
            return dto;

        }).collect(Collectors.toList());
        return results;
    }

    /**
     * 按主键检查集次是否存在，如果不存在则直接抛出异常
     *
     * @param id 集次主键
     */
    private void existsById(Long id) {
        // select count(*) from episode where id = ?
        if (!QueryChain.of(mapper)
                .where(EPISODE.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.EPISODE_NOT_FOUND, id + "号集次数据不存在");
        }
    }
}
