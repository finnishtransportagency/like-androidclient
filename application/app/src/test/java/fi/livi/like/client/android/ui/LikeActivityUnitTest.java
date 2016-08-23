package fi.livi.like.client.android.ui;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import fi.livi.like.client.android.BuildConfig;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class LikeActivityUnitTest {

    private ActivityController controller;

    @Test
    public void should_be_constructable() {
        controller = Robolectric.buildActivity(LikeActivity.class);
        controller.create().start();//todo; mock baseactivity/service to be able to resume!
        LikeActivity activity = (LikeActivity) controller.get();
        assertThat(activity, is(notNullValue()));    }
}
