package fi.livi.like.client.android;

import android.content.Intent;
import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import fi.livi.like.client.android.ui.LikeActivity;
import fi.livi.like.client.android.ui.util.AutoStarter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.robolectric.Shadows.shadowOf;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class AutoStarterUnitTest {

    private AutoStarter autoStarter;

    @Before
    public void setup() {
        List<ShadowApplication.Wrapper> registeredReceivers = shadowOf(RuntimeEnvironment.application).getRegisteredReceivers();
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (wrapper.broadcastReceiver instanceof AutoStarter) {
                autoStarter = (AutoStarter)wrapper.broadcastReceiver;
            }
        }
    }

    @Test
    public void only_one_boot_receiver_should_be_registered() {
        Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
        ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);

        assertThat(shadowApplication.hasReceiverForIntent(intent), is(true));
        assertThat(shadowApplication.getReceiversForIntent(intent).size(), is(1));
    }

    @Test
    public void autostarter_should_be_registered() {
        assertThat(autoStarter, is(notNullValue()));
    }

    @Test
    public void should_start_startactivity() {
        autoStarter.onReceive(RuntimeEnvironment.application, new Intent(Intent.ACTION_BOOT_COMPLETED));

        Intent expectedIntent = new Intent(RuntimeEnvironment.application, LikeActivity.class);
        expectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        assertReflectionEquals(expectedIntent, shadowOf(RuntimeEnvironment.application).getNextStartedActivity());
    }
}
