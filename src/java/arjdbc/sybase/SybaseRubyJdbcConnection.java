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
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyString;
import org.jruby.runtime.ObjectAllocator;
import org.jruby.runtime.builtin.IRubyObject;

import java.sql.ResultSet;
import java.sql.SQLException;

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

}
