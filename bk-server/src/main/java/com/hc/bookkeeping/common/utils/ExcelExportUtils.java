package com.hc.bookkeeping.common.utils;

import cn.hutool.core.date.DateUtil;
import com.hc.bookkeeping.common.dto.ExcelExportDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.ReflectionUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ExcelExportUtils {

    public static <T> void export(HttpServletResponse response, ExcelExportDto dto) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(dto.getSheetName());

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < dto.getColumns().size(); i++) {
            ExcelExportDto.ColumnConfig<?> column = dto.getColumns().get(i);
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column.getHeader());
            cell.setCellStyle(headerStyle);
        }

        for (int rowIdx = 0; rowIdx < dto.getData().size(); rowIdx++) {
            Object rowData = dto.getData().get(rowIdx);
            Row row = sheet.createRow(rowIdx + 1);

            for (int colIdx = 0; colIdx < dto.getColumns().size(); colIdx++) {
                ExcelExportDto.ColumnConfig<T> column = (ExcelExportDto.ColumnConfig<T>) dto.getColumns().get(colIdx);
                Cell cell = row.createCell(colIdx);

                T value = getFieldValue(rowData, column.getField());
                if (column.getConverter() != null) {
                    cell.setCellValue(column.getConverter().apply(value));
                } else {
                    cell.setCellValue(String.valueOf(value));
                }
            }
        }

        for (int i = 0; i < dto.getColumns().size(); i++) {
            sheet.autoSizeColumn(i);
        }

        String fileName = dto.getSheetName() + "_" + DateUtil.format(DateUtil.date(), "yyyy-MM-dd") + ".xlsx";
        fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        OutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        outputStream.flush();
        outputStream.close();
        workbook.close();
    }

    private static <T> T getFieldValue(Object obj, String fieldName) {
        if (obj == null || StringUtils.isEmpty(fieldName)) {
            return null;
        }
        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);
        if (field == null) {
            log.error("{} 中找不到字段: {}", obj, fieldName);
            return null;
        }
        ReflectionUtils.makeAccessible(field);
        return (T) ReflectionUtils.getField(field, obj);
    }
}
