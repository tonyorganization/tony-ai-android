package ton_core.repositories.sample_repository;

import ton_core.models.SampleResponse;
import ton_core.services.IOnApiCallback;

public interface ISampleRepository {
    void sampleApi(String text, IOnApiCallback<SampleResponse> onResult);
}
