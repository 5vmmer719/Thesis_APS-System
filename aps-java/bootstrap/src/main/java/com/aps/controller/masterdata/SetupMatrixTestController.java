package com.aps.controller.masterdata;

import com.aps.dto.request.masterdata.SetupMatrixBatchImportRequest;
import com.aps.dto.request.masterdata.SetupMatrixCreateRequest;
import com.aps.dto.response.masterdata.SetupMatrixDTO;
import com.aps.masterdata.SetupMatrixService;
import com.aps.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 换型矩阵测试控制器
 * 用于手动测试换型矩阵功能
 *
 * @author APS System
 * @since 2024-01-30
 */
@Slf4j
@Tag(name = "换型矩阵测试", description = "换型矩阵功能测试接口")
@RestController
@RequestMapping("/test/setup-matrix")
@RequiredArgsConstructor
public class SetupMatrixTestController {

    private final SetupMatrixService setupMatrixService;

    @Operation(summary = "初始化测试数据", description = "创建一组完整的测试换型矩阵数据")
    @PostMapping("/init-test-data")
    public ApiResponse<Map<String, Object>> initTestData() {
        log.info("初始化换型矩阵测试数据");

        Map<String, Object> result = new HashMap<>();
        int successCount = 0;

        try {
            // 1. 冲压工艺 - 模具换型矩阵
            successCount += createStampingMatrix();

            // 2. 焊装工艺 - 夹具换型矩阵
            successCount += createWeldingMatrix();

            // 3. 涂装工艺 - 颜色换型矩阵
            successCount += createPaintingMatrix();

            // 4. 总装工艺 - 配置换型矩阵
            successCount += createAssemblyMatrix();

            result.put("success", true);
            result.put("totalCreated", successCount);
            result.put("message", "测试数据初始化成功");

            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("初始化测试数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ApiResponse.error(50000, "初始化失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量导入涂装换型矩阵", description = "使用批量导入功能测试涂装颜色切换")
    @PostMapping("/batch-import-painting")
    public ApiResponse<String> batchImportPainting(@RequestParam(defaultValue = "REPLACE") String mode) {
        log.info("批量导入涂装换型矩阵, mode={}", mode);

        SetupMatrixBatchImportRequest request = new SetupMatrixBatchImportRequest();
        request.setProcessType(3); // 涂装
        request.setMode(mode);

        List<SetupMatrixBatchImportRequest.SetupMatrixItem> items = new ArrayList<>();

        // 红色切换
        items.add(createItem("红色", "白色", 15, 100.00, "红色到白色需要清洗喷枪"));
        items.add(createItem("红色", "黑色", 20, 150.00, "红色到黑色需要更长清洗时间"));
        items.add(createItem("红色", "蓝色", 18, 120.00, "红色到蓝色"));

        // 白色切换
        items.add(createItem("白色", "红色", 10, 80.00, "白色到红色较快"));
        items.add(createItem("白色", "黑色", 12, 90.00, "白色到黑色"));
        items.add(createItem("白色", "蓝色", 15, 100.00, "白色到蓝色"));

        // 黑色切换
        items.add(createItem("黑色", "红色", 25, 200.00, "黑色到红色需要彻底清洗"));
        items.add(createItem("黑色", "白色", 22, 180.00, "黑色到白色需要彻底清洗"));
        items.add(createItem("黑色", "蓝色", 20, 150.00, "黑色到蓝色"));

        // 蓝色切换
        items.add(createItem("蓝色", "红色", 16, 110.00, "蓝色到红色"));
        items.add(createItem("蓝色", "白色", 14, 95.00, "蓝色到白色"));
        items.add(createItem("蓝色", "黑色", 18, 130.00, "蓝色到黑色"));

        request.setItems(items);

        setupMatrixService.batchImportSetupMatrix(request);

        return ApiResponse.success("批量导入成功，共导入 " + items.size() + " 条记录");
    }

    @Operation(summary = "测试换型时间查询", description = "测试查询指定换型的时间")
    @GetMapping("/test-query-setup-time")
    public ApiResponse<Map<String, Object>> testQuerySetupTime(
            @RequestParam(defaultValue = "3") Integer processType,
            @RequestParam String fromKey,
            @RequestParam String toKey) {

        log.info("测试换型时间查询: processType={}, from={}, to={}", processType, fromKey, toKey);

        Integer minutes = setupMatrixService.getSetupMinutes(processType, fromKey, toKey);

        Map<String, Object> result = new HashMap<>();
        result.put("processType", processType);
        result.put("processTypeName", getProcessTypeName(processType));
        result.put("fromKey", fromKey);
        result.put("toKey", toKey);
        result.put("setupMinutes", minutes);
        result.put("setupHours", minutes / 60.0);

        if (minutes == 0) {
            if (fromKey.equals(toKey)) {
                result.put("reason", "相同换型键，无需换型");
            } else {
                result.put("reason", "未配置换型时间，返回默认值0");
            }
        }

        return ApiResponse.success(result);
    }

    @Operation(summary = "获取换型矩阵表格", description = "获取指定工艺的完整换型矩阵表格")
    @GetMapping("/matrix-table/{processType}")
    public ApiResponse<Map<String, Object>> getMatrixTable(@PathVariable Integer processType) {
        log.info("获取换型矩阵表格: processType={}", processType);

        List<SetupMatrixDTO> matrices = setupMatrixService.getActiveSetupMatricesByProcess(processType);

        // 构建矩阵表格
        Map<String, Map<String, Integer>> table = new HashMap<>();
        for (SetupMatrixDTO matrix : matrices) {
            table.computeIfAbsent(matrix.getFromKey(), k -> new HashMap<>())
                    .put(matrix.getToKey(), matrix.getSetupMinutes());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("processType", processType);
        result.put("processTypeName", getProcessTypeName(processType));
        result.put("totalRecords", matrices.size());
        result.put("matrixTable", table);
        result.put("matrices", matrices);

        return ApiResponse.success(result);
    }

    @Operation(summary = "清空测试数据", description = "删除所有测试换型矩阵数据（谨慎使用）")
    @DeleteMapping("/clear-test-data")
    public ApiResponse<String> clearTestData() {
        log.warn("清空换型矩阵测试数据");

        // 注意：这里需要在Service中添加批量删除方法，或者通过查询后逐个删除
        // 为了安全，这里只返回提示信息
        return ApiResponse.success("请通过数据库手动清理测试数据：DELETE FROM md_setup_matrix WHERE 1=1");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 创建冲压工艺换型矩阵
     */
    private int createStampingMatrix() {
        int count = 0;
        String[][] moldPairs = {
                {"MOLD_A", "MOLD_B", "30", "200"},
                {"MOLD_A", "MOLD_C", "45", "300"},
                {"MOLD_B", "MOLD_A", "35", "220"},
                {"MOLD_B", "MOLD_C", "25", "180"},
                {"MOLD_C", "MOLD_A", "40", "280"},
                {"MOLD_C", "MOLD_B", "28", "190"}
        };

        for (String[] pair : moldPairs) {
            try {
                SetupMatrixCreateRequest request = new SetupMatrixCreateRequest();
                request.setProcessType(1);
                request.setFromKey(pair[0]);
                request.setToKey(pair[1]);
                request.setSetupMinutes(Integer.parseInt(pair[2]));
                request.setSetupCost(new BigDecimal(pair[3]));
                request.setStatus(1);
                request.setRemark("冲压模具切换测试数据");
                setupMatrixService.createSetupMatrix(request);
                count++;
            } catch (Exception e) {
                log.warn("创建冲压换型矩阵失败: {} -> {}", pair[0], pair[1]);
            }
        }
        return count;
    }

    /**
     * 创建焊装工艺换型矩阵
     */
    private int createWeldingMatrix() {
        int count = 0;
        String[][] fixturePairs = {
                {"FIX_X1", "FIX_X2", "20", "150"},
                {"FIX_X1", "FIX_Y1", "35", "250"},
                {"FIX_X2", "FIX_X1", "18", "140"},
                {"FIX_X2", "FIX_Y1", "30", "220"},
                {"FIX_Y1", "FIX_X1", "32", "240"},
                {"FIX_Y1", "FIX_X2", "25", "200"}
        };

        for (String[] pair : fixturePairs) {
            try {
                SetupMatrixCreateRequest request = new SetupMatrixCreateRequest();
                request.setProcessType(2);
                request.setFromKey(pair[0]);
                request.setToKey(pair[1]);
                request.setSetupMinutes(Integer.parseInt(pair[2]));
                request.setSetupCost(new BigDecimal(pair[3]));
                request.setStatus(1);
                request.setRemark("焊装夹具切换测试数据");
                setupMatrixService.createSetupMatrix(request);
                count++;
            } catch (Exception e) {
                log.warn("创建焊装换型矩阵失败: {} -> {}", pair[0], pair[1]);
            }
        }
        return count;
    }

    /**
     * 创建涂装工艺换型矩阵
     */
    private int createPaintingMatrix() {
        int count = 0;
        String[][] colorPairs = {
                {"红色", "白色", "15", "100"},
                {"红色", "黑色", "20", "150"},
                {"白色", "红色", "10", "80"},
                {"白色", "黑色", "12", "90"},
                {"黑色", "红色", "25", "200"},
                {"黑色", "白色", "22", "180"}
        };

        for (String[] pair : colorPairs) {
            try {
                SetupMatrixCreateRequest request = new SetupMatrixCreateRequest();
                request.setProcessType(3);
                request.setFromKey(pair[0]);
                request.setToKey(pair[1]);
                request.setSetupMinutes(Integer.parseInt(pair[2]));
                request.setSetupCost(new BigDecimal(pair[3]));
                request.setStatus(1);
                request.setRemark("涂装颜色切换测试数据");
                setupMatrixService.createSetupMatrix(request);
                count++;
            } catch (Exception e) {
                log.warn("创建涂装换型矩阵失败: {} -> {}", pair[0], pair[1]);
            }
        }
        return count;
    }

    /**
     * 创建总装工艺换型矩阵
     */
    private int createAssemblyMatrix() {
        int count = 0;
        String[][] configPairs = {
                {"BASE", "PREMIUM", "10", "50"},
                {"BASE", "LUXURY", "15", "80"},
                {"PREMIUM", "BASE", "8", "40"},
                {"PREMIUM", "LUXURY", "12", "60"},
                {"LUXURY", "BASE", "18", "100"},
                {"LUXURY", "PREMIUM", "10", "55"}
        };

        for (String[] pair : configPairs) {
            try {
                SetupMatrixCreateRequest request = new SetupMatrixCreateRequest();
                request.setProcessType(4);
                request.setFromKey(pair[0]);
                request.setToKey(pair[1]);
                request.setSetupMinutes(Integer.parseInt(pair[2]));
                request.setSetupCost(new BigDecimal(pair[3]));
                request.setStatus(1);
                request.setRemark("总装配置切换测试数据");
                setupMatrixService.createSetupMatrix(request);
                count++;
            } catch (Exception e) {
                log.warn("创建总装换型矩阵失败: {} -> {}", pair[0], pair[1]);
            }
        }
        return count;
    }

    /**
     * 创建导入项
     */
    private SetupMatrixBatchImportRequest.SetupMatrixItem createItem(
            String fromKey, String toKey, int minutes, double cost, String remark) {
        SetupMatrixBatchImportRequest.SetupMatrixItem item = new SetupMatrixBatchImportRequest.SetupMatrixItem();
        item.setFromKey(fromKey);
        item.setToKey(toKey);
        item.setSetupMinutes(minutes);
        item.setSetupCost(BigDecimal.valueOf(cost));
        item.setRemark(remark);
        return item;
    }

    /**
     * 获取工艺类型名称
     */
    private String getProcessTypeName(Integer processType) {
        switch (processType) {
            case 1: return "冲压";
            case 2: return "焊装";
            case 3: return "涂装";
            case 4: return "总装";
            default: return "未知";
        }
    }
}

