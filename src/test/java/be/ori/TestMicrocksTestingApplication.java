package be.ori;

import org.springframework.boot.SpringApplication;

public class TestMicrocksTestingApplication {

	public static void main(String[] args) {
		SpringApplication.from(MicrocksTestingApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
