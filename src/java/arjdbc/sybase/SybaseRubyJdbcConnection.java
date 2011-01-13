/*
 **** BEGIN LICENSE BLOCK *****
 * Copyright (c) 2011 Arkadiy Kraportov <arkadiyk@gmail.com>
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
import arjdbc.jdbc.SQLBlock;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyString;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.ThreadContext;
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
        System.out.println("== 0.1");
    }

    public static RubyClass createSybaseJdbcConnectionClass(Ruby runtime, RubyClass jdbcConnection) {
        RubyClass clazz = RubyJdbcConnection.getConnectionAdapters(runtime).defineClassUnder("SybaseJdbcConnection",
                jdbcConnection, SYBASE_JDBCCONNECTION_ALLOCATOR);
        clazz.defineAnnotatedMethods(SybaseRubyJdbcConnection.class);
        System.out.println("== 0.2");

        return clazz;
    }

    private static ObjectAllocator SYBASE_JDBCCONNECTION_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            System.out.println("== 0.3");
            return new SybaseRubyJdbcConnection(runtime, klass);
        }
    };

    @Override
    protected boolean genericExecute(Statement stmt, String query) throws SQLException {
        Map<String, String> queryLimitOffset = extractLimitAndOffset(query);
        String offset = queryLimitOffset.get("offset");
        String limit = queryLimitOffset.get("limit");
        String count = queryLimitOffset.get("count");
        if((offset != null) ||                     // if OFFSET
           (limit != null && count != null)) {     // if OFFSET or LIMIT with COUNT

            System.out.println("== 1.0 " + queryLimitOffset);
            return executeQueryWithOffset(stmt, queryLimitOffset.get("query"), limit, offset, count);
        } else {
            System.out.println("== 1.1 " + queryLimitOffset);
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
     */
    private boolean executeQueryWithOffset(Statement stmt, String query, String limit, String offset, String count)
            throws SQLException {

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

//            stmt.execute("close crsr\n deallocate crsr");
//            return result;

    }

    /**
     * Parses MySQL formatted query
     * ex. if param is "SELECT * FROM table LIMIT 10 OFFSET 50",
     * the output will be {query="SELECT * FROM table", limit="10", offset="50"}
     *
     * @param queryString MySQL formatted query with OFFSET and optionally LIMIT
     * @return Map<String, String> with parsed out Limit, Offset and query string without LIMIT and OFFSET
     */
    private static Map<String, String> extractLimitAndOffset(String queryString){
        Map<String,String> parsedQuery = new HashMap<String,String>();
        Matcher countMatcher = Pattern.compile("\\sCOUNT\\s*\\(.+\\)", Pattern.CASE_INSENSITIVE).matcher(queryString);
        if(countMatcher.find()) {
            parsedQuery.put("count","Y");
            queryString = countMatcher.replaceAll(" 'F' as f ");
        }

        Matcher limitMatcher = Pattern.compile("\\sLIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(queryString);
        if(limitMatcher.find()) {
            parsedQuery.put("limit",limitMatcher.group(1));
            queryString = limitMatcher.replaceAll("");
        }

        Matcher offsetMatcher = Pattern.compile("\\sOFFSET\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(queryString);
        if(offsetMatcher.find()) {
            parsedQuery.put("offset",offsetMatcher.group(1));
            queryString = offsetMatcher.replaceAll("");
        }
        parsedQuery.put("query", queryString);
        return parsedQuery;
    }
}
