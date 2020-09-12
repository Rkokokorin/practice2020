package com.tuneit.itc.bom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mozilla.universalchardet.UniversalDetector;
import org.primefaces.model.ByteArrayContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;

import com.tuneit.itc.commons.excel.ExcelDeserializer;

@ManagedBean
@ApplicationScoped
public class QueryParserService {

    public static final String QUERY_COLUMN = "query";
    public static final String MANUFACTURER_COLUMN = "manufacturer";
    public static final String ARTICLE_COLUMN = "article";
    public static final String CSV_MIME_TYPE = "text/csv";
    public static final String TEXT_PLAIN_TYPE = "text/plain";
    public static final String XLS_MIME_TYPE = "application/vnd.ms-excel";
    public static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String OCTET_STREAM_MIME_TYPE = "application/octet-stream";
    public static final String CSV_EXTENSION = ".csv";
    public static final String XLS_EXTENSION = ".xls";
    public static final String XLSX_EXTENSION = ".xlsx";
    private static List<String> columns = new ArrayList<>(
        Arrays.asList(QUERY_COLUMN, MANUFACTURER_COLUMN, ARTICLE_COLUMN));
    public static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.withHeader(columns.toArray(new String[] {}));
    private final Logger logger = LoggerFactory.getLogger(QueryParserService.class);

    public List<BOMQueryRecord> parseQuery(UploadedFile file) {
        if (file == null) {
            logger.warn("Try to process null file!");
            return null;
        }
        logger.debug("Parse file {0} with type {1} and size {3,number,integer}",
            file.getFileName(), file.getContentType(), file.getSize());

        Map<String, String> columnNameToFieldName = new HashMap<>();
        columnNameToFieldName.put(QUERY_COLUMN, QUERY_COLUMN);
        columnNameToFieldName.put(MANUFACTURER_COLUMN, MANUFACTURER_COLUMN);
        columnNameToFieldName.put(ARTICLE_COLUMN, ARTICLE_COLUMN);
        String contentType = file.getContentType();
        String fileName = file.getFileName();
        fileName = fileName == null ? "" : fileName;
        fileName = fileName.toLowerCase();
        if (Objects.equals(contentType, CSV_MIME_TYPE) || Objects.equals(contentType, TEXT_PLAIN_TYPE)) {
            return parseCsvBom(file.getContents());
        } else if (Objects.equals(contentType, XLS_MIME_TYPE) || Objects.equals(contentType, XLSX_MIME_TYPE)) {
            return parseExcel(file, columnNameToFieldName);
        } else if (fileName.endsWith(CSV_EXTENSION)) {
            return parseCsvBom(file.getContents());
        } else if (fileName.endsWith(XLS_EXTENSION) || fileName.endsWith(XLSX_EXTENSION)) {
            return parseExcel(file, columnNameToFieldName);
        } else if (Objects.equals(OCTET_STREAM_MIME_TYPE, contentType)) {
            return parseExcel(file, columnNameToFieldName);
        } else {
            throw new CorruptedFileException("Unsupported type " + contentType);
        }
    }

    private List<BOMQueryRecord> parseExcel(UploadedFile file, Map<String, String> columnNameToFieldName) {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(file.getContents()))) {
            Sheet firstSheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = firstSheet.rowIterator();
            if (!rowIterator.hasNext()) {
                throw new EmptyFileException();
            }
            Row headerRow = rowIterator.next();
            List<String> header = new ArrayList<>();
            headerRow.cellIterator().forEachRemaining(cell -> header.add(ExcelDeserializer.getCellValue(cell)));
            validateHeaderRow(header);
            ExcelDeserializer deserializer = new ExcelDeserializer();
            return deserializer.parse(BOMQueryRecord.class, columnNameToFieldName, firstSheet)
                .stream()
                .map(ExcelDeserializer.Result::getParsed)
                .collect(Collectors.toList());
        } catch (IOException exc) {
            logger.warn("File has Excel type but can not be processed by Apache POI {0}", exc.getMessage());
            throw new CorruptedFileException("file " + file.getFileName() + " type " + file.getContentType());
        }
    }


    private List<BOMQueryRecord> parseCsvBom(byte[] uploadedCsvData) {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(uploadedCsvData, 0, uploadedCsvData.length);
        detector.dataEnd();
        String detectedCharset = detector.getDetectedCharset();
        detector.reset();
        logger.debug("Detected charset: {0}", detectedCharset);
        if (detectedCharset == null) {
            detectedCharset = StandardCharsets.UTF_8.name();
        }
        String uploadedCsv = new String(uploadedCsvData, Charset.forName(detectedCharset));

        CSVParser parsedCsv;

        try {
            parsedCsv = CSVParser.parse(uploadedCsv, CSV_FORMAT);
        } catch (IOException e) {
            logger.error("Unexpected IOException!", e);
            throw new CorruptedFileException("Can not parse file");
        }
        Iterator<CSVRecord> rowIterator = parsedCsv.iterator();
        if (!rowIterator.hasNext()) {
            throw new EmptyFileException();
        }
        CSVRecord headerRecord = rowIterator.next();
        List<String> header = new ArrayList<>();
        headerRecord.iterator().forEachRemaining(header::add);
        validateHeaderRow(header);
        Map<String, Integer> headerNameToColIndex = new HashMap<>();
        for (int i = 0; i < headerRecord.size(); ++i) {
            String cell = headerRecord.get(i);
            if (cell != null) {
                headerNameToColIndex.put(cell, i);
            }
        }
        List<BOMQueryRecord> queryRecords = new ArrayList<>();
        rowIterator.forEachRemaining(record -> {
            String article = getColumnFromCsvRecord(record, ARTICLE_COLUMN, headerNameToColIndex);
            String query = getColumnFromCsvRecord(record, QUERY_COLUMN, headerNameToColIndex);
            String manufacturer = getColumnFromCsvRecord(record, MANUFACTURER_COLUMN, headerNameToColIndex);
            queryRecords.add(new BOMQueryRecord(query, manufacturer, article));
        });
        logger.debug("{0}", queryRecords);
        return queryRecords;
    }

    private String getColumnFromCsvRecord(CSVRecord record, String columnName, Map<String, Integer> columnNameToIndex) {
        Integer index = columnNameToIndex.get(columnName);
        if (index == null) {
            return null;
        }
        int idx = index;
        if (idx >= record.size()) {
            return null;
        }
        return record.get(idx);
    }

    public StreamedContent getCsvExample() {
        String header = QueryParserService.CSV_FORMAT.format();
        return new ByteArrayContent(header.getBytes(), CSV_MIME_TYPE, "example.csv");
    }

    public StreamedContent getXlsExample() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        fillWorkbookHeader(workbook);
        return new ByteArrayContent(workbook.getBytes(), XLS_MIME_TYPE, "example.xls");
    }

    public StreamedContent getXlsxExample() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        fillWorkbookHeader(workbook);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            workbook.write(baos);
        } catch (IOException e) {
            logger.error(e);
        }
        return new ByteArrayContent(baos.toByteArray(), XLSX_MIME_TYPE, "example.xlsx");
    }

    private void fillWorkbookHeader(Workbook workbook) {
        Sheet sheet = workbook.createSheet();
        Row header = sheet.createRow(0);
        int cellIndex = 0;
        for (String column : columns) {
            Cell cell = header.createCell(cellIndex);
            cell.setCellValue(column);
            ++cellIndex;
        }
    }

    private void validateHeaderRow(List<String> header) {
        for (String column : columns) {
            if (!header.contains(column)) {
                throw new InvalidHeaderException(column);
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BOMQueryRecord implements Serializable {
        private String query;
        private String manufacturer;
        private String article;

        public static BOMQueryRecord fromCsvRecord(CSVRecord record) {
            long recordNumber = record.getRecordNumber();
            if (!record.isSet(QUERY_COLUMN)) {
                throw new InvalidCsvRecordException(QUERY_COLUMN, recordNumber);
            }
            if (!record.isSet(MANUFACTURER_COLUMN)) {
                throw new InvalidCsvRecordException(MANUFACTURER_COLUMN, recordNumber);
            }
            if (!record.isSet(ARTICLE_COLUMN)) {
                throw new InvalidCsvRecordException(ARTICLE_COLUMN, recordNumber);
            }
            return new BOMQueryRecord(record.get(QUERY_COLUMN),
                record.get(MANUFACTURER_COLUMN),
                record.get(ARTICLE_COLUMN));
        }

        @Override
        public String toString() {
            return Stream.of(query, manufacturer, article)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
        }
    }

    public static class CorruptedFileException extends RuntimeException {
        public CorruptedFileException(String message) {
            super(message);
        }
    }

    public static class InvalidCsvRecordException extends RuntimeException {
        public InvalidCsvRecordException(String columnName, long recordNumber) {
            super(columnName + " in row " + recordNumber + " is not mapped!");
        }
    }

    public static class EmptyFileException extends RuntimeException {

    }

    public static class InvalidHeaderException extends RuntimeException {
        @Getter
        private final String missingColumnName;

        public InvalidHeaderException(String missingColumnName) {
            this.missingColumnName = missingColumnName;
        }
    }

}
