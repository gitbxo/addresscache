package org.bxo.address;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * Command to compile application mvn compile
 *
 * Command to run service mvn spring-boot:run
 *
 * Dockerize using this: https://spring.io/guides/gs/spring-boot-docker
 * https://github.com/spring-guides/gs-spring-boot-docker/tree/master/complete
 * https://github.com/spring-guides/gs-actuator-service/tree/master/complete
 * https://github.com/learnk8s/spring-boot-k8s-hpa/blob/master/src/main/java/com/learnk8s/app/SpringBootApplication.java
 *
 **/

@SpringBootApplication
public class AddressApp {

	public static void main(String[] args) {
		SpringApplication.run(AddressApp.class, args);
	}

}
