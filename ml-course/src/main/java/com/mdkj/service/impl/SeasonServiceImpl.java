package com.mdkj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mdkj.dto.SeasonInsertDTO;
import com.mdkj.dto.SeasonPageDTO;
import com.mdkj.dto.SeasonUpdateDTO;
import com.mdkj.exception.ServiceException;
import com.mdkj.mapper.EpisodeMapper;
import com.mdkj.result.ResultCode;
import com.mdkj.vo.PageVO;
import com.mdkj.vo.SeasonSimpleListVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.relation.RelationManager;
import com.mybatisflex.core.update.UpdateChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.mdkj.entity.Season;
import com.mdkj.mapper.SeasonMapper;
import com.mdkj.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mdkj.entity.table.EpisodeTableDef.EPISODE;
import static com.mdkj.entity.table.SeasonTableDef.SEASON;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 季次表 服务层实现。
 *
 * @author YouTao
 * @since v1.0.0
 */
@Service
public class SeasonServiceImpl extends ServiceImpl<SeasonMapper, Season>  implements SeasonService{

    @Autowired
    private EpisodeMapper episodeMapper;

    @Override
    public boolean insert(SeasonInsertDTO dto) {
        String title = dto.getTitle();
        // 标题查重
        if (QueryChain.of(mapper)
                .where(SEASON.TITLE.eq(title))
                .exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }

        // 组装实体类
        Season season = BeanUtil.copyProperties(dto, Season.class);
        season.setInfo(StrUtil.isEmpty(dto.getInfo()) ? "暂无描述。" : dto.getInfo());
        season.setCreated(LocalDateTime.now());
        season.setUpdated(LocalDateTime.now());
        //DB添加
        if (mapper.insert(season) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库添加失败");
        }
        return true;
    }

    @Override
    public Season select(Long id) {
        // 指定联查字段
        RelationManager.addQueryRelations("course", "category", "episodes");
        Season season = mapper.selectOneWithRelationsById(id);
        if (ObjectUtil.isNull(season)) {
            throw new ServiceException(ResultCode.SEASON_NOT_FOUND, id + "号季次记录不存在");
        }
        return season;
    }

    @Override
    public List<SeasonSimpleListVO> simpleList() {
        return QueryChain.of(mapper)
                .orderBy(SEASON.IDX.asc(), SEASON.ID.desc())
                .withRelations()
                .listAs(SeasonSimpleListVO.class);
    }

    @Override
    public PageVO<Season> page(SeasonPageDTO dto) {
        QueryChain<Season> queryChain = QueryChain.of(mapper)
                .orderBy(SEASON.IDX.asc(), SEASON.ID.desc());

        // title条件
        if (ObjectUtil.isNotEmpty(dto.getTitle())) {
            queryChain.where(SEASON.TITLE.like(dto.getTitle()));
        }

        // fkCourseId条件
        if (ObjectUtil.isNotEmpty(dto.getFkCourseId())) {
            queryChain.where(SEASON.FK_COURSE_ID.eq(dto.getFkCourseId()));
        }

        // DB分页并转为VO
        Page<Season> result = queryChain.withRelations().page(new Page<>(dto.getPageNum(), dto.getPageSize()));
        PageVO<Season> pageVO = new PageVO<>();
        BeanUtil.copyProperties(result, pageVO);
        pageVO.setPageNum(result.getPageNumber());
        return pageVO;
    }

    @Override
    public boolean update(SeasonUpdateDTO dto) {
        String title = dto.getTitle();
        Long id = dto.getId();

        // 检查季次是否存在
        this.existsById(id);

        // 标题查重
        if (QueryChain.of(mapper)
                .where(SEASON.TITLE.eq(title))
                .and(SEASON.ID.ne(id))
                .exists()) {
            throw new ServiceException(ResultCode.TITLE_REPEAT, "标题" + title + "重复");
        }

        // 组装实体类
        Season season = mapper.selectOneById(id);
        BeanUtil.copyProperties(dto, season);
        season.setUpdated(LocalDateTime.now());

        if (mapper.update(season) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库修改失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean delete(Long id) {
        // 检查季次是否存在
        this.existsById(id);

        // 通过季次主键查询全部集次的ID列表
        List<Long> episodeIds = QueryChain.of(episodeMapper)
                .select(EPISODE.ID)
                .where(EPISODE.FK_SEASON_ID.eq(id))
                .objListAs(Long.class);

        // 存在集次时，批量删除集次
        this.clearEpisode(episodeIds);

        // 删除季次
        if (mapper.deleteById(id) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除季次记录失败");
        }
        return true;
    }

    @Transactional
    @Override
    public boolean deleteBatch(List<Long> ids) {
        // 检查季次是否存在
        if (QueryChain.of(mapper)
                .where(SEASON.ID.in(ids))
                .count() < ids.size()) {
            throw new ServiceException(ResultCode.SEASON_NOT_FOUND, "至少一个季次数据不存在");
        }

        // 通过季次主键列表查询全部集次的ID列表
        List<Long> episodeIds = QueryChain.of(episodeMapper)
                .select(EPISODE.ID)
                .where(EPISODE.FK_SEASON_ID.in(ids))
                .objListAs(Long.class);

        // 存在集次时，批量删除集次
        this.clearEpisode(episodeIds);

        // 批量删除季次
        if (mapper.deleteBatchByIds(ids) <= 0) {
            throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除季次记录失败");
        }
        return true;
    }

    private void existsById(Long id) {
        if (!QueryChain.of(mapper)
                .where(SEASON.ID.eq(id))
                .exists()) {
            throw new ServiceException(ResultCode.SEASON_NOT_FOUND, id + "号季次数据不存在");
        }
    }

    private void clearEpisode(List<Long> episodeIds) {
        // 存在集记录时，批量删除集
        if (ObjectUtil.isNotEmpty(episodeIds)) {
            if (episodeMapper.deleteBatchByIds(episodeIds) <= 0) {
                throw new ServiceException(ResultCode.MYSQL_ERROR, "数据库删除集次记录失败");
            }
        }
    }

}
