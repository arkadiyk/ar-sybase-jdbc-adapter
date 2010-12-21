module ::ArJdbc
  module SybaseJtds
    def arel2_visitors
      require 'arel/visitors/sybase_jtds'
      {'sybase_jtds' => ::Arel::Visitors::SybaseJtds}
    end
  end
end
