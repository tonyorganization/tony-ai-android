package ton_core.repositories;

import ton_core.models.SampleResponse;
import ton_core.repositories.sample_repository.ISampleRepository;
import ton_core.services.IOnApiCallback;
import ton_core.services.sample_service.ISampleService;
import ton_core.services.sample_service.SampleService;

public class SampleRepository implements ISampleRepository {
    private static SampleRepository INSTANCE;
    private final ISampleService sampleService;

    private SampleRepository() {
        sampleService = new SampleService();
    }

    public static synchronized SampleRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SampleRepository();
        }
        return INSTANCE;
    }

    @Override
    public void sampleApi(String text, IOnApiCallback<SampleResponse> onResult) {
        sampleService.sampleApi(text, onResult);
    }
}
