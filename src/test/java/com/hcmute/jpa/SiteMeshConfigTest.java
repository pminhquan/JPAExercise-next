package com.hcmute.jpa;

import org.junit.jupiter.api.Test;
import org.sitemesh.DecoratorSelector;
import org.sitemesh.builder.SiteMeshFilterBuilder;
import org.sitemesh.config.xml.XmlFilterConfigurator;
import org.sitemesh.webapp.WebAppContext;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class SiteMeshConfigTest {

    @Test
    public void testSiteMesh3XmlValidAndParsable() throws Exception {
        File xmlFile = new File("src/main/webapp/WEB-INF/sitemesh3.xml");
        assertTrue(xmlFile.exists(), "sitemesh3.xml must exist in WEB-INF");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(xmlFile);

        assertEquals("sitemesh", doc.getDocumentElement().getNodeName());

        SiteMeshFilterBuilder filterBuilder = new SiteMeshFilterBuilder();
        XmlFilterConfigurator configurator = new XmlFilterConfigurator(
                new org.sitemesh.config.ObjectFactory.Default(), doc.getDocumentElement());
        configurator.configureFilter(filterBuilder);

        assertNotNull(filterBuilder.create(), "SiteMeshFilter must be created without error");
    }

    @Test
    public void testDecoratorFileExists() {
        File decoratorFile = new File("src/main/webapp/WEB-INF/decorators/profile.jsp");
        assertTrue(decoratorFile.exists(), "profile.jsp decorator must exist");
    }

    @Test
    public void testProfileDecoratorsResolveToPhysicalDecoratorPath() throws Exception {
        File xmlFile = new File("src/main/webapp/WEB-INF/sitemesh3.xml");
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.parse(xmlFile);

        SiteMeshFilterBuilder filterBuilder = new SiteMeshFilterBuilder();
        XmlFilterConfigurator configurator = new XmlFilterConfigurator(
                new org.sitemesh.config.ObjectFactory.Default(), doc.getDocumentElement());
        configurator.configureFilter(filterBuilder);

        DecoratorSelector<WebAppContext> selector = filterBuilder.getDecoratorSelector();

        String[] testPaths = new String[]{"/profile", "/profile/edit", "/profile/settings/avatar"};
        for (String testPath : testPaths) {
            WebAppContext context = new WebAppContext("text/html", null, null, null, null, null, false) {
                @Override
                public String getPath() {
                    return testPath;
                }
            };

            org.sitemesh.content.Content dummyContent = new org.sitemesh.content.memory.InMemoryContent();
            String[] resolved = selector.selectDecoratorPaths(dummyContent, context);
            assertNotNull(resolved, "Resolved decorators should not be null for " + testPath);
            assertEquals(1, resolved.length, "Expected exactly 1 decorator for " + testPath);
            assertEquals("/WEB-INF/decorators/profile.jsp", resolved[0],
                    "Path " + testPath + " must resolve to /WEB-INF/decorators/profile.jsp under SiteMesh default prefix");

            File physicalFile = new File("src/main/webapp", resolved[0]);
            assertTrue(physicalFile.exists(), "Physical decorator file must exist at " + physicalFile.getPath());
        }

        // Verify that unmapped paths (e.g. login, products) do not resolve to any decorator (no global decorator)
        WebAppContext unmappedContext = new WebAppContext("text/html", null, null, null, null, null, false) {
            @Override
            public String getPath() {
                return "/products";
            }
        };
        String[] unmappedResolved = selector.selectDecoratorPaths(new org.sitemesh.content.memory.InMemoryContent(), unmappedContext);
        assertTrue(unmappedResolved == null || unmappedResolved.length == 0,
                "Unmapped paths must not resolve to any decorator (no global decorator)");
    }
}
