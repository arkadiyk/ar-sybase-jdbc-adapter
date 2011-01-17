/*
 **** BEGIN LICENSE BLOCK *****
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ***** END LICENSE BLOCK *****/
package arjdbc.sybase;

import arjdbc.jdbc.RubyJdbcConnection;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author arkadiy kraportov
 */
public class SybaseRubyJdbcConnection extends RubyJdbcConnection {

    protected SybaseRubyJdbcConnection(Ruby runtime, RubyClass metaClass) {
        super(runtime, metaClass);
    }

    public static RubyClass createSybaseJdbcConnectionClass(Ruby runtime, RubyClass jdbcConnection) {
        RubyClass clazz = RubyJdbcConnection.getConnectionAdapters(runtime).defineClassUnder("SybaseJdbcConnection",
                jdbcConnection, SYBASE_JDBCCONNECTION_ALLOCATOR);
        clazz.defineAnnotatedMethods(SybaseRubyJdbcConnection.class);

        return clazz;
    }

    private static ObjectAllocator SYBASE_JDBCCONNECTION_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new SybaseRubyJdbcConnection(runtime, klass);
        }
    };

    @Override
    protected boolean genericExecute(Statement stmt, String query) throws SQLException {
        Map<String, String> parsedQuery = extractLimitOffsetAndCount(query);
        String offset = parsedQuery.get("offset");
        String limit = parsedQuery.get("limit");
        String count = parsedQuery.get("count");
        if((offset != null) ||                     // if OFFSET
           (limit != null && count != null)) {     // if OFFSET or LIMIT with COUNT

            return executeQueryWithOffset(stmt, parsedQuery.get("query"), limit, offset, count);
        } else {
            return super.genericExecute(stmt, query);
        }

    }

    /**
     * Executes query with offset & limit in Sybase way. Requires Sybase ASE version 15 or above
     * <code>
     *     declare crsr  insensitive scroll cursor for
     *         select * from `original query`
     *     go
     *
     *     open crsr
     *     set cursor rows `limit` for crsr
     *     fetch absolute `offset` from crsr
     *
     *     close crsr
     *     deallocate crsr
     * </code>
     *
     *  If there is a count adds
     * <code>
     *     select @@rowcount
     * </code>
     */
    private boolean executeQueryWithOffset(Statement stmt, String query, String limit, String offset, String count)
            throws SQLException {

        try {
            stmt.execute("close crsr\ndeallocate crsr");
        } catch (SQLException e) {
            // could not find a better place to stick this in :(
            // Maybe cursorOpen flag to ThreadLocal ?
        }

        stmt.execute("declare crsr insensitive scroll cursor for " + query);

        if(limit != null) {
            stmt.execute("open crsr\n set cursor rows " + limit + " for crsr");
        } else {
            stmt.execute("open crsr\n set cursor rows 1000000 for crsr");  // a million records should be enough, i think
        }

        boolean result;
        if(offset != null) {
            result = stmt.execute("fetch absolute " + offset + " from crsr");
        } else {
            result = stmt.execute("fetch first from crsr");
        }

        if(count != null) {
            result = stmt.execute("select @@rowcount");
        }

        return result;

    }

    private static Pattern COUNT_PATTERN = Pattern.compile("\\sCOUNT\\s*\\(.+\\)", Pattern.CASE_INSENSITIVE);
//    private static Pattern COUNT_AS_MATCHER = Pattern.compile("\\sCOUNT\\s*\\(.+\\)\\s+AS count_(.+),", Pattern.CASE_INSENSITIVE);
    private static Pattern LIMIT_PATTERN = Pattern.compile("\\sLIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    private static Pattern OFFSET_PATTERN = Pattern.compile("\\sOFFSET\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
    /**
     * Parses MySQL formatted query
     * ex. if param is "SELECT COUNT(*) FROM table LIMIT 10 OFFSET 50",
     * the output will be {query="SELECT * FROM table", limit="10", offset="50", count="Y"}
     *
     * @param queryString MySQL formatted query string
     * @return Map<String, String> with parsed out Limit, Offset, Count flag and query string without LIMIT, OFFSET or COUNT
     */
    private static Map<String, String> extractLimitOffsetAndCount(String queryString){
        Map<String,String> parsedQuery = new HashMap<String,String>();
        Matcher countMatcher = COUNT_PATTERN.matcher(queryString);
        if(countMatcher.find()) {
            parsedQuery.put("count","Y");
            queryString = countMatcher.replaceAll(" 'F' as f ");
        }

/**
 * Not 100% sure if we need something like "User.group(:name).limit(10).offset(10).count"
 *   without "offset" it works fine as it is.
 *   If we do need it, it should be matched here and implemented in <code>executeQueryWithOffset</code>
 */
/*
        Matcher countAsMatcher = COUNT_AS_MATCHER.matcher(queryString);
        if(countAsMatcher.find()) {
            parsedQuery.put("count","Y");
            queryString = countMatcher.replaceAll(" 'F' as f ");
        }
 */

        Matcher limitMatcher = LIMIT_PATTERN.matcher(queryString);
        if(limitMatcher.find()) {
            parsedQuery.put("limit",limitMatcher.group(1));
            queryString = limitMatcher.replaceAll("");
        }

        Matcher offsetMatcher = OFFSET_PATTERN.matcher(queryString);
        if(offsetMatcher.find()) {
            parsedQuery.put("offset",offsetMatcher.group(1));
            queryString = offsetMatcher.replaceAll("");
        }
        parsedQuery.put("query", queryString);
        return parsedQuery;
    }
}
