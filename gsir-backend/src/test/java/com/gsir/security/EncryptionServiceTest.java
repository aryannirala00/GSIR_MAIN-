package com.gsir.security;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EncryptionServiceTest {

    @Test
    void testEncryptDecrypt() {
        String key = "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=";
        EncryptionService service = new EncryptionService(key);
        
        String secret = "my_super_secret_api_key";
        String encrypted = service.encrypt(secret);
        
        assertThat(encrypted).isNotEqualTo(secret);
        
        String decrypted = service.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(secret);
    }
}
