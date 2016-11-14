package fi.livi.like.client.android.backgroundservice.background;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import fi.livi.like.client.android.backgroundservice.Configuration;
import fi.livi.like.client.android.backgroundservice.background.util.TestUtil;
import fi.livi.like.client.android.backgroundservice.dependencies.backend.JourneyUpdate;
import fi.livi.like.client.android.backgroundservice.http.FailedJourneyUpdateHandler;
import fi.livi.like.client.android.backgroundservice.http.HttpLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FailedJourneyUpdateHandlerUnitTest {

    private FailedJourneyUpdateHandler failedJourneyUpdateHandler;

    private HttpLoader httpLoader;
    private Configuration configuration;

    @Before
    public void setup() {
        httpLoader = mock(HttpLoader.class);
        configuration = mock(Configuration.class);
        when(configuration.getFailedUpdateJourneyMaxCount()).thenReturn(5);
        when(configuration.getFailedUpdateJourneyResendDelay()).thenReturn(1);
    }

    @Test
    public void should_have_no_failed_updates_when_started() {
        failedJourneyUpdateHandler = new FailedJourneyUpdateHandler(httpLoader, configuration);
        assertThat(failedJourneyUpdateHandler.getFailedUpdates().isEmpty(), is(true));
    }

    @Test
    public void should_contain_added_journey_updates() {
        failedJourneyUpdateHandler = new FailedJourneyUpdateHandler(httpLoader, configuration);
        List<JourneyUpdate> failedJourneys1 = TestUtil.generateMockedJourneyUpdates(1);
        List<JourneyUpdate> failedJourneys2 = TestUtil.generateMockedJourneyUpdates(2);
        List<JourneyUpdate> allFailedJourneys = new ArrayList<>();
        allFailedJourneys.addAll(failedJourneys1);
        allFailedJourneys.addAll(failedJourneys2);

        failedJourneyUpdateHandler.addFailedUpdates(failedJourneys1);

        assertThat(failedJourneyUpdateHandler.getFailedUpdates(), is(failedJourneys1));

        failedJourneyUpdateHandler.addFailedUpdates(failedJourneys2);

        assertThat(failedJourneyUpdateHandler.getFailedUpdates(), is(allFailedJourneys));
    }

    @Test
    public void should_contain_newest_if_added_too_many() {
        when(configuration.getFailedUpdateJourneyMaxCount()).thenReturn(1);
        failedJourneyUpdateHandler = new FailedJourneyUpdateHandler(httpLoader, configuration);
        List<JourneyUpdate> failedJourneys1 = TestUtil.generateMockedJourneyUpdates(1);
        List<JourneyUpdate> failedJourneys2 = TestUtil.generateMockedJourneyUpdates(2);

        failedJourneyUpdateHandler.addFailedUpdates(failedJourneys1);

        assertThat(failedJourneyUpdateHandler.getFailedUpdates(), is(failedJourneys1));

        failedJourneyUpdateHandler.addFailedUpdates(failedJourneys2);

        List<JourneyUpdate> onlyJourney3List = new ArrayList<>();
        onlyJourney3List.add(failedJourneys2.get(1));
        assertThat(failedJourneyUpdateHandler.getFailedUpdates(), is(onlyJourney3List));
    }

    @Test
    public void should_resend_failed_updates_after_delay_and_only_once() {
        failedJourneyUpdateHandler = new FailedJourneyUpdateHandler(httpLoader, configuration);
        List<JourneyUpdate> failedJourneys = TestUtil.generateMockedJourneyUpdates(2);

        failedJourneyUpdateHandler.addFailedUpdates(failedJourneys);

        assertThat(failedJourneyUpdateHandler.getFailedUpdates().size(), is(2));
        verify(httpLoader, never()).postJourneyUpdate(anyListOf(JourneyUpdate.class));
        verify(httpLoader, timeout(3000).times(1)).postJourneyUpdate(failedJourneys);
        assertThat(failedJourneyUpdateHandler.getFailedUpdates().isEmpty(), is(true));
    }

    @Test
    public void should_reset_timer_when_new_failed_updates() throws InterruptedException {
        failedJourneyUpdateHandler = new FailedJourneyUpdateHandler(httpLoader, configuration);
        List<JourneyUpdate> failedJourneys = TestUtil.generateMockedJourneyUpdates(1);

        // add 1 failed journey, wait but not 1 sec when resend happens
        failedJourneyUpdateHandler.addFailedUpdates(failedJourneys);
        Thread.sleep(600);
        assertThat(failedJourneyUpdateHandler.getFailedUpdates().size(), is(1));
        verify(httpLoader, never()).postJourneyUpdate(anyListOf(JourneyUpdate.class));

        // add 1 failed journey, wait but not 1 sec when resend happens
        failedJourneyUpdateHandler.addFailedUpdates(new ArrayList<>(failedJourneys));
        Thread.sleep(600);
        assertThat(failedJourneyUpdateHandler.getFailedUpdates().size(), is(2));
        verify(httpLoader, never()).postJourneyUpdate(anyListOf(JourneyUpdate.class));

        // add 1 failed journey, wait and let resend happen
        failedJourneyUpdateHandler.addFailedUpdates(new ArrayList<>(failedJourneys));
        assertThat(failedJourneyUpdateHandler.getFailedUpdates().size(), is(3));

        // check all failed journeys are posted at once
        List<JourneyUpdate> allArrays = new ArrayList<>(failedJourneys);
        allArrays.addAll(failedJourneys);
        allArrays.addAll(failedJourneys);
        verify(httpLoader, timeout(1200).times(1)).postJourneyUpdate(allArrays);
        assertThat(failedJourneyUpdateHandler.getFailedUpdates().isEmpty(), is(true));
    }
}
