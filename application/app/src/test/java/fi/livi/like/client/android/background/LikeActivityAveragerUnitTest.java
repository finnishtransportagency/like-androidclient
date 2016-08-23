package fi.livi.like.client.android.background;

import org.junit.Before;
import org.junit.Test;

import fi.livi.like.client.android.background.datacollectors.activityrecognition.LikeActivityAverager;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class LikeActivityAveragerUnitTest {

    LikeActivityAverager likeActivityAverager;

    @Before
    public void setup() {
        likeActivityAverager = new LikeActivityAverager();
    }

    @Test
    public void should_just_return_if_null_value_given() {
        likeActivityAverager.onLikeActivityUpdate(null);

        assertThat(likeActivityAverager.getAverageLikeActivity(), is(nullValue()));
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
    public void should_reset_average_activity() {
        LikeActivity likeActivity = new LikeActivity(LikeActivity.Type.IN_VEHICLE, 22);
        likeActivityAverager.onLikeActivityUpdate(likeActivity);
        assertThat(likeActivityAverager.getAverageLikeActivity(), is(likeActivity));

        likeActivityAverager.reset();
        assertThat(likeActivityAverager.getAverageLikeActivity(), is(nullValue()));
    }
}
