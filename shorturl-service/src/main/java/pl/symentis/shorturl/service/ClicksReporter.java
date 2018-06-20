package pl.symentis.shorturl.service;

import static pl.symentis.shorturl.domain.ClickBuilder.clickBuilder;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import eu.bitwalker.useragentutils.UserAgent;
import pl.symentis.shorturl.dao.ClickRepository;
import pl.symentis.shorturl.domain.Click;

@Component
public class ClicksReporter {

	private final ClickRepository clickRepository;

	public ClicksReporter(ClickRepository clickRepository) {
		this.clickRepository = clickRepository;
	}
	
	@Async
	public CompletableFuture<Click> reportClick(
			String agent, 
			String ipAddress, 
			URL referer, 
			String shortcut){
		return CompletableFuture.supplyAsync(() -> {
			UserAgent userAgent = new UserAgent(agent);

			Click click = clickBuilder()
					.withAgent(userAgent.getBrowser().getName())
					.withOs(userAgent.getOperatingSystem().getName())
					.withIpAddress(ipAddress)
					.withLocalDateTime(LocalDateTime.now())
					.withReferer(referer)
					.withShortcut(shortcut)
					.build();
			clickRepository.save(click);
			return click;
		});
	}

}
