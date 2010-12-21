module Arel
  module Visitors
    class SybaseJtds < Arel::Visitors::ToSql
      def visit_Arel_Nodes_SelectStatement o
        if o.limit && !o.offset
          limit   = o.limit
          o.limit = nil
          sql = super
          sql.gsub!(/SELECT /, "SELECT TOP #{limit} ")
        end
        sql
      end

    end
  end
end
