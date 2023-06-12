package info.touret.bookstore.spring.book;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BookObservationHandler implements ObservationHandler<Observation.Context> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BookObservationHandler.class);

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }

    @Override
    public void onStart(Observation.Context context) {
        LOGGER.info("Before running the observation for context [{}]", context.getName());

    }

    @Override
    public void onStop(Observation.Context context) {
        LOGGER.info("After running the observation for context [{}]", context.getName());

    }
}
