package org.sakaiproject.webservices;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.User;

public class SakaiLoginLoginTest extends AbstractCXFTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	public static final String SESSION_ID = "***SESSION_HAS_BEEN_MOCKERIZED***";
	private static final String SOAP_OPERATION = "login";

	@Override
	protected <T extends AbstractWebService> Class<T> getTestClass() {
		return (Class<T>) SakaiLogin.class;
	}

	@Override
	protected String getOperation() {
		return SOAP_OPERATION;
	}

	private void addClientMocks(WebClient client) {
		addCXFClientMocks(client);
	}

	@Override
	protected void addServiceMocks(AbstractWebService service) {
		when(service.serverConfigurationService.getBoolean("webservices.allowlogin", false)).thenReturn(true);
		User mockUser = mock(User.class);
		when(service.userDirectoryService.authenticate("admin", "admin")).thenReturn(mockUser);
		when(service.userDirectoryService.authenticate("admin", "fail")).thenReturn(null);

		Session mockSession = mock(Session.class);
		when(mockSession.getId()).thenReturn(SESSION_ID);
		when(service.sessionManager.startSession()).thenReturn(mockSession);
	}

	@Test
	public void testLogin() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("id", "admin");
		client.query("pw", "admin");

		// client result
		String result = client.get(String.class);

		// test verifications
		assertNotNull(result);
		assertEquals(SESSION_ID, result);
	}

	@Test
	public void testLoginFailed() {
		WebClient client = WebClient.create(getFullEndpointAddress());

		addClientMocks(client);

		// client call
		client.accept("text/plain");
		client.path("/" + getOperation());
		client.query("id", "admin");
		client.query("pw", "fail");

		// client result
		thrown.expect(RuntimeException.class);
		client.get(String.class);
	}
}
