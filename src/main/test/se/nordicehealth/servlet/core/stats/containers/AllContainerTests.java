package se.nordicehealth.servlet.core.stats.containers;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AreaTest.class, MultipleOptionTest.class, SingleOptionTest.class, SliderOptionTest.class })
public class AllContainerTests {
}
