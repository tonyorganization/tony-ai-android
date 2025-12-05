package ton_core.services.sample_service;

import ton_core.TonApiClient;
import ton_core.endpoints.SampleEndpoint;
import ton_core.models.SampleRequest;
import ton_core.models.SampleResponse;
import ton_core.services.BaseService;
import ton_core.services.IOnApiCallback;

public class SampleService extends BaseService implements ISampleService {

    public SampleService() {
    }

    SampleEndpoint sampleEndpoint = TonApiClient.getInstance().create(SampleEndpoint.class);

    @Override
    public void sampleApi(String text, IOnApiCallback<SampleResponse> onResult) {
        handleWithoutBaseResponse(sampleEndpoint.getSampleData(new SampleRequest(text)), onResult);
    }
}
