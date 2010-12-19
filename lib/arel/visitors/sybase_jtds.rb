module Arel
  module Visitors
    class SybaseJtds < Arel::Visitors::ToSql
      def visit_Arel_Nodes_SelectStatement o
        "#{super} :: LIMIT: #{o.limit}"
      end
    end

  end
end
