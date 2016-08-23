package fi.livi.like.client.android.background;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import fi.livi.like.client.android.background.datacollectors.activityrecognition.LikeActivityConverter;
import fi.livi.like.client.android.dependencies.backend.LikeActivity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class LikeActivityConverterUnitTest {

    LikeActivityConverter likeActivityConverter;

    @Before
    public void setup() {
        likeActivityConverter = new LikeActivityConverter();
    }

    @Test
    public void should_return_null_on_unsupported_types() {
        ActivityRecognitionResult activityRecognitionResult = mock(ActivityRecognitionResult.class);

        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(new DetectedActivity(DetectedActivity.STILL, 99));
        LikeActivity likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
        assertThat(likeActivity, is(nullValue()));

        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(new DetectedActivity(DetectedActivity.UNKNOWN, 99));
        likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
        assertThat(likeActivity, is(nullValue()));

        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(new DetectedActivity(DetectedActivity.TILTING, 99));
        likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
        assertThat(likeActivity, is(nullValue()));
    }

    @Test
    public void should_return_correct_likeactivity_on_supported_types() {
        ActivityRecognitionResult activityRecognitionResult = mock(ActivityRecognitionResult.class);

        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(new DetectedActivity(DetectedActivity.WALKING, 99));
        LikeActivity likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
        assertThat(likeActivity.getType(), is(LikeActivity.Type.WALKING));
        assertThat(likeActivity.getConfidence(), is(99));

        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(new DetectedActivity(DetectedActivity.RUNNING, 88));
        likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
        assertThat(likeActivity.getType(), is(LikeActivity.Type.RUNNING));
        assertThat(likeActivity.getConfidence(), is(88));

        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(new DetectedActivity(DetectedActivity.IN_VEHICLE, 77));
        likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
        assertThat(likeActivity.getType(), is(LikeActivity.Type.IN_VEHICLE));
        assertThat(likeActivity.getConfidence(), is(77));

        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(new DetectedActivity(DetectedActivity.ON_BICYCLE, 66));
        likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);
        assertThat(likeActivity.getType(), is(LikeActivity.Type.ON_BICYCLE));
        assertThat(likeActivity.getConfidence(), is(66));
    }

    @Test
    public void should_get_walking_or_running_from_on_foot_activity() {
        List<DetectedActivity> detectedActivities = new ArrayList<>(3);
        detectedActivities.add(new DetectedActivity(DetectedActivity.ON_FOOT, 99));
        detectedActivities.add(new DetectedActivity(DetectedActivity.WALKING, 81));
        detectedActivities.add(new DetectedActivity(DetectedActivity.RUNNING, 11));

        ActivityRecognitionResult activityRecognitionResult = mock(ActivityRecognitionResult.class);
        when(activityRecognitionResult.getMostProbableActivity()).thenReturn(detectedActivities.get(0));
        when(activityRecognitionResult.getProbableActivities()).thenReturn(detectedActivities);

        LikeActivity likeActivity = likeActivityConverter.convertToLikeActivity(activityRecognitionResult);

        assertThat(likeActivity.getType(), is(LikeActivity.Type.WALKING));
        assertThat(likeActivity.getConfidence(), is(81));
    }
}