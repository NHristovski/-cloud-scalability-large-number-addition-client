package scalability.cloud.largenumberadditionclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import scalability.cloud.largenumberadditionclient.message.SumResponse;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

@Component
public class Runner {


    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    private final RestTemplate restTemplate;

    @Value("${url}")
    private String url;
    @Value("${numThreads}")
    private int numThreads;

    public Runner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        startTimer();

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        BigInteger currentStart = BigInteger.ZERO;
//        long limit = 5000000000L;
        long limit = 250000000L;
        String currentUrl = url + String.format("start=%s&limit=%s", currentStart, limit);


        List<Callable<BigInteger>> callables = new ArrayList<>();

        BigInteger end = new BigInteger("3500000000");

        while (currentStart.compareTo(end) < 0) {
            logger.info("Current start is {} is less than {}", currentStart, end);

            callables.add(callable(currentUrl));

            currentStart = currentStart.add(BigInteger.valueOf(limit));
            currentUrl = url + String.format("start=%s&limit=%s", currentStart, limit);
        }

        try {
            List<Future<BigInteger>> futures = executorService.invokeAll(callables);

            BigInteger finalSum = futures.stream()
                    .map(f -> {
                        try {
                            return f.get(200, TimeUnit.MINUTES);
                        } catch (Exception e) {
                            logger.error("Error while retrieving sum", e);
                            return BigInteger.ZERO;
                        }
                    })
                    .reduce(BigInteger.ZERO, BigInteger::add);

            logger.info("Total sum is {}", finalSum);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Callable<BigInteger> callable(String currentUrl) {
        return () -> {
            try {
                logger.info("Calling url: {}", currentUrl);
                SumResponse sumResponse = restTemplate.getForObject(currentUrl, SumResponse.class);

                logger.info("Response -> Sum is {}.", sumResponse.getSum());

                return sumResponse.getSum();
            } catch (Exception e) {
                logger.error("Error while calling {}", currentUrl, e);
                return null;
            }
        };
    }

    private static void startTimer() {
        logger.info("Starting time counter");
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            Instant start = Instant.now();

            while (true) {
                try {
                    sleep(120_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    Instant end = Instant.now();
                    double elapsedMinutes = Duration.between(start, end).toMillis() / 1000.0 / 60.0;
                    logger.info("Elapsed time: {} minutes", String.format("%.2f", elapsedMinutes));
                }
            }

        });
        logger.info("Time counter started");
    }
}
