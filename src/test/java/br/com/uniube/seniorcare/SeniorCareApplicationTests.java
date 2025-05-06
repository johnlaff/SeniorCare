package br.com.uniube.seniorcare;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SeniorCareApplicationTests {

    @Test
    void applicationHasMainMethod() {
        // This test verifies that the main method exists and can be called
        assertDoesNotThrow(() -> {
            // Use MockedStatic to prevent SpringApplication.run from actually running
            try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
                // Call the main method
                SeniorCareApplication.main(new String[]{});

                // Verify that SpringApplication.run was called with the correct arguments
                mocked.verify(() -> 
                    SpringApplication.run(SeniorCareApplication.class, new String[]{}),
                    Mockito.times(1)
                );
            }
        });
    }
}
