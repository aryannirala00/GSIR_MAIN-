package com.gsir.tools.network;

import com.gsir.common.error.GsirException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureHttpClientTest {

    private final SecureHttpClient secureHttpClient = new SecureHttpClient();

    @Test
    void testGet_SSRF_Localhost_Blocked() {
        assertThatThrownBy(() -> secureHttpClient.get("https://127.0.0.1/metadata"))
            .isInstanceOf(GsirException.class)
            .hasMessageContaining("local/private network is forbidden");
            
        assertThatThrownBy(() -> secureHttpClient.get("https://localhost/api"))
            .isInstanceOf(GsirException.class)
            .hasMessageContaining("local/private network is forbidden");
    }

    @Test
    void testGet_SSRF_Metadata_Blocked() {
        assertThatThrownBy(() -> secureHttpClient.get("https://169.254.169.254/latest/meta-data/"))
            .isInstanceOf(GsirException.class)
            .hasMessageContaining("local/private network is forbidden");
    }

    @Test
    void testGet_SSRF_0000_Blocked() {
        assertThatThrownBy(() -> secureHttpClient.get("https://0.0.0.0/api"))
            .isInstanceOf(GsirException.class)
            .hasMessageContaining("local/private network is forbidden");
    }

    @Test
    void testGet_HTTP_Blocked() {
        assertThatThrownBy(() -> secureHttpClient.get("http://api.openalex.org/works"))
            .isInstanceOf(GsirException.class)
            .hasMessageContaining("Only HTTPS is allowed");
    }

    // A real redirect test is hard without a mock server, but we can verify 
    // the code works against our logic. In a real scenario we'd use WireMock.
}
