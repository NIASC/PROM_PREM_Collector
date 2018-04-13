package niasc.servlet.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import niasc.servlet.implementation.io.AllIOTests;
import niasc.servlet.implementation.mail.AllMailTests;

@RunWith(Suite.class)
@SuiteClasses({ ClientRequestProcesserTest.class, CryptoTest.class, LocaleSETest.class, AllMailTests.class, AllIOTests.class })
public class AllImplementationTests {
}
