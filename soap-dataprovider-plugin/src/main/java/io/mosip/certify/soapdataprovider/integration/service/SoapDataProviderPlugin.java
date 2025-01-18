package io.mosip.certify.soapdataprovider.integration.service;

import io.mosip.certify.api.exception.DataProviderExchangeException;
import io.mosip.certify.api.spi.DataProviderPlugin;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@ConditionalOnProperty(value = "mosip.certify.integration.data-provider-plugin", havingValue = "SoapDataProviderPlugin")
@Component
@Slf4j
public class SoapDataProviderPlugin implements DataProviderPlugin {
    @Autowired
    NumberConversionClient numberConversionClient;

    @Override
    public JSONObject fetchData(Map<String, Object> identityDetails) throws DataProviderExchangeException {
        String response = numberConversionClient.convert(new BigDecimal(123456.85));
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "India");
            jsonObject.put("capital", "Delhi");
            jsonObject.put("currency", response);
            jsonObject.put("population", 6567456);
            return jsonObject;
        } catch (JSONException e) {
            log.error("JSON exception", e);
        }
        throw new DataProviderExchangeException("FAILED_TO_FETCH_DATA");
    }
}
