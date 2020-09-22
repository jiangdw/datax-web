package com.wugui.datax.admin.tool.datax;


import static com.wugui.datax.admin.util.JdbcConstants.CLICKHOUSE;
import static com.wugui.datax.admin.util.JdbcConstants.HBASE;
import static com.wugui.datax.admin.util.JdbcConstants.HIVE;
import static com.wugui.datax.admin.util.JdbcConstants.MONGODB;
import static com.wugui.datax.admin.util.JdbcConstants.MYSQL;
import static com.wugui.datax.admin.util.JdbcConstants.ORACLE;
import static com.wugui.datax.admin.util.JdbcConstants.PARQUET_FILE;
import static com.wugui.datax.admin.util.JdbcConstants.POSTGRESQL;
import static com.wugui.datax.admin.util.JdbcConstants.RABBITMQ;
import static com.wugui.datax.admin.util.JdbcConstants.SQL_SERVER;
import static com.wugui.datax.admin.util.JdbcConstants.TEXT_FILE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wugui.datatx.core.util.Constants;
import com.wugui.datax.admin.dto.DataXJsonBuildDto;
import com.wugui.datax.admin.dto.HbaseReaderDto;
import com.wugui.datax.admin.dto.HbaseWriterDto;
import com.wugui.datax.admin.dto.HiveReaderDto;
import com.wugui.datax.admin.dto.HiveWriterDto;
import com.wugui.datax.admin.dto.MongoDBReaderDto;
import com.wugui.datax.admin.dto.MongoDBWriterDto;
import com.wugui.datax.admin.dto.ParquetFileReaderDto;
import com.wugui.datax.admin.dto.RabbitmqWriterDto;
import com.wugui.datax.admin.dto.RdbmsReaderDto;
import com.wugui.datax.admin.dto.RdbmsWriterDto;
import com.wugui.datax.admin.entity.JobDatasource;
import com.wugui.datax.admin.tool.datax.reader.ClickHouseReader;
import com.wugui.datax.admin.tool.datax.reader.HBaseReader;
import com.wugui.datax.admin.tool.datax.reader.HiveReader;
import com.wugui.datax.admin.tool.datax.reader.MongoDBReader;
import com.wugui.datax.admin.tool.datax.reader.MysqlReader;
import com.wugui.datax.admin.tool.datax.reader.OracleReader;
import com.wugui.datax.admin.tool.datax.reader.ParquetFileReader;
import com.wugui.datax.admin.tool.datax.reader.PostgresqlReader;
import com.wugui.datax.admin.tool.datax.reader.SqlServerReader;
import com.wugui.datax.admin.tool.datax.writer.ClickHouseWriter;
import com.wugui.datax.admin.tool.datax.writer.HBaseWriter;
import com.wugui.datax.admin.tool.datax.writer.HiveWriter;
import com.wugui.datax.admin.tool.datax.writer.MongoDBWriter;
import com.wugui.datax.admin.tool.datax.writer.MysqlWriter;
import com.wugui.datax.admin.tool.datax.writer.OraclelWriter;
import com.wugui.datax.admin.tool.datax.writer.PostgresqllWriter;
import com.wugui.datax.admin.tool.datax.writer.RabbitmqWriter;
import com.wugui.datax.admin.tool.datax.writer.SqlServerlWriter;
import com.wugui.datax.admin.tool.pojo.DataxHbasePojo;
import com.wugui.datax.admin.tool.pojo.DataxHivePojo;
import com.wugui.datax.admin.tool.pojo.DataxMongoDBPojo;
import com.wugui.datax.admin.tool.pojo.DataxParquetFilePojo;
import com.wugui.datax.admin.tool.pojo.DataxRabbitmqPojo;
import com.wugui.datax.admin.tool.pojo.DataxRdbmsPojo;
import com.wugui.datax.admin.util.JdbcConstants;

import lombok.Data;

/**
 * 构建 com.wugui.datax json的工具类
 *
 * @author jingwk
 * @ClassName DataxJsonHelper
 * @Version 2.1.1
 * @since 2020/03/14 08:24
 */
@Data
public class DataxJsonHelper implements DataxJsonInterface {

    /**
     * 读取的表，根据datax示例，支持多个表（先不考虑，后面再去实现， 这里先用list保存吧）
     * <p>
     * 目的表的表名称。支持写入一个或者多个表。当配置为多张表时，必须确保所有表结构保持一致
     */
    private List<String> readerTables;
    /**
     * 读取的字段列表
     */
    private List<String> readerColumns;
    /**
     * reader jdbc 数据源
     */
    private JobDatasource readerDatasource;
    /**
     * writer jdbc 数据源
     */
    private JobDatasource writerDatasource;
    /**
     * 写入的表
     */
    private List<String> writerTables;
    /**
     * 写入的字段列表
     */
    private List<String> writerColumns;

    private Map<String, Object> buildReader;

    private Map<String, Object> buildWriter;

    private BaseDataxPlugin readerPlugin;

    private BaseDataxPlugin writerPlugin;

    private HiveReaderDto hiveReaderDto;

    private HiveWriterDto hiveWriterDto;

    private HbaseReaderDto hbaseReaderDto;

    private HbaseWriterDto hbaseWriterDto;

    private RdbmsReaderDto rdbmsReaderDto;

    private RdbmsWriterDto rdbmsWriterDto;

    private MongoDBReaderDto mongoDBReaderDto;

    private MongoDBWriterDto mongoDBWriterDto;
    
    private RabbitmqWriterDto rabbitmqWriterDto;
    
    private ParquetFileReaderDto parquetFileReaderDto;


    //用于保存额外参数
    private Map<String, Object> extraParams = Maps.newHashMap();

    public void initReader(DataXJsonBuildDto dataxJsonDto, JobDatasource readerDatasource) {

        this.readerDatasource = readerDatasource;
        this.readerTables = dataxJsonDto.getReaderTables();
        this.readerColumns = dataxJsonDto.getReaderColumns();
        this.hiveReaderDto = dataxJsonDto.getHiveReader();
        this.rdbmsReaderDto = dataxJsonDto.getRdbmsReader();
        this.hbaseReaderDto = dataxJsonDto.getHbaseReader();
        this.parquetFileReaderDto = dataxJsonDto.getParquetFileReader();
        
        // reader 插件
        String datasource = readerDatasource.getDatasource();

        this.readerColumns = convertKeywordsColumns(datasource, this.readerColumns);
        if (MYSQL.equalsIgnoreCase(datasource)) {
            readerPlugin = new MysqlReader();
            buildReader = buildReader();
        } else if (ORACLE.equalsIgnoreCase(datasource)) {
            readerPlugin = new OracleReader();
            buildReader = buildReader();
        } else if (SQL_SERVER.equalsIgnoreCase(datasource)) {
            readerPlugin = new SqlServerReader();
            buildReader = buildReader();
        } else if (POSTGRESQL.equalsIgnoreCase(datasource)) {
            readerPlugin = new PostgresqlReader();
            buildReader = buildReader();
        } else if (CLICKHOUSE.equalsIgnoreCase(datasource)) {
            readerPlugin = new ClickHouseReader();
            buildReader = buildReader();
        } else if (HIVE.equalsIgnoreCase(datasource)) {
            readerPlugin = new HiveReader();
            buildReader = buildHiveReader();
        } else if (HBASE.equalsIgnoreCase(datasource)) {
            readerPlugin = new HBaseReader();
            buildReader = buildHBaseReader();
        } else if (MONGODB.equalsIgnoreCase(datasource)) {
            readerPlugin = new MongoDBReader();
            buildReader = buildMongoDBReader();
        } else if (PARQUET_FILE.equalsIgnoreCase(datasource)) {
            readerPlugin = new ParquetFileReader();
            buildReader = buildParquetFileReader();
        } else if (RABBITMQ.equalsIgnoreCase(datasource)) {
        	// TODO
        } else if (TEXT_FILE.equalsIgnoreCase(datasource)) {
            // TODO
        }
    }

	public void initWriter(DataXJsonBuildDto dataxJsonDto, JobDatasource readerDatasource) {
        this.writerDatasource = readerDatasource;
        this.writerTables = dataxJsonDto.getWriterTables();
        this.writerColumns = dataxJsonDto.getWriterColumns();
        this.hiveWriterDto = dataxJsonDto.getHiveWriter();
        this.rdbmsWriterDto = dataxJsonDto.getRdbmsWriter();
        this.hbaseWriterDto = dataxJsonDto.getHbaseWriter();
        this.mongoDBWriterDto = dataxJsonDto.getMongoDBWriter();
        this.rabbitmqWriterDto = dataxJsonDto.getRabbitmqWriter();
        // writer
        String datasource = readerDatasource.getDatasource();
        this.writerColumns = convertKeywordsColumns(datasource, this.writerColumns);
        if (MYSQL.equalsIgnoreCase(datasource)) {
            writerPlugin = new MysqlWriter();
            buildWriter = this.buildWriter();
        } else if (ORACLE.equalsIgnoreCase(datasource)) {
            writerPlugin = new OraclelWriter();
            buildWriter = this.buildWriter();
        } else if (JdbcConstants.SQL_SERVER.equalsIgnoreCase(datasource)) {
            writerPlugin = new SqlServerlWriter();
            buildWriter = this.buildWriter();
        } else if (POSTGRESQL.equalsIgnoreCase(datasource)) {
            writerPlugin = new PostgresqllWriter();
            buildWriter = this.buildWriter();
        } else if (JdbcConstants.CLICKHOUSE.equalsIgnoreCase(datasource)) {
            writerPlugin = new ClickHouseWriter();
            buildWriter = buildWriter();
        } else if (JdbcConstants.HIVE.equalsIgnoreCase(datasource)) {
            writerPlugin = new HiveWriter();
            buildWriter = this.buildHiveWriter();
        } else if (JdbcConstants.HBASE.equalsIgnoreCase(datasource)) {
            writerPlugin = new HBaseWriter();
            buildWriter = this.buildHBaseWriter();
        } else if (JdbcConstants.MONGODB.equalsIgnoreCase(datasource)) {
            writerPlugin = new MongoDBWriter();
            buildWriter = this.buildMongoDBWriter();
        } else if (JdbcConstants.RABBITMQ.equalsIgnoreCase(datasource)) {
        	readerPlugin = new RabbitmqWriter();
        	buildWriter = this.buildRabbitmqWriter();
        }
    }

    private List<String> convertKeywordsColumns(String datasource, List<String> columns) {
        if (columns == null) {
            return null;
        }

        List<String> toColumns = new ArrayList<>();
        columns.forEach(s -> {
            toColumns.add(doConvertKeywordsColumn(datasource, s));
        });
        return toColumns;
    }

    private String doConvertKeywordsColumn(String dbType, String column) {
        if (column == null) {
            return null;
        }

        column = column.trim();
        column = column.replace("[", "");
        column = column.replace("]", "");
        column = column.replace("`", "");
        column = column.replace("\"", "");
        column = column.replace("'", "");

        switch (dbType) {
            case MYSQL:
                return String.format("`%s`", column);
            case SQL_SERVER:
                return String.format("[%s]", column);
            case POSTGRESQL:
            case ORACLE:
                return String.format("\"%s\"", column);
            default:
                return column;
        }
    }

    @Override
    public Map<String, Object> buildJob() {
        Map<String, Object> res = Maps.newLinkedHashMap();
        Map<String, Object> jobMap = Maps.newLinkedHashMap();
        jobMap.put("setting", buildSetting());
        jobMap.put("content", ImmutableList.of(buildContent()));
        res.put("job", jobMap);
        return res;
    }

    @Override
    public Map<String, Object> buildSetting() {
        Map<String, Object> res = Maps.newLinkedHashMap();
        Map<String, Object> speedMap = Maps.newLinkedHashMap();
        Map<String, Object> errorLimitMap = Maps.newLinkedHashMap();
        speedMap.putAll(ImmutableMap.of("channel", 3, "byte", 1048576));
        errorLimitMap.putAll(ImmutableMap.of("record", 0, "percentage", 0.02));
        res.put("speed", speedMap);
        res.put("errorLimit", errorLimitMap);
        return res;
    }

    @Override
    public Map<String, Object> buildContent() {
        Map<String, Object> res = Maps.newLinkedHashMap();
        res.put("reader", this.buildReader);
        res.put("writer", this.buildWriter);
        return res;
    }

    @Override
    public Map<String, Object> buildReader() {
        DataxRdbmsPojo dataxPluginPojo = new DataxRdbmsPojo();
        dataxPluginPojo.setJobDatasource(readerDatasource);
        dataxPluginPojo.setTables(readerTables);
        dataxPluginPojo.setRdbmsColumns(readerColumns);
        dataxPluginPojo.setSplitPk(rdbmsReaderDto.getReaderSplitPk());
        if (StringUtils.isNotBlank(rdbmsReaderDto.getQuerySql())) {
            dataxPluginPojo.setQuerySql(rdbmsReaderDto.getQuerySql());
        }
        //where
        if (StringUtils.isNotBlank(rdbmsReaderDto.getWhereParams())) {
            dataxPluginPojo.setWhereParam(rdbmsReaderDto.getWhereParams());
        }
        return readerPlugin.build(dataxPluginPojo);
    }

    @Override
    public Map<String, Object> buildHiveReader() {
        DataxHivePojo dataxHivePojo = new DataxHivePojo();
        dataxHivePojo.setJdbcDatasource(readerDatasource);
        List<Map<String, Object>> columns = Lists.newArrayList();
        readerColumns.forEach(c -> {
            Map<String, Object> column = Maps.newLinkedHashMap();
            column.put("index", c.split(Constants.SPLIT_SCOLON)[0]);
            column.put("type", c.split(Constants.SPLIT_SCOLON)[2]);
            columns.add(column);
        });
        dataxHivePojo.setColumns(columns);
        dataxHivePojo.setReaderDefaultFS(hiveReaderDto.getReaderDefaultFS());
        dataxHivePojo.setReaderFieldDelimiter(hiveReaderDto.getReaderFieldDelimiter());
        dataxHivePojo.setReaderFileType(hiveReaderDto.getReaderFileType());
        dataxHivePojo.setReaderPath(hiveReaderDto.getReaderPath());
        dataxHivePojo.setSkipHeader(hiveReaderDto.getReaderSkipHeader());
        return readerPlugin.buildHive(dataxHivePojo);
    }

    @Override
    public Map<String, Object> buildHBaseReader() {
        DataxHbasePojo dataxHbasePojo = new DataxHbasePojo();
        dataxHbasePojo.setJdbcDatasource(readerDatasource);
        List<Map<String, Object>> columns = Lists.newArrayList();
        for (int i = 0; i < readerColumns.size(); i++) {
            Map<String, Object> column = Maps.newLinkedHashMap();
            column.put("name", readerColumns.get(i));
            column.put("type", "string");
            columns.add(column);
        }
        dataxHbasePojo.setColumns(columns);
        dataxHbasePojo.setReaderHbaseConfig(readerDatasource.getZkAdress());
        String readerTable=!CollectionUtils.isEmpty(readerTables)?readerTables.get(0):Constants.STRING_BLANK;
        dataxHbasePojo.setReaderTable(readerTable);
        dataxHbasePojo.setReaderMode(hbaseReaderDto.getReaderMode());
        dataxHbasePojo.setReaderRange(hbaseReaderDto.getReaderRange());
        return readerPlugin.buildHbase(dataxHbasePojo);
    }

    @Override
    public Map<String, Object> buildMongoDBReader() {
        DataxMongoDBPojo dataxMongoDBPojo = new DataxMongoDBPojo();
        dataxMongoDBPojo.setJdbcDatasource(readerDatasource);
        List<Map<String, Object>> columns = Lists.newArrayList();
        buildColumns(readerColumns, columns);
        dataxMongoDBPojo.setColumns(columns);
        dataxMongoDBPojo.setAddress(readerDatasource.getJdbcUrl());
        dataxMongoDBPojo.setDbName(readerDatasource.getDatabaseName());
        dataxMongoDBPojo.setReaderTable(readerTables.get(0));
        return readerPlugin.buildMongoDB(dataxMongoDBPojo);
    }


    @Override
    public Map<String, Object> buildWriter() {
        DataxRdbmsPojo dataxPluginPojo = new DataxRdbmsPojo();
        dataxPluginPojo.setJobDatasource(writerDatasource);
        dataxPluginPojo.setTables(writerTables);
        dataxPluginPojo.setRdbmsColumns(writerColumns);
        dataxPluginPojo.setPreSql(rdbmsWriterDto.getPreSql());
        dataxPluginPojo.setPostSql(rdbmsWriterDto.getPostSql());
        return writerPlugin.build(dataxPluginPojo);
    }

    @Override
    public Map<String, Object> buildHiveWriter() {
        DataxHivePojo dataxHivePojo = new DataxHivePojo();
        dataxHivePojo.setJdbcDatasource(writerDatasource);
        List<Map<String, Object>> columns = Lists.newArrayList();
        writerColumns.forEach(c -> {
            Map<String, Object> column = Maps.newLinkedHashMap();
            column.put("name", c.split(Constants.SPLIT_SCOLON)[1]);
            column.put("type", c.split(Constants.SPLIT_SCOLON)[2]);
            columns.add(column);
        });
        dataxHivePojo.setColumns(columns);
        dataxHivePojo.setWriterDefaultFS(hiveWriterDto.getWriterDefaultFS());
        dataxHivePojo.setWriteFieldDelimiter(hiveWriterDto.getWriteFieldDelimiter());
        dataxHivePojo.setWriterFileType(hiveWriterDto.getWriterFileType());
        dataxHivePojo.setWriterPath(hiveWriterDto.getWriterPath());
        dataxHivePojo.setWriteMode(hiveWriterDto.getWriteMode());
        dataxHivePojo.setWriterFileName(hiveWriterDto.getWriterFileName());
        return writerPlugin.buildHive(dataxHivePojo);
    }

    @Override
    public Map<String, Object> buildHBaseWriter() {
        DataxHbasePojo dataxHbasePojo = new DataxHbasePojo();
        dataxHbasePojo.setJdbcDatasource(writerDatasource);
        List<Map<String, Object>> columns = Lists.newArrayList();
        for (int i = 0; i < writerColumns.size(); i++) {
            Map<String, Object> column = Maps.newLinkedHashMap();
            column.put("index", i + 1);
            column.put("name", writerColumns.get(i));
            column.put("type", "string");
            columns.add(column);
        }
        dataxHbasePojo.setColumns(columns);
        dataxHbasePojo.setWriterHbaseConfig(writerDatasource.getZkAdress());
        String writerTable=!CollectionUtils.isEmpty(writerTables)?writerTables.get(0):Constants.STRING_BLANK;
        dataxHbasePojo.setWriterTable(writerTable);
        dataxHbasePojo.setWriterVersionColumn(hbaseWriterDto.getWriterVersionColumn());
        dataxHbasePojo.setWriterRowkeyColumn(hbaseWriterDto.getWriterRowkeyColumn());
        dataxHbasePojo.setWriterMode(hbaseWriterDto.getWriterMode());
        return writerPlugin.buildHbase(dataxHbasePojo);
    }


    @Override
    public Map<String, Object> buildMongoDBWriter() {
        DataxMongoDBPojo dataxMongoDBPojo = new DataxMongoDBPojo();
        dataxMongoDBPojo.setJdbcDatasource(writerDatasource);
        List<Map<String, Object>> columns = Lists.newArrayList();
        buildColumns(writerColumns, columns);
        dataxMongoDBPojo.setColumns(columns);
        dataxMongoDBPojo.setAddress(writerDatasource.getJdbcUrl());
        dataxMongoDBPojo.setDbName(writerDatasource.getDatabaseName());
        dataxMongoDBPojo.setWriterTable(readerTables.get(0));
        dataxMongoDBPojo.setUpsertInfo(mongoDBWriterDto.getUpsertInfo());
        return writerPlugin.buildMongoDB(dataxMongoDBPojo);
    }
    
    @Override
    public Map<String, Object> buildRabbitmqWriter() {
    	DataxRabbitmqPojo dataxRabbitmqPojo = new DataxRabbitmqPojo();
    	dataxRabbitmqPojo.setJdbcDatasource(writerDatasource);
        List<Map<String, Object>> columns = Lists.newArrayList();
        buildColumns(writerColumns, columns);
        dataxRabbitmqPojo.setColumns(columns);
        dataxRabbitmqPojo.setHost(writerDatasource.getJdbcUrl());
        dataxRabbitmqPojo.setPort(writerDatasource.getExtra());
        dataxRabbitmqPojo.setBatchSize(rabbitmqWriterDto.getBatchSize());
        dataxRabbitmqPojo.setExchange(rabbitmqWriterDto.getExchange());
        dataxRabbitmqPojo.setFieldDelimiter(rabbitmqWriterDto.getFieldDelimiter());
        dataxRabbitmqPojo.setJointColumn(rabbitmqWriterDto.getJointColumn());
        dataxRabbitmqPojo.setMessagePrefix(rabbitmqWriterDto.getMessagePrefix());
        dataxRabbitmqPojo.setMessageSuffix(rabbitmqWriterDto.getMessageSuffix());
        dataxRabbitmqPojo.setUsername(writerDatasource.getJdbcUsername());
        dataxRabbitmqPojo.setPassword(writerDatasource.getJdbcPassword());
        dataxRabbitmqPojo.setQueue(rabbitmqWriterDto.getQueue());
        dataxRabbitmqPojo.setRoutingKey(rabbitmqWriterDto.getRoutingKey());
    	return writerPlugin.buildRabbitmq(dataxRabbitmqPojo);
    }
    
    private Map<String, Object> buildParquetFileReader() {
		DataxParquetFilePojo dataxParquetFilePojo = new DataxParquetFilePojo();
		dataxParquetFilePojo.setJdbcDatasource(writerDatasource);
		List<Map<String, Object>> columns = Lists.newArrayList();
        buildColumns(writerColumns, columns);
        dataxParquetFilePojo.setColumns(columns);
        dataxParquetFilePojo.setEncoding(parquetFileReaderDto.getEncoding());
        dataxParquetFilePojo.setFieldDelimiter(parquetFileReaderDto.getFieldDelimiter());
        dataxParquetFilePojo.setHost(writerDatasource.getJdbcUrl());
        dataxParquetFilePojo.setPath(parquetFileReaderDto.getPath());
		return writerPlugin.buildParquetFile(dataxParquetFilePojo);
	}

    private void buildColumns(List<String> columns, List<Map<String, Object>> returnColumns) {
        columns.forEach(c -> {
            Map<String, Object> column = Maps.newLinkedHashMap();
            column.put("name", c.split(Constants.SPLIT_SCOLON)[0]);
            column.put("type", c.split(Constants.SPLIT_SCOLON)[1]);
            returnColumns.add(column);
        });
    }
}
