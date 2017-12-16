package com.aps.env.comm;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * <dl>
 * <dt>com.aps.env.comm.DatasourceConfiguration</dt>
 * <dd>Description:</dd>
 * <dd>Copyright:  Copyright (C) 2017</dd>
 * <dd>Company:    AppleShow Technology </dd>
 * <dd>CreateDate: 2017/12/13</dd>
 * </dl>
 *
 * @author appleshow
 */
@Configuration
public class DatasourceConfiguration {
    @Bean(name = "dataSource")
    @Qualifier(value = "dataSource")
    @Primary
    @ConfigurationProperties(prefix = "c3p0")
    public DataSource dataSource() {
        return DataSourceBuilder.create().type(ComboPooledDataSource.class).build();
    }
}
