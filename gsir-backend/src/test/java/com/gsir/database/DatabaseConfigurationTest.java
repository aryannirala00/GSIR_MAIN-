package com.gsir.database;

import com.gsir.common.id.GsirIdGenerator;
import com.gsir.common.persistence.IdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigurationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private GsirIdGenerator gsirIdGenerator;

    @Autowired
    private IdGenerator idGenerator;

    @Test
    @DisplayName("Test DataSource should be configured and establish active connection")
    void testDataSourceConfigured() throws Exception {
        assertThat(dataSource).isNotNull();
        try (Connection connection = dataSource.getConnection()) {
            assertThat(connection.isValid(1)).isTrue();
            assertThat(connection.getMetaData().getDatabaseProductName()).containsIgnoringCase("H2");
        }
    }

    @Test
    @DisplayName("UUIDv7 generator should be wired into the Spring context")
    void testUuidV7GeneratorAutowired() {
        assertThat(gsirIdGenerator).isNotNull();
        assertThat(idGenerator).isNotNull();

        UUID id = gsirIdGenerator.generate();
        assertThat(id.version()).isEqualTo(7);
    }
}

