package com.mtn.aggregator;



import com.mtn.aggregator.controllers.GHATransferTests;
import com.mtn.aggregator.controllers.TransferServiceControllerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
		GHATransferTests.class,
		TransferServiceControllerTest.class
})
public class TransferServiceTest {

   
}
