package edu.wvu.stat.rc2;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.wvu.stat.rc2.resources.RCRestError;

import edu.wvu.stat.rc2.RCCustomError;

public class RCCustomErrorTest {

	@Test
	public void testDuplicateName() throws Exception {
		RCCustomError err = new RCCustomError(RCRestError.DuplicateName, "workspace");
		assertEquals("There already is a workspace with that name.", err.getMessage());
	}

}
