package com.wms.controller;

import com.wms.annotation.RequirePermission;
import com.wms.common.Result;
import com.wms.service.ReportService;
import com.wms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @RequirePermission("dashboard:view")
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.success(reportService.getDashboard());
    }

    @RequirePermission("report:sale:rank")
    @GetMapping("/sale/rank")
    public Result<List<SaleRankVO>> getSaleRank(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(reportService.getSaleRank(startDate, endDate, limit));
    }

    @RequirePermission("report:purchase:trend")
    @GetMapping("/purchase/trend")
    public Result<List<Map<String, Object>>> getPurchaseTrend(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(reportService.getPurchaseTrend(startDate, endDate));
    }

    @RequirePermission("report:stock:warning")
    @GetMapping("/stock/warning")
    public Result<Map<String, Object>> getStockWarningReport() {
        return Result.success(reportService.getStockWarningReportWithStats());
    }

    @RequirePermission("report:profit")
    @GetMapping("/profit")
    public Result<Map<String, Object>> getProfitAnalysis(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(reportService.getProfitReport(startDate, endDate));
    }
}
