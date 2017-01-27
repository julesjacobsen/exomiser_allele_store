package org.monarchinitiative.exomiser.allelestore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
@Configuration
public class MainConfig {

    private static final Logger logger = LoggerFactory.getLogger(MainConfig.class);

    @Autowired
    private Environment environment;

    @Bean
    public Path workingDir() {
        String workingDir = environment.getProperty("working-directory");
        logger.info("Working directory set to: {}", workingDir);
        return Paths.get(workingDir);
    }

}
