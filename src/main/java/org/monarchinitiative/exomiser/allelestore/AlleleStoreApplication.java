package org.monarchinitiative.exomiser.allelestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AlleleStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlleleStoreApplication.class, args).close();
	}
}
