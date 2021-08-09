package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrGroupService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrRespVO;
import com.atguigu.gulimall.product.vo.AttrVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import static java.util.stream.Collectors.*;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrGroupService attrGroupService;

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    private CategoryDao categoryDao;
    @Resource
    private AttrGroupDao attrGroupDao;


    @Override
    public void saveAttr(AttrVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        baseMapper.insert(attrEntity);
        //属性类型[0-销售属性，1-基本属性] 只有基本属性才有属性分组id
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, String type, Long catelogId) {
        //增加属性类型的判断，属性类型[0-销售属性，1-基本属性]
        QueryWrapper<AttrEntity> queryWrapper =
                new QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(type)
                        ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                        : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        //catelogId为0的时候默认查询全部
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );
        PageUtils pageUtils = new PageUtils(page);

        Map<Long, List<CategoryEntity>> CatIdMap = categoryService.list().stream()
                .collect(groupingBy(CategoryEntity::getCatId));
        List<AttrRespVO> respVOS = page.getRecords().stream().map(attrEntity -> {
            AttrRespVO attrRepoVO = new AttrRespVO();
            BeanUtils.copyProperties(attrEntity, attrRepoVO);
            //设置分类名称
            //根据catelogId查询缓存中分类名称
            List<CategoryEntity> categoryEntities = CatIdMap.get(attrEntity.getCatelogId());
            if (categoryEntities != null) {
                attrRepoVO.setCatelogName(categoryEntities.get(0).getName());
            }
            //销售属性不存在分组
            if ("base".equalsIgnoreCase(type)) {
                //设置分组名称
                //先去pms_attr_attrgroup_relation根据attr_id查询出attr_group_id
                //再根据attr_group_id在pms_attr_group中查询attr_group_name
                AttrAttrgroupRelationEntity byId = attrAttrgroupRelationService.getById(attrEntity.getAttrId());
//            Optional.ofNullable(byId).ifPresent(item -> {
//                AttrGroupEntity entity = attrGroupService.getById(byId.getAttrId());
//                attrRepoVO.setGroupName(entity.getAttrGroupName());
//            });
                if (byId != null && byId.getAttrGroupId() != null) {
                    AttrGroupEntity entity = attrGroupService.getById(byId.getAttrGroupId());
                    attrRepoVO.setGroupName(entity.getAttrGroupName());
                }
            }
            return attrRepoVO;
        }).collect(toList());
        pageUtils.setList(respVOS);
        return pageUtils;
    }

    @Override
    public PageUtils queryBaseAttrsPage(Map<String, Object> params, String type, Long cateLogId) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        //属性类型[0-销售属性，1-基本属性]
        wrapper.eq("attr_type", "base".equalsIgnoreCase(type)
                ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
                : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());

        String key = (String) params.get("key");

        if (cateLogId != 0) {
            wrapper.eq("catelog_id", cateLogId);
        }
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w -> w.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );
        PageUtils pageUtils = new PageUtils(page);

        //设置所属分类和所属分组
        List<AttrRespVO> collect = page.getRecords().stream().map(item -> {
            AttrRespVO attrRespVO = new AttrRespVO();
            BeanUtils.copyProperties(item, attrRespVO);

            //设置属性分类
            Long catelogId = item.getCatelogId();
            CategoryEntity categoryEntity = categoryService.getById(catelogId);
            if (categoryEntity != null) {
                String categoryName = categoryEntity.getName();
                attrRespVO.setCatelogName(categoryName);

            }
            //设置属性分组,销售属性不涉及分组
            if ("base".equals(type)) {
                Long attrId = item.getAttrId();
//                AttrAttrgroupRelationEntity relationEntity =
//                        attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));

                AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationService.getById(attrId);
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
                    String groupName = attrGroupEntity.getAttrGroupName();
                    attrRespVO.setGroupName(groupName);
                }
            }
            return attrRespVO;
        }).collect(toList());

        pageUtils.setList(collect);
        return pageUtils;
    }

    @Override
    public AttrRespVO getAttrsInfo(Long attrId) {
        if (attrId == null || attrId < 1) {
            return null;
        }
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVO attrRespVO = new AttrRespVO();
        BeanUtils.copyProperties(attrEntity, attrRespVO);

        //设置所属分类
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVO.setCatelogPath(catelogPath);

        //设置分组


        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));

        if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
            AttrGroupEntity groupEntity = attrGroupService.getById(relationEntity.getAttrGroupId());
            if (groupEntity != null) {
                attrRespVO.setGroupName(groupEntity.getAttrGroupName());
            }
            attrRespVO.setAttrGroupId(groupEntity.getAttrGroupId());
        }

        return attrRespVO;

    }

    @Override
    public PageUtils getOtherAttrs2(Map<String, Object> params, Long attrgroupId) {

        AttrGroupEntity groupEntities =
                attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrgroupId));
        Long catelogId = groupEntities.getCatelogId();


        List<AttrEntity> attrEntities = baseMapper.selectList(new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId));

        List<Long> attrIds = attrEntities.stream().filter(item -> {

            QueryWrapper<AttrAttrgroupRelationEntity> wrapper =
                    new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", item.getAttrId());
            if (attrAttrgroupRelationService.getOne(wrapper) == null) {
                return true;
            }
            return false;
        }).map(AttrEntity::getAttrId).collect(toList());

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(w -> {
                w.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        if (attrIds != null) {
            //select * from pms_attr where attr_id not in ()
            wrapper.in("attr_id", attrIds);
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        // SELECT attr_id FROM pms_attr WHERE id In (?) and search_type=1;
//        List<Long> searchAttrIds = baseMapper.selectSearchAttrIds(attrIds);
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().select("attr_id").in("attr_id", attrIds).eq("search_type", 1);
        List<Long> searchAttrIds = baseMapper.selectList(wrapper).stream().map(AttrEntity::getAttrId).collect(toList());
        return searchAttrIds;
    }


    @Override
    public AttrRespVO getAttrInfo(Long attrId) {
        AttrRespVO attrRespVO = new AttrRespVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVO);

//        attrRespVO.setAttrGroupId();
//        attrRespVO.setCategoryPath();
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //1.设置分组信息
//        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectById(attrId);
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity =
                    attrAttrgroupRelationDao.selectOne(
                            new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrAttrgroupRelationEntity != null) {
                attrRespVO.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity entity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (entity != null) {
                    attrRespVO.setGroupName(entity.getAttrGroupName());
                }

            }
        }

        //2.设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrRespVO.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryService.getById(catelogId);
        if (categoryEntity != null) {
            attrRespVO.setCatelogName(categoryEntity.getName());
        }
        return attrRespVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAttr(AttrRespVO attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        //修改本身
        this.updateById(attrEntity);
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            //修改分组关联表
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attr.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());

//            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
//            if (count > 0) {
//                attrAttrgroupRelationDao.update(attrAttrgroupRelationEntity
//                        , new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attr.getAttrId()));
//            } else {
//                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
//            }
            attrAttrgroupRelationService.saveOrUpdate(attrAttrgroupRelationEntity);
        }
    }


    @Override
    public PageUtils getOtherAttrs(Map<String, Object> params, int attrgroupId) {
        //根据分组得到三级分类id，然后查询出该三级分类下所有分组已经关联好的属性
        Long catelogId = attrGroupDao.selectById(attrgroupId).getCatelogId();
        List<AttrGroupEntity> group = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id",
                catelogId));
        List<Long> attrGroupIds = group.stream().map(AttrGroupEntity::getAttrGroupId).collect(toList());
        List<AttrAttrgroupRelationEntity> relationEntities =
                attrAttrgroupRelationDao.selectList(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIds));
        //三级分类下所有分组对应的属性id集合
        //所以那些没有分组的属性就是可以额外添加的
        List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(toList());

        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>()
                .eq("catelog_id", catelogId)
                .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn("attr_id", attrIds);
        }
        String key = (String) params.get("key");
        if (StringUtils.isNotEmpty(key)) {
            wrapper.and(item -> item.eq("attr_id", key).or().like("attr_name", key));
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveDetail(AttrVO attrVO) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVO, attrEntity);
        this.save(attrEntity);
        Long attrGroupId = attrVO.getAttrGroupId();
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attrGroupId);
        relationEntity.setAttrId(attrEntity.getAttrId());
        attrAttrgroupRelationService.save(relationEntity);
    }

}