package com.edocomar.demofeed.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Before;
import org.junit.Test;

public class SubscriptionsSerdeTest {

	ConcurrentMap<String, Set<String>>subs = new ConcurrentHashMap<>();
	SubscriptionsSerde serde = new SubscriptionsSerde();
	
	@Before
	public void setUp() {
		Set<String> u1feeds = ConcurrentHashMap.newKeySet();
		u1feeds.addAll(Arrays.asList("F1","F2","F3"));
		subs.put("user1", u1feeds);
		Set<String> u2feeds = ConcurrentHashMap.newKeySet();
		u2feeds.addAll(Arrays.asList("F2","F3","F4"));
		subs.put("user2", u2feeds);
	}

	@Test
	public void testWriteAndReadBack() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serde.write(subs, bos);
		System.out.println(new String(bos.toByteArray()));
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ConcurrentMap<String, Set<String>>subs2 = new ConcurrentHashMap<>();
		serde.readInto(subs2, bis);
		
		assertEquals(subs, subs2);
		assertTrue(subs2.values().iterator().next() instanceof ConcurrentHashMap.KeySetView);
	}

}
