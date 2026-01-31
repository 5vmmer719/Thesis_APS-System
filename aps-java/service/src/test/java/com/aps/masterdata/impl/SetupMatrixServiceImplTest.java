package com.aps.masterdata.impl;

import com.aps.dto.request.masterdata.SetupMatrixBatchImportRequest;
import com.aps.dto.request.masterdata.SetupMatrixCreateRequest;
import com.aps.dto.request.masterdata.SetupMatrixQueryRequest;
import com.aps.dto.request.masterdata.SetupMatrixUpdateRequest;
import com.aps.dto.response.masterdata.SetupMatrixDTO;
import com.aps.entity.masterdata.SetupMatrix;
import com.aps.exception.BusinessException;
import com.aps.mapper.masterdata.SetupMatrixMapper;
import com.aps.response.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 换型矩阵Service单元测试
 *
 * @author APS System
 * @since 2024-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("换型矩阵Service单元测试")
class SetupMatrixServiceImplTest {

    @Mock
    private SetupMatrixMapper setupMatrixMapper;

    @InjectMocks
    private SetupMatrixServiceImpl setupMatrixService;

    private SetupMatrix testMatrix;
    private SetupMatrixCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testMatrix = new SetupMatrix();
        testMatrix.setId(1L);
        testMatrix.setProcessType(3); // 涂装
        testMatrix.setFromKey("红色");
        testMatrix.setToKey("白色");
        testMatrix.setSetupMinutes(15);
        testMatrix.setSetupCost(BigDecimal.valueOf(100.00));
        testMatrix.setStatus(1);
        testMatrix.setRemark("测试换型矩阵");

        createRequest = new SetupMatrixCreateRequest();
        createRequest.setProcessType(3);
        createRequest.setFromKey("红色");
        createRequest.setToKey("白色");
        createRequest.setSetupMinutes(15);
        createRequest.setSetupCost(BigDecimal.valueOf(100.00));
        createRequest.setStatus(1);
    }

    @Test
    @DisplayName("分页查询换型矩阵 - 成功")
    void testListSetupMatrices_Success() {
        // Given
        SetupMatrixQueryRequest request = new SetupMatrixQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setProcessType(3);

        Page<SetupMatrix> mockPage = new Page<>(1, 10);
        mockPage.setRecords(Arrays.asList(testMatrix));
        mockPage.setTotal(1);

        when(setupMatrixMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        // When
        PageResult<SetupMatrixDTO> result = setupMatrixService.listSetupMatrices(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("红色", result.getRecords().get(0).getFromKey());
        verify(setupMatrixMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据ID获取换型矩阵 - 成功")
    void testGetSetupMatrixById_Success() {
        // Given
        when(setupMatrixMapper.selectById(1L)).thenReturn(testMatrix);

        // When
        SetupMatrixDTO result = setupMatrixService.getSetupMatrixById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("红色", result.getFromKey());
        assertEquals("白色", result.getToKey());
        assertEquals("涂装", result.getProcessTypeName());
        verify(setupMatrixMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("根据ID获取换型矩阵 - 不存在")
    void testGetSetupMatrixById_NotFound() {
        // Given
        when(setupMatrixMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            setupMatrixService.getSetupMatrixById(999L);
        });
        assertEquals(40401, exception.getCode());
        assertTrue(exception.getMessage().contains("不存在"));
    }

    @Test
    @DisplayName("创建换型矩阵 - 成功")
    void testCreateSetupMatrix_Success() {
        // Given
        when(setupMatrixMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // 模拟 MyBatis-Plus 的 id 自动回填行为
        doAnswer(invocation -> {
            SetupMatrix matrix = invocation.getArgument(0);
            matrix.setId(1L); // 模拟数据库生成的 ID
            return 1;
        }).when(setupMatrixMapper).insert(any(SetupMatrix.class));

        // When
        Long id = setupMatrixService.createSetupMatrix(createRequest);

        // Then
        assertNotNull(id);
        assertEquals(1L, id);
        verify(setupMatrixMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(setupMatrixMapper, times(1)).insert(any(SetupMatrix.class));
    }

    @Test
    @DisplayName("创建换型矩阵 - 重复")
    void testCreateSetupMatrix_Duplicate() {
        // Given
        when(setupMatrixMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            setupMatrixService.createSetupMatrix(createRequest);
        });
        assertEquals(40001, exception.getCode());
        assertTrue(exception.getMessage().contains("已存在"));
        verify(setupMatrixMapper, never()).insert(any(SetupMatrix.class));
    }

    @Test
    @DisplayName("更新换型矩阵 - 成功")
    void testUpdateSetupMatrix_Success() {
        // Given
        SetupMatrixUpdateRequest updateRequest = new SetupMatrixUpdateRequest();
        updateRequest.setId(1L);
        updateRequest.setSetupMinutes(20);
        updateRequest.setSetupCost(BigDecimal.valueOf(150.00));

        when(setupMatrixMapper.selectById(1L)).thenReturn(testMatrix);
        when(setupMatrixMapper.updateById(any(SetupMatrix.class))).thenReturn(1);

        // When
        setupMatrixService.updateSetupMatrix(updateRequest);

        // Then
        verify(setupMatrixMapper, times(1)).selectById(1L);
        verify(setupMatrixMapper, times(1)).updateById(any(SetupMatrix.class));
    }

    @Test
    @DisplayName("删除换型矩阵 - 成功")
    void testDeleteSetupMatrix_Success() {
        // Given
        when(setupMatrixMapper.selectById(1L)).thenReturn(testMatrix);
        when(setupMatrixMapper.deleteById(1L)).thenReturn(1);

        // When
        setupMatrixService.deleteSetupMatrix(1L);

        // Then
        verify(setupMatrixMapper, times(1)).selectById(1L);
        verify(setupMatrixMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("批量导入换型矩阵 - REPLACE模式")
    void testBatchImportSetupMatrix_ReplaceMode() {
        // Given
        SetupMatrixBatchImportRequest request = new SetupMatrixBatchImportRequest();
        request.setProcessType(3);
        request.setMode("REPLACE");

        SetupMatrixBatchImportRequest.SetupMatrixItem item1 = new SetupMatrixBatchImportRequest.SetupMatrixItem();
        item1.setFromKey("红色");
        item1.setToKey("白色");
        item1.setSetupMinutes(15);
        item1.setSetupCost(BigDecimal.valueOf(100.00));

        SetupMatrixBatchImportRequest.SetupMatrixItem item2 = new SetupMatrixBatchImportRequest.SetupMatrixItem();
        item2.setFromKey("白色");
        item2.setToKey("黑色");
        item2.setSetupMinutes(12);
        item2.setSetupCost(BigDecimal.valueOf(90.00));

        request.setItems(Arrays.asList(item1, item2));

        when(setupMatrixMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(5);
        when(setupMatrixMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        // 模拟 MyBatis-Plus 的 id 自动回填行为
        doAnswer(invocation -> {
            SetupMatrix matrix = invocation.getArgument(0);
            matrix.setId(System.currentTimeMillis()); // 模拟数据库生成的 ID
            return 1;
        }).when(setupMatrixMapper).insert(any(SetupMatrix.class));

        // When
        setupMatrixService.batchImportSetupMatrix(request);

        // Then
        verify(setupMatrixMapper, times(1)).delete(any(LambdaQueryWrapper.class)); // 删除旧数据
        verify(setupMatrixMapper, times(2)).insert(any(SetupMatrix.class)); // 插入2条新数据
    }

    @Test
    @DisplayName("查询换型时间 - 找到匹配")
    void testGetSetupMinutes_Found() {
        // Given
        when(setupMatrixMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testMatrix);

        // When
        Integer minutes = setupMatrixService.getSetupMinutes(3, "红色", "白色");

        // Then
        assertEquals(15, minutes);
        verify(setupMatrixMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("查询换型时间 - 未找到")
    void testGetSetupMinutes_NotFound() {
        // Given
        when(setupMatrixMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        Integer minutes = setupMatrixService.getSetupMinutes(3, "红色", "蓝色");

        // Then
        assertEquals(0, minutes); // 未找到返回0
        verify(setupMatrixMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("查询换型时间 - 相同换型键")
    void testGetSetupMinutes_SameKey() {
        // When
        Integer minutes = setupMatrixService.getSetupMinutes(3, "红色", "红色");

        // Then
        assertEquals(0, minutes); // 相同换型键返回0
        verify(setupMatrixMapper, never()).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取指定工艺的所有启用换型矩阵")
    void testGetActiveSetupMatricesByProcess() {
        // Given
        SetupMatrix matrix2 = new SetupMatrix();
        matrix2.setId(2L);
        matrix2.setProcessType(3);
        matrix2.setFromKey("白色");
        matrix2.setToKey("黑色");
        matrix2.setSetupMinutes(12);
        matrix2.setSetupCost(BigDecimal.valueOf(90.00));
        matrix2.setStatus(1);

        when(setupMatrixMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(testMatrix, matrix2));

        // When
        List<SetupMatrixDTO> result = setupMatrixService.getActiveSetupMatricesByProcess(3);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(setupMatrixMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }
}

