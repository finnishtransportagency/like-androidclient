package fi.livi.like.client.android.background;

import org.junit.Test;

import java.util.Date;

import fi.livi.like.client.android.background.tracking.JourneyUpdateBuilder;
import fi.livi.like.client.android.dependencies.backend.JourneyUpdate;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;
import fi.livi.like.client.android.dependencies.backend.LikeLocation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class JourneyUpdateBuilderUnitTest {

    @Test
    public void should_build_journey_update_based_on_given_data() {
        JourneyUpdate update = new JourneyUpdate("1", 2, 3, new Date(1L), null, new LikeActivity(LikeActivity.Type.IN_VEHICLE, 55));

        JourneyUpdateBuilder journeyUpdateBuilder = new JourneyUpdateBuilder();
        journeyUpdateBuilder.setUserId(update.getUserId());
        journeyUpdateBuilder.setJourneyId(update.getJourneyId());
        journeyUpdateBuilder.setSubJourneyId(update.getSubJourneyId());
        journeyUpdateBuilder.setTimestamp(update.getTimestamp());
        journeyUpdateBuilder.setLikeActivity(update.getLikeActivity());

        assertReflectionEquals(update, journeyUpdateBuilder.build());
    }

    @Test
    public void should_indicate_invalid_as_long_as_missing_fields() {
        JourneyUpdateBuilder journeyUpdateBuilder = new JourneyUpdateBuilder();

        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(false));

        journeyUpdateBuilder.setUserId("abc");
        journeyUpdateBuilder.setJourneyId(123);
        journeyUpdateBuilder.setSubJourneyId(456);
        journeyUpdateBuilder.setTimestamp(new Date());
        journeyUpdateBuilder.setLikeLocation(mock(LikeLocation.class));
        journeyUpdateBuilder.setLikeActivity(mock(LikeActivity.class));
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(true));

        journeyUpdateBuilder.setUserId("");
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(false));
        journeyUpdateBuilder.setUserId("abc");
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(true));

        journeyUpdateBuilder.setJourneyId(0);
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(false));
        journeyUpdateBuilder.setJourneyId(123);
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(true));

        journeyUpdateBuilder.setSubJourneyId(0);
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(false));
        journeyUpdateBuilder.setSubJourneyId(456);
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(true));

        journeyUpdateBuilder.setTimestamp(null);
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(false));
        journeyUpdateBuilder.setTimestamp(new Date());
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(true));

        journeyUpdateBuilder.setLikeLocation(null);
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(false));
        journeyUpdateBuilder.setLikeLocation(mock(LikeLocation.class));
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(true));

        journeyUpdateBuilder.setLikeActivity(null);
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(false));
        journeyUpdateBuilder.setLikeActivity(mock(LikeActivity.class));
        assertThat(journeyUpdateBuilder.isJourneyUpdateValid(), is(true));
    }
}
