package com.aps.order.impl;

import cn.hutool.core.bean.BeanUtil;

import com.aps.dto.request.order.ProdOrderCreateRequest;
import com.aps.dto.request.order.SalesOrderCreateRequest;
import com.aps.dto.request.order.SalesOrderQueryRequest;
import com.aps.dto.request.order.ToProdRequest;
import com.aps.dto.response.order.SalesOrderDTO;
import com.aps.entity.masterdata.Model;
import com.aps.entity.order.SalesOrder;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.ModelMapper;
import com.aps.mapper.order.SalesOrderMapper;
import com.aps.order.ProductionOrderService;
import com.aps.order.SalesOrderService;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 销售订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderMapper salesOrderMapper;
    private final ModelMapper modelMapper;
    private final ProductionOrderService productionOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(SalesOrderCreateRequest request) {
        log.info("创建销售订单, salesNo={}", request.getSalesNo());

        // 1. 校验销售订单号唯一性
        if (salesOrderMapper.existsBySalesNo(request.getSalesNo())) {
            throw new BusinessException(40000, "销售订单号已存在: " + request.getSalesNo());
        }

        // 2. 校验车型存在且启用
        Model model = modelMapper.selectById(request.getModelId());
        if (model == null || model.getDeleted() == 1) {
            throw new BusinessException(40000, "车型不存在或已删除");
        }
        if (model.getStatus() != 1) {
            throw new BusinessException(40000, "车型已停用");
        }

        // 3. 校验交期
        if (request.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessException(40000, "交期不能早于当前日期");
        }

        // 4. 创建销售订单
        SalesOrder salesOrder = new SalesOrder();
        BeanUtil.copyProperties(request, salesOrder);
        salesOrder.setStatus(SalesOrder.Status.NEW.getCode());
        salesOrder.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        
        // 处理 JSON 字段：空字符串转为 null，避免 MySQL JSON 类型报错
        if (salesOrder.getSourcePayload() != null && salesOrder.getSourcePayload().trim().isEmpty()) {
            salesOrder.setSourcePayload(null);
        }

        salesOrderMapper.insert(salesOrder);

        log.info("创建销售订单成功, id={}, salesNo={}", salesOrder.getId(), salesOrder.getSalesNo());
        return salesOrder.getId();
    }

    @Override
    public PageResult<SalesOrderDTO> list(SalesOrderQueryRequest request) {
        log.info("查询销售订单列表, request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();

        if (request.getModelId() != null) {
            wrapper.eq(SalesOrder::getModelId, request.getModelId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SalesOrder::getStatus, request.getStatus());
        }
        if (request.getFromDueDate() != null) {
            wrapper.ge(SalesOrder::getDueDate, request.getFromDueDate());
        }
        if (request.getToDueDate() != null) {
            wrapper.le(SalesOrder::getDueDate, request.getToDueDate());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.and(w -> w
                    .like(SalesOrder::getSalesNo, request.getKeyword())
                    .or()
                    .like(SalesOrder::getCustomerCode, request.getKeyword())
            );
        }

        // 排序：优先级降序、交期升序、创建时间降序
        wrapper.orderByDesc(SalesOrder::getPriority)
                .orderByAsc(SalesOrder::getDueDate)
                .orderByDesc(SalesOrder::getCreatedTime);

        // 分页查询
        Page<SalesOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<SalesOrder> result = salesOrderMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<SalesOrderDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public SalesOrderDTO getById(Long id) {
        log.info("获取销售订单详情, id={}", id);

        SalesOrder salesOrder = salesOrderMapper.selectById(id);
        if (salesOrder == null || salesOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "销售订单不存在");
        }

        return convertToDTO(salesOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        log.info("审批销售订单, id={}", id);

        // 1. 查询订单
        SalesOrder salesOrder = salesOrderMapper.selectById(id);
        if (salesOrder == null || salesOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "销售订单不存在");
        }

        // 2. 校验状态
        if (!SalesOrder.Status.NEW.getCode().equals(salesOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许审批，当前状态: "
                    + SalesOrder.Status.getDesc(salesOrder.getStatus()));
        }

        // 3. 更新状态
        salesOrder.setStatus(SalesOrder.Status.APPROVED.getCode());
        salesOrderMapper.updateById(salesOrder);

        log.info("审批销售订单成功, id={}, salesNo={}", id, salesOrder.getSalesNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long toProdOrder(Long id, ToProdRequest request) {
        log.info("销售订单转生产订单, id={}, request={}", id, request);

        // 1. 查询销售订单
        SalesOrder salesOrder = salesOrderMapper.selectById(id);
        if (salesOrder == null || salesOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "销售订单不存在");
        }

        // 2. 校验状态
        if (!SalesOrder.Status.APPROVED.getCode().equals(salesOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许转生产，当前状态: "
                    + SalesOrder.Status.getDesc(salesOrder.getStatus()));
        }

        // 3. 创建生产订单
        ProdOrderCreateRequest prodRequest = new ProdOrderCreateRequest();
        prodRequest.setProdNo("MO" + salesOrder.getSalesNo().substring(2)); // SO -> MO
        prodRequest.setSalesId(salesOrder.getId());
        prodRequest.setOrderKind(request.getOrderKind());
        prodRequest.setModelId(salesOrder.getModelId());
        prodRequest.setBomId(request.getBomId());
        prodRequest.setRouteVersion(request.getRouteVersion());
        prodRequest.setQty(salesOrder.getQty());
        prodRequest.setDueDate(salesOrder.getDueDate());
        prodRequest.setPriority(salesOrder.getPriority());
        prodRequest.setRemark("由销售订单 " + salesOrder.getSalesNo() + " 转换");

        Long prodOrderId = productionOrderService.create(prodRequest);

        // 4. 更新销售订单状态
        salesOrder.setStatus(SalesOrder.Status.CONVERTED.getCode());
        salesOrderMapper.updateById(salesOrder);

        log.info("销售订单转生产订单成功, salesId={}, prodOrderId={}", id, prodOrderId);
        return prodOrderId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        log.info("取消销售订单, id={}", id);

        // 1. 查询订单
        SalesOrder salesOrder = salesOrderMapper.selectById(id);
        if (salesOrder == null || salesOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "销售订单不存在");
        }

        // 2. 校验状态
        if (SalesOrder.Status.CONVERTED.getCode().equals(salesOrder.getStatus())) {
            throw new BusinessException(40002, "订单已转生产，不允许取消");
        }
        if (SalesOrder.Status.CANCELLED.getCode().equals(salesOrder.getStatus())) {
            throw new BusinessException(40002, "订单已取消");
        }

        // 3. 更新状态
        salesOrder.setStatus(SalesOrder.Status.CANCELLED.getCode());
        salesOrderMapper.updateById(salesOrder);

        log.info("取消销售订单成功, id={}, salesNo={}", id, salesOrder.getSalesNo());
    }

    /**
     * 转换为DTO
     */
    private SalesOrderDTO convertToDTO(SalesOrder salesOrder) {
        SalesOrderDTO dto = new SalesOrderDTO();
        BeanUtil.copyProperties(salesOrder, dto);

        // 设置状态文本
        dto.setStatusText(SalesOrder.Status.getDesc(salesOrder.getStatus()));

        // 设置车型信息
        if (salesOrder.getModelName() != null) {
            dto.setModelCode(salesOrder.getModelCode());
            dto.setModelName(salesOrder.getModelName());
        } else {
            Model model = modelMapper.selectById(salesOrder.getModelId());
            if (model != null) {
                dto.setModelCode(model.getModelCode());
                dto.setModelName(model.getModelName());
            }
        }

        return dto;
    }
}

