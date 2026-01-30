package com.aps.order.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aps.dto.request.order.ProdOrderAttrsSetRequest;
import com.aps.dto.request.order.ProdOrderCreateRequest;
import com.aps.dto.request.order.ProdOrderQueryRequest;
import com.aps.dto.request.order.ProdOrderUpdateRequest;
import com.aps.dto.response.order.ProdOrderDTO;
import com.aps.entity.masterdata.Bom;
import com.aps.entity.masterdata.Model;
import com.aps.entity.masterdata.Route;
import com.aps.entity.order.ProductionOrder;
import com.aps.entity.order.ProductionOrderAttr;
import com.aps.entity.order.SalesOrder;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.BomMapper;
import com.aps.mapper.masterdata.ModelMapper;
import com.aps.mapper.masterdata.RouteMapper;
import com.aps.mapper.order.ProductionOrderAttrMapper;
import com.aps.mapper.order.ProductionOrderMapper;
import com.aps.mapper.order.SalesOrderMapper;
import com.aps.order.ProductionOrderService;
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
 * 生产订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductionOrderServiceImpl implements ProductionOrderService {

    private final ProductionOrderMapper productionOrderMapper;
    private final ProductionOrderAttrMapper attrMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final ModelMapper modelMapper;
    private final BomMapper bomMapper;
    private final RouteMapper routeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProdOrderCreateRequest request) {
        log.info("创建生产订单, prodNo={}", request.getProdNo());

        // 1. 校验生产订单号唯一性
        if (productionOrderMapper.existsByProdNo(request.getProdNo())) {
            throw new BusinessException(40000, "生产订单号已存在: " + request.getProdNo());
        }

        // 2. 校验车型
        Model model = modelMapper.selectById(request.getModelId());
        if (model == null || model.getDeleted() == 1) {
            throw new BusinessException(40000, "车型不存在或已删除");
        }
        if (model.getStatus() != 1) {
            throw new BusinessException(40000, "车型已停用");
        }

        // 3. 校验BOM
        Bom bom = bomMapper.selectById(request.getBomId());
        if (bom == null || bom.getDeleted() == 1) {
            throw new BusinessException(40000, "BOM不存在或已删除");
        }
        if (!bom.getModelId().equals(request.getModelId())) {
            throw new BusinessException(40000, "BOM与车型不匹配");
        }
        if (bom.getStatus() != 1) {
            throw new BusinessException(40000, "BOM已停用");
        }

        // 4. 校验工艺路线版本（检查四大工艺是否都有对应版本）
        for (int processType = 1; processType <= 4; processType++) {
            LambdaQueryWrapper<Route> routeWrapper = new LambdaQueryWrapper<>();
            routeWrapper.eq(Route::getModelId, request.getModelId())
                    .eq(Route::getProcessType, processType)
                    .eq(Route::getVersion, request.getRouteVersion())
                    .eq(Route::getStatus, 1)
                    .eq(Route::getDeleted, 0);

            Route route = routeMapper.selectOne(routeWrapper);
            if (route == null) {
                throw new BusinessException(40000,
                        String.format("工艺路线版本不存在: 工艺类型=%d, 版本=%s", processType, request.getRouteVersion()));
            }
        }

        // 5. 校验交期
        if (request.getDueDate().isBefore(LocalDate.now())) {
            throw new BusinessException(40000, "交期不能早于当前日期");
        }

        // 6. 如果有销售订单ID，校验销售订单
        if (request.getSalesId() != null) {
            SalesOrder sales = salesOrderMapper.selectById(request.getSalesId());
            if (sales == null || sales.getDeleted() == 1) {
                throw new BusinessException(40000, "销售订单不存在");
            }
        }

        // 7. 创建生产订单
        ProductionOrder productionOrder = new ProductionOrder();
        BeanUtil.copyProperties(request, productionOrder);
        productionOrder.setStatus(ProductionOrder.Status.NEW.getCode());
        productionOrder.setPriority(request.getPriority() != null ? request.getPriority() : 0);

        productionOrderMapper.insert(productionOrder);

        log.info("创建生产订单成功, id={}, prodNo={}", productionOrder.getId(), productionOrder.getProdNo());
        return productionOrder.getId();
    }

    @Override
    public PageResult<ProdOrderDTO> list(ProdOrderQueryRequest request) {
        log.info("查询生产订单列表, request={}", request);

        // 构建查询条件
        LambdaQueryWrapper<ProductionOrder> wrapper = new LambdaQueryWrapper<>();

        if (request.getStatus() != null) {
            wrapper.eq(ProductionOrder::getStatus, request.getStatus());
        }
        if (request.getOrderKind() != null) {
            wrapper.eq(ProductionOrder::getOrderKind, request.getOrderKind());
        }
        if (request.getFromDueDate() != null) {
            wrapper.ge(ProductionOrder::getDueDate, request.getFromDueDate());
        }
        if (request.getToDueDate() != null) {
            wrapper.le(ProductionOrder::getDueDate, request.getToDueDate());
        }
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            wrapper.like(ProductionOrder::getProdNo, request.getKeyword());
        }

        // 排序：优先级降序、交期升序、创建时间降序
        wrapper.orderByDesc(ProductionOrder::getPriority)
                .orderByAsc(ProductionOrder::getDueDate)
                .orderByDesc(ProductionOrder::getCreatedTime);

        // 分页查询
        Page<ProductionOrder> page = new Page<>(request.getPageNum(), request.getPageSize());
        IPage<ProductionOrder> result = productionOrderMapper.selectPage(page, wrapper);

        // 转换为DTO
        List<ProdOrderDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageResult<>(dtoList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public ProdOrderDTO getById(Long id) {
        log.info("获取生产订单详情, id={}", id);

        // 查询订单（含属性）
        ProductionOrder productionOrder = productionOrderMapper.selectByIdWithAttrs(id);

        if (productionOrder == null || productionOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "生产订单不存在");
        }

        return convertToDTO(productionOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProdOrderUpdateRequest request) {
        log.info("更新生产订单, id={}, request={}", id, request);

        // 1. 查询订单
        ProductionOrder productionOrder = productionOrderMapper.selectById(id);
        if (productionOrder == null || productionOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "生产订单不存在");
        }

        // 2. 校验状态（仅草稿/待审批可编辑）
        if (!isEditable(productionOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许编辑，当前状态: "
                    + ProductionOrder.Status.getDesc(productionOrder.getStatus()));
        }

        // 3. 更新字段
        if (request.getQty() != null) {
            productionOrder.setQty(request.getQty());
        }
        if (request.getDueDate() != null) {
            if (request.getDueDate().isBefore(LocalDate.now())) {
                throw new BusinessException(40000, "交期不能早于当前日期");
            }
            productionOrder.setDueDate(request.getDueDate());
        }
        if (request.getPriority() != null) {
            productionOrder.setPriority(request.getPriority());
        }
        if (request.getRemark() != null) {
            productionOrder.setRemark(request.getRemark());
        }

        productionOrderMapper.updateById(productionOrder);

        log.info("更新生产订单成功, id={}, prodNo={}", id, productionOrder.getProdNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setAttrs(Long id, ProdOrderAttrsSetRequest request) {
        log.info("设置生产订单属性, id={}, attrCount={}", id, request.getAttrs().size());

        // 1. 查询订单
        ProductionOrder productionOrder = productionOrderMapper.selectById(id);
        if (productionOrder == null || productionOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "生产订单不存在");
        }

        // 2. 校验状态
        if (!isEditable(productionOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许设置属性");
        }

        // 3. 删除旧属性
        attrMapper.deleteByOrdId(id);

        // 4. 插入新属性
        if (request.getAttrs() != null && !request.getAttrs().isEmpty()) {
            List<ProductionOrderAttr> attrs = request.getAttrs().stream()
                    .map(item -> {
                        ProductionOrderAttr attr = new ProductionOrderAttr();
                        attr.setOrdId(id);
                        attr.setAttrKey(item.getAttrKey());
                        attr.setAttrValue(item.getAttrValue());
                        attr.setRemark(item.getRemark());
                        return attr;
                    })
                    .collect(Collectors.toList());

            attrMapper.batchInsert(attrs);
        }

        log.info("设置生产订单属性成功, id={}, attrCount={}", id, request.getAttrs().size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitApprove(Long id) {
        log.info("提交生产订单审批, id={}", id);

        // 1. 查询订单
        ProductionOrder productionOrder = productionOrderMapper.selectById(id);
        if (productionOrder == null || productionOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "生产订单不存在");
        }

        // 2. 校验状态
        if (!ProductionOrder.Status.NEW.getCode().equals(productionOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许提交审批");
        }

        // 3. 更新状态
        productionOrder.setStatus(ProductionOrder.Status.PENDING_APPROVAL.getCode());
        productionOrderMapper.updateById(productionOrder);

        // TODO: 创建审批任务（如果需要审批流程）

        log.info("提交生产订单审批成功, id={}, prodNo={}", id, productionOrder.getProdNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        log.info("审批生产订单, id={}", id);

        // 1. 查询订单
        ProductionOrder productionOrder = productionOrderMapper.selectById(id);
        if (productionOrder == null || productionOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "生产订单不存在");
        }

        // 2. 校验状态
        if (!ProductionOrder.Status.PENDING_APPROVAL.getCode().equals(productionOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许审批");
        }

        // 3. 更新状态
        productionOrder.setStatus(ProductionOrder.Status.APPROVED.getCode());
        productionOrderMapper.updateById(productionOrder);

        log.info("审批生产订单成功, id={}, prodNo={}", id, productionOrder.getProdNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String comment) {
        log.info("驳回生产订单, id={}, comment={}", id, comment);

        // 1. 查询订单
        ProductionOrder productionOrder = productionOrderMapper.selectById(id);
        if (productionOrder == null || productionOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "生产订单不存在");
        }

        // 2. 校验状态
        if (!ProductionOrder.Status.PENDING_APPROVAL.getCode().equals(productionOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许驳回");
        }

        // 3. 更新状态（驳回回到新建状态）
        productionOrder.setStatus(ProductionOrder.Status.NEW.getCode());
        if (comment != null && !comment.isEmpty()) {
            String newRemark = (productionOrder.getRemark() != null ? productionOrder.getRemark() + "; " : "")
                    + "驳回原因: " + comment;
            productionOrder.setRemark(newRemark);
        }
        productionOrderMapper.updateById(productionOrder);

        log.info("驳回生产订单成功, id={}, prodNo={}, comment={}", id, productionOrder.getProdNo(), comment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        log.info("取消生产订单, id={}", id);

        // 1. 查询订单
        ProductionOrder productionOrder = productionOrderMapper.selectById(id);
        if (productionOrder == null || productionOrder.getDeleted() == 1) {
            throw new BusinessException(40400, "生产订单不存在");
        }

        // 2. 校验状态
        if (!isCancellable(productionOrder.getStatus())) {
            throw new BusinessException(40002, "订单状态不允许取消，当前状态: "
                    + ProductionOrder.Status.getDesc(productionOrder.getStatus()));
        }

        // 3. 更新状态
        productionOrder.setStatus(ProductionOrder.Status.CANCELLED.getCode());
        productionOrderMapper.updateById(productionOrder);

        log.info("取消生产订单成功, id={}, prodNo={}", id, productionOrder.getProdNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        log.info("更新生产订单状态, id={}, status={}", id, status);

        ProductionOrder productionOrder = new ProductionOrder();
        productionOrder.setId(id);
        productionOrder.setStatus(status);
        productionOrderMapper.updateById(productionOrder);

        log.info("更新生产订单状态成功, id={}, status={}", id, status);
    }

    /**
     * 检查状态是否可编辑
     */
    private boolean isEditable(Integer status) {
        return ProductionOrder.Status.NEW.getCode().equals(status)
                || ProductionOrder.Status.PENDING_APPROVAL.getCode().equals(status);
    }

    /**
     * 检查状态是否可取消
     */
    private boolean isCancellable(Integer status) {
        return ProductionOrder.Status.NEW.getCode().equals(status)
                || ProductionOrder.Status.PENDING_APPROVAL.getCode().equals(status);
    }

    /**
     * 转换为DTO
     */
    private ProdOrderDTO convertToDTO(ProductionOrder order) {
        ProdOrderDTO dto = new ProdOrderDTO();
        BeanUtil.copyProperties(order, dto);

        // 设置状态文本
        dto.setStatusText(ProductionOrder.Status.getDesc(order.getStatus()));

        // 设置订单类型文本
        dto.setOrderKindText(ProductionOrder.OrderKind.getDesc(order.getOrderKind()));

        // 设置关联信息
        dto.setModelCode(order.getModelCode());
        dto.setModelName(order.getModelName());
        dto.setBomCode(order.getBomCode());
        dto.setSalesNo(order.getSalesNo());

        // 设置属性列表
        if (order.getAttrs() != null) {
            dto.setAttrs(order.getAttrs());
        }

        return dto;
    }
}

