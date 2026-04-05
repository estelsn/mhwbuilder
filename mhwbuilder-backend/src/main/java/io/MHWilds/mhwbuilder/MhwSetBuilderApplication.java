package io.MHWilds.mhwbuilder;

import io.MHWilds.mhwbuilder.util.H2SeedInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MhwSetBuilderApplication {

	public static void main(String[] args) {
		H2SeedInitializer.initialize();
		SpringApplication.run(MhwSetBuilderApplication.class, args);

	}

}
