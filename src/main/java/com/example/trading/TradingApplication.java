package com.example.trading;

import com.example.trading.service.AUMServerService;
import com.example.trading.service.FillServerService;
import com.example.trading.service.PositionServerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class TradingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TradingApplication.class, args);
	}


	@Bean
	public CommandLineRunner run(FillServerService fillServerService,
						  AUMServerService aumServerService,
						  PositionServerService positionServerService) {
		return args -> {
			// Run X fill servers simulating fill generating logic
			for (int i = 0; i < 3; i++) {
				fillServerService.simulateFills();
			}

			aumServerService.simulateAUMSplits();
			positionServerService.sendPositions();
		};
	}

}
