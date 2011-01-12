/*
 **** BEGIN LICENSE BLOCK *****
 * Copyright (c) 2006-2010 Nick Sieger <nick@nicksieger.com>
 * Copyright (c) 2006-2007 Ola Bini <ola.bini@gmail.com>
 * Copyright (c) 2008-2009 Thomas E Enebo <enebo@acm.org>
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
import org.jruby.runtime.load.BasicLibraryService;

import java.io.IOException;

public class SybaseAdapterJavaService implements BasicLibraryService {
    public boolean basicLoad(final Ruby runtime) throws IOException {
        System.out.println("*****  BASIC LOAD: " + runtime);

        RubyClass jdbcConnection = RubyJdbcConnection.createJdbcConnectionClass(runtime);
        SybaseRubyJdbcConnection.createSybaseJdbcConnectionClass(runtime, jdbcConnection);

        System.out.println("****1  BASIC LOAD: " + jdbcConnection);

        return true;
    }
}
