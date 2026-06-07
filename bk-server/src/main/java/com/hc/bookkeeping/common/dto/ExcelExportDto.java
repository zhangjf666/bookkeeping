package com.hc.bookkeeping.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

@Data
public class ExcelExportDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sheetName;

    private List<ColumnConfig<?>> columns;

    private List<?> data;

    @Data
    public static class ColumnConfig<T> implements Serializable {
        
        private static final long serialVersionUID = 1L;

        private String header;

        private String field;

        private Function<T, String> converter;
    }
}
