module Arel
  module Visitors
    class SybaseJtds < Arel::Visitors::ToSql
      def visit_Arel_Nodes_SelectStatement o
        if o.limit && !o.offset
          limit   = o.limit
          o.limit = nil
          sql = super
          if sql =~ /DISTINCT /
            sql.gsub!(/DISTINCT /, "DISTINCT TOP #{limit} ")
          else
            sql.gsub!(/SELECT /, "SELECT TOP #{limit} ")
          end
        end
        sql
      end

    end
  end
end
