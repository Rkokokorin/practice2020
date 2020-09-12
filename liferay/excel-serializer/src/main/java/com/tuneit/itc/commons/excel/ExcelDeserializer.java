package com.tuneit.itc.commons.excel;

import lombok.Data;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class ExcelDeserializer {

    private static final Logger logger = Logger.getLogger(ExcelDeserializer.class.getCanonicalName());


    private final Function<Cell, String> stringDeser = ExcelDeserializer::getCellValue;
    private final Function<Cell, Long> longDeser = cell -> ((long) cell.getNumericCellValue());
    private final Function<Cell, Date> dateDeser = Cell::getDateCellValue;
    private final Function<Cell, Double> doubleDeser = Cell::getNumericCellValue;
    private final Function<Cell, Integer> intDeser = cell -> ((int) cell.getNumericCellValue());
    private final Function<Cell, Boolean> boolDeser = Cell::getBooleanCellValue;

    private Map<Class<?>, Function<Cell, ?>> deserializers;

    {
        deserializers = new HashMap<>();
        deserializers.put(String.class, stringDeser);
        deserializers.put(Long.class, longDeser);
        deserializers.put(Date.class, dateDeser);
        deserializers.put(Double.class, doubleDeser);
        deserializers.put(Integer.class, intDeser);
        deserializers.put(Boolean.class, boolDeser);

    }

    public static String getCellValue(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case NUMERIC:
                String dataFormatString = cell.getCellStyle().getDataFormatString();
                double numericCellValue = cell.getNumericCellValue();
                if (Objects.equals(dataFormatString, "General")) {
                    long longVal = (long) numericCellValue;
                    if (Math.abs(numericCellValue - longVal) < 0.0000001) {
                        return String.valueOf(longVal);
                    } else {
                        return new DecimalFormat("#").format(numericCellValue);
                    }
                } else {
                    return String.format(dataFormatString, numericCellValue);

                }
            case BLANK:
                return null;
            default:
                logger.warning(() -> "Unknown type " + cell.getCellTypeEnum());
        }
        return null;
    }

    private static Class<?> getType(Field field) {
        Class<?> type = field.getType();
        if (!type.isPrimitive()) {
            return type;
        }
        if (type.equals(int.class)) {
            return Integer.class;
        } else if (type.equals(long.class)) {
            return Long.class;
        } else if (type.equals(double.class)) {
            return Double.class;
        } else if (type.equals(Boolean.class)) {
            return Boolean.class;
        }
        throw new IllegalArgumentException("Type " + type + " is unsupported");
    }

    public <T> List<Result<T>> parse(Class<? extends T> rowType, Map<String, String> columnNameToField, Sheet sheet) {
        Map<String, Field> fieldByColumnName = new HashMap<>();
        columnNameToField.forEach((columnName, fieldName) -> {
            try {
                Field field = rowType.getDeclaredField(fieldName);
                field.trySetAccessible();
                fieldByColumnName.put(columnName, field);
            } catch (NoSuchFieldException e) {
                logger.severe(() -> String.format("Class %s has no field %s", rowType, fieldName));
            }
        });
        return parseInternal(rowType, fieldByColumnName, sheet);
    }

    public <T> List<Result<T>> parse(Class<? extends T> rowType, Sheet sheet) {
        Map<String, Field> fieldByColumnName = Arrays.stream(rowType.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(TableColumn.class))
            .peek(f -> f.setAccessible(true))
            .collect(Collectors.toMap(f -> f.getAnnotation(TableColumn.class).columnName(), f -> f));
        return parseInternal(rowType, fieldByColumnName, sheet);
    }

    private <T> List<Result<T>> parseInternal(Class<? extends T> rowType, Map<String, Field> fieldByColumnName,
                                              Sheet sheet) {

        Iterator<Row> iter = sheet.rowIterator();
        Row header = iter.next();
        List<Result<T>> results = new ArrayList<>();
        iter.forEachRemaining(row -> {
            if (isEmpty(row)) {
                return;
            }
            T newRes = newInstance(rowType);
            Map<String, String> remainingCols = new LinkedHashMap<>();
            short fst = row.getFirstCellNum();
            short lst = row.getLastCellNum();
            for (int i = fst; i < lst; ++i) {
                Cell cell = row.getCell(i);
                if (cell == null || isEmpty(cell)) {
                    continue;
                }
                String headerCellVal = header.getCell(i).getStringCellValue().trim();
                Field field = fieldByColumnName.get(headerCellVal);
                if (field == null) {
                    remainingCols.put(headerCellVal, getCellValue(cell));
                } else {
                    Function<Cell, ?> deser = getDeserializer(getType(field));
                    if (deser == null) {
                        throw new IllegalStateException("Type " + field.getType() + " is unsupported");
                    }
                    try {
                        field.set(newRes, deser.apply(cell));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            results.add(new Result<>(newRes, remainingCols));
        });
        return results;

    }

    private boolean isEmpty(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case BOOLEAN:
            case NUMERIC:
            case FORMULA:
                return false;
            case STRING:
                return cell.getStringCellValue().trim().isEmpty();
            default:
                return true;
        }
    }

    private boolean isEmpty(Row row) {
        Iterator<Cell> iter = row.cellIterator();
        while (iter.hasNext()) {
            Cell next = iter.next();
            boolean empty = isEmpty(next);
            if (!empty) {
                return false;
            }
        }
        return true;
    }

    @SneakyThrows
    private <T> T newInstance(Class<T> ofClass) {
        return ofClass.getDeclaredConstructor().newInstance();
    }

    private Function<Cell, ?> getDeserializer(Class<?> forClass) {
        return deserializers.get(forClass);
    }

    @Data
    public static class Result<T> {
        private final T parsed;
        private final Map<String, String> unrecognized;
    }
}
