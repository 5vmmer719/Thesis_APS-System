package com.aps.masterdata.impl;


import com.aps.dto.request.masterdata.ModelCreateRequest;
import com.aps.dto.request.masterdata.ModelQueryRequest;
import com.aps.dto.request.masterdata.ModelUpdateRequest;
import com.aps.dto.response.masterdata.ModelDTO;
import com.aps.entity.masterdata.Model;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.ModelMapper;
import com.aps.masterdata.ModelService;
import com.aps.response.PageResult;
import com.aps.utils.RequestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {

    private final ModelMapper modelMapper;

    @Override
    public PageResult<ModelDTO> listModels(ModelQueryRequest request) {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Model::getModelCode, request.getKeyword())
                    .or()
                    .like(Model::getModelName, request.getKeyword()));
        }

        if (StringUtils.hasText(request.getPlatform())) {
            wrapper.eq(Model::getPlatform, request.getPlatform());
        }

        if (request.getStatus() != null) {
            wrapper.eq(Model::getStatus, request.getStatus());
        }

        wrapper.orderByDesc(Model::getCreatedTime);

        Page<Model> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Model> resultPage = modelMapper.selectPage(page, wrapper);

        List<ModelDTO> modelDTOS = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(
                modelDTOS,
                resultPage.getTotal(),
                request.getPageNum(),
                request.getPageSize()
        );
    }

    @Override
    public ModelDTO getModelById(Long id) {
        Model model = modelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(40401, "车型不存在");
        }
        return convertToDTO(model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createModel(ModelCreateRequest request) {
        LambdaQueryWrapper<Model> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Model::getModelCode, request.getModelCode());

        if (modelMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(40001, "车型编码已存在");
        }

        Model model = new Model();
        BeanUtils.copyProperties(request, model);

        modelMapper.insert(model);

        log.info("创建车型成功: id={}, modelCode={}, 操作人={}",
                model.getId(), model.getModelCode(), RequestUtil.getUsername());

        return model.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateModel(ModelUpdateRequest request) {
        Model model = modelMapper.selectById(request.getId());
        if (model == null) {
            throw new BusinessException(40401, "车型不存在");
        }

        if (StringUtils.hasText(request.getModelName())) {
            model.setModelName(request.getModelName());
        }
        if (StringUtils.hasText(request.getPlatform())) {
            model.setPlatform(request.getPlatform());
        }
        if (request.getStatus() != null) {
            model.setStatus(request.getStatus());
        }
        if (StringUtils.hasText(request.getRemark())) {
            model.setRemark(request.getRemark());
        }

        modelMapper.updateById(model);

        log.info("更新车型成功: id={}, modelCode={}, 操作人={}",
                model.getId(), model.getModelCode(), RequestUtil.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        Model model = modelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(40401, "车型不存在");
        }

        // TODO: 检查车型是否被引用（BOM、订单等）

        modelMapper.deleteById(id);

        log.info("删除车型成功: id={}, modelCode={}, 操作人={}",
                model.getId(), model.getModelCode(), RequestUtil.getUsername());
    }

    private ModelDTO convertToDTO(Model model) {
        ModelDTO dto = new ModelDTO();
        BeanUtils.copyProperties(model, dto);
        dto.setStatusText(model.getStatus() == 1 ? "启用" : "停用");
        return dto;
    }
}
