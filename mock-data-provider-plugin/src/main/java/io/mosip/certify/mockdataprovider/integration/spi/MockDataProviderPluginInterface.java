package io.mosip.certify.mockdataprovider.integration.spi;

import java.util.Map;

public interface MockDataProviderPluginInterface {
    Map<String, Object> fetchJSONFromPlugin(Map<String, Object> identityDetails);
}
