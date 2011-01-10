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
        if(!query.matches("/OFFSET/")) {
            return super.executeQuery(context, query, maxRows);
        } else {
            return executeQueryWithOffset(context, query);
        }

    }

    private IRubyObject executeQueryWithOffset(final ThreadContext context, final String query) {
        return (IRubyObject) withConnectionAndRetry(context, new SQLBlock() {
            public Object call(Connection c) throws SQLException {
                Matcher limitMatcher = Pattern.compile("\\sLIMIT\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(query);
                String limitStr = limitMatcher.group(1);
                String sybQuery = limitMatcher.replaceAll("");

                Matcher offsetMatcher = Pattern.compile("\\sOFFSET\\s+(\\d+)", Pattern.CASE_INSENSITIVE).matcher(sybQuery);
                String offsetStr = offsetMatcher.group(1);
                sybQuery = offsetMatcher.replaceAll("");


                int limit = 0, offset = 0;
                if (offsetStr == null) {
                    throw new RuntimeException("OFFSET is not in \"" + query + "\"");
                } else {
                    offset = Integer.parseInt(offsetStr);
                }
                if(limitStr != null) {
                    limit = Integer.parseInt(limitStr);
                }

                Statement stmt = null;
                try {
                    DatabaseMetaData metadata = c.getMetaData();
                    stmt = c.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    ResultSet rs = stmt.executeQuery(sybQuery);
                    rs.absolute(offset);
//                    rs.

                    return unmarshalResult(context, metadata, rs, false);
                } catch (SQLException sqe) {
                    if (context.getRuntime().isDebug()) {
                        System.out.println("Error SQL: " + sybQuery);
                    }
                    throw sqe;
                } finally {
                    close(stmt);
                }
            }
        });
    }


}
