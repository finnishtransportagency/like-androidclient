package fi.livi.like.client.android.backgroundservice.background;

import org.junit.Before;
import org.junit.Test;

import fi.livi.like.client.android.backgroundservice.data.DataStorage;
import fi.livi.like.client.android.backgroundservice.datacollectors.activityrecognition.LikeActivityAverager;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.LikeActivity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LikeActivityAveragerUnitTest {

    private LikeActivityAverager likeActivityAverager;

    private DataStorage dataStorage;

    @Before
    public void setup() {
        dataStorage = mock(DataStorage.class);

        likeActivityAverager = new LikeActivityAverager(dataStorage);
    }

    @Test
    public void should_just_return_if_null_value_given() {
        likeActivityAverager.onLikeActivityUpdate(null);

        assertThat(likeActivityAverager.getAverageLikeActivity(), is(nullValue()));
    }

    @Test
    public void should_return_same_if_set_on_datastorage() {
        LikeActivity likeActivity = new LikeActivity(LikeActivity.Type.IN_VEHICLE, 22);
        when(dataStorage.getLastAverageLikeActivity()).thenReturn(likeActivity);
        likeActivityAverager = new LikeActivityAverager(dataStorage);

        assertThat(likeActivityAverager.getAverageLikeActivity(), is(likeActivity));
    }

    @Test
    public void should_return_same_if_one_sample_given() {
        LikeActivity likeActivity = new LikeActivity(LikeActivity.Type.IN_VEHICLE, 22);
        likeActivityAverager.onLikeActivityUpdate(likeActivity);

        assertThat(likeActivityAverager.getAverageLikeActivity(), is(likeActivity));
    }

    @Test
    public void should_return_activity_with_most_samples_3() {
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.IN_VEHICLE, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.IN_VEHICLE, 87));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 17));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.IN_VEHICLE));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(87));
    }

    @Test
    public void should_return_activity_with_most_samples_5() {
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.WALKING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 87));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 17));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 45));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.IN_VEHICLE, 12));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.RUNNING));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(45));
    }

    @Test
    public void should_return_activity_with_most_samples_x() {
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.WALKING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 87));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 17));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 45));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.IN_VEHICLE, 12));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.RUNNING));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(45));

        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 17));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.ON_BICYCLE));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(17));
    }

    @Test
    public void should_return_last_activity_when_same_amount_of_samples() {
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 87));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 14));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 45));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 12));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.RUNNING));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(14));

        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 17));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.ON_BICYCLE));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(17));
    }

    @Test
    public void should_give_more_weight_on_activity_type_if_confidence_high_enough() {
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 33));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 80));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.ON_BICYCLE));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(80));
    }

    @Test
    public void should_not_give_more_weight_on_activity_type_when_confidence_high_enough_but_same_activity_type() {
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 22));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.RUNNING, 80));//if this would give more weight, 2 BICYCLEs would not change average activity
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 18));
        likeActivityAverager.onLikeActivityUpdate(new LikeActivity(LikeActivity.Type.ON_BICYCLE, 30));

        assertThat(likeActivityAverager.getAverageLikeActivity().getType(), is(LikeActivity.Type.ON_BICYCLE));
        assertThat(likeActivityAverager.getAverageLikeActivity().getConfidence(), is(30));
    }

    @Test
    public void should_reset_average_activity() {
        LikeActivity likeActivity = new LikeActivity(LikeActivity.Type.IN_VEHICLE, 22);
        likeActivityAverager.onLikeActivityUpdate(likeActivity);
        assertThat(likeActivityAverager.getAverageLikeActivity(), is(likeActivity));

        likeActivityAverager.reset();
        assertThat(likeActivityAverager.getAverageLikeActivity(), is(nullValue()));
    }
}
