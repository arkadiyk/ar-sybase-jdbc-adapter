require 'arjdbc'
require 'helper'

#module ActiveRecord
#  module ConnectionAdapters
#    class JdbcAdapter < AbstractAdapter
#      def initialize
#      end
#    end
#  end
#end

module ConnectionTests
  class MockConnection
    def adapter=(adapt)
    end
    def jndi_connection?
      false
    end
  end

  describe 'the sybase jtds connection' do
    before do
      @config = {
        :driver =>  'net.sourceforge.jtds.Driver',
        :url => "jdbc:jtds:sybase://test:1234/database",
        :dialect => 'sybase_jtds'
      }
      @adapter = ActiveRecord::ConnectionAdapters::JdbcAdapter.new MockConnection.new, nil, @config
    end

    it "instantiate correct adapter when using 'sybase_jtds' dialect" do
      @adapter.must_be_kind_of(::ArJdbc::SybaseJtds)
    end

    it "should configure arel2 visitors for SybaseJtds" do
      ::Arel::Visitors::VISITORS.must_include('sybase_jtds')
      visitor = ::Arel::Visitors::VISITORS['sybase_jtds']
      visitor.must_equal(::Arel::Visitors::SybaseJtds)
    end
  end
end
