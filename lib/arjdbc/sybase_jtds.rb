module ::ArJdbc
  module SybaseJtds
    def arel2_visitors
      {'sybase_jtds' => ::Arel::Visitors::SybaseJtds}
    end
  end
end
