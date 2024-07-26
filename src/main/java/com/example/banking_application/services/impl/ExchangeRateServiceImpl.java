package com.example.banking_application.services.impl;

import com.example.banking_application.config.ForexConfigurations;
import com.example.banking_application.models.dtos.ExchangeRateDto;
import com.example.banking_application.models.entities.ExchangeRate;
import com.example.banking_application.repositories.ExchangeRateRepository;
import com.example.banking_application.services.ExchangeRateService;
import org.hibernate.ObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;


@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private final Logger LOGGER = LoggerFactory.getLogger(ExchangeRateServiceImpl.class);
    private final ExchangeRateRepository exRateRepository;
    private final RestClient restClient;
    private final ForexConfigurations forexApiConfig;

    public ExchangeRateServiceImpl(ExchangeRateRepository exRateRepository,
                             @Qualifier("genericRestClient") RestClient restClient,
                             ForexConfigurations forexApiConfig) {
        this.exRateRepository = exRateRepository;
        this.restClient = restClient;
        this.forexApiConfig = forexApiConfig;
    }

    @Override
    public boolean hasInitializedExRates() {
        return this.exRateRepository.count() > 0;
    }

    @Override
    public ExchangeRateDto fetchExRates() {
        return this.restClient
                .get()
                .uri(this.forexApiConfig.getUrl(), this.forexApiConfig.getKey())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(ExchangeRateDto.class);
    }

    @Override
    public void updateRates(ExchangeRateDto exRatesDTO) {
        // message to see whether rates update frequently
        this.LOGGER.info("Updating {} rates.", exRatesDTO.rates().size());

        if (!this.forexApiConfig.getBase().equals(exRatesDTO.base())) {
            throw new IllegalArgumentException("The updated exchange rates are not based on " +
                    this.forexApiConfig.getBase() + "rather on " + exRatesDTO.base());
        }

        exRatesDTO.rates().forEach((currency, rate) -> {
            ExchangeRate exchangeRate =
                    this.exRateRepository.findByCurrency(currency)
                    .orElseGet(() -> {
                       ExchangeRate exchangeRate1 = new ExchangeRate();
                       exchangeRate1.setCurrency(currency);
                       return exchangeRate1;
                    });

            exchangeRate.setRate(rate);

            this.exRateRepository.save(exchangeRate);
        });

    }
    private Optional<BigDecimal> findExchangeRate(String fromCurrency, String toCurrency) {

        if (Objects.equals(fromCurrency,  toCurrency)) {
            return Optional.of(BigDecimal.ONE);
        }
        Optional<BigDecimal> fromSearchedCurrency = this.forexApiConfig.getBase().equals(fromCurrency) ?
                Optional.of(BigDecimal.ONE) :
                this.exRateRepository.findByCurrency(fromCurrency).map(ExchangeRate::getRate);

        Optional<BigDecimal> toSearchedCurrency = this.forexApiConfig.getBase().equals( toCurrency) ?
                Optional.of(BigDecimal.ONE) :
                this.exRateRepository.findByCurrency( toCurrency).map(ExchangeRate::getRate);

        if (fromSearchedCurrency.isEmpty() || toSearchedCurrency.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(toSearchedCurrency.get().divide(fromSearchedCurrency.get(), 2, RoundingMode.HALF_DOWN));
        }
    }

    @Override
    public BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal amount) {
        return findExchangeRate(fromCurrency, toCurrency)
                .orElseThrow(() ->
                        new ObjectNotFoundException((Object) ("Conversion from " +
                                fromCurrency + " to "
                                + toCurrency + " not possible!"), fromCurrency + "~" + toCurrency))
                .multiply(amount);
    }
}
