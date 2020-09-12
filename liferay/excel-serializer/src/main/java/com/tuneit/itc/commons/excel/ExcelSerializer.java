package com.tuneit.itc.commons.excel;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


/**
 * Class for write java beans into table in Microsoft formats (xlx/xlsx) using Apache POI for this purpose.
 *
 * <p>Every instance of serializer associated with single instance of {@link org.apache.poi.ss.usermodel.Workbook}.</p>
 *
 * <p>Supported types of java bean properties: {@code byte}, {@code short}, {@code int}, {@code long}, {@code char},
 * {@link Byte}, {@link Short}, {@link Integer}, {@link Long}, {@link Character}, {@link String}, {@link Date},
 * {@link Calendar}</p>
 */
public class ExcelSerializer {

    private static final TableObjectWriter nullWriter =
        ((object, cell) -> cell.setCellValue(""));
    private final Logger log = Logger.getLogger(ExcelDeserializer.class.getCanonicalName());
    private final Set<Class<?>> supportedClasses;
    private final Map<Class<?>, TableObjectWriter> writers;
    private final TableObjectWriter integerWriter =
        (object, cell) -> cell.setCellValue((((Integer) object).doubleValue()));
    private final TableObjectWriter floatWriter =
        (object, cell) -> cell.setCellValue((Float) object);
    private final TableObjectWriter doubleWriter =
        (object, cell) -> cell.setCellValue((Double) object);
    private final TableObjectWriter byteWriter =
        (object, cell) -> cell.setCellValue(((Byte) object).doubleValue());
    private final TableObjectWriter longWriter =
        (object, cell) -> cell.setCellValue(((Long) object).doubleValue());
    private final TableObjectWriter stringWriter =
        (object, cell) -> cell.setCellValue(((String) object));
    private final TableObjectWriter dateWriter;
    private final TableObjectWriter calendarWriter =
        (object, cell) -> cell.setCellValue(((Calendar) object));
    private final TableObjectWriter shortWriter =
        (object, cell) -> cell.setCellValue(((Short) object).doubleValue());
    private final TableObjectWriter charWriter =
        (object, cell) -> cell.setCellValue(((Character) object).toString());
    private final CellStyle dateCellStyle;

    private final Map<Class<?>, TableObjectWriter> writersCache = new HashMap<>();

    private Workbook workbook;

    public ExcelSerializer(Workbook wb) {
        this.workbook = wb;
        CreationHelper createHelper = wb.getCreationHelper();
        dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));
        dateWriter = (object, cell) -> {
            cell.setCellStyle(dateCellStyle);
            cell.setCellValue(((Date) object));
        };
        Map<Class<?>, TableObjectWriter> localWriters = new HashMap<>();
        localWriters.put(int.class, integerWriter);
        localWriters.put(Integer.class, integerWriter);
        localWriters.put(Float.class, floatWriter);
        localWriters.put(float.class, floatWriter);
        localWriters.put(double.class, doubleWriter);
        localWriters.put(Double.class, doubleWriter);
        localWriters.put(Byte.class, byteWriter);
        localWriters.put(byte.class, byteWriter);
        localWriters.put(Character.class, charWriter);
        localWriters.put(char.class, charWriter);
        localWriters.put(Long.class, longWriter);
        localWriters.put(long.class, longWriter);
        localWriters.put(String.class, stringWriter);
        localWriters.put(Date.class, dateWriter);
        localWriters.put(java.sql.Date.class, dateWriter);
        localWriters.put(java.sql.Time.class, dateWriter);
        localWriters.put(java.sql.Timestamp.class, dateWriter);
        localWriters.put(Calendar.class, calendarWriter);
        localWriters.put(Short.class, shortWriter);
        localWriters.put(short.class, shortWriter);

        writers = Collections.unmodifiableMap(localWriters);
        supportedClasses = writers.keySet();
    }

    /**
     * Appends single object to specified {@code sheet}.
     *
     * <p>Reads every field (expecting {@code transient}) using getter method (named according java beans convention)
     * and writes it into cell according to {@link TableColumn#column()} (undefined behaviour if not specified).
     *
     * @param obj   java bean to write into table
     * @param sheet to write
     * @throws NonSerializableTypeException if can not find writer to table cell for this type of object.
     * @throws ObjectFieldAccessException   if can not access to field using property (there is not getter method,
     *                                      getter is not accessible or not truly java getter method).
     */
    public void writeObject(Object obj, Sheet sheet) {
        if (sheet.getWorkbook() != this.workbook) {
            throw new SheetFromOtherWorkbookException();
        }
        List<PropertyWithXlsxMeta<Object>> objectProperties = readObjectProperties(obj);
        validateObjects(objectProperties);
        int lastRowNum = sheet.getPhysicalNumberOfRows();
        Row row = sheet.createRow(lastRowNum);
        for (int i = 0; i < objectProperties.size(); i++) {
            PropertyWithXlsxMeta<Object> prop = objectProperties.get(i);
            int columnInd = prop.meta == null ? i : prop.meta.column();
            Cell cell = row.createCell(columnInd >= 0 ? columnInd : i);
            TableObjectWriter writer = findWriter(prop.property);
            if (writer == null) {
                throw new NonSerializableTypeException(prop.property);
            }
            writer.writeObject(prop.property, cell);
        }

    }

    /**
     * Writes every object in {@code objects} into specified {@code sheet} using
     * {@link ExcelSerializer#writeObject(Object, Sheet)}.
     *
     * <p>May leave sheet in inconsistent state (if exception occurred).
     *
     * @param objects objects to write into sheet
     * @param sheet   sheet to write
     */
    public void writeCollection(List<?> objects, Sheet sheet) {
        objects.forEach(o -> writeObject(o, sheet));
    }


    private void validateObjects(Collection<PropertyWithXlsxMeta<Object>> objects) {
        Optional<Object> anyNonSerializable = objects.stream()
            .map(o -> o.property)
            .filter(o -> !validateObject(o))
            .findAny();
        if (anyNonSerializable.isPresent()) {
            throw new NonSerializableTypeException(anyNonSerializable.get());
        }
    }

    private boolean validateObject(Object object) {
        return findWriter(object) != null;
    }

    private List<PropertyWithXlsxMeta<Object>> readObjectProperties(Object obj) {
        return Arrays.stream(obj.getClass().getDeclaredFields())
            .filter(f -> !Modifier.isTransient(f.getModifiers()))
            .map(f -> new PropertyWithXlsxMeta<>(f.getName(), f.getAnnotation(TableColumn.class)))
            .map(fld -> new PropertyWithXlsxMeta<>("get" + fld.property.substring(0, 1).toUpperCase()
                + fld.property.substring(1), fld.meta))
            .map(getterName -> {
                try {
                    Method getter = obj.getClass().getMethod(getterName.property);
                    TableColumn tableColumn = getter.getAnnotation(TableColumn.class);
                    return new PropertyWithXlsxMeta<>(getter, tableColumn != null ? tableColumn : getterName.meta);
                } catch (NoSuchMethodException e) {
                    log.severe(() -> String.format("An error occurred during object to xlsx serialization - " +
                        "can not find getter method named %s.", getterName));
                    throw new ObjectFieldAccessException(e);
                }
            })
            .map(getter -> {
                try {
                    Object getterRes = getter.property.invoke(obj);
                    return new PropertyWithXlsxMeta<>(getterRes, getter.meta);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.severe(() -> String.format("Unexpected error occurred during invocation of " +
                            "accessor method %s, exception %s",
                        getter.property.toGenericString(), e.toString()));
                    throw new ObjectFieldAccessException(e);
                }
            }).collect(Collectors.toList());
    }

    private TableObjectWriter findWriter(Object obj) {
        if (obj == null) {
            return nullWriter;
        }
        Class<?> clazz = obj.getClass();
        TableObjectWriter defaultWriter = writers.get(clazz);
        if (defaultWriter != null) {
            return defaultWriter;
        }
        return null;
        /*TableObjectWriter cachedWriter = writersCache.get(clazz);
        if (cachedWriter != null) {
            return cachedWriter;
        }
        List<Class<?>> superclasses = writers.keySet().stream()
                .filter(cl -> cl.isAssignableFrom(clazz))
                .collect(Collectors.toList());
        if (superclasses.isEmpty()) {
            return null;
        }

        Class<?> aClass = superclasses.get(0);
        TableObjectWriter suitableWriter = writers.get(aClass);
        writersCache.put(clazz, suitableWriter);
        return suitableWriter;*/
    }

    private interface TableObjectWriter {
        void writeObject(Object object, Cell cell);
    }

    public abstract static class XlsxSerializationException extends RuntimeException {
        public XlsxSerializationException() {
        }

        public XlsxSerializationException(String s) {
            super(s);
        }

        public XlsxSerializationException(String s, Throwable throwable) {
            super(s, throwable);
        }

        public XlsxSerializationException(Throwable throwable) {
            super(throwable);
        }

        public XlsxSerializationException(String s, Throwable throwable, boolean b, boolean b1) {
            super(s, throwable, b, b1);
        }
    }

    public static class NonSerializableTypeException extends XlsxSerializationException {
        NonSerializableTypeException(Object wrongObject) {
            super("Can't write object " + wrongObject.toString() + " of type " + wrongObject.getClass().toString()
                + " to xlsx");
        }
    }

    public static class ObjectFieldAccessException extends XlsxSerializationException {
        ObjectFieldAccessException(Throwable throwable) {
            super(throwable);
        }
    }

    public static class SheetFromOtherWorkbookException extends XlsxSerializationException {
        SheetFromOtherWorkbookException() {
            super("Specified sheet belongs to other workbook");
        }
    }

    @AllArgsConstructor
    private static class PropertyWithXlsxMeta<S> {
        final S property;
        final TableColumn meta;
    }

}
