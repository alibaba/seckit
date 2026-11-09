package com.alibaba.seckit.jdbc;

import com.alibaba.seckit.jdbc.filters.OracleFilter;
import com.alibaba.seckit.jdbc.parser.OracleUrlParser;
import com.alibaba.seckit.jdbc.parser.oracle.NVFactory;
import com.alibaba.seckit.jdbc.parser.oracle.NVPair;
import org.junit.Test;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.*;

public class OracleTnsTest {
    private static final String PREFIX = "jdbc:oracle:thin:@";

    @Test
    public void parsesNestedListsAndRepeatedChildren() throws JdbcURLException {
        String descriptor = "(DESCRIPTION_LIST=(LOAD_BALANCE=on)"
                + "(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=host1)(PORT=1521))"
                + "(ADDRESS=(PROTOCOL=TCPS)(HOST=host2)(PORT=5221)))"
                + "(CONNECT_DATA=(SERVICE_NAME=orcl)))"
                + "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=host3))(CONNECT_DATA=(SID=test))))";
        NVPair root = parse(descriptor);
        assertEquals("DESCRIPTION_LIST", root.getName());
        assertEquals(3, root.getListSize());
        NVPair addresses = root.getListElement(1).getListElement(0);
        assertEquals(2, addresses.getListSize());
        assertEquals("host2", addresses.getListElement(1).getListElement(1).getAtom());
        assertEquals(descriptor, root.toString());
        assertEquals(descriptor, parse(root.toString()).toString());
        FilterResult result = new OracleFilter().filterPropertiesWithResult(PREFIX + descriptor);
        assertTrue(result.isSafe());
        assertEquals(PREFIX + descriptor, result.getAfter());
    }

    @Test
    public void acceptsFormattingWhitespaceWithoutChangingQuotedWhitespace() throws JdbcURLException {
        String descriptor = " \n ( DESCRIPTION = \t ( ADDRESS = ( PROTOCOL = TCP ) ( HOST = host ) )"
                + "\r\n ( CONNECT_DATA = ( SERVICE_NAME = \"  service name  \" ) ) ) \t";
        String expected = "(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=host))"
                + "(CONNECT_DATA=(SERVICE_NAME=\"  service name  \")))";
        assertEquals(expected, parse(descriptor).toString());
        OracleUrlParser parser = new OracleUrlParser(PREFIX + descriptor);
        parser.parse();
        assertEquals("DESCRIPTION", parser.getNvPair().getName());
        assertEquals(PREFIX + expected, parser.toString());
    }

    @Test
    public void preservesQuotedDelimitersEscapesAndUnicode() throws JdbcURLException {
        String descriptor = "(CONNECT_DATA=(SERVICE_NAME=\"svc (x)=y,#' \\\"quoted\\\" \\\\ 中文\")"
                + "(SID='single \\'quote\\' \"double\"'))";
        NVPair root = parse(descriptor);
        assertEquals("svc (x)=y,#' \"quoted\" \\ 中文", root.getListElement(0).getAtom());
        assertEquals("single 'quote' \"double\"", root.getListElement(1).getAtom());
        assertEquals(descriptor, root.toString());
        NVPair reparsed = parse(root.toString());
        assertEquals(2, reparsed.getListSize());
        assertEquals(root.getListElement(0).getAtom(), reparsed.getListElement(0).getAtom());
        assertEquals(root.getListElement(1).getAtom(), reparsed.getListElement(1).getAtom());
    }

    @Test
    public void rejectsMalformedDescriptors() throws JdbcURLException {
        String[] malformed = {
                null, "", " \t", "()", "(=value)", "(HOST)", "HOST=value", "(HOST=value",
                "(HOST=value))", "(HOST=value)garbage", "(HOST=value)(PORT=1521)",
                "(HOST=value)?foo=bar", "(DESCRIPTION=(HOST=a)trailing)",
                "(DESCRIPTION=value(HOST=a))", "(DESCRIPTION=(HOST=a), (PORT=1521))",
                "(DESCRIPTION=((HOST=a)))", "(HO ST=value)", "(HOST=a b)", "(HOST=a=b)",
                "(HOST=\"unfinished)", "(HOST='unfinished)", "(HOST=\"a\"junk)",
                "(HOST=\"a\"\"b\")", "(HOST='a'\"b\")", "(HOST=a\"b\")",
                "(HOST=\"a\"(PORT=1))", "(HOST=a#comment)", "(HOST=a\\b)",
                "(HOST=\"bad\\x\")", "(HOST=\"unfinished\\)", "(HOST=bad\u0000value)",
                "(HOST=bad\u007fvalue)", "(HOST=bad\u0085value)", "(HOST=bad\ud800value)",
                "(HOST=bad\udc00value)", "(HOST=\"bad\nvalue\")", "(HOST=a\u00a0b)"
        };
        for (String descriptor : malformed) {
            assertInvalid(descriptor);
        }
    }

    @Test
    public void rejectsMalformedJdbcDescriptors() throws JdbcURLException {
        String[] malformed = {
                "(DESCRIPTION=(HOST=a))suffix", "(DESCRIPTION=(HOST=a)))(PORT=1)",
                "(DESCRIPTION=atom(HOST=a))", "(DESCRIPTION=(HOST=\"unterminated))"
        };
        for (String descriptor : malformed) {
            try {
                new OracleFilter().filterPropertiesWithResult(PREFIX + descriptor);
                fail("Malformed JDBC descriptor was accepted");
            } catch (JdbcURLUnsafeException expected) {
                assertNotNull(expected.getMessage());
            }
        }
    }

    @Test
    public void boundsNestingBeforeStackOverflow() throws JdbcURLException {
        String deepest = nestedDescriptor(64);
        assertEquals(deepest, parse(deepest).toString());
        assertEquals(PREFIX + deepest, new OracleFilter().filterProperties(PREFIX + deepest));
        assertInvalid(nestedDescriptor(65));
        assertInvalid(nestedDescriptor(1000));
    }

    @Test
    public void boundsInputLength() throws JdbcURLException {
        StringBuilder descriptor = new StringBuilder("(HOST=");
        for (int index = 0; index < 65529; index++) {
            descriptor.append('a');
        }
        descriptor.append(')');
        assertEquals(65536, descriptor.length());
        assertEquals(descriptor.toString(), parse(descriptor.toString()).toString());
        assertInvalid(descriptor + " ");
    }

    @Test
    public void filtersListEmptyAndDisallowedProtocolsWithoutRemovingNeighbors() throws JdbcURLException {
        String descriptor = "(DESCRIPTION=(ADDRESS=(PROTOCOL=(HOST=hidden))"
                + "(HOST=host1)(PROTOCOL=)(PROTOCOL=LDAP)(PORT=1521)"
                + "(PROTOCOL=TCP)(PROTOCOL='tCpS')(PROTOCOL=\"tcp\")(FOO=bar)(BAR=baz)))";
        String expected = "(DESCRIPTION=(ADDRESS=(HOST=host1)(PORT=1521)"
                + "(PROTOCOL=TCP)(PROTOCOL='tCpS')(PROTOCOL=\"tcp\")))";
        OracleFilter filter = new OracleFilter();
        FilterResult result = filter.filterPropertiesWithResult(PREFIX + descriptor);
        assertFalse(result.isSafe());
        assertTrue(result.getDeleted().containsKey("PROTOCOL"));
        assertTrue(result.getDeleted().containsKey("FOO"));
        assertTrue(result.getDeleted().containsKey("BAR"));
        assertEquals(PREFIX + expected, result.getAfter());
        assertEquals(expected, parse(result.getAfter().substring(PREFIX.length())).toString());
        assertTrue(filter.filterPropertiesWithResult(result.getAfter()).isSafe());
    }

    @Test
    public void removesAChildOnlyOnceWhenBothChecksRejectIt() throws JdbcURLException {
        OracleFilter filter = new OracleFilter();
        Set<String> accepted = new HashSet<>(filter.getAcceptedPairNames());
        accepted.remove("PROTOCOL");
        filter.setAcceptedPairNames(accepted);
        FilterResult result = filter.filterPropertiesWithResult(PREFIX
                + "(DESCRIPTION=(ADDRESS=(PROTOCOL=(HOST=hidden))(HOST=host1)"
                + "(PROTOCOL=LDAP)(PORT=1521)))");
        assertFalse(result.isSafe());
        assertEquals(PREFIX + "(DESCRIPTION=(ADDRESS=(HOST=host1)(PORT=1521)))", result.getAfter());
    }

    @Test
    public void preservesQuotedAtomsAfterFiltering() throws JdbcURLException {
        String service = "\"svc)(PROTOCOL=LDAP) (x)=y # \\\"quoted\\\"\"";
        String descriptor = "(DESCRIPTION=(ADDRESS=(PROTOCOL=\"TCPS\")(HOST='host name'))"
                + "(CONNECT_DATA=(FOO=bar)(SERVICE_NAME=" + service + ")(SID=''))(FOO=baz))";
        String expected = "(DESCRIPTION=(ADDRESS=(PROTOCOL=\"TCPS\")(HOST='host name'))"
                + "(CONNECT_DATA=(SERVICE_NAME=" + service + ")(SID='')))";
        OracleFilter filter = new OracleFilter();
        FilterResult result = filter.filterPropertiesWithResult(PREFIX + descriptor);
        assertFalse(result.isSafe());
        assertEquals(PREFIX + expected, result.getAfter());
        NVPair data = parse(expected).getListElement(1);
        assertEquals(2, data.getListSize());
        assertEquals("svc)(PROTOCOL=LDAP) (x)=y # \"quoted\"", data.getListElement(0).getAtom());
        assertEquals("", data.getListElement(1).getAtom());
        assertEquals(result.getAfter(), filter.filterProperties(result.getAfter()));
    }

    @Test
    public void serializesParentsWhoseChildrenWereAllRemoved() throws JdbcURLException {
        OracleFilter filter = new OracleFilter();
        FilterResult result = filter.filterPropertiesWithResult(PREFIX
                + "(DESCRIPTION=(ADDRESS=(PROTOCOL=(HOST=hidden))(FOO=bar))(FOO=baz))");
        assertFalse(result.isSafe());
        assertEquals(PREFIX + "(DESCRIPTION=(ADDRESS=))", result.getAfter());
        assertEquals("(DESCRIPTION=(ADDRESS=))", parse("(DESCRIPTION=(ADDRESS=))").toString());
        assertEquals(result.getAfter(), filter.filterProperties(result.getAfter()));
        result = filter.filterPropertiesWithResult(PREFIX + "(DESCRIPTION=(FOO=bar))");
        assertEquals(PREFIX + "(DESCRIPTION=)", result.getAfter());
        assertEquals(result.getAfter(), filter.filterProperties(result.getAfter()));
    }

    @Test
    public void appliesTheSameChecksToTheRoot() throws JdbcURLException {
        OracleFilter filter = new OracleFilter();
        String[] rejected = {"(FOO=(DESCRIPTION=(HOST=host)))", "(PROTOCOL=(HOST=host))", "(PROTOCOL=LDAP)"};
        for (String descriptor : rejected) {
            FilterResult result = filter.filterPropertiesWithResult(PREFIX + descriptor);
            assertFalse(result.isSafe());
            assertEquals(PREFIX, result.getAfter());
            assertEquals(1, result.getDeleted().size());
        }
        assertEquals(PREFIX + "(PROTOCOL=TCP)", filter.filterProperties(PREFIX + "(PROTOCOL=TCP)"));
    }

    @Test
    public void checksNamesIndependentlyOfTheDefaultLocale() throws JdbcURLException {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            String descriptor = "(description=(address=(protocol=tcps)(host=host))"
                    + "(connect_data=(sid=test)))";
            FilterResult result = new OracleFilter().filterPropertiesWithResult(PREFIX + descriptor);
            assertTrue(result.isSafe());
            assertEquals(PREFIX + descriptor, result.getAfter());
        } finally {
            Locale.setDefault(previous);
        }
    }

    private NVPair parse(String descriptor) throws JdbcURLException {
        return new NVFactory().createNVPair(descriptor);
    }

    private void assertInvalid(String descriptor) throws JdbcURLException {
        try {
            parse(descriptor);
            fail("Malformed descriptor was accepted");
        } catch (JdbcURLUnsafeException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    private String nestedDescriptor(int depth) {
        StringBuilder descriptor = new StringBuilder();
        for (int index = 1; index < depth; index++) {
            descriptor.append("(DESCRIPTION=");
        }
        descriptor.append("(HOST=host)");
        for (int index = 1; index < depth; index++) {
            descriptor.append(')');
        }
        return descriptor.toString();
    }
}
