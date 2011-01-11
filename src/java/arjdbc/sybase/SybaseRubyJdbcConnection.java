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

    private RubyString _row_num;

    protected SybaseRubyJdbcConnection(Ruby runtime, RubyClass metaClass) {
        super(runtime, metaClass);
        _row_num = runtime.newString("_row_num");
    }

    public static RubyClass createSybaseJdbcConnectionClass(Ruby runtime, RubyClass jdbcConnection) {
        RubyClass clazz = RubyJdbcConnection.getConnectionAdapters(runtime).defineClassUnder("SyabseJdbcConnection",
                jdbcConnection, SYBASE_JDBCCONNECTION_ALLOCATOR);
        clazz.defineAnnotatedMethods(SybaseRubyJdbcConnection.class);

        return clazz;
    }

    private static ObjectAllocator SYBASE_JDBCCONNECTION_ALLOCATOR = new ObjectAllocator() {
        public IRubyObject allocate(Ruby runtime, RubyClass klass) {
            return new SybaseRubyJdbcConnection(runtime, klass);
        }
    };

    protected static IRubyObject booleanToRuby(Ruby runtime, ResultSet resultSet, boolean booleanValue)
            throws SQLException {
        if (booleanValue == false && resultSet.wasNull()) return runtime.getNil();
        return runtime.newBoolean(booleanValue);
    }


    @Override
    protected IRubyObject executeQuery(final ThreadContext context, final String query, final int maxRows) {
        if(!query.matches("\\sOFFSET\\s+(\\d+)")) {
            return super.executeQuery(context, query, maxRows);
        } else {
            return executeQueryWithOffset(context, query);
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
    private IRubyObject executeQueryWithOffset(final ThreadContext context, final String query) {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {
            public Object call(Connection c) throws SQLException {
                Map<String, String> queryLimitOffset = extractLimitAndOffset(query);

                Statement stmt = null;
                try {
                    DatabaseMetaData metadata = c.getMetaData();
                    stmt = c.createStatement();
                    stmt.execute("declare crsr insensitive scroll cursor for " + queryLimitOffset.get("query"));

                    if(queryLimitOffset.get("limit") != null) {
                        stmt.execute("open crsr\n set cursor rows " + queryLimitOffset.get("limit") + " for crsr");
                    } else {
                        stmt.execute("open crsr\n set cursor rows 1000000 for crsr");  // a million records should be enough, i think
                    }
                    ResultSet rs = stmt.executeQuery("fetch absolute " + queryLimitOffset.get("offset") + " from crsr");
                    IRubyObject result =  unmarshalResult(context, metadata, rs, false);
                    stmt.execute("close crsr\n deallocate crsr");
                    return result;

                } catch (SQLException sqe) {
                    if (context.getRuntime().isDebug()) {
                        System.out.println("Error SQL: " + queryLimitOffset.get("query"));
                    }
                    throw sqe;
                } finally {
                    close(stmt);
                }
            }
        });
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
        Matcher limitMatcher = Pattern.compile("\\sLIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(queryString);
        if(limitMatcher.find()) {
            parsedQuery.put("limit",limitMatcher.group(1));
            queryString = limitMatcher.replaceAll("");
        }

        Matcher offsetMatcher = Pattern.compile("\\sOFFSET\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(queryString);
        if(offsetMatcher.find()) {
            parsedQuery.put("offset",offsetMatcher.group(1));
            parsedQuery.put("query", offsetMatcher.replaceAll(""));
        }
        return parsedQuery;
    }
}
