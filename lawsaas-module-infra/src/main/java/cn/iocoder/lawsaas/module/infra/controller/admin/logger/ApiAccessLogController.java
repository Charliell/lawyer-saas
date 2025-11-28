package cn.iocoder.lawsaas.module.infra.controller.admin.logger;

import cn.iocoder.lawsaas.module.infra.dal.dataobject.logger.ApiAccessLogDO;
import cn.iocoder.lawsaas.module.infra.service.logger.ApiAccessLogService;
import cn.iocoder.lawsaas.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.lawsaas.framework.common.pojo.CommonResult;
import cn.iocoder.lawsaas.framework.common.pojo.PageParam;
import cn.iocoder.lawsaas.framework.common.pojo.PageResult;
import cn.iocoder.lawsaas.framework.common.util.object.BeanUtils;
import cn.iocoder.lawsaas.framework.excel.core.util.ExcelUtils;
import cn.iocoder.lawsaas.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogPageReqVO;
import cn.iocoder.lawsaas.module.infra.controller.admin.logger.vo.apiaccesslog.ApiAccessLogRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.lawsaas.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.lawsaas.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - API 访问日志")
@RestController
@RequestMapping("/infra/api-access-log")
@Validated
public class ApiAccessLogController {

    @Resource
    private ApiAccessLogService apiAccessLogService;

    @GetMapping("/page")
    @Operation(summary = "获得API 访问日志分页")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:query')")
    public CommonResult<PageResult<ApiAccessLogRespVO>> getApiAccessLogPage(@Valid ApiAccessLogPageReqVO pageReqVO) {
        PageResult<ApiAccessLogDO> pageResult = apiAccessLogService.getApiAccessLogPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ApiAccessLogRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出API 访问日志 Excel")
    @PreAuthorize("@ss.hasPermission('infra:api-access-log:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportApiAccessLogExcel(@Valid ApiAccessLogPageReqVO exportReqVO,
                                        HttpServletResponse response) throws IOException {
        exportReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<ApiAccessLogDO> list = apiAccessLogService.getApiAccessLogPage(exportReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "API 访问日志.xls", "数据", ApiAccessLogRespVO.class,
                BeanUtils.toBean(list, ApiAccessLogRespVO.class));
    }

}
