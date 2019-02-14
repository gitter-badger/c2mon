package cern.c2mon.server.common.util;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.HSQL;

@Data
@Builder
public class HsqlDatabaseBuilder {
  private String url;
  private String username;
  private String password;
  
  @Singular
  private List<Resource> scripts;

  public DataSource toDataSource() {
    DataSource dataSource;

    if (url == null) {
      // Start an in-process, in-memory HSQL server
      dataSource = new EmbeddedDatabaseBuilder().setType(HSQL).setName("c2mondb").build();
    } else {
      dataSource = DataSourceBuilder.create().url(url).username(username).password(password).build();
    }

    if (!scripts.isEmpty()) {
      ResourceDatabasePopulator populator = new ResourceDatabasePopulator(scripts.toArray(new Resource[scripts.size()]));
      populator.setContinueOnError(true);
      DatabasePopulatorUtils.execute(populator, dataSource);
    }

    return dataSource;
  }
}
