package net.revirtualis.profat;

import net.revirtualis.profat.config.ProfatProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProfatProperties.class)
public class ProfatApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProfatApplication.class, args);
	}

}
