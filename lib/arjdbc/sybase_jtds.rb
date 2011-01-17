require 'arjdbc/mssql/adapter'

module ::ArJdbc
  module SybaseJtds
    def arel2_visitors
      require 'arel/visitors/sybase_jtds'
      {'sybase_jtds' => ::Arel::Visitors::SybaseJtds}
    end

    def self.jdbc_connection_class
      ::ActiveRecord::ConnectionAdapters::SybaseJdbcConnection
    end

    def self.column_selector
      [/sybase/i, lambda {|cfg,col| col.extend(::ArJdbc::MsSQL::Column)}]
    end

    def supports_migrations?
      false
    end

  end
end
